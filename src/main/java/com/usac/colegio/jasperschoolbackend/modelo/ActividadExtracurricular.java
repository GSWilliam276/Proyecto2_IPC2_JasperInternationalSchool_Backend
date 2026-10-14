/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 *
 * @author eduar
 */
public class ActividadExtracurricular {
    //Actividad extracurricular. Un costo de cero significa que es gratuita (por ejemplo, una excursion)
    private int idActividad;
    private String nombre;
    private String descripcion; //opcional
    private LocalDate fecha;
    private int cupo;
    private BigDecimal costo;   //BigDecimal por ser dinero
    private EstadoGeneral estado;

    public ActividadExtracurricular(int idActividad, String nombre, String descripcion, LocalDate fecha,
                                    int cupo, BigDecimal costo, EstadoGeneral estado) {
        this.idActividad = idActividad;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.cupo = cupo;
        this.costo = costo;
        this.estado = estado;
    }

    public boolean esGratuita() {
        return costo.signum() == 0;
    }

    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public int getCupo() { return cupo; }
    public BigDecimal getCosto() { return costo; }
    public EstadoGeneral getEstado() { return estado; }
}
