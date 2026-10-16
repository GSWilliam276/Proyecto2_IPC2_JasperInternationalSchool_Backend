/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class SolicitudEstudiante {
    //Datos de un estudiante. Al editar, el cui se ignora: no cambia
    private String cui;
    private String nombre;
    private String telefono;
    private String direccion;
    private String fechaNacimiento;        //"2015-03-10"
    private String alergias;               //opcional
    private String condicionesMedicas;     //opcional
    private String encargadoNombre;
    private String encargadoTelefono;
    private String encargadoParentesco;
    private String encargado2Nombre;       //opcional: los tres, o ninguno
    private String encargado2Telefono;
    private String encargado2Parentesco;

    public SolicitudEstudiante() {
    }

    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public String getCondicionesMedicas() { return condicionesMedicas; }
    public void setCondicionesMedicas(String condicionesMedicas) { this.condicionesMedicas = condicionesMedicas; }
    public String getEncargadoNombre() { return encargadoNombre; }
    public void setEncargadoNombre(String encargadoNombre) { this.encargadoNombre = encargadoNombre; }
    public String getEncargadoTelefono() { return encargadoTelefono; }
    public void setEncargadoTelefono(String encargadoTelefono) { this.encargadoTelefono = encargadoTelefono; }
    public String getEncargadoParentesco() { return encargadoParentesco; }
    public void setEncargadoParentesco(String encargadoParentesco) { this.encargadoParentesco = encargadoParentesco; }
    public String getEncargado2Nombre() { return encargado2Nombre; }
    public void setEncargado2Nombre(String encargado2Nombre) { this.encargado2Nombre = encargado2Nombre; }
    public String getEncargado2Telefono() { return encargado2Telefono; }
    public void setEncargado2Telefono(String encargado2Telefono) { this.encargado2Telefono = encargado2Telefono; }
    public String getEncargado2Parentesco() { return encargado2Parentesco; }
    public void setEncargado2Parentesco(String encargado2Parentesco) { this.encargado2Parentesco = encargado2Parentesco; }
}
