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
public class SolicitudParametrosBiblioteca {
    private Integer diasPrestamo;
    private BigDecimal multaPorDia;

    public SolicitudParametrosBiblioteca() {
    }

    public Integer getDiasPrestamo() { return diasPrestamo; }
    public void setDiasPrestamo(Integer diasPrestamo) { this.diasPrestamo = diasPrestamo; }
    public BigDecimal getMultaPorDia() { return multaPorDia; }
    public void setMultaPorDia(BigDecimal multaPorDia) { this.multaPorDia = multaPorDia; }
}
