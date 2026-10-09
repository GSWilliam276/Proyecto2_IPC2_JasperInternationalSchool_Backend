/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaCarrera {
    //Lo que el backend devuelve de una carrera
    private int idCarrera;
    private String nombre;
    private String estado;

    public RespuestaCarrera() {
    }

    public RespuestaCarrera(int idCarrera, String nombre, String estado) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
        this.estado = estado;
    }

    public int getIdCarrera() { return idCarrera; }
    public String getNombre() { return nombre; }
    public String getEstado() { return estado; }
}
