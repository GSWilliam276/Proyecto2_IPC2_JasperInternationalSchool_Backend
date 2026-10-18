/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
import java.util.List;
/**
 *
 * @author eduar
 */
public class Saldo {
    //Lo que un estudiante debe en el año activo. Esta es la unica definicion de "moroso": los reportes la reutilizan
    private final String anioNombre;
    private final BigDecimal monto;
    private final boolean inscripcionPendiente;
    private final List<Integer> mesesPendientes;

    public Saldo(String anioNombre, BigDecimal monto, boolean inscripcionPendiente, List<Integer> mesesPendientes) {
        this.anioNombre = anioNombre;
        this.monto = monto;
        this.inscripcionPendiente = inscripcionPendiente;
        this.mesesPendientes = mesesPendientes;
    }

    public boolean isSolvente() { return !inscripcionPendiente && mesesPendientes.isEmpty(); }
    public String getAnioNombre() { return anioNombre; }
    public BigDecimal getMonto() { return monto; }
    public boolean isInscripcionPendiente() { return inscripcionPendiente; }
    public List<Integer> getMesesPendientes() { return mesesPendientes; }
}
