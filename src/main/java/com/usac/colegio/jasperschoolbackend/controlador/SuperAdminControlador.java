/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.SuperAdmin;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.SuperAdminDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SuperAdminDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaUsuario;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSuperAdminNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionUltimoSuperAdmin;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudCrearUsuario;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudEditarUsuario;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import com.usac.colegio.jasperschoolbackend.transferencia.ValidacionUsuario;
import java.util.Optional;
/**
 *
 * @author eduar
 */
@Path("superadmins")
public class SuperAdminControlador {
    private final SuperAdminDAO superAdminDAO = new SuperAdminDAOPersistencia();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"SUPERADMIN"})
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {

        if (pagina < 1 || tamano < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RespuestaError("La página y el tamaño deben ser mayores que cero"))
                    .build();
        }

        try {
            List<SuperAdmin> superAdmins = superAdminDAO.listarPaginado(pagina, tamano, busqueda);

            List<RespuestaUsuario> respuesta = new ArrayList<>();
            for (SuperAdmin s : superAdmins) {
                respuesta.add(new RespuestaUsuario(
                        s.getIdUsuario(), s.getCui(), s.getNombre(), s.getCorreo(),
                        s.getTelefono(), s.getDireccion(), s.getEstado().name()));
            }

            return Response.ok(respuesta).build();

        } catch (ExcepcionPersistencia e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new RespuestaError("Error interno al listar los superadmins"))
                    .build();
        }
    }
    
    /** CU008 Activar SuperAdmin. */
    @PUT
    @Path("{id}/activar")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"SUPERADMIN"})
    public Response activar(@PathParam("id") int id) {
        try {
            superAdminDAO.activar(id);
            return Response.noContent().build(); //204: se hizo, no hay nada que devolver
        } catch (ExcepcionSuperAdminNoEncontrado e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new RespuestaError(e.getMessage())).build();
        } catch (ExcepcionPersistencia e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new RespuestaError("Error interno al activar el superadmin")).build();
        }
    }

    /** CU009 Desactivar SuperAdmin. No permite desactivar al ultimo activo. */
    @PUT
    @Path("{id}/desactivar")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"SUPERADMIN"})
    public Response desactivar(@PathParam("id") int id) {
        try {
            superAdminDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionSuperAdminNoEncontrado e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new RespuestaError(e.getMessage())).build();
        } catch (ExcepcionUltimoSuperAdmin e) {
            //409: la peticion es valida, pero choca con una regla de negocio
            return Response.status(Response.Status.CONFLICT)
                    .entity(new RespuestaError(e.getMessage())).build();
        } catch (ExcepcionPersistencia e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new RespuestaError("Error interno al desactivar el superadmin")).build();
        }
    }
    
    /** CU006 Crear SuperAdmin. */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"SUPERADMIN"})
    public Response crear(SolicitudCrearUsuario solicitud) {
        //validacion en el backend: nunca se confia solo en el formulario de Angular
        String problema = ValidacionUsuario.validar(solicitud);
        if (problema != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RespuestaError(problema)).build();
        }

        SuperAdmin nuevo = new SuperAdmin(0, solicitud.getCui().trim(), solicitud.getNombre().trim(),
                solicitud.getCorreo().trim(), solicitud.getTelefono().trim(),
                solicitud.getDireccion().trim(), solicitud.getContrasena(), EstadoGeneral.ACTIVO);

        try {
            superAdminDAO.crear(nuevo); //el DAO hashea la contraseña y guarda usuario + superadmin en una transaccion
            RespuestaUsuario respuesta = new RespuestaUsuario(nuevo.getIdUsuario(), nuevo.getCui(),
                    nuevo.getNombre(), nuevo.getCorreo(), nuevo.getTelefono(), nuevo.getDireccion(),
                    nuevo.getEstado().name());
            return Response.status(Response.Status.CREATED).entity(respuesta).build(); //201
        } catch (ExcepcionCorreoDuplicado | ExcepcionCuiDuplicado e) {
            //409: la peticion esta bien armada, pero choca con un dato que ya existe
            return Response.status(Response.Status.CONFLICT)
                    .entity(new RespuestaError(e.getMessage())).build();
        } catch (ExcepcionPersistencia e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new RespuestaError("Error interno al crear el superadmin")).build();
        }
    }
    
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"SUPERADMIN"})
    public Response editar(@PathParam("id") int id, SolicitudEditarUsuario solicitud) {
        String problema = ValidacionUsuario.validarEdicion(solicitud);
        if (problema != null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new RespuestaError(problema)).build();
        }
        try {
            Optional<SuperAdmin> actual = superAdminDAO.buscarPorId(id);
            if (actual.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new RespuestaError("No existe un superadmin con ese id")).build();
            }
            SuperAdmin s = actual.get();
            SuperAdmin editado = new SuperAdmin(id, s.getCui(), solicitud.getNombre().trim(), s.getCorreo(),
                    solicitud.getTelefono().trim(), solicitud.getDireccion().trim(),
                    s.getContrasena(), s.getEstado());
            superAdminDAO.editar(editado);
            return Response.noContent().build();
        } catch (ExcepcionSuperAdminNoEncontrado e) {
            return Response.status(Response.Status.NOT_FOUND).entity(new RespuestaError(e.getMessage())).build();
        } catch (ExcepcionPersistencia e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new RespuestaError("Error interno al editar el superadmin")).build();
        }
    }
}
