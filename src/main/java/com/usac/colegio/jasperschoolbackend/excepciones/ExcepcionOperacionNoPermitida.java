/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.excepciones;

/**
 *
 * @author eduar
 */
public class ExcepcionOperacionNoPermitida extends ExcepcionPersistencia {
    //Una regla de negocio impide la operacion: año cerrado, estudiantes inscritos, cupo menor a los inscritos
    public ExcepcionOperacionNoPermitida(String mensaje) {
        super(mensaje, null);
    }
}
