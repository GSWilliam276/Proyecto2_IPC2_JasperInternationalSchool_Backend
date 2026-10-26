/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.FilaIngreso;
import com.usac.colegio.jasperschoolbackend.modelo.FilaMora;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.ReporteDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ReporteDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.utilidades.GeneradorReporte;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
/**
 *
 * @author eduar
 */
@Path("reportes")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"BIBLIOTECARIO", "SECRETARIA", "ADMIN", "SUPERADMIN"})
public class ReporteControlador {
    //Reportes (CU096 a CU101). Cada uno: tabla en JSON y el mismo contenido en PDF con JasperReports
    private final ReporteDAO reporteDAO = new ReporteDAOPersistencia();

    @GET
    @Path("prestamos-mora")
    @RolesPermitidos({"BIBLIOTECARIO"})
    public Response prestamosMora(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        try {
            LocalDate d = fecha(desde);
            LocalDate h = fecha(hasta);
            String problema = validarRango(d, h);
            if (problema != null) {
                return error(Response.Status.BAD_REQUEST, problema);
            }
            return Response.ok(reporteDAO.prestamosEnMora(d, h)).build();
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "Las fechas deben tener el formato AAAA-MM-DD");
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al generar el reporte");
        }
    }

    @GET
    @Path("prestamos-mora/pdf")
    @Produces("application/pdf")
    @RolesPermitidos({"BIBLIOTECARIO"})
    public Response prestamosMoraPdf(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        try {
            LocalDate d = fecha(desde);
            LocalDate h = fecha(hasta);
            String problema = validarRango(d, h);
            if (problema != null) {
                return error(Response.Status.BAD_REQUEST, problema);
            }
            List<Map<String, ?>> filas = new ArrayList<>();
            for (FilaMora f : reporteDAO.prestamosEnMora(d, h)) {
                Map<String, Object> m = new HashMap<>();
                m.put("libro", f.libro());
                m.put("prestatario", f.prestatario());
                m.put("tipo", f.tipo());
                m.put("fechaPrestamo", f.fechaPrestamo());
                m.put("fechaEsperada", f.fechaEsperada());
                m.put("diasAtraso", f.diasAtraso());
                m.put("recargo", f.recargo());
                filas.add(m);
            }
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("titulo", "Préstamos de biblioteca en mora");
            parametros.put("rango", "Préstamos realizados: " + rango(d, h));
            parametros.put("generado", "Generado el "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            byte[] pdf = GeneradorReporte.pdf("prestamos_mora", parametros, filas);
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"prestamos-en-mora.pdf\"").build();
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "Las fechas deben tener el formato AAAA-MM-DD");
        } catch (ExcepcionPersistencia | JRException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al generar el PDF");
        }
    }
    
    @GET
    @Path("ingresos")
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response ingresos(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        try {
            LocalDate d = fecha(desde);
            LocalDate h = fecha(hasta);
            String problema = validarRango(d, h);
            if (problema != null) {
                return error(Response.Status.BAD_REQUEST, problema);
            }
            return Response.ok(reporteDAO.ingresos(d, h)).build();
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "Las fechas deben tener el formato AAAA-MM-DD");
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al generar el reporte");
        }
    }

    @GET
    @Path("ingresos/pdf")
    @Produces("application/pdf")
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response ingresosPdf(@QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        try {
            LocalDate d = fecha(desde);
            LocalDate h = fecha(hasta);
            String problema = validarRango(d, h);
            if (problema != null) {
                return error(Response.Status.BAD_REQUEST, problema);
            }
            List<Map<String, ?>> filas = new ArrayList<>();
            for (FilaIngreso f : reporteDAO.ingresos(d, h)) {
                Map<String, Object> m = new HashMap<>();
                m.put("concepto", f.concepto());
                m.put("cantidad", f.cantidad());
                m.put("total", f.total());
                filas.add(m);
            }
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("titulo", "Reporte de ingresos");
            parametros.put("rango", "Pagos realizados: " + rango(d, h));
            parametros.put("generado", "Generado el "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            byte[] pdf = GeneradorReporte.pdf("ingresos", parametros, filas);
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"ingresos.pdf\"").build();
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "Las fechas deben tener el formato AAAA-MM-DD");
        } catch (ExcepcionPersistencia | JRException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al generar el PDF");
        }
    }

    //Metodos de Apoyo

    private LocalDate fecha(String texto) {
        return (texto == null || texto.isBlank()) ? null : LocalDate.parse(texto.trim());
    }

    private String validarRango(LocalDate d, LocalDate h) {
        return (d != null && h != null && d.isAfter(h)) ? "La fecha inicial no puede ser posterior a la final" : null;
    }

    private String rango(LocalDate d, LocalDate h) {
        if (d == null && h == null) {
            return "todos los registros";
        }
        return (d == null ? "desde el inicio" : "desde " + d) + " " + (h == null ? "hasta hoy" : "hasta " + h);
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).type(MediaType.APPLICATION_JSON).entity(new RespuestaError(mensaje)).build();
    }
}
