/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.correo;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author eduar
 */
public class EnviadorCorreoConsola implements EnviadorCorreo{
    //Envio simulado: escribe el codigo en el log del servidor en lugar de mandar un correo.
    //Se reemplaza por una implementacion real cuando se defina el medio de envio
    private static final Logger LOG = Logger.getLogger(EnviadorCorreoConsola.class.getName());

    @Override
    public void enviarCodigoRecuperacion(String destinatario, String nombre, String codigo) {
        LOG.log(Level.INFO, "[CORREO SIMULADO] Para: {0} ({1}) | Codigo de recuperacion: {2}",
                new Object[]{destinatario, nombre, codigo});
    }
}
