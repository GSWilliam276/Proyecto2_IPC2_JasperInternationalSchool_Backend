/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.excepciones;

/**
 *
 * @author eduar
 */
public class ExcepcionCursoEnUso extends ExcepcionPersistencia {
    public ExcepcionCursoEnUso(String mensaje) {
        super(mensaje, null);
    }
}
