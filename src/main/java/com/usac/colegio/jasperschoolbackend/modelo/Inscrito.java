/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

/**
 *
 * @author eduar
 */
public record Inscrito
        //Estudiante inscrito en el año consultado (sin saldo todavia)
        (int idEstudiante, String nombre, String cui, String grado, String seccion) {
}
