/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class SolicitudCurriculo {
    //Para crear un curriculo. idCarrera solo se manda si el grado es de Diversificado
    private Integer idGrado;
    private Integer idCarrera;

    public SolicitudCurriculo() {
    }

    public Integer getIdGrado() { return idGrado; }
    public void setIdGrado(Integer idGrado) { this.idGrado = idGrado; }
    public Integer getIdCarrera() { return idCarrera; }
    public void setIdCarrera(Integer idCarrera) { this.idCarrera = idCarrera; }
}
