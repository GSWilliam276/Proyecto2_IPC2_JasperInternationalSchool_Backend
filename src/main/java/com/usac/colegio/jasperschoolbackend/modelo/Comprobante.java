/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoComprobante;
import java.time.LocalDateTime;
/**
 *
 * @author eduar
 */
public class Comprobante {
    //Comprobante (factura) de un pago. Se anula junto con el pago
    private final int idComprobante;
    private final String numero;
    private final LocalDateTime fechaEmision;
    private final EstadoComprobante estado;
    private final Pago pago;

    public Comprobante(int idComprobante, String numero, LocalDateTime fechaEmision, EstadoComprobante estado, Pago pago) {
        this.idComprobante = idComprobante;
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
        this.pago = pago;
    }

    public int getIdComprobante() { return idComprobante; }
    public String getNumero() { return numero; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public EstadoComprobante getEstado() { return estado; }
    public Pago getPago() { return pago; }
}
