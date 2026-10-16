/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaEstudiante {
    //Lo que el backend devuelve de un estudiante: su expediente. Nunca incluye contraseña
    private int idUsuario;
    private String cui;
    private String nombre;
    private String correo;               // null mientras no tenga acceso
    private String telefono;
    private String direccion;
    private String fechaNacimiento;
    private int edad;
    private String alergias;
    private String condicionesMedicas;
    private String encargadoNombre;
    private String encargadoTelefono;
    private String encargadoParentesco;
    private String encargado2Nombre;
    private String encargado2Telefono;
    private String encargado2Parentesco;
    private String estado;
    private boolean tieneAcceso;

    public RespuestaEstudiante() {
    }

    public RespuestaEstudiante(int idUsuario, String cui, String nombre, String correo, String telefono,
                               String direccion, String fechaNacimiento, int edad, String alergias,
                               String condicionesMedicas, String encargadoNombre, String encargadoTelefono,
                               String encargadoParentesco, String encargado2Nombre, String encargado2Telefono,
                               String encargado2Parentesco, String estado, boolean tieneAcceso) {
        this.idUsuario = idUsuario;
        this.cui = cui;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.alergias = alergias;
        this.condicionesMedicas = condicionesMedicas;
        this.encargadoNombre = encargadoNombre;
        this.encargadoTelefono = encargadoTelefono;
        this.encargadoParentesco = encargadoParentesco;
        this.encargado2Nombre = encargado2Nombre;
        this.encargado2Telefono = encargado2Telefono;
        this.encargado2Parentesco = encargado2Parentesco;
        this.estado = estado;
        this.tieneAcceso = tieneAcceso;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getCui() { return cui; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public int getEdad() { return edad; }
    public String getAlergias() { return alergias; }
    public String getCondicionesMedicas() { return condicionesMedicas; }
    public String getEncargadoNombre() { return encargadoNombre; }
    public String getEncargadoTelefono() { return encargadoTelefono; }
    public String getEncargadoParentesco() { return encargadoParentesco; }
    public String getEncargado2Nombre() { return encargado2Nombre; }
    public String getEncargado2Telefono() { return encargado2Telefono; }
    public String getEncargado2Parentesco() { return encargado2Parentesco; }
    public String getEstado() { return estado; }
    public boolean isTieneAcceso() { return tieneAcceso; }
}
