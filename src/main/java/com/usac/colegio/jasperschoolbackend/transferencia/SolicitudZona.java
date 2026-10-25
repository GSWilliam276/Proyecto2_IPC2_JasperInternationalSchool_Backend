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
public class SolicitudZona {
    private String nombre;
    private BigDecimal punteo;

    public SolicitudZona() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getPunteo() { return punteo; }
    public void setPunteo(BigDecimal punteo) { this.punteo = punteo; }
}
