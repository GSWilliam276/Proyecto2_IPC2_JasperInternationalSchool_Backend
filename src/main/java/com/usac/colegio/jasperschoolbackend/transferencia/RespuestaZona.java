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
public class RespuestaZona {
    private int idActividadZona;
    private String nombre;
    private BigDecimal punteo;

    public RespuestaZona() {
    }

    public RespuestaZona(int idActividadZona, String nombre, BigDecimal punteo) {
        this.idActividadZona = idActividadZona;
        this.nombre = nombre;
        this.punteo = punteo;
    }

    public int getIdActividadZona() { return idActividadZona; }
    public String getNombre() { return nombre; }
    public BigDecimal getPunteo() { return punteo; }
}
