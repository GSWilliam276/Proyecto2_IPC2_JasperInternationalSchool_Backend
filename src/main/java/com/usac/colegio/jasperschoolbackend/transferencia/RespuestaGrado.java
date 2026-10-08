/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaGrado {
    private int idGrado;
    private String nombre;
    private String nivel;
    private String estado;

    public RespuestaGrado() {
    }

    public RespuestaGrado(int idGrado, String nombre, String nivel, String estado) {
        this.idGrado = idGrado;
        this.nombre = nombre;
        this.nivel = nivel;
        this.estado = estado;
    }

    public int getIdGrado() { return idGrado; }
    public String getNombre() { return nombre; }
    public String getNivel() { return nivel; }
    public String getEstado() { return estado; }
}
