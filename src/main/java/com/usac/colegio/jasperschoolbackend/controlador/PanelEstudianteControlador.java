/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Multa;
import com.usac.colegio.jasperschoolbackend.modelo.Prestamo;
import com.usac.colegio.jasperschoolbackend.modelo.Saldo;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.MultaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.PanelEstudianteDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.PrestamoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.SolvenciaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.MultaDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.PanelEstudianteDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.PrestamoDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SolvenciaDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaMulta;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaPrestamo;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaSaldo;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
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
@Path("estudiante")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ESTUDIANTE"})
public class PanelEstudianteControlador {
    //Panel del Estudiante (CU091 a CU095): solo lectura y solo lo propio, el id sale del token
    private final PanelEstudianteDAO panelDAO = new PanelEstudianteDAOPersistencia();
    private final SolvenciaDAO solvenciaDAO = new SolvenciaDAOPersistencia();
    private final PrestamoDAO prestamoDAO = new PrestamoDAOPersistencia();
    private final MultaDAO multaDAO = new MultaDAOPersistencia();

    @GET
    @Path("perfil")
    public Response perfil(@Context ContainerRequestContext contexto) {
        try {
            return panelDAO.perfil(id(contexto)).map(p -> Response.ok(p).build())
                    .orElseGet(() -> error(Response.Status.NOT_FOUND, "No se encontró su perfil"));
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar el perfil");
        }
    }

    @GET
    @Path("notas")
    public Response notas(@Context ContainerRequestContext contexto) {
        try {
            return Response.ok(panelDAO.notas(id(contexto))).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar las notas");
        }
    }

    @GET
    @Path("pagos")
    public Response pagos(@Context ContainerRequestContext contexto) {
        try {
            return Response.ok(panelDAO.pagos(id(contexto))).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar los pagos");
        }
    }

    @GET
    @Path("saldo")
    public Response saldo(@Context ContainerRequestContext contexto) {
        int id = id(contexto);
        try {
            Saldo s = solvenciaDAO.consultarSaldo(id);
            return Response.ok(new RespuestaSaldo(id, s.getAnioNombre(), s.isSolvente(), s.getMonto(),
                    s.isInscripcionPendiente(), s.getMesesPendientes())).build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al calcular el saldo");
        }
    }

    @GET
    @Path("prestamos")
    public Response prestamos(@Context ContainerRequestContext contexto) {
        try {
            List<RespuestaPrestamo> r = new ArrayList<>();
            for (Prestamo p : prestamoDAO.listar(1, 100, null, null, id(contexto), null, null)) {
                r.add(new RespuestaPrestamo(p.getIdPrestamo(), p.getIdLibro(), p.getLibroTitulo(),
                        p.getIdPrestatario(), p.getPrestatarioNombre(), p.getTipoPrestatario(),
                        p.getBibliotecarioNombre(), p.getFechaPrestamo().toString(), p.getFechaEsperada().toString(),
                        p.getFechaDevolucion() == null ? null : p.getFechaDevolucion().toString(), p.isDevuelto(),
                        p.isEnMora(), p.getDiasAtraso(), p.getMultaMonto(), p.getMultaEstado()));
            }
            return Response.ok(r).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar los préstamos");
        }
    }

    @GET
    @Path("multas")
    public Response multas(@Context ContainerRequestContext contexto) {
        try {
            List<RespuestaMulta> r = new ArrayList<>();
            for (Multa m : multaDAO.listar(1, 100, null, id(contexto))) {
                r.add(new RespuestaMulta(m.getIdMulta(), m.getIdPrestamo(), m.getLibroTitulo(), m.getIdEstudiante(),
                        m.getEstudianteNombre(), m.getDiasAtraso(), m.getMonto(), m.getEstado(),
                        m.getFechaGeneracion().toString(), m.getIdPago()));
            }
            return Response.ok(r).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar las multas");
        }
    }

    private int id(ContainerRequestContext contexto) {
        return Integer.parseInt(String.valueOf(contexto.getProperty("idUsuario")));
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
