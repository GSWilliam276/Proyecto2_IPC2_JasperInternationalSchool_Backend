/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public class ParametrosBiblioteca {
    //Parametros de la biblioteca. Es una sola fila (id 1) que el Admin consulta y edita
    private int diasPrestamo;
    private BigDecimal multaPorDia; //BigDecimal por ser dinero

    public ParametrosBiblioteca(int diasPrestamo, BigDecimal multaPorDia) {
        this.diasPrestamo = diasPrestamo;
        this.multaPorDia = multaPorDia;
    }

    public int getDiasPrestamo() { return diasPrestamo; }
    public void setDiasPrestamo(int diasPrestamo) { this.diasPrestamo = diasPrestamo; }
    public BigDecimal getMultaPorDia() { return multaPorDia; }
    public void setMultaPorDia(BigDecimal multaPorDia) { this.multaPorDia = multaPorDia; }
}
