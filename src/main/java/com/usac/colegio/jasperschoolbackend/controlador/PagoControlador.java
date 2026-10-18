/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoPago;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionActividadNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPagoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Comprobante;
import com.usac.colegio.jasperschoolbackend.modelo.Pago;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.ComprobanteDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.PagoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ComprobanteDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.PagoDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaComprobante;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaPago;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudAnularPago;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudPagoActividad;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudPagoInscripcion;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudPagoMensualidad;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
/**
 *
 * @author eduar
 */
@Path("pagos")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"SECRETARIA"})
public class PagoControlador {
    //Pagos (CU062, CU063 y CU066 por ahora). Solo la Secretaria: el pago guarda quien lo registro
    private final PagoDAO pagoDAO = new PagoDAOPersistencia();
    private final ComprobanteDAO comprobanteDAO = new ComprobanteDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("idEstudiante") Integer idEstudiante,
                           @QueryParam("concepto") String concepto,
                           @QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        ConceptoPago filtroConcepto = null;
        if (concepto != null && !concepto.isBlank()) {
            try {
                filtroConcepto = ConceptoPago.valueOf(concepto);
            } catch (IllegalArgumentException e) {
                return error(Response.Status.BAD_REQUEST, "El concepto debe ser INSCRIPCION, MENSUALIDAD, ACTIVIDAD o MULTA");
            }
        }
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
            List<RespuestaPago> respuesta = new ArrayList<>();
            for (Pago p : pagoDAO.listar(pagina, tamano, idEstudiante, filtroConcepto, fechaDesde, fechaHasta)) {
                respuesta.add(aRespuesta(p));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los pagos");
        }
    }

    @POST
    @Path("inscripcion")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pagarInscripcion(SolicitudPagoInscripcion solicitud, @Context ContainerRequestContext contexto) {
        if (solicitud == null || solicitud.getIdEstudiante() == null) {
            return error(Response.Status.BAD_REQUEST, "El estudiante es obligatorio");
        }
        try {
            int idSecretaria = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            Pago pago = pagoDAO.registrarInscripcion(solicitud.getIdEstudiante(), idSecretaria);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(pago)).build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al registrar el pago de inscripción");
        }
    }

    @POST
    @Path("mensualidad")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pagarMensualidad(SolicitudPagoMensualidad solicitud, @Context ContainerRequestContext contexto) {
        if (solicitud == null || solicitud.getIdEstudiante() == null || solicitud.getMeses() == null
                || solicitud.getMeses().isEmpty()) {
            return error(Response.Status.BAD_REQUEST, "El estudiante y al menos un mes son obligatorios");
        }
        TreeSet<Integer> meses = new TreeSet<>();   //Sin repetidos y en orden
        for (Integer mes : solicitud.getMeses()) {
            if (mes == null || mes < 1 || mes > 12) {
                return error(Response.Status.BAD_REQUEST, "Los meses deben ser números del 1 al 12");
            }
            meses.add(mes);
        }
        try {
            int idSecretaria = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            List<Pago> pagos = pagoDAO.registrarMensualidades(solicitud.getIdEstudiante(), new ArrayList<>(meses), idSecretaria);
            List<RespuestaPago> respuesta = new ArrayList<>();
            for (Pago p : pagos) {
                respuesta.add(aRespuesta(p));
            }
            return Response.status(Response.Status.CREATED).entity(respuesta).build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al registrar el pago de mensualidad");
        }
    }
    
    @POST
    @Path("actividad")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pagarActividad(SolicitudPagoActividad solicitud, @Context ContainerRequestContext contexto) {
        if (solicitud == null || solicitud.getIdEstudiante() == null || solicitud.getIdActividad() == null) {
            return error(Response.Status.BAD_REQUEST, "El estudiante y la actividad son obligatorios");
        }
        try {
            int idSecretaria = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            Pago pago = pagoDAO.registrarActividad(solicitud.getIdEstudiante(), solicitud.getIdActividad(), idSecretaria);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(pago)).build();
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionActividadNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al registrar el pago de actividad");
        }
    }

    @PUT
    @Path("{id}/anular")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response anular(@PathParam("id") int id, SolicitudAnularPago solicitud) {
        if (solicitud == null || solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()) {
            return error(Response.Status.BAD_REQUEST, "El motivo de la anulación es obligatorio");
        }
        if (solicitud.getMotivo().trim().length() > 255) {
            return error(Response.Status.BAD_REQUEST, "El motivo no puede pasar de 255 caracteres");
        }
        try {
            pagoDAO.anular(id, solicitud.getMotivo().trim());
            return Response.noContent().build();
        } catch (ExcepcionPagoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al anular el pago");
        }
    }

    @POST
    @Path("{id}/comprobante")
    public Response emitirComprobante(@PathParam("id") int id) {
        try {
            return Response.status(Response.Status.CREATED).entity(aRespuesta(comprobanteDAO.emitir(id))).build();
        } catch (ExcepcionPagoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al emitir el comprobante");
        }
    }

    @GET
    @Path("{id}/comprobante")
    public Response consultarComprobante(@PathParam("id") int id) {
        try {
            Optional<Comprobante> comprobante = comprobanteDAO.buscarPorPago(id);
            if (comprobante.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "El pago no tiene comprobante");
            }
            return Response.ok(aRespuesta(comprobante.get())).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar el comprobante");
        }
    }

    private RespuestaComprobante aRespuesta(Comprobante c) {
        return new RespuestaComprobante(c.getIdComprobante(), c.getNumero(), c.getFechaEmision().toString(),
                c.getEstado().name(), aRespuesta(c.getPago()));
    }

    //Metodos de Apoyo

    private RespuestaPago aRespuesta(Pago p) {
        return new RespuestaPago(p.getIdPago(), p.getIdEstudiante(), p.getEstudianteNombre(), p.getIdAnioLectivo(),
                p.getAnioNombre(), p.getConcepto().name(), p.getMonto(), p.getMes(), p.getActividadNombre(),
                p.getFecha().toString(), p.getSecretariaNombre(), p.getEstado().name(), p.getMotivoAnulacion());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
