/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public class RespuestaNota {
    private int idEstudiante;
    private String estudianteNombre;
    private BigDecimal valor;   //null = sin nota

    public RespuestaNota() {
    }

    public RespuestaNota(int idEstudiante, String estudianteNombre, BigDecimal valor) {
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.valor = valor;
    }

    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public BigDecimal getValor() { return valor; }
}
