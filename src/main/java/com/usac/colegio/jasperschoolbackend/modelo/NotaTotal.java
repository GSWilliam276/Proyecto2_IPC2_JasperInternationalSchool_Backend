/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public record NotaTotal
        //Total (zona + examen) de un estudiante en un curso; solo existe si tiene al menos una nota
        (int idEstudiante, String estudiante, String curso, BigDecimal total) {
}
