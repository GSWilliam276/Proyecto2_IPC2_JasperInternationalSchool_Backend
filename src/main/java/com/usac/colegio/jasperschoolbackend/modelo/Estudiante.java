/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoEstudiante;
import java.time.LocalDate;

/**
 *
 * @author eduar
 */
public class Estudiante extends Usuario {
    //El estudiante tiene correo y contrasena desde que se registra,
    //pero no puede entrar al sistema hasta que se habilita su acceso al inscribirlo
    private LocalDate fechaNacimiento;
    private String alergias;            //opcional
    private String condicionesMedicas;  //opcional
    private String encargadoNombre;
    private String encargadoTelefono;
    private String encargadoParentesco;
    private String encargado2Nombre;       //opcional: los tres del segundo, o ninguno
    private String encargado2Telefono;
    private String encargado2Parentesco;
    private EstadoEstudiante estado;

    public Estudiante(int idUsuario, String cui, String nombre, String correo, String telefono,
                      String direccion, String contrasena, LocalDate fechaNacimiento,
                      String alergias, String condicionesMedicas,
                      String encargadoNombre, String encargadoTelefono, String encargadoParentesco,
                      String encargado2Nombre, String encargado2Telefono, String encargado2Parentesco,
                      EstadoEstudiante estado, boolean accesoHabilitado) {
        super(idUsuario, cui, nombre, correo, telefono, direccion, contrasena);
        this.fechaNacimiento = fechaNacimiento;
        this.alergias = alergias;
        this.condicionesMedicas = condicionesMedicas;
        this.encargadoNombre = encargadoNombre;
        this.encargadoTelefono = encargadoTelefono;
        this.encargadoParentesco = encargadoParentesco;
        this.encargado2Nombre = encargado2Nombre;
        this.encargado2Telefono = encargado2Telefono;
        this.encargado2Parentesco = encargado2Parentesco;
        this.estado = estado;
    }

    //Verdadero si los tres datos del segundo encargado vienen llenos
    public boolean tieneSegundoEncargado() {
        return encargado2Nombre != null && encargado2Telefono != null && encargado2Parentesco != null;
    }

    //El segundo encargado se llena completo o no se llena: nunca a medias
    public boolean segundoEncargadoCoherente() {
        int llenos = 0;
        if (encargado2Nombre != null) llenos++;
        if (encargado2Telefono != null) llenos++;
        if (encargado2Parentesco != null) llenos++;
        return llenos == 0 || llenos == 3;
    }

    //Solo un estudiante activo se puede inscribir
    public boolean puedeInscribirse() {
        return estado == EstadoEstudiante.ACTIVO;
    }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

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

    public EstadoEstudiante getEstado() { return estado; }
    public void setEstado(EstadoEstudiante estado) { this.estado = estado; }

}
