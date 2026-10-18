/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

/**
 *
 * @author eduar
 */
public class ResultadoInscripcion {
    //Resultado de inscribir: la inscripcion y, solo si se creo un acceso nuevo, la contraseña provisional en claro
    private final Inscripcion inscripcion;
    private final String contrasenaProvisional; //Null en una reinscripcion

    public ResultadoInscripcion(Inscripcion inscripcion, String contrasenaProvisional) {
        this.inscripcion = inscripcion;
        this.contrasenaProvisional = contrasenaProvisional;
    }

    public Inscripcion getInscripcion() { return inscripcion; }
    public String getContrasenaProvisional() { return contrasenaProvisional; }
}
