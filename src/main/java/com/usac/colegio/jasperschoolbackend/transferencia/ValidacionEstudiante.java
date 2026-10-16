/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
/**
 *
 * @author eduar
 */
public class ValidacionEstudiante {
    //Reglas de validacion de estudiantes. Devuelve el mensaje del problema, o null si todo esta bien
    private static final int EDAD_MINIMA = 3;   //En Guatemala se entra a pre-primaria a los 3 años
    private static final int EDAD_MAXIMA = 25;  //Tope razonable

    private ValidacionEstudiante() {
    }

    public static String validar(SolicitudEstudiante s, boolean alCrear) {
        if (s == null || vacio(s.getNombre()) || vacio(s.getTelefono()) || vacio(s.getDireccion())
                || vacio(s.getFechaNacimiento()) || vacio(s.getEncargadoNombre())
                || vacio(s.getEncargadoTelefono()) || vacio(s.getEncargadoParentesco())
                || (alCrear && vacio(s.getCui()))) {
            return "Faltan datos obligatorios del estudiante o de su encargado principal";
        }
        if (alCrear && !s.getCui().trim().matches("\\d{13}")) {
            return "El CUI debe tener exactamente 13 dígitos";
        }
        if (s.getNombre().trim().length() > 100 || s.getDireccion().trim().length() > 100) {
            return "Nombre y dirección no pueden pasar de 100 caracteres";
        }
        if (!s.getTelefono().trim().matches("\\d{8,15}")) {
            return "El teléfono debe tener entre 8 y 15 dígitos";
        }
        String problema = validarFechaNacimiento(s.getFechaNacimiento().trim(), alCrear);
        if (problema != null) {
            return problema;
        }
        if (largo(s.getAlergias()) > 255 || largo(s.getCondicionesMedicas()) > 255) {
            return "Las alergias y las condiciones médicas no pueden pasar de 255 caracteres";
        }
        problema = validarEncargado(s.getEncargadoNombre(), s.getEncargadoTelefono(), s.getEncargadoParentesco(), "principal");
        if (problema != null) {
            return problema;
        }
        //El segundo encargado se llena completo o no se llena: nunca a medias
        int llenos = (vacio(s.getEncargado2Nombre()) ? 0 : 1)
                + (vacio(s.getEncargado2Telefono()) ? 0 : 1)
                + (vacio(s.getEncargado2Parentesco()) ? 0 : 1);
        if (llenos != 0 && llenos != 3) {
            return "El segundo encargado se llena completo (nombre, teléfono y parentesco) o no se llena";
        }
        if (llenos == 3) {
            return validarEncargado(s.getEncargado2Nombre(), s.getEncargado2Telefono(), s.getEncargado2Parentesco(), "secundario");
        }
        return null;
    }

    private static String validarFechaNacimiento(String texto, boolean alCrear) {
        LocalDate nacimiento;
        try {
            nacimiento = LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return "La fecha de nacimiento debe tener el formato AAAA-MM-DD";
        }
        LocalDate hoy = LocalDate.now();
        if (nacimiento.isAfter(hoy)) {
            return "La fecha de nacimiento no puede ser futura";
        }
        //Los limites de edad aplican al registrar. Al editar solo se corrige el dato:
        //si no, un estudiante registrado a los 25 quedaria sin poder editarse al cumplir 26
        if (!alCrear) {
            return null;
        }
        int edad = Period.between(nacimiento, hoy).getYears();
        if (edad < EDAD_MINIMA) {
            return "El estudiante debe tener al menos " + EDAD_MINIMA + " años";
        }
        if (edad > EDAD_MAXIMA) {
            return "El estudiante no puede tener más de " + EDAD_MAXIMA + " años";
        }
        return null;
    }

    private static String validarEncargado(String nombre, String telefono, String parentesco, String cual) {
        if (nombre.trim().length() > 100) {
            return "El nombre del encargado " + cual + " no puede pasar de 100 caracteres";
        }
        if (parentesco.trim().length() > 50) {
            return "El parentesco del encargado " + cual + " no puede pasar de 50 caracteres";
        }
        if (!telefono.trim().matches("\\d{8,15}")) {
            return "El teléfono del encargado " + cual + " debe tener entre 8 y 15 dígitos";
        }
        return null;
    }

    private static int largo(String texto) {
        return texto == null ? 0 : texto.trim().length();
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
