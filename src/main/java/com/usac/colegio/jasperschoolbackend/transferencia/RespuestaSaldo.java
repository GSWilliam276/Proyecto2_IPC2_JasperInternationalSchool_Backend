/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.math.BigDecimal;
import java.util.List;
/**
 *
 * @author eduar
 */
public class RespuestaSaldo {
    private int idEstudiante;
    private String anioNombre;
    private boolean solvente;
    private BigDecimal saldo;
    private boolean inscripcionPendiente;
    private List<Integer> mesesPendientes;

    public RespuestaSaldo() {
    }

    public RespuestaSaldo(int idEstudiante, String anioNombre, boolean solvente, BigDecimal saldo,
                          boolean inscripcionPendiente, List<Integer> mesesPendientes) {
        this.idEstudiante = idEstudiante;
        this.anioNombre = anioNombre;
        this.solvente = solvente;
        this.saldo = saldo;
        this.inscripcionPendiente = inscripcionPendiente;
        this.mesesPendientes = mesesPendientes;
    }

    public int getIdEstudiante() { return idEstudiante; }
    public String getAnioNombre() { return anioNombre; }
    public boolean isSolvente() { return solvente; }
    public BigDecimal getSaldo() { return saldo; }
    public boolean isInscripcionPendiente() { return inscripcionPendiente; }
    public List<Integer> getMesesPendientes() { return mesesPendientes; }
}
