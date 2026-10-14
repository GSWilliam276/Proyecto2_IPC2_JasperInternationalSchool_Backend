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
public class RespuestaTarifa {
    private int idTarifa;
    private int idGrado;
    private String gradoNombre;
    private String nivel;
    private String concepto;
    private int idAnioLectivo;
    private String anioNombre;
    private String anioEstado;
    private BigDecimal monto;

    public RespuestaTarifa() {
    }

    public RespuestaTarifa(int idTarifa, int idGrado, String gradoNombre, String nivel, String concepto,
                           int idAnioLectivo, String anioNombre, String anioEstado, BigDecimal monto) {
        this.idTarifa = idTarifa;
        this.idGrado = idGrado;
        this.gradoNombre = gradoNombre;
        this.nivel = nivel;
        this.concepto = concepto;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.anioEstado = anioEstado;
        this.monto = monto;
    }

    public int getIdTarifa() { return idTarifa; }
    public int getIdGrado() { return idGrado; }
    public String getGradoNombre() { return gradoNombre; }
    public String getNivel() { return nivel; }
    public String getConcepto() { return concepto; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public String getAnioEstado() { return anioEstado; }
    public BigDecimal getMonto() { return monto; }
}
