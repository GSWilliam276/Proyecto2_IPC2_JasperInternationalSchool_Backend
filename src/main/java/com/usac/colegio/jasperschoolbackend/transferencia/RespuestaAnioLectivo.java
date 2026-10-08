/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaAnioLectivo {
    //Lo que el backend devuelve de un año lectivo
    private int idAnioLectivo;
    private String nombre;
    private String fechaInicio;
    private String fechaFin;
    private String estado;

    public RespuestaAnioLectivo() {
    }

    public RespuestaAnioLectivo(int idAnioLectivo, String nombre, String fechaInicio, String fechaFin, String estado) {
        this.idAnioLectivo = idAnioLectivo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getNombre() { return nombre; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
}
