/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaAlumno {
    private int idEstudiante;
    private String nombre;
    private String cui;

    public RespuestaAlumno() {
    }

    public RespuestaAlumno(int idEstudiante, String nombre, String cui) {
        this.idEstudiante = idEstudiante;
        this.nombre = nombre;
        this.cui = cui;
    }

    public int getIdEstudiante() { return idEstudiante; }
    public String getNombre() { return nombre; }
    public String getCui() { return cui; }
}
