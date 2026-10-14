/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Seccion;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.SeccionDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SeccionDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaSeccion;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudEditarSeccion;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudSeccion;
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
 */
@Path("secciones")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN"})
public class SeccionControlador {
    //Secciones (CU040 a CU044). Admin y, por herencia, SuperAdmin.
    private static final int CUPO_MAXIMO_PERMITIDO = 200; //Limite razonable

    private final SeccionDAO seccionDAO = new SeccionDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda,
                           @QueryParam("idGrado") Integer idGrado,
                           @QueryParam("idAnioLectivo") Integer idAnioLectivo,
                           @QueryParam("todos") @DefaultValue("false") boolean todos) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaSeccion> respuesta = new ArrayList<>();
            for (Seccion s : seccionDAO.listar(pagina, tamano, busqueda, idGrado, idAnioLectivo, !todos)) {
                respuesta.add(aRespuesta(s));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las secciones");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudSeccion solicitud) {
        if (solicitud == null || solicitud.getIdGrado() == null) {
            return error(Response.Status.BAD_REQUEST, "El grado es obligatorio");
        }
        String problema = validarNombreYCupo(solicitud.getNombre(), solicitud.getCupoMaximo());
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            Seccion nueva = seccionDAO.crear(solicitud.getNombre().trim(), solicitud.getIdGrado(),
                    solicitud.getIdCarrera(), solicitud.getCupoMaximo());
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nueva)).build();
        } catch (ExcepcionNombreDuplicado | ExcepcionReferenciaInvalida e) {
            //409: la peticion esta bien armada, pero choca con una regla o con el estado de los datos
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear la sección");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editar(@PathParam("id") int id, SolicitudEditarSeccion solicitud) {
        if (solicitud == null) {
            return error(Response.Status.BAD_REQUEST, "El nombre y el cupo son obligatorios");
        }
        String problema = validarNombreYCupo(solicitud.getNombre(), solicitud.getCupoMaximo());
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            seccionDAO.editar(id, solicitud.getNombre().trim(), solicitud.getCupoMaximo());
            return Response.noContent().build();
        } catch (ExcepcionSeccionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionNombreDuplicado | ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar la sección");
        }
    }

    @PUT
    @Path("{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            seccionDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionSeccionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar la sección");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") int id) {
        try {
            seccionDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionSeccionNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar la sección");
        }
    }

    //Metodos de Apoyo

    private String validarNombreYCupo(String nombre, Integer cupo) {
        if (nombre == null || nombre.isBlank() || cupo == null) {
            return "El nombre y el cupo son obligatorios";
        }
        if (nombre.trim().length() > 10) {
            return "El nombre no puede pasar de 10 caracteres";
        }
        if (cupo < 1 || cupo > CUPO_MAXIMO_PERMITIDO) {
            return "El cupo debe estar entre 1 y " + CUPO_MAXIMO_PERMITIDO;
        }
        return null;
    }

    private RespuestaSeccion aRespuesta(Seccion s) {
        return new RespuestaSeccion(s.getIdSeccion(), s.getNombre(), s.getGrado().getIdGrado(),
                s.getGrado().getNombre(), s.getGrado().getNivel().name(),
                s.getCarrera() == null ? null : s.getCarrera().getIdCarrera(),
                s.getCarrera() == null ? null : s.getCarrera().getNombre(),
                s.getIdAnioLectivo(), s.getAnioNombre(), s.getEstadoAnio().name(),
                s.getCupoMaximo(), s.getInscritos(), s.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
