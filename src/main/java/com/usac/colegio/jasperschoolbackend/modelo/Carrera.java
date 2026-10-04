/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
/**
 *
 * @author eduar
 */
public class Carrera {
    private int idCarrera;
    private String nombre;
    private EstadoGeneral estado;

    public Carrera() {
    }

    public Carrera(int idCarrera, String nombre, EstadoGeneral estado) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
        this.estado = estado;
    }

    public int getIdCarrera() { return idCarrera; }
    public void setIdCarrera(int idCarrera) { this.idCarrera = idCarrera; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
}
