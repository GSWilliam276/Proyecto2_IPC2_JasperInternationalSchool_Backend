/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public class RespuestaParametrosBiblioteca {
    private int diasPrestamo;
    private BigDecimal multaPorDia;

    public RespuestaParametrosBiblioteca() {
    }

    public RespuestaParametrosBiblioteca(int diasPrestamo, BigDecimal multaPorDia) {
        this.diasPrestamo = diasPrestamo;
        this.multaPorDia = multaPorDia;
    }

    public int getDiasPrestamo() { return diasPrestamo; }
    public BigDecimal getMultaPorDia() { return multaPorDia; }
}
