/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaGraduacion {
    private String nivel;
    private String fecha;

    public RespuestaGraduacion() {
    }

    public RespuestaGraduacion(String nivel, String fecha) {
        this.nivel = nivel;
        this.fecha = fecha;
    }

    public String getNivel() { return nivel; }
    public String getFecha() { return fecha; }
}
