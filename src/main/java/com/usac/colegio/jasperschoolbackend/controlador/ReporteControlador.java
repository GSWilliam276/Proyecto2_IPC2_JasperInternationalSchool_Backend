/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.FilaIngreso;
import com.usac.colegio.jasperschoolbackend.modelo.FilaMora;
import com.usac.colegio.jasperschoolbackend.modelo.FilaPago;
import com.usac.colegio.jasperschoolbackend.modelo.FilaSolvencia;
import com.usac.colegio.jasperschoolbackend.modelo.Inscrito;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.ReporteDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.SolvenciaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ReporteDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SolvenciaDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaHistorial;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaSolventes;
import com.usac.colegio.jasperschoolbackend.utilidades.GeneradorReporte;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private final SolvenciaDAO solvenciaDAO = new SolvenciaDAOPersistencia();

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
    
    @GET
    @Path("historial-pagos")
    @RolesPermitidos({"SECRETARIA", "ADMIN", "SUPERADMIN"})
    public Response historialPagos(@QueryParam("idEstudiante") Integer idEstudiante,
                                   @QueryParam("desde") String desde, @QueryParam("hasta") String hasta,
                                   @QueryParam("pagina") @DefaultValue("1") int pagina,
                                   @QueryParam("tamano") @DefaultValue("10") int tamano) {
        if (idEstudiante == null) {
            return error(Response.Status.BAD_REQUEST, "El estudiante es obligatorio");
        }
        if (pagina < 1 || tamano < 1 || tamano > 100) {
            return error(Response.Status.BAD_REQUEST, "La página debe ser mayor que cero y el tamaño de 1 a 100");
        }
        try {
            LocalDate d = fecha(desde);
            LocalDate h = fecha(hasta);
            String problema = validarRango(d, h);
            if (problema != null) {
                return error(Response.Status.BAD_REQUEST, problema);
            }
            String nombre = reporteDAO.nombreEstudiante(idEstudiante);
            if (nombre == null) {
                return error(Response.Status.NOT_FOUND, "No existe un estudiante con ese id");
            }
            Object[] tot = reporteDAO.totalPagos(idEstudiante, d, h);
            List<FilaPago> filas = reporteDAO.historialPagos(idEstudiante, d, h, pagina, tamano);
            Boolean solvente = null;
            BigDecimal saldo = null;
            boolean inscPend = false;
            List<Integer> meses = new ArrayList<>();
            String aviso = null;
            try {
                var s = solvenciaDAO.consultarSaldo(idEstudiante);
                solvente = s.isSolvente();
                saldo = s.getMonto();
                inscPend = s.isInscripcionPendiente();
                meses = s.getMesesPendientes();
            } catch (ExcepcionEstudianteNoEncontrado | ExcepcionOperacionNoPermitida
                     | ExcepcionReferenciaInvalida e) {
                aviso = e.getMessage();
            }
            return Response.ok(new RespuestaHistorial(nombre, filas, (Long) tot[0], pagina, tamano,
                    (BigDecimal) tot[1], solvente, saldo, inscPend, meses, aviso)).build();
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "Las fechas deben tener el formato AAAA-MM-DD");
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al generar el reporte");
        }
    }

    @GET
    @Path("historial-pagos/pdf")
    @Produces("application/pdf")
    @RolesPermitidos({"SECRETARIA", "ADMIN", "SUPERADMIN"})
    public Response historialPagosPdf(@QueryParam("idEstudiante") Integer idEstudiante,
                                      @QueryParam("desde") String desde, @QueryParam("hasta") String hasta) {
        if (idEstudiante == null) {
            return error(Response.Status.BAD_REQUEST, "El estudiante es obligatorio");
        }
        try {
            LocalDate d = fecha(desde);
            LocalDate h = fecha(hasta);
            String problema = validarRango(d, h);
            if (problema != null) {
                return error(Response.Status.BAD_REQUEST, problema);
            }
            String nombre = reporteDAO.nombreEstudiante(idEstudiante);
            if (nombre == null) {
                return error(Response.Status.NOT_FOUND, "No existe un estudiante con ese id");
            }
            String textoSaldo;
            try {
                var s = solvenciaDAO.consultarSaldo(idEstudiante);
                textoSaldo = s.isSolvente() ? "Saldo pendiente: ninguno (solvente)"
                        : "Saldo pendiente: Q" + String.format(Locale.US, "%,.2f", s.getMonto());
            } catch (ExcepcionEstudianteNoEncontrado | ExcepcionOperacionNoPermitida
                     | ExcepcionReferenciaInvalida e) {
                textoSaldo = "Saldo pendiente: no disponible (" + e.getMessage() + ")";
            }
            List<Map<String, ?>> filas = new ArrayList<>();
            for (FilaPago f : reporteDAO.historialPagos(idEstudiante, d, h, null, null)) {
                Map<String, Object> m = new HashMap<>();
                m.put("fecha", f.fecha());
                m.put("detalle", f.detalle());
                m.put("monto", f.monto());
                filas.add(m);
            }
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("titulo", "Historial de pagos");
            parametros.put("estudiante", "Estudiante: " + nombre);
            parametros.put("rango", "Pagos realizados: " + rango(d, h));
            parametros.put("saldo", textoSaldo);
            parametros.put("generado", "Generado el "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            byte[] pdf = GeneradorReporte.pdf("historial_pagos", parametros, filas);
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"historial-de-pagos.pdf\"").build();
        } catch (DateTimeParseException e) {
            return error(Response.Status.BAD_REQUEST, "Las fechas deben tener el formato AAAA-MM-DD");
        } catch (ExcepcionPersistencia | JRException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al generar el PDF");
        }
    }

        private List<FilaSolvencia> conSaldo(List<Inscrito> inscritos, boolean activo) {
        List<FilaSolvencia> filas = new ArrayList<>();
        for (Inscrito i : inscritos) {
            String estado = "No disponible";
            BigDecimal monto = null;
            if (activo) {
                try {
                    var s = solvenciaDAO.consultarSaldo(i.idEstudiante());
                    estado = s.isSolvente() ? "Solvente" : "Moroso";
                    monto = s.getMonto();
                } catch (ExcepcionReferenciaInvalida e) {
                    estado = "Sin tarifa";
                } catch (ExcepcionPersistencia e) {
                    estado = "No disponible";
                }
            }
            filas.add(new FilaSolvencia(i.idEstudiante(), i.nombre(), i.cui(), i.grado(), i.seccion(), estado, monto));
        }
        return filas;
    }

    @GET
    @Path("solventes-morosos")
    @RolesPermitidos({"SECRETARIA", "ADMIN", "SUPERADMIN"})
    public Response solventesMorosos(@QueryParam("idAnio") Integer idAnio, @QueryParam("idGrado") Integer idGrado,
                                     @QueryParam("idSeccion") Integer idSeccion,
                                     @QueryParam("pagina") @DefaultValue("1") int pagina,
                                     @QueryParam("tamano") @DefaultValue("10") int tamano) {
        if (pagina < 1 || tamano < 1 || tamano > 100) {
            return error(Response.Status.BAD_REQUEST, "La página debe ser mayor que cero y el tamaño de 1 a 100");
        }
        try {
            String[] anio = reporteDAO.anio(idAnio);
            if (anio == null) {
                return error(Response.Status.NOT_FOUND, "No existe el año lectivo indicado (o no hay uno activo)");
            }
            int id = Integer.parseInt(anio[0]);
            boolean activo = "ACTIVO".equals(anio[2]);
            long total = reporteDAO.contarInscritos(id, idGrado, idSeccion);
            List<FilaSolvencia> filas = conSaldo(
                    reporteDAO.inscritos(id, idGrado, idSeccion, pagina, tamano), activo);
            return Response.ok(new RespuestaSolventes(id, anio[1], activo, filas, total, pagina, tamano)).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al generar el reporte");
        }
    }

    @GET
    @Path("solventes-morosos/pdf")
    @Produces("application/pdf")
    @RolesPermitidos({"SECRETARIA", "ADMIN", "SUPERADMIN"})
    public Response solventesMorososPdf(@QueryParam("idAnio") Integer idAnio, @QueryParam("idGrado") Integer idGrado,
                                        @QueryParam("idSeccion") Integer idSeccion) {
        try {
            String[] anio = reporteDAO.anio(idAnio);
            if (anio == null) {
                return error(Response.Status.NOT_FOUND, "No existe el año lectivo indicado (o no hay uno activo)");
            }
            int id = Integer.parseInt(anio[0]);
            boolean activo = "ACTIVO".equals(anio[2]);
            List<Map<String, ?>> filas = new ArrayList<>();
            for (FilaSolvencia f : conSaldo(reporteDAO.inscritos(id, idGrado, idSeccion, null, null), activo)) {
                Map<String, Object> m = new HashMap<>();
                m.put("estudiante", f.estudiante());
                m.put("ubicacion", f.grado() + " · " + f.seccion());
                m.put("estado", f.estado());
                m.put("monto", f.monto());
                filas.add(m);
            }
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("titulo", "Alumnos solventes y morosos");
            parametros.put("anio", "Año lectivo: " + anio[1]);
            parametros.put("filtros", "Grado: " + (idGrado == null ? "todos" : "id " + idGrado)
                    + " · Sección: " + (idSeccion == null ? "todas" : "id " + idSeccion));
            parametros.put("generado", "Generado el "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            byte[] pdf = GeneradorReporte.pdf("solventes_morosos", parametros, filas);
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"solventes-y-morosos.pdf\"").build();
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
