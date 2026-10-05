/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.resources;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Admin;
import com.usac.colegio.jasperschoolbackend.modelo.SuperAdmin;
import com.usac.colegio.jasperschoolbackend.modelo.Usuario;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.UsuarioDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.UsuarioDAO;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaInicioSesion;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudInicioSesion;
import com.usac.colegio.jasperschoolbackend.utilidades.JwtUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
/**
 *
 * @author eduar
 * CU001 Iniciar Sesion.
 * Ruta final: POST /api/v1/auth/login
 */
@Path("auth")
public class AutenticacionResource {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOPersistencia();

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response iniciarSesion(SolicitudInicioSesion solicitud) {
        //Validacion en el backend: nunca se confia en el formulario de Angular
        if (solicitud == null
                || solicitud.getCorreo() == null || solicitud.getCorreo().isBlank()
                || solicitud.getContrasena() == null || solicitud.getContrasena().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new RespuestaError("El correo y la contraseña son obligatorios"))
                    .build();
        }

        try {
            Optional<Usuario> encontrado = usuarioDAO.buscarPorCorreo(solicitud.getCorreo());

            //Mensaje para que asi no se revele cuales correos estan registrados
            if (encontrado.isEmpty() || !encontrado.get().validarContrasena(solicitud.getContrasena())) {
                return respuestaNoAutenticado("Credenciales inválidas");
            }

            Usuario usuario = encontrado.get();

            //Ya con la contraseña correcta, se revisa que la cuenta este activa
            if (!estaActivo(usuario)) {
                return respuestaNoAutenticado("La cuenta no está disponible");
            }

            String rol = obtenerRol(usuario);
            String token = JwtUtil.generar(usuario.getIdUsuario(), rol);

            return Response.ok(new RespuestaInicioSesion(token, usuario.getIdUsuario(), usuario.getNombre(), rol))
                    .build();

        } catch (ExcepcionPersistencia e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new RespuestaError("Error interno al iniciar sesión"))
                    .build();
        }
    }

    //Metodos Privados de Apoyo

    private Response respuestaNoAutenticado(String mensaje) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new RespuestaError(mensaje))
                .build();
    }

    private boolean estaActivo(Usuario usuario) {
        if (usuario instanceof Admin admin) {
            return admin.getEstado() == EstadoGeneral.ACTIVO;
        }
        if (usuario instanceof SuperAdmin superAdmin) {
            return superAdmin.getEstado() == EstadoGeneral.ACTIVO;
        }
        return false;
    }

    private String obtenerRol(Usuario usuario) {
        if (usuario instanceof SuperAdmin) {
            return "SUPERADMIN";
        }
        return "ADMIN";
    }
}
