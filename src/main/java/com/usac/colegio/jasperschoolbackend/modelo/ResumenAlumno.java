/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public record ResumenAlumno(int idEstudiante, String nombre, BigDecimal zona, BigDecimal zonaMaxima,
                            BigDecimal examenFinal, BigDecimal total, String estado) {
}
