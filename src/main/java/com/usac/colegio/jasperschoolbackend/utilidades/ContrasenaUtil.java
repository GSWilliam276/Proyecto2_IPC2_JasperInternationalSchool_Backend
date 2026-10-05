/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import at.favre.lib.crypto.bcrypt.BCrypt;
/**
 *
 * @author eduar
 * Centraliza el hash y la verificacion de contraseñas con BCrypt
 * Ninguna contraseña se guarda ni se compara en texto plano
 */
public class ContrasenaUtil {
    //Costo del algoritmo: cada +1 duplica el tiempo de calculo
    private static final int COSTO = 10;

    private ContrasenaUtil() {
    }

    public static String hashear(String contrasena) {
        return BCrypt.withDefaults().hashToString(COSTO, contrasena.toCharArray());
    }

    public static boolean verificar(String contrasena, String hash) {
        return BCrypt.verifyer().verify(contrasena.toCharArray(), hash).verified;
    }
}
