/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
/**
 *
 * @author eduar
 * Genera y valida los tokens de sesion (JWT)
 */
public class JwtUtil {
    private static final String SECRETO = "x8avEcstFb9P4UjZDmdI6JQiCGg3OK70AqzpuWNRVMShTeXk";
    private static final SecretKey LLAVE = Keys.hmacShaKeyFor(SECRETO.getBytes(StandardCharsets.UTF_8));
    private static final long VIGENCIA_MS = 60L * 60L * 1000L; //1 hora

    private JwtUtil() {
    }

    public static String generar(int idUsuario, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(idUsuario))
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + VIGENCIA_MS))
                .signWith(LLAVE)
                .compact();
    }

    //Valida firma y vencimiento. Lanza JwtException si el token no sirve
    public static Claims validar(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(LLAVE)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
