/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
/**
 *
 * @author eduar
 */
public class Grado {
    private int idGrado;
    private String nombre;
    private Nivel nivel;
    private EstadoGeneral estado;

    public Grado() {
    }

    public Grado(int idGrado, String nombre, Nivel nivel, EstadoGeneral estado) {
        this.idGrado = idGrado;
        this.nombre = nombre;
        this.nivel = nivel;
        this.estado = estado;
    }

    public int getIdGrado() { return idGrado; }
    public void setIdGrado(int idGrado) { this.idGrado = idGrado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Nivel getNivel() { return nivel; }
    public void setNivel(Nivel nivel) { this.nivel = nivel; }

    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
}
