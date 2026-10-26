/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
/**
 *
 * @author eduar
 */
public final class GeneradorReporte {
    private static final Map<String, JasperReport> COMPILADOS = new ConcurrentHashMap<>();

    private GeneradorReporte() {
    }

    public static byte[] pdf(String plantilla, Map<String, Object> parametros, Collection<Map<String, ?>> filas)
            throws JRException {
        JasperReport reporte = COMPILADOS.get(plantilla);
        if (reporte == null) {
            try (InputStream entrada = GeneradorReporte.class.getResourceAsStream("/reportes/" + plantilla + ".jrxml")) {
                if (entrada == null) {
                    throw new JRException("No se encontró la plantilla " + plantilla);
                }
                reporte = JasperCompileManager.compileReport(entrada);
            } catch (java.io.IOException e) {
                throw new JRException("No se pudo leer la plantilla " + plantilla, e);
            }
            COMPILADOS.put(plantilla, reporte);
        }
        JasperPrint impresion = JasperFillManager.fillReport(reporte, parametros, new JRMapCollectionDataSource(filas));
        return JasperExportManager.exportReportToPdf(impresion);
    }
}
