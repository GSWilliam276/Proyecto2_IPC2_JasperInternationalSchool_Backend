/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import com.usac.colegio.jasperschoolbackend.enums.Puesto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
/**
 *
 * @author eduar
 */
public final class ValidacionEmpleado {
    //Reglas de validacion de empleados. Cada metodo devuelve el mensaje del problema, o null si todo esta bien
    private static final BigDecimal SALARIO_MAXIMO = new BigDecimal("99999999.99");

    private ValidacionEmpleado() {
    }

    public static String validarCreacion(SolicitudCrearEmpleado s) {
        if (s == null || vacio(s.getCui()) || vacio(s.getPuesto())) {
            return "Todos los campos son obligatorios";
        }
        if (!s.getCui().trim().matches("\\d{13}")) {
            return "El CUI debe tener exactamente 13 dígitos";
        }
        Puesto puesto;
        try {
            puesto = Puesto.valueOf(s.getPuesto());
        } catch (IllegalArgumentException e) {
            return "El puesto no es válido";
        }
        String problema = validarDatos(s.getNombre(), s.getTelefono(), s.getDireccion(),
                s.getFechaContratacion(), s.getSalario());
        if (problema != null) {
            return problema;
        }
        return validarCuenta(puesto, s);
    }

    public static String validarEdicion(SolicitudEditarEmpleado s) {
        if (s == null) {
            return "Todos los campos son obligatorios";
        }
        return validarDatos(s.getNombre(), s.getTelefono(), s.getDireccion(),
                s.getFechaContratacion(), s.getSalario());
    }

    //La regla de puesto contra cuenta: quien tiene acceso, quien puede tenerlo y quien no
    private static String validarCuenta(Puesto puesto, SolicitudCrearEmpleado s) {
        boolean traeCredenciales = !vacio(s.getCorreo()) || !vacio(s.getContrasena());

        if (puesto.exigeCuenta()) {
            if (vacio(s.getCorreo()) || vacio(s.getContrasena())) {
                return "Este puesto necesita correo y contraseña provisional";
            }
            if (s.getCorreo().trim().length() > 100
                    || !s.getCorreo().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                return "El correo no tiene un formato válido";
            }
            //El maximo de 64 evita el limite de 72 bytes de BCrypt
            if (s.getContrasena().length() < 8 || s.getContrasena().length() > 64) {
                return "La contraseña debe tener entre 8 y 64 caracteres";
            }
            if (s.getIdUsuario() != null) {
                return "Este puesto crea su propia cuenta: no se enlaza a una existente";
            }
            return null;
        }
        if (traeCredenciales) {
            return "Este puesto no lleva correo ni contraseña";
        }
        if (!puesto.admiteCuenta() && s.getIdUsuario() != null) {
            return "Este puesto no puede tener cuenta de acceso";
        }
        return null;
    }

    private static String validarDatos(String nombre, String telefono, String direccion,
                                       String fecha, BigDecimal salario) {
        if (vacio(nombre) || vacio(telefono) || vacio(direccion) || vacio(fecha) || salario == null) {
            return "Todos los campos son obligatorios";
        }
        if (!telefono.trim().matches("\\d{8,15}")) {
            return "El teléfono debe tener entre 8 y 15 dígitos";
        }
        if (nombre.trim().length() > 100 || direccion.trim().length() > 100) {
            return "Nombre y dirección no pueden pasar de 100 caracteres";
        }
        try {
            if (LocalDate.parse(fecha.trim()).isAfter(LocalDate.now())) {
                return "La fecha de contratación no puede ser futura";
            }
        } catch (DateTimeParseException e) {
            return "La fecha debe tener el formato AAAA-MM-DD";
        }
        if (salario.signum() <= 0) {
            return "El salario debe ser mayor que cero";
        }
        if (salario.stripTrailingZeros().scale() > 2) {
            return "El salario admite como máximo 2 decimales";
        }
        if (salario.compareTo(SALARIO_MAXIMO) > 0) {
            return "El salario es demasiado alto";
        }
        return null;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
