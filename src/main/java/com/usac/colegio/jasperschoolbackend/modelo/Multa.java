/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 *
 * @author eduar
 */
public class Multa {
    //Multa por devolucion tardia. Modelo de lectura. El monto es el que se copio al generarla
    private final int idMulta;
    private final int idPrestamo;
    private final String libroTitulo;
    private final int idEstudiante;
    private final String estudianteNombre;
    private final int diasAtraso;
    private final BigDecimal monto;
    private final String estado;          // PENDIENTE o PAGADA
    private final LocalDate fechaGeneracion;
    private final Integer idPago;         // pago vigente que la cubre; null si esta pendiente

    public Multa(int idMulta, int idPrestamo, String libroTitulo, int idEstudiante, String estudianteNombre,
                 int diasAtraso, BigDecimal monto, String estado, LocalDate fechaGeneracion, Integer idPago) {
        this.idMulta = idMulta;
        this.idPrestamo = idPrestamo;
        this.libroTitulo = libroTitulo;
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.diasAtraso = diasAtraso;
        this.monto = monto;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
        this.idPago = idPago;
    }

    public int getIdMulta() { return idMulta; }
    public int getIdPrestamo() { return idPrestamo; }
    public String getLibroTitulo() { return libroTitulo; }
    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public int getDiasAtraso() { return diasAtraso; }
    public BigDecimal getMonto() { return monto; }
    public String getEstado() { return estado; }
    public LocalDate getFechaGeneracion() { return fechaGeneracion; }
    public Integer getIdPago() { return idPago; }
}
