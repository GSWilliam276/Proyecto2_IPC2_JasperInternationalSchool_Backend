/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionLibroNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPrestamoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Prestamo;
import com.usac.colegio.jasperschoolbackend.modelo.Prestatario;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.PrestamoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.PrestamoDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaPrestamo;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaPrestatario;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudPrestamo;
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
/**
 *
 * @author eduar
 */
@Path("prestamos")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"BIBLIOTECARIO"})
public class PrestamoControlador {
    //Prestamos y devoluciones (CU075 a CU077). Solo el Bibliotecario: el prestamo guarda quien lo registro
    private final PrestamoDAO prestamoDAO = new PrestamoDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("estado") String estado,
                           @QueryParam("idLibro") Integer idLibro,
                           @QueryParam("idPrestatario") Integer idPrestatario,
                           @QueryParam("desde") String desde,
                           @QueryParam("hasta") String hasta) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        String filtroEstado = (estado == null || estado.isBlank()) ? null : estado;
        if (filtroEstado != null && !List.of("PRESTADO", "DEVUELTO", "EN_MORA").contains(filtroEstado)) {
            return error(Response.Status.BAD_REQUEST, "El estado debe ser PRESTADO, DEVUELTO o EN_MORA");
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
            List<RespuestaPrestamo> respuesta = new ArrayList<>();
            for (Prestamo p : prestamoDAO.listar(pagina, tamano, filtroEstado, idLibro, idPrestatario, fechaDesde,
                    fechaHasta)) {
                respuesta.add(aRespuesta(p));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los préstamos");
        }
    }

    @GET
    @Path("prestatarios")
    public Response prestatarios(@QueryParam("busqueda") String busqueda) {
        try {
            List<RespuestaPrestatario> respuesta = new ArrayList<>();
            for (Prestatario p : prestamoDAO.listarPrestatarios(busqueda)) {
                respuesta.add(new RespuestaPrestatario(p.getIdUsuario(), p.getNombre(), p.getCui(), p.getTipo()));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los prestatarios");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrar(SolicitudPrestamo solicitud, @Context ContainerRequestContext contexto) {
        if (solicitud == null || solicitud.getIdLibro() == null || solicitud.getIdPrestatario() == null) {
            return error(Response.Status.BAD_REQUEST, "El libro y el prestatario son obligatorios");
        }
        try {
            int idBibliotecario = Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
            Prestamo p = prestamoDAO.registrar(solicitud.getIdLibro(), solicitud.getIdPrestatario(), idBibliotecario);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(p)).build();
        } catch (ExcepcionLibroNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionReferenciaInvalida | ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al registrar el préstamo");
        }
    }

    @PUT
    @Path("{id}/devolver")
    public Response devolver(@PathParam("id") int id) {
        try {
            return Response.ok(aRespuesta(prestamoDAO.devolver(id))).build();
        } catch (ExcepcionPrestamoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al registrar la devolución");
        }
    }

    //Metodos de Apoyo

    private RespuestaPrestamo aRespuesta(Prestamo p) {
        return new RespuestaPrestamo(p.getIdPrestamo(), p.getIdLibro(), p.getLibroTitulo(), p.getIdPrestatario(),
                p.getPrestatarioNombre(), p.getTipoPrestatario(), p.getBibliotecarioNombre(),
                p.getFechaPrestamo().toString(), p.getFechaEsperada().toString(),
                p.getFechaDevolucion() == null ? null : p.getFechaDevolucion().toString(), p.isDevuelto(),
                p.isEnMora(), p.getDiasAtraso(), p.getMultaMonto(), p.getMultaEstado());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
