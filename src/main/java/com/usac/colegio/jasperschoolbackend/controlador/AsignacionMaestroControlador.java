/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionDuplicada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.AsignacionMaestro;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
import com.usac.colegio.jasperschoolbackend.modelo.Maestro;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.AsignacionMaestroDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AsignacionMaestroDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaAsignacion;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaCurso;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaMaestro;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudAsignacion;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
@Path("asignaciones")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN"})
public class AsignacionMaestroControlador {
    private final AsignacionMaestroDAO asignacionDAO = new AsignacionMaestroDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("idMaestro") Integer idMaestro,
                           @QueryParam("idGrado") Integer idGrado,
                           @QueryParam("idSeccion") Integer idSeccion,
                           @QueryParam("todos") @DefaultValue("false") boolean todos) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaAsignacion> respuesta = new ArrayList<>();
            for (AsignacionMaestro a : asignacionDAO.listar(pagina, tamano, idMaestro, idGrado, idSeccion, todos)) {
                respuesta.add(aRespuesta(a));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las asignaciones");
        }
    }

    @GET
    @Path("maestros")
    public Response maestros() {
        try {
            List<RespuestaMaestro> respuesta = new ArrayList<>();
            for (Maestro m : asignacionDAO.listarMaestrosActivos()) {
                respuesta.add(new RespuestaMaestro(m.getIdUsuario(), m.getCui(), m.getNombre()));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los maestros");
        }
    }

    @GET
    @Path("secciones/{idSeccion}/cursos-sin-maestro")
    public Response cursosSinMaestro(@PathParam("idSeccion") int idSeccion) {
        try {
            List<RespuestaCurso> respuesta = new ArrayList<>();
            for (Curso c : asignacionDAO.cursosSinMaestro(idSeccion)) {
                respuesta.add(new RespuestaCurso(c.getIdCurso(), c.getNombre(), c.getDescripcion(), c.getEstado().name()));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionSeccionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los cursos");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response asignar(SolicitudAsignacion solicitud) {
        if (solicitud == null || solicitud.getIdSeccion() == null || solicitud.getIdCurso() == null
                || solicitud.getIdMaestro() == null) {
            return error(Response.Status.BAD_REQUEST, "La sección, el curso y el maestro son obligatorios");
        }
        try {
            AsignacionMaestro nueva = asignacionDAO.asignar(solicitud.getIdSeccion(), solicitud.getIdCurso(),
                    solicitud.getIdMaestro());
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nueva)).build();
        } catch (ExcepcionSeccionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionAsignacionDuplicada | ExcepcionReferenciaInvalida | ExcepcionOperacionNoPermitida e) {
            //409: la peticion esta bien armada, pero choca con una regla o con el estado de los datos
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al asignar el maestro");
        }
    }

    @DELETE
    @Path("{id}")
    public Response quitar(@PathParam("id") int id) {
        try {
            asignacionDAO.quitar(id);
            return Response.noContent().build();
        } catch (ExcepcionAsignacionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al quitar la asignación");
        }
    }

    //Metodos de Apoyo

    private RespuestaAsignacion aRespuesta(AsignacionMaestro a) {
        return new RespuestaAsignacion(a.getIdAsignacion(), a.getIdSeccion(), a.getSeccionNombre(), a.getIdGrado(),
                a.getGradoNombre(), a.getNivel().name(), a.getIdCarrera(), a.getCarreraNombre(),
                a.getIdAnioLectivo(), a.getAnioNombre(), a.getEstadoAnio().name(), a.getIdCurso(),
                a.getCursoNombre(), a.getIdMaestro(), a.getMaestroNombre());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
