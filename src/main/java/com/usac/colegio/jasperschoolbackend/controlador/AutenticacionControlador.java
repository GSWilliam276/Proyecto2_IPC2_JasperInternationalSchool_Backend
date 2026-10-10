/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.correo.EnviadorCorreo;
import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Admin;
import com.usac.colegio.jasperschoolbackend.modelo.CodigoRecuperacion;
import com.usac.colegio.jasperschoolbackend.modelo.SuperAdmin;
import com.usac.colegio.jasperschoolbackend.modelo.Usuario;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.UsuarioDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CodigoRecuperacionDAO;
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
import com.usac.colegio.jasperschoolbackend.seguridad.Publico;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaMensaje;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudCambiarContrasena;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudRecuperacion;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudRestablecer;
import com.usac.colegio.jasperschoolbackend.utilidades.GeneradorCodigo;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import java.time.LocalDateTime;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.CodigoRecuperacionDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.correo.EnviadorCorreoConsola;
/**
 *
 * @author eduar
 * CU001 Iniciar Sesion.
 * Ruta final: POST /api/v1/auth/login
 */
@Path("auth")
public class AutenticacionControlador {
    private final UsuarioDAO usuarioDAO = new UsuarioDAOPersistencia();
    private final CodigoRecuperacionDAO codigoDAO = new CodigoRecuperacionDAOPersistencia();
    private final EnviadorCorreo enviadorCorreo = new EnviadorCorreoConsola();

    @POST
    @Path("login")
    @Publico
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

    /** CU102 Cambiar Contraseña. Cualquier rol con sesion valida */
    @PUT
    @Path("contrasena")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cambiarContrasena(SolicitudCambiarContrasena solicitud,
                                      @Context ContainerRequestContext peticion) {
        if (solicitud == null || solicitud.getContrasenaActual() == null || solicitud.getContrasenaActual().isBlank()
                || solicitud.getContrasenaNueva() == null || solicitud.getContrasenaNueva().isBlank()) {
            return respuestaError(Response.Status.BAD_REQUEST, "Todos los campos son obligatorios");
        }
        String nueva = solicitud.getContrasenaNueva();
        // el maximo de 64 evita el limite de 72 bytes de BCrypt
        if (nueva.length() < 8 || nueva.length() > 64) {
            return respuestaError(Response.Status.BAD_REQUEST, "La contraseña nueva debe tener entre 8 y 64 caracteres");
        }
        if (nueva.equals(solicitud.getContrasenaActual())) {
            return respuestaError(Response.Status.BAD_REQUEST, "La contraseña nueva debe ser distinta de la actual");
        }

        // el id viene del token que valido el filtro, no de lo que mande el navegador
        int idUsuario = (Integer) peticion.getProperty("idUsuario");

        try {
            Optional<Usuario> usuario = usuarioDAO.buscarPorId(idUsuario);
            if (usuario.isEmpty()) {
                return respuestaError(Response.Status.NOT_FOUND, "No existe el usuario");
            }
            // 400 y no 401: un 401 haria que Angular cierre la sesion por un simple error de tipeo
            if (!usuario.get().validarContrasena(solicitud.getContrasenaActual())) {
                return respuestaError(Response.Status.BAD_REQUEST, "La contraseña actual no es correcta");
            }
            usuarioDAO.actualizarContrasena(idUsuario, nueva); // el DAO la guarda con hash
            return Response.noContent().build();
        } catch (ExcepcionPersistencia e) {
            return respuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al cambiar la contraseña");
        }
    }
    
    /** CU002 Solicitar Codigo de Recuperacion. Responde lo mismo exista o no el correo */
    @POST
    @Path("recuperacion/solicitar")
    @Publico
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response solicitarCodigo(SolicitudRecuperacion solicitud) {
        if (solicitud == null || solicitud.getCorreo() == null || solicitud.getCorreo().isBlank()) {
            return respuestaError(Response.Status.BAD_REQUEST, "El correo es obligatorio");
        }
        try {
            Optional<Usuario> encontrado = usuarioDAO.buscarPorCorreo(solicitud.getCorreo().trim());
            if (encontrado.isPresent() && estaActivo(encontrado.get())) {
                Usuario usuario = encontrado.get();
                String codigo = GeneradorCodigo.generar();
                codigoDAO.generar(usuario.getIdUsuario(), codigo, LocalDateTime.now().plusMinutes(15));
                enviadorCorreo.enviarCodigoRecuperacion(usuario.getCorreo(), usuario.getNombre(), codigo);
            }
            //Misma respuesta en los dos casos: no se revela que correos estan registrados
            return Response.ok(new RespuestaMensaje(
                    "Si el correo está registrado, recibirá un código de recuperación")).build();
        } catch (ExcepcionPersistencia e) {
            return respuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al solicitar el código");
        }
    }

    /** CU003 Restablecer Contraseña con el codigo recibido */
    @POST
    @Path("recuperacion/restablecer")
    @Publico
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response restablecerContrasena(SolicitudRestablecer solicitud) {
        if (solicitud == null || solicitud.getCorreo() == null || solicitud.getCorreo().isBlank()
                || solicitud.getCodigo() == null || solicitud.getCodigo().isBlank()
                || solicitud.getContrasenaNueva() == null || solicitud.getContrasenaNueva().isBlank()) {
            return respuestaError(Response.Status.BAD_REQUEST, "Todos los campos son obligatorios");
        }
        String nueva = solicitud.getContrasenaNueva();
        if (nueva.length() < 8 || nueva.length() > 64) {
            return respuestaError(Response.Status.BAD_REQUEST, "La contraseña nueva debe tener entre 8 y 64 caracteres");
        }
        try {
            Optional<Usuario> usuario = usuarioDAO.buscarPorCorreo(solicitud.getCorreo().trim());
            //Mismo mensaje si el correo no existe, la cuenta esta inactiva o el codigo no sirve
            if (usuario.isEmpty() || !estaActivo(usuario.get())) {
                return respuestaError(Response.Status.BAD_REQUEST, "Código inválido o vencido");
            }
            String codigo = solicitud.getCodigo().trim().toUpperCase();
            Optional<CodigoRecuperacion> vigente = codigoDAO.buscarVigente(usuario.get().getIdUsuario(), codigo);
            if (vigente.isEmpty()) {
                return respuestaError(Response.Status.BAD_REQUEST, "Código inválido o vencido");
            }
            //Primero se marca como usado y despues se cambia la contraseña: si algo fallara
            //entre las dos, el codigo ya no sirve y la persona pide uno nuevo, en vez de
            //quedar un codigo reutilizable
            codigoDAO.marcarComoUsado(vigente.get().getIdCodigoRecuperacion());
            usuarioDAO.actualizarContrasena(usuario.get().getIdUsuario(), nueva);
            return Response.noContent().build();
        } catch (ExcepcionPersistencia e) {
            return respuestaError(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al restablecer la contraseña");
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

    private Response respuestaError(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
