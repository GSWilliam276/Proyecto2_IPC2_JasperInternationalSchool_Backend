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
public class RespuestaPrestamo {
    private int idPrestamo;
    private int idLibro;
    private String libroTitulo;
    private int idPrestatario;
    private String prestatarioNombre;
    private String tipoPrestatario;
    private String bibliotecarioNombre;
    private String fechaPrestamo;
    private String fechaEsperada;
    private String fechaDevolucion;
    private boolean devuelto;
    private boolean enMora;
    private int diasAtraso;
    private BigDecimal multaMonto;
    private String multaEstado;

    public RespuestaPrestamo() {
    }

    public RespuestaPrestamo(int idPrestamo, int idLibro, String libroTitulo, int idPrestatario,
                             String prestatarioNombre, String tipoPrestatario, String bibliotecarioNombre,
                             String fechaPrestamo, String fechaEsperada, String fechaDevolucion, boolean devuelto,
                             boolean enMora, int diasAtraso, BigDecimal multaMonto, String multaEstado) {
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
        this.devuelto = devuelto;
        this.enMora = enMora;
        this.diasAtraso = diasAtraso;
        this.multaMonto = multaMonto;
        this.multaEstado = multaEstado;
    }

    public int getIdPrestamo() { return idPrestamo; }
    public int getIdLibro() { return idLibro; }
    public String getLibroTitulo() { return libroTitulo; }
    public int getIdPrestatario() { return idPrestatario; }
    public String getPrestatarioNombre() { return prestatarioNombre; }
    public String getTipoPrestatario() { return tipoPrestatario; }
    public String getBibliotecarioNombre() { return bibliotecarioNombre; }
    public String getFechaPrestamo() { return fechaPrestamo; }
    public String getFechaEsperada() { return fechaEsperada; }
    public String getFechaDevolucion() { return fechaDevolucion; }
    public boolean isDevuelto() { return devuelto; }
    public boolean isEnMora() { return enMora; }
    public int getDiasAtraso() { return diasAtraso; }
    public BigDecimal getMultaMonto() { return multaMonto; }
    public String getMultaEstado() { return multaEstado; }
}
