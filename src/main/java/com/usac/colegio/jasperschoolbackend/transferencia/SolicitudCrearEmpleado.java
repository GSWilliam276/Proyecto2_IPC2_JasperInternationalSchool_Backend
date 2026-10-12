/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public class SolicitudCrearEmpleado {
    //Datos para crear un empleado. Correo y contrasena solo aplican a puestos con cuenta propia
    private String cui;
    private String nombre;
    private String telefono;
    private String direccion;
    private String puesto;             //MAESTRO, LIMPIEZA, etc.
    private String fechaContratacion;  //"2024-03-01"
    private BigDecimal salario;
    private String correo;             //solo MAESTRO, SECRETARIA y BIBLIOTECARIO
    private String contrasena;         //contrasena provisional
    private Integer idUsuario;         //solo ADMINISTRATIVO: cuenta de Admin o SuperAdmin a enlazar

    public SolicitudCrearEmpleado() {
    }

    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
    public String getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(String fechaContratacion) { this.fechaContratacion = fechaContratacion; }
    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
}
