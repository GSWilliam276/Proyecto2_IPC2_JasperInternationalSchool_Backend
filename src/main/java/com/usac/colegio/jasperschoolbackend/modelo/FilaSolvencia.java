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
public record FilaSolvencia
        //Fila del reporte: estado Solvente, Moroso, Sin tarifa o No disponible; monto null si no aplica
        (int idEstudiante, String estudiante, String cui, String grado, String seccion,
                            String estado, BigDecimal monto) {
}
