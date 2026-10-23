/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionAjena;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.AsignacionMaestro;
import com.usac.colegio.jasperschoolbackend.modelo.Inscripcion;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.AsignacionMaestroDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.InscripcionDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AsignacionMaestroDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.InscripcionDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.AccesoMaestro;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaAlumno;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaAsignacion;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
@Path("maestro")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"MAESTRO"})
public class MaestroControlador {
    //Panel del Maestro: cursos asignados (CU079) y nomina de alumnos (CU080). Solo ve lo suyo
    private final AsignacionMaestroDAO asignacionDAO = new AsignacionMaestroDAOPersistencia();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAOPersistencia();

    @GET
    @Path("asignaciones")
    public Response misAsignaciones(@Context ContainerRequestContext contexto) {
        try {
            int idMaestro = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            List<RespuestaAsignacion> respuesta = new ArrayList<>();
            // sin "todos": solo el año lectivo activo
            for (AsignacionMaestro a : asignacionDAO.listar(1, 200, idMaestro, null, null, false)) {
                respuesta.add(new RespuestaAsignacion(a.getIdAsignacion(), a.getIdSeccion(), a.getSeccionNombre(),
                        a.getIdGrado(), a.getGradoNombre(), a.getNivel().name(), a.getIdCarrera(),
                        a.getCarreraNombre(), a.getIdAnioLectivo(), a.getAnioNombre(), a.getEstadoAnio().name(),
                        a.getIdCurso(), a.getCursoNombre(), a.getIdMaestro(), a.getMaestroNombre()));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar sus asignaciones");
        }
    }

    @GET
    @Path("asignaciones/{id}/estudiantes")
    public Response nomina(@PathParam("id") int id, @Context ContainerRequestContext contexto) {
        try {
            int idMaestro = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            AsignacionMaestro a = AccesoMaestro.asignacionPropia(asignacionDAO, id, idMaestro);
            List<RespuestaAlumno> respuesta = new ArrayList<>();
            for (Inscripcion i : inscripcionDAO.listarActivasDeSeccion(a.getIdSeccion())) {
                respuesta.add(new RespuestaAlumno(i.getIdEstudiante(), i.getEstudianteNombre(), i.getEstudianteCui()));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionAsignacionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionAsignacionAjena e) {
            return error(Response.Status.FORBIDDEN, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar la nómina");
        }
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
