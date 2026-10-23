/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaInicioSesion {
    private String token;
    private int idUsuario;
    private String nombre;
    private String rol;
    private boolean contrasenaProvisional;

    public RespuestaInicioSesion() {
    }

    public RespuestaInicioSesion(String token, int idUsuario, String nombre, String rol, boolean contrasenaProvisional) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.rol = rol;
        this.contrasenaProvisional = contrasenaProvisional;
    }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public boolean isContrasenaProvisional() { return contrasenaProvisional; }
    public void setContrasenaProvisional(boolean v) { this.contrasenaProvisional = v; }
}
