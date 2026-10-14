/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoTarifa;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionTarifaDuplicada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionTarifaNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Tarifa;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.TarifaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.TarifaDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaTarifa;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudEditarTarifa;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudTarifa;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
@Path("tarifas")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN", "SECRETARIA"})
public class TarifaControlador {
    //Tarifas (CU051 a CU053). Consultar: Admin y Secretaria. Crear y editar: Admin y, por herencia, SuperAdmin
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("100000"); //Tope razonable

    private final TarifaDAO tarifaDAO = new TarifaDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("idGrado") Integer idGrado,
                           @QueryParam("todos") @DefaultValue("false") boolean todos) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaTarifa> respuesta = new ArrayList<>();
            for (Tarifa t : tarifaDAO.listar(pagina, tamano, idGrado, todos)) {
                respuesta.add(aRespuesta(t));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las tarifas");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response crear(SolicitudTarifa solicitud) {
        if (solicitud == null || solicitud.getIdGrado() == null || solicitud.getConcepto() == null) {
            return error(Response.Status.BAD_REQUEST, "El grado, el concepto y el monto son obligatorios");
        }
        ConceptoTarifa concepto;
        try {
            concepto = ConceptoTarifa.valueOf(solicitud.getConcepto());
        } catch (IllegalArgumentException e) {
            return error(Response.Status.BAD_REQUEST, "El concepto debe ser INSCRIPCION o MENSUALIDAD");
        }
        String problema = validarMonto(solicitud.getMonto());
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            Tarifa nueva = tarifaDAO.crear(solicitud.getIdGrado(), concepto, solicitud.getMonto());
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nueva)).build();
        } catch (ExcepcionTarifaDuplicada | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear la tarifa");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response editar(@PathParam("id") int id, SolicitudEditarTarifa solicitud) {
        String problema = validarMonto(solicitud == null ? null : solicitud.getMonto());
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            tarifaDAO.editar(id, solicitud.getMonto());
            return Response.noContent().build();
        } catch (ExcepcionTarifaNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar la tarifa");
        }
    }

    //Metodos de Apoyo

    private String validarMonto(BigDecimal monto) {
        if (monto == null) {
            return "El monto es obligatorio";
        }
        if (monto.signum() <= 0) {
            return "El monto debe ser mayor que cero";
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            return "El monto admite como máximo 2 decimales";
        }
        if (monto.compareTo(MONTO_MAXIMO) > 0) {
            return "El monto no puede pasar de Q100,000";
        }
        return null;
    }

    private RespuestaTarifa aRespuesta(Tarifa t) {
        return new RespuestaTarifa(t.getIdTarifa(), t.getGrado().getIdGrado(), t.getGrado().getNombre(),
                t.getGrado().getNivel().name(), t.getConcepto().name(), t.getIdAnioLectivo(),
                t.getAnioNombre(), t.getEstadoAnio().name(), t.getMonto());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
