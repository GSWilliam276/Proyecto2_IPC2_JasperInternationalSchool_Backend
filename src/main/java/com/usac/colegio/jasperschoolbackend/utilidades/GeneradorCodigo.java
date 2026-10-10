/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import java.security.SecureRandom;
/**
 *
 * @author eduar
 * Genera los codigos de recuperacion con SecureRandom, que no es predecible como Random
 */
public class GeneradorCodigo {
    //No se usa ni 0, O, 1 ni I, para que no se confundan al leerlos
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int LONGITUD = 8;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeneradorCodigo() {
    }

    public static String generar() {
        StringBuilder codigo = new StringBuilder(LONGITUD);
        for (int i = 0; i < LONGITUD; i++) {
            codigo.append(ALFABETO.charAt(ALEATORIO.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }
}
