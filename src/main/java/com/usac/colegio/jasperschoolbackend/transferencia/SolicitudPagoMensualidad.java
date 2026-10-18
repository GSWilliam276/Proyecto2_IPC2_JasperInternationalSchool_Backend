/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.util.List;
/**
 *
 * @author eduar
 */
public class SolicitudPagoMensualidad {
    private Integer idEstudiante;
    private List<Integer> meses;   // 1 = enero ... 12 = diciembre; uno o varios

    public SolicitudPagoMensualidad() {
    }

    public Integer getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Integer idEstudiante) { this.idEstudiante = idEstudiante; }
    public List<Integer> getMeses() { return meses; }
    public void setMeses(List<Integer> meses) { this.meses = meses; }
}
