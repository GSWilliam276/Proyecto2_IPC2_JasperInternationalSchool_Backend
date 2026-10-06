/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaUsuario {
    private int idUsuario;
    private String cui;
    private String nombre;
    private String correo;
    private String telefono;
    private String direccion;
    private String estado;

    public RespuestaUsuario() {
    }

    public RespuestaUsuario(int idUsuario, String cui, String nombre, String correo,
                            String telefono, String direccion, String estado) {
        this.idUsuario = idUsuario;
        this.cui = cui;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.estado = estado;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getCui() { return cui; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getEstado() { return estado; }
}
