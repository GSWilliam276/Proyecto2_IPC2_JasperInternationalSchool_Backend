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
public class RespuestaActividad {
    private int idActividad;
    private String nombre;
    private String descripcion;
    private String fecha;
    private int cupo;
    private BigDecimal costo;
    private boolean gratuita;
    private String estado;

    public RespuestaActividad() {
    }

    public RespuestaActividad(int idActividad, String nombre, String descripcion, String fecha, int cupo,
                              BigDecimal costo, boolean gratuita, String estado) {
        this.idActividad = idActividad;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.cupo = cupo;
        this.costo = costo;
        this.gratuita = gratuita;
        this.estado = estado;
    }

    public int getIdActividad() { return idActividad; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getFecha() { return fecha; }
    public int getCupo() { return cupo; }
    public BigDecimal getCosto() { return costo; }
    public boolean isGratuita() { return gratuita; }
    public String getEstado() { return estado; }
}
