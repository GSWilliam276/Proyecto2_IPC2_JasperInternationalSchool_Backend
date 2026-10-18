/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class SolicitudInscripcion {
    private Integer idEstudiante;
    private Integer idSeccion;
    private String correo;   //Solo se pide si el estudiante todavia no tiene acceso

    public SolicitudInscripcion() {
    }

    public Integer getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Integer idEstudiante) { this.idEstudiante = idEstudiante; }
    public Integer getIdSeccion() { return idSeccion; }
    public void setIdSeccion(Integer idSeccion) { this.idSeccion = idSeccion; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
