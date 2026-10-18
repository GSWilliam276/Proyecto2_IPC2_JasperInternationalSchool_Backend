/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Saldo;
import com.usac.colegio.jasperschoolbackend.modelo.Solvencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.SolvenciaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SolvenciaDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaSaldo;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaSolvencia;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudSolvencia;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
@Path("solvencias")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"SECRETARIA"})
public class SolvenciaControlador {
    //Solvencias (CU069) y consulta de saldo. Solo la Secretaria
    private final SolvenciaDAO solvenciaDAO = new SolvenciaDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("idEstudiante") Integer idEstudiante) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaSolvencia> respuesta = new ArrayList<>();
            for (Solvencia s : solvenciaDAO.listar(pagina, tamano, idEstudiante)) {
                respuesta.add(aRespuesta(s));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las solvencias");
        }
    }

    @GET
    @Path("saldo/{idEstudiante}")
    public Response saldo(@PathParam("idEstudiante") int idEstudiante) {
        try {
            Saldo s = solvenciaDAO.consultarSaldo(idEstudiante);
            return Response.ok(new RespuestaSaldo(idEstudiante, s.getAnioNombre(), s.isSolvente(), s.getMonto(),
                    s.isInscripcionPendiente(), s.getMesesPendientes())).build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al calcular el saldo");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response emitir(SolicitudSolvencia solicitud, @Context ContainerRequestContext contexto) {
        if (solicitud == null || solicitud.getIdEstudiante() == null) {
            return error(Response.Status.BAD_REQUEST, "El estudiante es obligatorio");
        }
        try {
            int idSecretaria = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            Solvencia s = solvenciaDAO.emitir(solicitud.getIdEstudiante(), idSecretaria);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(s)).build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al emitir la solvencia");
        }
    }

    private RespuestaSolvencia aRespuesta(Solvencia s) {
        return new RespuestaSolvencia(s.getIdSolvencia(), s.getIdEstudiante(), s.getEstudianteNombre(),
                s.getAnioNombre(), s.getSecretariaNombre(), s.getFechaEmision().toString(), s.isYaExistia());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
