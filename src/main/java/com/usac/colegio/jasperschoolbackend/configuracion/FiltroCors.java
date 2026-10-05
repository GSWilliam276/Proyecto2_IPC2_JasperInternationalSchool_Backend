/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.configuracion;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
/**
 *
 * @author eduar
 */
@Provider
public class FiltroCors implements ContainerResponseFilter {
    //Permite que el frontend de Angular (localhost:4200) consuma esta API
    @Override
    public void filter(ContainerRequestContext peticion, ContainerResponseContext respuesta) throws IOException {
        respuesta.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:4200");
        respuesta.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
        respuesta.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    }
}
