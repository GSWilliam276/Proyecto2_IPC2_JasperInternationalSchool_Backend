/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCarreraConEstudiantes;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCarreraNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Carrera;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.CarreraDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CarreraDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaCarrera;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudCarrera;
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
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
@Path("carreras")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"SUPERADMIN"})
public class CarreraControlador {
    //CU019 Crear, CU020 Editar, CU021 Activar, CU022 Desactivar y CU023 Consultar carrera
    //Exclusivo de SuperAdmin
    private final CarreraDAO carreraDAO = new CarreraDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaCarrera> respuesta = new ArrayList<>();
            for (Carrera c : carreraDAO.listarPaginado(pagina, tamano, busqueda)) {
                respuesta.add(aRespuesta(c));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las carreras");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudCarrera solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            carreraDAO.crear(new Carrera(0, solicitud.getNombre().trim(), EstadoGeneral.ACTIVO));
            return Response.status(Response.Status.CREATED).build();
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear la carrera");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editar(@PathParam("id") int id, SolicitudCarrera solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            //el DAO solo actualiza el nombre: el estado de este objeto no se usa
            carreraDAO.editar(new Carrera(id, solicitud.getNombre().trim(), EstadoGeneral.ACTIVO));
            return Response.noContent().build();
        } catch (ExcepcionCarreraNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar la carrera");
        }
    }

    @PUT
    @Path("{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            carreraDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionCarreraNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar la carrera");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") int id) {
        try {
            carreraDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionCarreraNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionCarreraConEstudiantes e) {
            //409: la peticion es valida, pero choca con una regla de negocio
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar la carrera");
        }
    }

    //Metodos de Apoyo

    private String validar(SolicitudCarrera s) {
        if (s == null || s.getNombre() == null || s.getNombre().isBlank()) {
            return "El nombre es obligatorio";
        }
        if (s.getNombre().trim().length() > 100) { 
            return "El nombre no puede pasar de 100 caracteres";
        }
        return null;
    }

    private RespuestaCarrera aRespuesta(Carrera c) {
        return new RespuestaCarrera(c.getIdCarrera(), c.getNombre(), c.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
