/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import com.usac.colegio.jasperschoolbackend.modelo.ResumenAlumno;
import java.math.BigDecimal;
import java.util.List;
/**
 *
 * @author eduar
 */
public record RespuestaResumen(BigDecimal notaMinima, int aprobados, int reprobados, int enCurso,
                               List<ResumenAlumno> alumnos) {

}
