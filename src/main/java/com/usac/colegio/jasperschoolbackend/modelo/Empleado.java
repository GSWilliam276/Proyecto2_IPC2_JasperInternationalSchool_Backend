/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Puesto;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 *
 * @author eduar
 */
public class Empleado {
    //Ficha de un empleado del colegio (recursos humanos)
    //No hereda de Usuario: solo algunos puestos tienen cuenta de acceso,
    //y cuando la tienen se enlaza por idUsuario
    private int idEmpleado;
    private String cui;
    private String nombre;
    private String telefono;
    private String direccion;
    private Puesto puesto;
    private LocalDate fechaContratacion;
    private BigDecimal salario; //BigDecimal por ser dinero
    private EstadoGeneral estado;
    private Integer idUsuario;  //null = sin cuenta de acceso

    public Empleado(int idEmpleado, String cui, String nombre, String telefono, String direccion,
                    Puesto puesto, LocalDate fechaContratacion, BigDecimal salario,
                    EstadoGeneral estado, Integer idUsuario) {
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

    public boolean tieneCuenta() {
        return idUsuario != null;
    }

    
    //Revisa la regla de puesto contra cuenta:
    //maestro, secretaria y bibliotecario exigen cuenta; mantenimiento y limpieza no pueden
    //tenerla; administrativo puede tenerla o no
    public boolean cuentaCoherenteConPuesto() {
        if (puesto.exigeCuenta()) {
            return tieneCuenta();
        }
        if (!puesto.admiteCuenta()) {
            return !tieneCuenta();
        }
        return true;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getCui() { return cui; }
    public void setCui(String cui) { this.cui = cui; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Puesto getPuesto() { return puesto; }
    public void setPuesto(Puesto puesto) { this.puesto = puesto; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public BigDecimal getSalario() { return salario; }
    public void setSalario(BigDecimal salario) { this.salario = salario; }

    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
}
