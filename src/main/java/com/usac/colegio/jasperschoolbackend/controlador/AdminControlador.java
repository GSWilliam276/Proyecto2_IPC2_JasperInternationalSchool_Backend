/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAdminNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Admin;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.AdminDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AdminDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaUsuario;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudCrearUsuario;
import com.usac.colegio.jasperschoolbackend.transferencia.ValidacionUsuario;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 * CU005 Crear Admin, CU008 Activar, CU009 Desactivar, CU010 Consultar
 * Todo es exclusivo de SuperAdmin. A diferencia de SuperAdmin, aqui no hay
 * regla del "ultimo": un SuperAdmin siempre puede cubrir la funcion del Admin
 */
@Path("admins")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"SUPERADMIN"}) //En la clase: protege todos los endpoints de abajo
public class AdminControlador {
    private final AdminDAO adminDAO = new AdminDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaUsuario> respuesta = new ArrayList<>();
            for (Admin a : adminDAO.listarPaginado(pagina, tamano, busqueda)) {
                respuesta.add(aRespuesta(a));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los administradores");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudCrearUsuario solicitud) {
        String problema = ValidacionUsuario.validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }

        Admin nuevo = new Admin(0, solicitud.getCui().trim(), solicitud.getNombre().trim(),
                solicitud.getCorreo().trim(), solicitud.getTelefono().trim(),
                solicitud.getDireccion().trim(), solicitud.getContrasena(), EstadoGeneral.ACTIVO);
        try {
            adminDAO.crear(nuevo); //Hashea la contraseña y guarda usuario + admin en una transaccion
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nuevo)).build();
        } catch (ExcepcionCorreoDuplicado | ExcepcionCuiDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear el administrador");
        }
    }

    @PUT
    @Path("{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            adminDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionAdminNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar el administrador");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") int id) {
        try {
            adminDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionAdminNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar el administrador");
        }
    }

    //Metodos de Apoyo 

    //Convierte la entidad en la respuesta, sin la contraseña
    private RespuestaUsuario aRespuesta(Admin a) {
        return new RespuestaUsuario(a.getIdUsuario(), a.getCui(), a.getNombre(), a.getCorreo(),
                a.getTelefono(), a.getDireccion(), a.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
