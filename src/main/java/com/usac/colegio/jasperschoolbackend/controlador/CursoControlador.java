/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCursoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.CursoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CursoDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaCurso;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudCurso;
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
@Path("cursos")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN"})
public class CursoControlador {
    //Catalogo de cursos: crear, editar, activar, desactivar y consultar. Admin y, por herencia, SuperAdmin
    private final CursoDAO cursoDAO = new CursoDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaCurso> respuesta = new ArrayList<>();
            for (Curso c : cursoDAO.listarPaginado(pagina, tamano, busqueda)) {
                respuesta.add(aRespuesta(c));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los cursos");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudCurso solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        Curso nuevo = new Curso(0, solicitud.getNombre().trim(), descripcionLimpia(solicitud), EstadoGeneral.ACTIVO);
        try {
            cursoDAO.crear(nuevo);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nuevo)).build();
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear el curso");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editar(@PathParam("id") int id, SolicitudCurso solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            //el DAO solo actualiza nombre y descripcion: el estado de este objeto no se usa
            cursoDAO.editar(new Curso(id, solicitud.getNombre().trim(), descripcionLimpia(solicitud), EstadoGeneral.ACTIVO));
            return Response.noContent().build();
        } catch (ExcepcionCursoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar el curso");
        }
    }

    @PUT
    @Path("{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            cursoDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionCursoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar el curso");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") int id) {
        try {
            cursoDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionCursoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar el curso");
        }
    }

    //Metodos de Apoyo

    private String validar(SolicitudCurso s) {
        if (s == null || s.getNombre() == null || s.getNombre().isBlank()) {
            return "El nombre es obligatorio";
        }
        if (s.getNombre().trim().length() > 100) {
            return "El nombre no puede pasar de 100 caracteres";
        }
        if (s.getDescripcion() != null && s.getDescripcion().trim().length() > 255) {
            return "La descripción no puede pasar de 255 caracteres";
        }
        return null;
    }

    //Una descripcion vacia se guarda como null: asi queda una sola forma de "sin descripcion"
    private String descripcionLimpia(SolicitudCurso s) {
        return (s.getDescripcion() == null || s.getDescripcion().isBlank()) ? null : s.getDescripcion().trim();
    }

    private RespuestaCurso aRespuesta(Curso c) {
        return new RespuestaCurso(c.getIdCurso(), c.getNombre(), c.getDescripcion(), c.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }  
}
