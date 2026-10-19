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
public class RespuestaMulta {
    private int idMulta;
    private int idPrestamo;
    private String libroTitulo;
    private int idEstudiante;
    private String estudianteNombre;
    private int diasAtraso;
    private BigDecimal monto;
    private String estado;
    private String fechaGeneracion;
    private Integer idPago;

    public RespuestaMulta() {
    }

    public RespuestaMulta(int idMulta, int idPrestamo, String libroTitulo, int idEstudiante, String estudianteNombre,
                          int diasAtraso, BigDecimal monto, String estado, String fechaGeneracion, Integer idPago) {
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
    public String getFechaGeneracion() { return fechaGeneracion; }
    public Integer getIdPago() { return idPago; }
}
