/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
import java.util.List;
/**
 *
 * @author eduar
 */
public record NotaCurso(int idAsignacion, String curso, String maestro, List<NotaActividad> actividades,
                        BigDecimal zonaMaxima, BigDecimal zonaObtenida, BigDecimal examenFinal, BigDecimal total) {
}
