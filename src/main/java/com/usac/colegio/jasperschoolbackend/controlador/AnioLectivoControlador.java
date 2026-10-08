/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAnioLectivoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSolapamientoFechas;
import com.usac.colegio.jasperschoolbackend.modelo.AnioLectivo;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.AnioLectivoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AnioLectivoDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaAnioLectivo;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudAnioLectivo;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 * CU011 Crear, CU012 Editar, CU013 Cerrar año lectivo, y su consulta
 * Exclusivo de SuperAdmin
 */
@Path("anios-lectivos")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"SUPERADMIN"})
public class AnioLectivoControlador {
    private final AnioLectivoDAO anioDAO = new AnioLectivoDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaAnioLectivo> respuesta = new ArrayList<>();
            for (AnioLectivo a : anioDAO.listarPaginado(pagina, tamano, busqueda)) {
                respuesta.add(aRespuesta(a));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los años lectivos");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudAnioLectivo solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            //Un solo año lectivo activo a la vez
            if (anioDAO.buscarActivo().isPresent()) {
                return error(Response.Status.CONFLICT,
                        "Ya existe un año lectivo activo. Ciérrelo antes de crear otro");
            }
            AnioLectivo nuevo = new AnioLectivo(0, solicitud.getNombre().trim(),
                    LocalDate.parse(solicitud.getFechaInicio()), LocalDate.parse(solicitud.getFechaFin()),
                    EstadoAnioLectivo.ACTIVO);
            anioDAO.crear(nuevo); //el DAO revisa el solapamiento de fechas
            //201 sin cuerpo: el DAO no devuelve el id generado, y la lista se recarga igual
            return Response.status(Response.Status.CREATED).build();
        } catch (ExcepcionSolapamientoFechas e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear el año lectivo");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editar(@PathParam("id") int id, SolicitudAnioLectivo solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            Optional<AnioLectivo> actual = anioDAO.buscarPorId(id);
            if (actual.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "No existe un año lectivo con ese id");
            }
            //un año cerrado queda en solo lectura
            if (actual.get().getEstado() == EstadoAnioLectivo.CERRADO) {
                return error(Response.Status.CONFLICT, "Un año lectivo cerrado no se puede editar");
            }
            AnioLectivo editado = new AnioLectivo(id, solicitud.getNombre().trim(),
                    LocalDate.parse(solicitud.getFechaInicio()), LocalDate.parse(solicitud.getFechaFin()),
                    actual.get().getEstado());
            anioDAO.editar(editado);
            return Response.noContent().build();
        } catch (ExcepcionAnioLectivoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionSolapamientoFechas e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar el año lectivo");
        }
    }

    @PUT
    @Path("{id}/cerrar")
    public Response cerrar(@PathParam("id") int id) {
        try {
            Optional<AnioLectivo> actual = anioDAO.buscarPorId(id);
            if (actual.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "No existe un año lectivo con ese id");
            }
            if (actual.get().getEstado() == EstadoAnioLectivo.CERRADO) {
                return error(Response.Status.CONFLICT, "El año lectivo ya está cerrado");
            }
            anioDAO.cerrar(id); //irreversible
            return Response.noContent().build();
        } catch (ExcepcionAnioLectivoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al cerrar el año lectivo");
        }
    }

    //Metodos de Apoyo

    private String validar(SolicitudAnioLectivo s) {
        if (s == null || vacio(s.getNombre()) || vacio(s.getFechaInicio()) || vacio(s.getFechaFin())) {
            return "Todos los campos son obligatorios";
        }
        if (s.getNombre().trim().length() > 50) {
            return "El nombre no puede pasar de 50 caracteres";
        }
        try {
            LocalDate inicio = LocalDate.parse(s.getFechaInicio());
            LocalDate fin = LocalDate.parse(s.getFechaFin());
            if (!fin.isAfter(inicio)) {
                return "La fecha de fin debe ser posterior a la de inicio";
            }
        } catch (DateTimeParseException e) {
            return "Las fechas deben tener el formato AAAA-MM-DD";
        }
        return null;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private RespuestaAnioLectivo aRespuesta(AnioLectivo a) {
        return new RespuestaAnioLectivo(a.getIdAnioLectivo(), a.getNombre(),
                a.getFechaInicio().toString(), a.getFechaFin().toString(), a.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
