/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ParametrosBiblioteca;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.ParametrosBibliotecaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ParametrosBibliotecaDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaParametrosBiblioteca;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudParametrosBiblioteca;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Optional;
/**
 *
 * @author eduar
 */
@Path("parametros-biblioteca")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN", "BIBLIOTECARIO"})
//CU103 Editar y CU104 Consultar parametros de biblioteca
public class ParametrosBibliotecaControlador {
    private static final BigDecimal MULTA_MAXIMA = new BigDecimal("1000");

    private final ParametrosBibliotecaDAO parametrosDAO = new ParametrosBibliotecaDAOPersistencia();

    @GET
    public Response consultar() {
        try {
            Optional<ParametrosBiblioteca> parametros = parametrosDAO.consultar();
            if (parametros.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "Los parámetros de biblioteca no están configurados");
            }
            return Response.ok(aRespuesta(parametros.get())).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar los parámetros");
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response editar(SolicitudParametrosBiblioteca solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            parametrosDAO.actualizar(solicitud.getDiasPrestamo(), solicitud.getMultaPorDia());
            //Se vuelve a leer para devolver lo que de verdad quedo guardado
            return Response.ok(aRespuesta(parametrosDAO.consultar().orElseThrow(
                    () -> new ExcepcionPersistencia("No se pudo leer lo guardado", null)))).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar los parámetros");
        }
    }

    //Metodo de Apoyo

    private String validar(SolicitudParametrosBiblioteca s) {
        if (s == null || s.getDiasPrestamo() == null || s.getMultaPorDia() == null) {
            return "El plazo y la multa son obligatorios";
        }
        if (s.getDiasPrestamo() < 1 || s.getDiasPrestamo() > 365) {
            return "El plazo debe estar entre 1 y 365 días";
        }
        if (s.getMultaPorDia().signum() <= 0) {
            return "La multa por día debe ser mayor que cero";
        }
        if (s.getMultaPorDia().stripTrailingZeros().scale() > 2) {
            return "La multa admite como máximo 2 decimales";
        }
        if (s.getMultaPorDia().compareTo(MULTA_MAXIMA) > 0) {
            return "La multa por día no puede pasar de Q1,000";
        }
        return null;
    }

    private RespuestaParametrosBiblioteca aRespuesta(ParametrosBiblioteca p) {
        return new RespuestaParametrosBiblioteca(p.getDiasPrestamo(), p.getMultaPorDia());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
