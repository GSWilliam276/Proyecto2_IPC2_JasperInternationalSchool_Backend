/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.time.LocalDate;
/**
 *
 * @author eduar
 */
public class RegistroAsistencia {
    //Una fila de asistencia. En la planilla, presente es null si el estudiante no tiene registro ese dia
    private final int idEstudiante;
    private final String estudianteNombre;
    private final String cui;
    private final LocalDate fecha;
    private final Boolean presente;

    public RegistroAsistencia(int idEstudiante, String estudianteNombre, String cui, LocalDate fecha, Boolean presente) {
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.cui = cui;
        this.fecha = fecha;
        this.presente = presente;
    }

    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public String getCui() { return cui; }
    public LocalDate getFecha() { return fecha; }
    public Boolean getPresente() { return presente; }
}
