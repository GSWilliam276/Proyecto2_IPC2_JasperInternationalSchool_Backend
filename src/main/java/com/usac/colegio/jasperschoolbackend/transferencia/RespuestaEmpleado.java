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
public class RespuestaEmpleado {
    //Lo que el backend devuelve de un empleado. idUsuario es null si no tiene cuenta
    private int idEmpleado;
    private String cui;
    private String nombre;
    private String telefono;
    private String direccion;
    private String puesto;
    private String fechaContratacion;
    private BigDecimal salario;
    private String estado;
    private Integer idUsuario;

    public RespuestaEmpleado() {
    }

    public RespuestaEmpleado(int idEmpleado, String cui, String nombre, String telefono, String direccion,
                             String puesto, String fechaContratacion, BigDecimal salario,
                             String estado, Integer idUsuario) {
        this.idEmpleado = idEmpleado;
        this.cui = cui;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.puesto = puesto;
        this.fechaContratacion = fechaContratacion;
        this.salario = salario;
        this.estado = estado;
        this.idUsuario = idUsuario;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public String getCui() { return cui; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getPuesto() { return puesto; }
    public String getFechaContratacion() { return fechaContratacion; }
    public BigDecimal getSalario() { return salario; }
    public String getEstado() { return estado; }
    public Integer getIdUsuario() { return idUsuario;}
}
