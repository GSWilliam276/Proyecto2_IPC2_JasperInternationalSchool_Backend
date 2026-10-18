/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaPrestatario {
    private int idUsuario;
    private String nombre;
    private String cui;
    private String tipo;

    public RespuestaPrestatario() {
    }

    public RespuestaPrestatario(int idUsuario, String nombre, String cui, String tipo) {
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
