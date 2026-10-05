/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.utilidades.ContrasenaUtil;
/**
 *
 * @author eduar
 */
public abstract class Usuario {
    private int idUsuario;
    private String cui;
    private String nombre;
    private String correo;
    private String telefono;
    private String direccion;
    private String contrasena;

    public Usuario() {
    }

    public Usuario(int idUsuario, String cui, String nombre, String correo,
                   String telefono, String direccion, String contrasena) {
        this.idUsuario = idUsuario;
        this.cui = cui;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.contrasena = contrasena;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public boolean validarContrasena(String intentada) {
        return ContrasenaUtil.verificar(intentada, this.contrasena);
    }
}
