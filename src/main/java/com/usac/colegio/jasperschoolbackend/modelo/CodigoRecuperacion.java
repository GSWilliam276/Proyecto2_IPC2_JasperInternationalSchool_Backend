/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.time.LocalDateTime;
/**
 *
 * @author eduar
 */
public class CodigoRecuperacion {
    private final int idCodigoRecuperacion;
    private final String codigo;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaExpiracion;
    private boolean fueUsado;
    private final Usuario usuario;

    public CodigoRecuperacion(int idCodigoRecuperacion, String codigo, LocalDateTime fechaCreacion,
                               LocalDateTime fechaExpiracion, boolean fueUsado, Usuario usuario) {
        this.idCodigoRecuperacion = idCodigoRecuperacion;
        this.codigo = codigo;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.fueUsado = fueUsado;
        this.usuario = usuario;
    }

    public int getIdCodigoRecuperacion() { return idCodigoRecuperacion; }
    public String getCodigo() { return codigo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public Usuario getUsuario() { return usuario; }

    public boolean isFueUsado() { return fueUsado; }
    public void setFueUsado(boolean fueUsado) { this.fueUsado = fueUsado; }
}
