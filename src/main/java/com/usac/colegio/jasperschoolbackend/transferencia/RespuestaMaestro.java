/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaMaestro {
    private int idUsuario;
    private String cui;
    private String nombre;

    public RespuestaMaestro() {
    }

    public RespuestaMaestro(int idUsuario, String cui, String nombre) {
        this.idUsuario = idUsuario;
        this.cui = cui;
        this.nombre = nombre;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getCui() { return cui; }
    public String getNombre() { return nombre; }
}
