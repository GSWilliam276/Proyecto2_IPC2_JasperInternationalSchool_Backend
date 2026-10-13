/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaCurso {
    private int idCurso;
    private String nombre;
    private String descripcion;
    private String estado;

    public RespuestaCurso() {
    }

    public RespuestaCurso(int idCurso, String nombre, String descripcion, String estado) {
        this.idCurso = idCurso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdCurso() { return idCurso; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
}
