/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.util.List;
/**
 *
 * @author eduar
 */
public class SolicitudAsistencia {
    private String fecha;                  //"2026-10-23"
    private List<MarcaAsistencia> marcas;

    public SolicitudAsistencia() {
    }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public List<MarcaAsistencia> getMarcas() { return marcas; }
    public void setMarcas(List<MarcaAsistencia> marcas) { this.marcas = marcas; }
}
