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
public class Curso {
    //Curso del catalogo del colegio (Matematica, Lenguaje...). Se enlaza a los grados por el curriculo
    private int idCurso;
    private String nombre;
    private String descripcion; //opcional
    private EstadoGeneral estado;

    public Curso(int idCurso, String nombre, String descripcion, EstadoGeneral estado) {
        this.idCurso = idCurso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
}
