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
public class SolicitudTarifa {
    private Integer idGrado;
    private String concepto; // INSCRIPCION o MENSUALIDAD
    private BigDecimal monto;

    public SolicitudTarifa() {
    }

    public Integer getIdGrado() { return idGrado; }
    public void setIdGrado(Integer idGrado) { this.idGrado = idGrado; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
