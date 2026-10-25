/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

/**
 *
 * @author eduar
 */
public record PerfilEstudiante(String nombre, String cui, String correo, String telefono, String direccion,
                               String fechaNacimiento, String encargadoNombre, String encargadoTelefono,
                               String encargadoParentesco, String grado, String seccion, String carrera,
                               String anio) {
}
