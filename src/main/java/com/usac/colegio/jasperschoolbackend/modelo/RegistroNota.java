/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public class RegistroNota {
    //Nota de un estudiante; valor null = sin nota
    private final int idEstudiante;
    private final String estudianteNombre;
    private final BigDecimal valor;

    public RegistroNota(int idEstudiante, String estudianteNombre, BigDecimal valor) {
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.valor = valor;
    }

    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public BigDecimal getValor() { return valor; }
}
