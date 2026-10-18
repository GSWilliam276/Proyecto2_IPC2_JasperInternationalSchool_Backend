/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
/**
 *
 * @author eduar
 */
public class Prestamo {
    //Prestamo de un libro. Modelo de lectura: el atraso se calcula, no se guarda
    private final int idPrestamo;
    private final int idLibro;
    private final String libroTitulo;
    private final int idPrestatario;
    private final String prestatarioNombre;
    private final String tipoPrestatario;      //ESTUDIANTE o MAESTRO
    private final String bibliotecarioNombre;
    private final LocalDate fechaPrestamo;
    private final LocalDate fechaEsperada;
    private final LocalDate fechaDevolucion;   //null mientras no se devuelva
    private final BigDecimal multaMonto;       //null si no hubo multa
    private final String multaEstado;

    public Prestamo(int idPrestamo, int idLibro, String libroTitulo, int idPrestatario, String prestatarioNombre,
                    String tipoPrestatario, String bibliotecarioNombre, LocalDate fechaPrestamo,
                    LocalDate fechaEsperada, LocalDate fechaDevolucion, BigDecimal multaMonto, String multaEstado) {
        this.idPrestamo = idPrestamo;
        this.idLibro = idLibro;
        this.libroTitulo = libroTitulo;
        this.idPrestatario = idPrestatario;
        this.prestatarioNombre = prestatarioNombre;
        this.tipoPrestatario = tipoPrestatario;
        this.bibliotecarioNombre = bibliotecarioNombre;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaEsperada = fechaEsperada;
        this.fechaDevolucion = fechaDevolucion;
        this.multaMonto = multaMonto;
        this.multaEstado = multaEstado;
    }

    public boolean isDevuelto() { return fechaDevolucion != null; }

    //Dias de atraso: hasta la devolucion, o hasta hoy si todavia no se devuelve. Cero si no hay atraso
    public int getDiasAtraso() {
        LocalDate fin = fechaDevolucion != null ? fechaDevolucion : LocalDate.now();
        long dias = ChronoUnit.DAYS.between(fechaEsperada, fin);
        return dias > 0 ? (int) dias : 0;
    }

    public boolean isEnMora() { return fechaDevolucion == null && getDiasAtraso() > 0; }

    public int getIdPrestamo() { return idPrestamo; }
    public int getIdLibro() { return idLibro; }
    public String getLibroTitulo() { return libroTitulo; }
    public int getIdPrestatario() { return idPrestatario; }
    public String getPrestatarioNombre() { return prestatarioNombre; }
    public String getTipoPrestatario() { return tipoPrestatario; }
    public String getBibliotecarioNombre() { return bibliotecarioNombre; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaEsperada() { return fechaEsperada; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public BigDecimal getMultaMonto() { return multaMonto; }
    public String getMultaEstado() { return multaEstado; }
}
