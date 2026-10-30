/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import java.math.BigDecimal;

/**
 *
 * @author eduar
 */
public final class Dictamen {
    //Unica definicion de aprobado/reprobado: la usan el panel del Estudiante y el del Maestro
    public static final BigDecimal NOTA_MINIMA = new BigDecimal("60");

    private Dictamen() {
    }

    //EN_CURSO mientras no exista examen final; despues APROBADO o REPROBADO segun el total
    public static String estado(BigDecimal examenFinal, BigDecimal total) {
        if (examenFinal == null) {
            return "EN_CURSO";
        }
        return total.compareTo(NOTA_MINIMA) >= 0 ? "APROBADO" : "REPROBADO";
    }
}
