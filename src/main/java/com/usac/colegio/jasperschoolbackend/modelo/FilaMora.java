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
public record FilaMora
        //Una fila del reporte de prestamos en mora
        (String libro, String prestatario, String tipo, String fechaPrestamo,
                       String fechaEsperada, Integer diasAtraso, BigDecimal recargo) {
}
