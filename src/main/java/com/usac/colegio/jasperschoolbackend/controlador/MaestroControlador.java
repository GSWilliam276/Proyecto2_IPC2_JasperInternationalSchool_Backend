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
import com.usac.colegio.jasperschoolbackend.modelo.RegistroAsistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.AsignacionMaestroDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.AsistenciaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.InscripcionDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AsignacionMaestroDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AsistenciaDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.InscripcionDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.AccesoMaestro;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.MarcaAsistencia;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaAlumno;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaAsignacion;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaAsistencia;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudAsistencia;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAOPersistencia();

    @GET
    @Path("asignaciones")
    public Response misAsignaciones(@Context ContainerRequestContext contexto) {
        try {
            int idMaestro = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            List<RespuestaAsignacion> respuesta = new ArrayList<>();
            //Sin "todos": solo el año lectivo activo
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

        @GET
    @Path("asignaciones/{id}/asistencia/{fecha}")
    public Response planilla(@PathParam("id") int id, @PathParam("fecha") String fecha,
                             @Context ContainerRequestContext contexto) {
        LocalDate f;
        try {
            f = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "La fecha debe tener el formato AAAA-MM-DD");
        }
        try {
            AccesoMaestro.asignacionPropia(asignacionDAO, id, idMaestro(contexto));
            return Response.ok(aRespuestas(asistenciaDAO.planilla(id, f))).build();
        } catch (ExcepcionPersistencia e) {
            return errorDe(e, "Error interno al consultar la asistencia");
        }
    }

    @GET
    @Path("asignaciones/{id}/asistencia")
    public Response historial(@PathParam("id") int id, @QueryParam("idEstudiante") Integer idEstudiante,
                              @QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                              @Context ContainerRequestContext contexto) {
        LocalDate fechaDesde;
        LocalDate fechaHasta;
        try {
            fechaDesde = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde.trim());
            fechaHasta = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta.trim());
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "Las fechas deben tener el formato AAAA-MM-DD");
        }
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            return error(Response.Status.BAD_REQUEST, "La fecha inicial no puede ser posterior a la final");
        }
        try {
            AccesoMaestro.asignacionPropia(asignacionDAO, id, idMaestro(contexto));
            return Response.ok(aRespuestas(asistenciaDAO.listar(id, idEstudiante, fechaDesde, fechaHasta))).build();
        } catch (ExcepcionPersistencia e) {
            return errorDe(e, "Error interno al listar la asistencia");
        }
    }

    @POST
    @Path("asignaciones/{id}/asistencia")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarAsistencia(@PathParam("id") int id, SolicitudAsistencia solicitud,
                                        @Context ContainerRequestContext contexto) {
        return guardarAsistencia(id, solicitud, contexto, true);
    }

    @PUT
    @Path("asignaciones/{id}/asistencia")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editarAsistencia(@PathParam("id") int id, SolicitudAsistencia solicitud,
                                     @Context ContainerRequestContext contexto) {
        return guardarAsistencia(id, solicitud, contexto, false);
    }

    private Response guardarAsistencia(int id, SolicitudAsistencia s, ContainerRequestContext contexto, boolean crear) {
        if (s == null || s.getFecha() == null || s.getMarcas() == null || s.getMarcas().isEmpty()) {
            return error(Response.Status.BAD_REQUEST, "La fecha y las marcas de asistencia son obligatorias");
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(s.getFecha().trim());
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "La fecha debe tener el formato AAAA-MM-DD");
        }
        if (fecha.isAfter(LocalDate.now())) {
            return error(Response.Status.BAD_REQUEST, "La fecha no puede ser futura");
        }
        Map<Integer, Boolean> marcas = new LinkedHashMap<>();
        for (MarcaAsistencia m : s.getMarcas()) {
            if (m == null || m.getIdEstudiante() == null || m.getPresente() == null) {
                return error(Response.Status.BAD_REQUEST, "Cada marca necesita el estudiante y si estuvo presente");
            }
            if (marcas.put(m.getIdEstudiante(), m.getPresente()) != null) {
                return error(Response.Status.BAD_REQUEST, "Un estudiante aparece más de una vez");
            }
        }
        try {
            AccesoMaestro.asignacionPropia(asignacionDAO, id, idMaestro(contexto));
            if (crear) {
                asistenciaDAO.registrar(id, fecha, marcas);
                return Response.status(Response.Status.CREATED).build();
            }
            asistenciaDAO.editar(id, fecha, marcas);
            return Response.noContent().build();
        } catch (ExcepcionPersistencia e) {
            return errorDe(e, "Error interno al guardar la asistencia");
        }
    }

    private int idMaestro(ContainerRequestContext contexto) {
        return Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
    }

    private List<RespuestaAsistencia> aRespuestas(List<RegistroAsistencia> lista) {
        List<RespuestaAsistencia> r = new ArrayList<>();
        for (RegistroAsistencia a : lista) {
            r.add(new RespuestaAsistencia(a.getIdEstudiante(), a.getEstudianteNombre(), a.getCui(),
                    a.getFecha().toString(), a.getPresente()));
        }
        return r;
    }

    //Las tres excepciones del maestro extienden ExcepcionPersistencia: se distinguen por su tipo
    private Response errorDe(ExcepcionPersistencia e, String mensajeGenerico) {
        if (e instanceof ExcepcionAsignacionNoEncontrada) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        }
        if (e instanceof ExcepcionAsignacionAjena) {
            return error(Response.Status.FORBIDDEN, e.getMessage());
        }
        if (e instanceof ExcepcionOperacionNoPermitida) {
            return error(Response.Status.CONFLICT, e.getMessage());
        }
        return error(Response.Status.INTERNAL_SERVER_ERROR, mensajeGenerico);
    }
    
    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
