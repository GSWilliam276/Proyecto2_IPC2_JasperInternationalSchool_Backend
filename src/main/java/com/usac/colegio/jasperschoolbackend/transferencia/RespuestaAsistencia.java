/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaAsistencia {
    private int idEstudiante;
    private String estudianteNombre;
    private String cui;
    private String fecha;
    private Boolean presente;   //null = sin registro

    public RespuestaAsistencia() {
    }

    public RespuestaAsistencia(int idEstudiante, String estudianteNombre, String cui, String fecha, Boolean presente) {
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.cui = cui;
        this.fecha = fecha;
        this.presente = presente;
    }

    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public String getCui() { return cui; }
    public String getFecha() { return fecha; }
    public Boolean getPresente() { return presente; }
}
