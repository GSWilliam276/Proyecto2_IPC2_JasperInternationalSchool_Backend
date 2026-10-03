/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
/**
 *
 * @author eduar
 */
public class SuperAdmin extends Usuario {
    private EstadoGeneral estado;

    public SuperAdmin() {
        super();
    }

    public SuperAdmin(int idUsuario, String cui, String nombre, String correo,
                       String telefono, String direccion, String contrasena, EstadoGeneral estado) {
        super(idUsuario, cui, nombre, correo, telefono, direccion, contrasena);
        this.estado = estado;
    }

    public EstadoGeneral getEstado() { return estado; }
    public void setEstado(EstadoGeneral estado) { this.estado = estado; }
}
