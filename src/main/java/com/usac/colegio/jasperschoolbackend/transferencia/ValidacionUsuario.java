/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
//Reglas de validacion compartidas por la creacion de SuperAdmin y de Admin
public class ValidacionUsuario {

    private ValidacionUsuario() {
    }

    //Devuelve el mensaje del primer problema encontrado, o null si todo esta bien
    public static String validar(SolicitudCrearUsuario s) {
        if (s == null || vacio(s.getCui()) || vacio(s.getNombre()) || vacio(s.getCorreo())
                || vacio(s.getTelefono()) || vacio(s.getDireccion()) || vacio(s.getContrasena())) {
            return "Todos los campos son obligatorios";
        }
        if (!s.getCui().trim().matches("\\d{13}")) {
            return "El CUI debe tener exactamente 13 dígitos";
        }
        if (!s.getCorreo().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "El correo no tiene un formato válido";
        }
        if (!s.getTelefono().trim().matches("\\d{8,15}")) {
            return "El teléfono debe tener entre 8 y 15 dígitos";
        }
        if (s.getNombre().trim().length() > 100 || s.getCorreo().trim().length() > 100
                || s.getDireccion().trim().length() > 100) {
            return "Nombre, correo y dirección no pueden pasar de 100 caracteres";
        }
        //el maximo de 64 evita el limite de 72 bytes de BCrypt
        if (s.getContrasena().length() < 8 || s.getContrasena().length() > 64) {
            return "La contraseña debe tener entre 8 y 64 caracteres";
        }
        return null;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
    
    ///Valida los datos de una edicion. Devuelve el mensaje del problema, o null si todo esta bien
    public static String validarEdicion(SolicitudEditarUsuario s) {
        if (s == null || vacio(s.getNombre()) || vacio(s.getTelefono()) || vacio(s.getDireccion())) {
            return "Todos los campos son obligatorios";
        }
        if (!s.getTelefono().trim().matches("\\d{8,15}")) {
            return "El teléfono debe tener entre 8 y 15 dígitos";
        }
        if (s.getNombre().trim().length() > 100 || s.getDireccion().trim().length() > 100) {
            return "Nombre y dirección no pueden pasar de 100 caracteres";
        }
        return null;
    }
}
