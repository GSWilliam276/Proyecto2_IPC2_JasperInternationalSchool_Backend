/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

/**
 *
 * @author eduar
 */
public class Prestatario {
    //Estudiante o maestro activo que puede pedir un libro prestado
    private final int idUsuario;
    private final String nombre;
    private final String cui;
    private final String tipo;   //ESTUDIANTE o MAESTRO

    public Prestatario(int idUsuario, String nombre, String cui, String tipo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.cui = cui;
        this.tipo = tipo;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getCui() { return cui; }
    public String getTipo() { return tipo; }
}
