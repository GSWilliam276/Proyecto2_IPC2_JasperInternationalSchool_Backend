/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Puesto;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuentaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEmpleadoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Empleado;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.EmpleadoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.EmpleadoDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaEmpleado;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudCrearEmpleado;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudEditarEmpleado;
import com.usac.colegio.jasperschoolbackend.transferencia.ValidacionEmpleado;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
@Path("empleados")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN"})
public class EmpleadoControlador {
    //Gestion de empleados (recursos humanos): crear, editar, activar, desactivar y consultar
    //Exclusivo de Admin y, por herencia, de SuperAdmin
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaEmpleado> respuesta = new ArrayList<>();
            for (Empleado e : empleadoDAO.listarPaginado(pagina, tamano, busqueda)) {
                respuesta.add(aRespuesta(e));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los empleados");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudCrearEmpleado solicitud) {
        String problema = ValidacionEmpleado.validarCreacion(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }

        Puesto puesto = Puesto.valueOf(solicitud.getPuesto());
        Empleado nuevo = new Empleado(0, solicitud.getCui().trim(), solicitud.getNombre().trim(),
                solicitud.getTelefono().trim(), solicitud.getDireccion().trim(), puesto,
                LocalDate.parse(solicitud.getFechaContratacion().trim()), solicitud.getSalario(),
                EstadoGeneral.ACTIVO, solicitud.getIdUsuario());
        try {
            if (puesto.exigeCuenta()) {
                //ficha + cuenta + fila del rol, en una sola transaccion
                empleadoDAO.crearConCuenta(nuevo, solicitud.getCorreo().trim(), solicitud.getContrasena());
            } else {
                empleadoDAO.crear(nuevo);
            }
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nuevo)).build();
        } catch (ExcepcionCorreoDuplicado | ExcepcionCuiDuplicado | ExcepcionCuentaInvalida e) {
            //409: la peticion esta bien armada, pero choca con datos que ya existen
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear el empleado");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editar(@PathParam("id") int id, SolicitudEditarEmpleado solicitud) {
        String problema = ValidacionEmpleado.validarEdicion(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            Optional<Empleado> actual = empleadoDAO.buscarPorId(id);
            if (actual.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "No existe un empleado con ese id");
            }
            //Se conservan CUI, puesto, estado y cuenta: solo cambian los cinco campos editables
            Empleado a = actual.get();
            Empleado editado = new Empleado(id, a.getCui(), solicitud.getNombre().trim(),
                    solicitud.getTelefono().trim(), solicitud.getDireccion().trim(), a.getPuesto(),
                    LocalDate.parse(solicitud.getFechaContratacion().trim()), solicitud.getSalario(),
                    a.getEstado(), a.getIdUsuario());
            empleadoDAO.editar(editado);
            return Response.noContent().build();
        } catch (ExcepcionEmpleadoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar el empleado");
        }
    }

    @PUT
    @Path("{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            empleadoDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionEmpleadoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar el empleado");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") int id) {
        try {
            empleadoDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionEmpleadoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar el empleado");
        }
    }

    //Metodos de Apoyo

    private RespuestaEmpleado aRespuesta(Empleado e) {
        return new RespuestaEmpleado(e.getIdEmpleado(), e.getCui(), e.getNombre(), e.getTelefono(),
                e.getDireccion(), e.getPuesto().name(), e.getFechaContratacion().toString(),
                e.getSalario(), e.getEstado().name(), e.getIdUsuario());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
