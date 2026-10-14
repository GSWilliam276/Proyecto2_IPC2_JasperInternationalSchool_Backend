/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoTarifa;
import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public class Tarifa {
    //Monto de un concepto (inscripcion o mensualidad) para un grado, en un año lectivo
    private int idTarifa;
    private Grado grado;
    private int idAnioLectivo;
    private String anioNombre;
    private EstadoAnioLectivo estadoAnio;
    private ConceptoTarifa concepto;
    private BigDecimal monto; //BigDecimal por ser dinero

    public Tarifa(int idTarifa, Grado grado, int idAnioLectivo, String anioNombre,
                  EstadoAnioLectivo estadoAnio, ConceptoTarifa concepto, BigDecimal monto) {
        this.idTarifa = idTarifa;
        this.grado = grado;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.estadoAnio = estadoAnio;
        this.concepto = concepto;
        this.monto = monto;
    }

    public int getIdTarifa() { return idTarifa; }
    public Grado getGrado() { return grado; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public EstadoAnioLectivo getEstadoAnio() { return estadoAnio; }
    public ConceptoTarifa getConcepto() { return concepto; }
    public BigDecimal getMonto() { return monto; }
}
