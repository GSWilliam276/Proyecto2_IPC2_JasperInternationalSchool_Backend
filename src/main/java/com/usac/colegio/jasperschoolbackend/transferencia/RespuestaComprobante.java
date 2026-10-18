/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaComprobante {
    private int idComprobante;
    private String numero;
    private String fechaEmision;
    private String estado;
    private RespuestaPago pago;

    public RespuestaComprobante() {
    }

    public RespuestaComprobante(int idComprobante, String numero, String fechaEmision, String estado, RespuestaPago pago) {
        this.idComprobante = idComprobante;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
        this.pago = pago;
    }

    public int getIdComprobante() { return idComprobante; }
    public String getNumero() { return numero; }
    public String getFechaEmision() { return fechaEmision; }
    public String getEstado() { return estado; }
    public RespuestaPago getPago() { return pago; }
}
