/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.seguridad;

import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.utilidades.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
/**
 *
 * @author eduar
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class FiltroAutenticacion implements ContainerRequestFilter {
//Se ejecuta en TODOS los endpoints. Los unicos que se saltan la
//revision son los marcados con @Publico
    @Context
    private ResourceInfo infoRecurso;

    @Override
    public void filter(ContainerRequestContext peticion) throws IOException {
        //La peticion de prueba del navegador (CORS) no lleva token
        if ("OPTIONS".equalsIgnoreCase(peticion.getMethod())) {
            return;
        }
        if (esPublico()) {
            return;
        }

        String encabezado = peticion.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            rechazar(peticion, Response.Status.UNAUTHORIZED, "Debe iniciar sesión");
            return;
        }

        Claims datos;
        try {
            datos = JwtUtil.validar(encabezado.substring("Bearer ".length()));
        } catch (JwtException | IllegalArgumentException e) {
            rechazar(peticion, Response.Status.UNAUTHORIZED, "Sesión inválida o vencida");
            return;
        }

        String rol = datos.get("rol", String.class);
        String[] permitidos = rolesPermitidos();
        //Sin @RolesPermitidos basta con tener una sesion valida, de cualquier rol
        if (permitidos.length > 0 && !Arrays.asList(permitidos).contains(rol)) {
            rechazar(peticion, Response.Status.FORBIDDEN, "No tiene permiso para esta acción");
            return;
        }

        //El id y el rol salen del token, nunca de lo que mande el navegador
        peticion.setProperty("idUsuario", Integer.parseInt(datos.getSubject()));
        peticion.setProperty("rol", rol);
    }

    private boolean esPublico() {
        return infoRecurso.getResourceMethod().isAnnotationPresent(Publico.class)
                || infoRecurso.getResourceClass().isAnnotationPresent(Publico.class);
    }

    private String[] rolesPermitidos() {
        RolesPermitidos anotacion = infoRecurso.getResourceMethod().getAnnotation(RolesPermitidos.class);
        if (anotacion == null) {
            anotacion = infoRecurso.getResourceClass().getAnnotation(RolesPermitidos.class);
        }
        return anotacion == null ? new String[0] : anotacion.value();
    }

    private void rechazar(ContainerRequestContext peticion, Response.Status estado, String mensaje) {
        peticion.abortWith(Response.status(estado)
                .type(MediaType.APPLICATION_JSON)
                .entity(new RespuestaError(mensaje))
                .build());
    }
}
