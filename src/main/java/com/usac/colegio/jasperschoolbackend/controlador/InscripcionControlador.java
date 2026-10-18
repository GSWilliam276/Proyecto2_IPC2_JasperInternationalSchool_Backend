/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionInscripcionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Inscripcion;
import com.usac.colegio.jasperschoolbackend.modelo.ResultadoInscripcion;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.InscripcionDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.InscripcionDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaInscripcion;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudInscripcion;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
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
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
@Path("inscripciones")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"SECRETARIA"})
public class InscripcionControlador {
    //Inscripciones (CU059 a CU061). Solo la Secretaria: la inscripcion guarda quien la registro
    private final InscripcionDAO inscripcionDAO = new InscripcionDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("idAnio") Integer idAnio,
                           @QueryParam("idGrado") Integer idGrado,
                           @QueryParam("idSeccion") Integer idSeccion,
                           @QueryParam("idEstudiante") Integer idEstudiante) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaInscripcion> respuesta = new ArrayList<>();
            for (Inscripcion i : inscripcionDAO.listar(pagina, tamano, idAnio, idGrado, idSeccion, idEstudiante)) {
                respuesta.add(aRespuesta(i, null));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las inscripciones");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response inscribir(SolicitudInscripcion solicitud, @Context ContainerRequestContext contexto) {
        if (solicitud == null || solicitud.getIdEstudiante() == null || solicitud.getIdSeccion() == null) {
            return error(Response.Status.BAD_REQUEST, "El estudiante y la sección son obligatorios");
        }
        String correo = solicitud.getCorreo() == null ? null : solicitud.getCorreo().trim();
        if (correo != null && !correo.isEmpty()) {
            if (correo.length() > 100 || !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                return error(Response.Status.BAD_REQUEST, "El correo no tiene un formato válido");
            }
        } else {
            correo = null;
        }
        try {
            int idSecretaria = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            ResultadoInscripcion resultado = inscripcionDAO.inscribir(solicitud.getIdEstudiante(),
                    solicitud.getIdSeccion(), correo, idSecretaria);
            return Response.status(Response.Status.CREATED)
                    .entity(aRespuesta(resultado.getInscripcion(), resultado.getContrasenaProvisional())).build();
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionSeccionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida | ExcepcionCorreoDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al inscribir al estudiante");
        }
    }

    @PUT
    @Path("{id}/anular")
    public Response anular(@PathParam("id") int id) {
        try {
            inscripcionDAO.anular(id);
            return Response.noContent().build();
        } catch (ExcepcionInscripcionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al anular la inscripción");
        }
    }

    //Metodos de Apoyo 

    private RespuestaInscripcion aRespuesta(Inscripcion i, String contrasenaProvisional) {
        return new RespuestaInscripcion(i.getIdInscripcion(), i.getIdEstudiante(), i.getEstudianteNombre(),
                i.getEstudianteCui(), i.getIdSeccion(), i.getSeccionNombre(), i.getIdGrado(), i.getGradoNombre(),
                i.getNivel().name(), i.getCarreraNombre(), i.getIdAnioLectivo(), i.getAnioNombre(),
                i.getEstadoAnio().name(), i.getFecha().toString(), i.getEstado().name(), contrasenaProvisional);
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
