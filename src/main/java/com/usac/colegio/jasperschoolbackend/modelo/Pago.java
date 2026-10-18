/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoPago;
import com.usac.colegio.jasperschoolbackend.enums.EstadoPago;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author eduar
 */
public class Pago {
    //Pago registrado. Modelo de lectura: ids y los nombres que muestra la pantalla. El monto es el que se cobro
    private final int idPago;
    private final int idEstudiante;
    private final String estudianteNombre;
    private final int idAnioLectivo;
    private final String anioNombre;
    private final ConceptoPago concepto;
    private final BigDecimal monto;
    private final Integer mes;               // solo en mensualidades
    private final String actividadNombre;    // solo en pagos de actividad
    private final LocalDate fecha;
    private final String secretariaNombre;
    private final EstadoPago estado;
    private final String motivoAnulacion;    // solo si esta anulado

    public Pago(int idPago, int idEstudiante, String estudianteNombre, int idAnioLectivo, String anioNombre,
                ConceptoPago concepto, BigDecimal monto, Integer mes, String actividadNombre, LocalDate fecha,
                String secretariaNombre, EstadoPago estado, String motivoAnulacion) {
        this.idPago = idPago;
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.concepto = concepto;
        this.monto = monto;
        this.mes = mes;
        this.actividadNombre = actividadNombre;
        this.fecha = fecha;
        this.secretariaNombre = secretariaNombre;
        this.estado = estado;
        this.motivoAnulacion = motivoAnulacion;
    }

    public int getIdPago() { return idPago; }
    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public ConceptoPago getConcepto() { return concepto; }
    public BigDecimal getMonto() { return monto; }
    public Integer getMes() { return mes; }
    public String getActividadNombre() { return actividadNombre; }
    public LocalDate getFecha() { return fecha; }
    public String getSecretariaNombre() { return secretariaNombre; }
    public EstadoPago getEstado() { return estado; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
}
