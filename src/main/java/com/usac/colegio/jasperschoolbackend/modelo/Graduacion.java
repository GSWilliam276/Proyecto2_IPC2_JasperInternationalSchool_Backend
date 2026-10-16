/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import java.time.LocalDate;
/**
 *
 * @author eduar
 */
public class Graduacion {
    //Una graduacion de nivel de un estudiante: de que nivel y en que fecha
    private final Nivel nivel;
    private final LocalDate fecha;

    public Graduacion(Nivel nivel, LocalDate fecha) {
        this.nivel = nivel;
        this.fecha = fecha;
    }

    public Nivel getNivel() { return nivel; }
    public LocalDate getFecha() { return fecha; }
}
