/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.excepciones;

/**
 *
 * @author eduar
 */
public class ExcepcionReferenciaInvalida extends ExcepcionPersistencia {
    //Se intento usar un grado, carrera o curso inexistente o inactivo, o una carrera en un grado que no es de Diversificado
    public ExcepcionReferenciaInvalida(String mensaje) {
        super(mensaje, null);
    }
}
