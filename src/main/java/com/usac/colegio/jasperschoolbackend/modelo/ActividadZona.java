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
public class ActividadZona {
    //Actividad evaluable de un curso y seccion (una asignacion)
    private int idActividadZona;
    private final int idAsignacion;
    private final String nombre;
    private final BigDecimal punteo;

    public ActividadZona(int idActividadZona, int idAsignacion, String nombre, BigDecimal punteo) {
        this.idActividadZona = idActividadZona;
        this.idAsignacion = idAsignacion;
        this.nombre = nombre;
        this.punteo = punteo;
    }

    public int getIdActividadZona() { return idActividadZona; }
    public void setIdActividadZona(int id) { this.idActividadZona = id; }
    public int getIdAsignacion() { return idAsignacion; }
    public String getNombre() { return nombre; }
    public BigDecimal getPunteo() { return punteo; }
}
