/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.correo;

/**
 *
 * @author eduar
 */
public interface EnviadorCorreo {
    //Contrato para enviar el codigo de recuperacion
    void enviarCodigoRecuperacion(String destinatario, String nombre, String codigo);
}
