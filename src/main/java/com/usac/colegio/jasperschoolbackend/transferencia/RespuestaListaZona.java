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
public class RespuestaListaZona {
    private List<RespuestaZona> actividades;
    private BigDecimal total;
    private BigDecimal disponible;   //Lo que falte para llegar a 70

    public RespuestaListaZona() {
    }

    public RespuestaListaZona(List<RespuestaZona> actividades, BigDecimal total, BigDecimal disponible) {
        this.actividades = actividades;
        this.total = total;
        this.disponible = disponible;
    }

    public List<RespuestaZona> getActividades() { return actividades; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getDisponible() { return disponible; }
}
