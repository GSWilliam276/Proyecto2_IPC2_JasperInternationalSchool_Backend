/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import java.security.SecureRandom;
/**
 *
 * @author eduar
 */
public final class GeneradorContrasena {
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITOS = "23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private GeneradorContrasena() {
    }

    public static String generar() {
        String todo = MAYUSCULAS + MINUSCULAS + DIGITOS;
        char[] c = new char[10];
        c[0] = elegir(MAYUSCULAS);
        c[1] = elegir(MINUSCULAS);
        c[2] = elegir(DIGITOS);
        for (int i = 3; i < c.length; i++) {
            c[i] = elegir(todo);
        }
        for (int i = c.length - 1; i > 0; i--) { //Se mezcla para que las tres primeras no queden siempre al inicio
            int j = ALEATORIO.nextInt(i + 1);
            char t = c[i];
            c[i] = c[j];
            c[j] = t;
        }
        return new String(c);
    }

    private static char elegir(String alfabeto) {
        return alfabeto.charAt(ALEATORIO.nextInt(alfabeto.length()));
    }
}
