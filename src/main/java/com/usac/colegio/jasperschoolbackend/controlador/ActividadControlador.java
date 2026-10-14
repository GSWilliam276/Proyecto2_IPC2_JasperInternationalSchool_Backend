/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionActividadNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ActividadExtracurricular;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.ActividadDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ActividadDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaActividad;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudActividad;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
@Path("actividades")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN", "SECRETARIA"})
public class ActividadControlador {
    private static final BigDecimal COSTO_MAXIMO = new BigDecimal("100000"); //Tope razonable
    private static final int CUPO_MAXIMO = 1000;                              

    private final ActividadDAO actividadDAO = new ActividadDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda,
                           @QueryParam("soloActivas") @DefaultValue("false") boolean soloActivas) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaActividad> respuesta = new ArrayList<>();
            for (ActividadExtracurricular a : actividadDAO.listarFiltrado(pagina, tamano, busqueda, soloActivas)) {
                respuesta.add(aRespuesta(a));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las actividades");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response crear(SolicitudActividad solicitud) {
        String problema = validar(solicitud, true);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        ActividadExtracurricular nueva = construir(0, solicitud, EstadoGeneral.ACTIVO);
        try {
            actividadDAO.crear(nueva);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nueva)).build();
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear la actividad");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response editar(@PathParam("id") int id, SolicitudActividad solicitud) {
        //Al editar se permite una fecha pasada: sirve para corregir una actividad ya realizada
        String problema = validar(solicitud, false);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            //El DAO no toca el estado: el de este objeto no se usa
            actividadDAO.editar(construir(id, solicitud, EstadoGeneral.ACTIVO));
            return Response.noContent().build();
        } catch (ExcepcionActividadNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar la actividad");
        }
    }

    @PUT
    @Path("{id}/activar")
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response activar(@PathParam("id") int id) {
        try {
            actividadDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionActividadNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar la actividad");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response desactivar(@PathParam("id") int id) {
        try {
            actividadDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionActividadNoEncontrada e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar la actividad");
        }
    }

    //Metodos de Apoyo 

    private String validar(SolicitudActividad s, boolean alCrear) {
        if (s == null || s.getNombre() == null || s.getNombre().isBlank() || s.getFecha() == null
                || s.getFecha().isBlank() || s.getCupo() == null || s.getCosto() == null) {
            return "El nombre, la fecha, el cupo y el costo son obligatorios";
        }
        if (s.getNombre().trim().length() > 100) {
            return "El nombre no puede pasar de 100 caracteres";
        }
        if (s.getDescripcion() != null && s.getDescripcion().trim().length() > 255) {
            return "La descripción no puede pasar de 255 caracteres";
        }
        try {
            if (alCrear && LocalDate.parse(s.getFecha().trim()).isBefore(LocalDate.now())) {
                return "La fecha de la actividad no puede ser anterior a hoy";
            }
            LocalDate.parse(s.getFecha().trim());
        } catch (DateTimeParseException e) {
            return "La fecha debe tener el formato AAAA-MM-DD";
        }
        if (s.getCupo() < 1 || s.getCupo() > CUPO_MAXIMO) {
            return "El cupo debe estar entre 1 y " + CUPO_MAXIMO;
        }
        if (s.getCosto().signum() < 0) {
            return "El costo no puede ser negativo (use cero para una actividad gratuita)";
        }
        if (s.getCosto().stripTrailingZeros().scale() > 2) {
            return "El costo admite como máximo 2 decimales";
        }
        if (s.getCosto().compareTo(COSTO_MAXIMO) > 0) {
            return "El costo no puede pasar de Q100,000";
        }
        return null;
    }

    private ActividadExtracurricular construir(int id, SolicitudActividad s, EstadoGeneral estado) {
        String descripcion = (s.getDescripcion() == null || s.getDescripcion().isBlank()) ? null : s.getDescripcion().trim();
        return new ActividadExtracurricular(id, s.getNombre().trim(), descripcion,
                LocalDate.parse(s.getFecha().trim()), s.getCupo(), s.getCosto(), estado);
    }

    private RespuestaActividad aRespuesta(ActividadExtracurricular a) {
        return new RespuestaActividad(a.getIdActividad(), a.getNombre(), a.getDescripcion(),
                a.getFecha().toString(), a.getCupo(), a.getCosto(), a.esGratuita(), a.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
