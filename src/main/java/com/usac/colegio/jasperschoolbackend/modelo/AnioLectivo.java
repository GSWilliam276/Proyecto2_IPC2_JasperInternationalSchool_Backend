/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import java.time.LocalDate;
/**
 *
 * @author eduar
 */
public class AnioLectivo {
   private int idAnioLectivo;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoAnioLectivo estado;

    public AnioLectivo() {
    }

    public AnioLectivo(int idAnioLectivo, String nombre, LocalDate fechaInicio,
                        LocalDate fechaFin, EstadoAnioLectivo estado) {
        this.idAnioLectivo = idAnioLectivo;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getIdAnioLectivo() { return idAnioLectivo; }
    public void setIdAnioLectivo(int idAnioLectivo) { this.idAnioLectivo = idAnioLectivo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public EstadoAnioLectivo getEstado() { return estado; }
    public void setEstado(EstadoAnioLectivo estado) { this.estado = estado; }

    public boolean esRangoValido() {
        return fechaFin.isAfter(fechaInicio);
    }

    public void cerrarAnioLectivo() {
        this.estado = EstadoAnioLectivo.CERRADO;
    } 
}
