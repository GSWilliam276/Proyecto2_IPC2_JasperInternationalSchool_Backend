/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.excepciones;

/**
 *
 * @author eduar
 */
public class ExcepcionCurriculoDuplicado extends ExcepcionPersistencia {
    //El curso ya esta en el curriculo, o el curriculo ya existe
    public ExcepcionCurriculoDuplicado(String mensaje) {
        super(mensaje, null);
    }
}
