/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionGradoConEstudiantes;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionGradoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Grado;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.GradoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.GradoDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaGrado;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudGrado;
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
@Path("grados")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"SUPERADMIN"})
public class GradoControlador {
    //CU014 Crear, CU015 Editar, CU016 Activar, CU017 Desactivar y CU018 Consultar grado
    //Exclusivo de SuperAdmin
    private final GradoDAO gradoDAO = new GradoDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaGrado> respuesta = new ArrayList<>();
            for (Grado g : gradoDAO.listarPaginado(pagina, tamano, busqueda)) {
                respuesta.add(aRespuesta(g));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los grados");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudGrado solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            Grado nuevo = new Grado(0, solicitud.getNombre().trim(),
                    Nivel.valueOf(solicitud.getNivel()), EstadoGeneral.ACTIVO);
            gradoDAO.crear(nuevo);
            return Response.status(Response.Status.CREATED).build(); //Sin cuerpo al igual que año lectivo
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear el grado");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editar(@PathParam("id") int id, SolicitudGrado solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            //El DAO solo actualiza nombre y nivel, el estado de este objeto no se usa
            Grado editado = new Grado(id, solicitud.getNombre().trim(),
                    Nivel.valueOf(solicitud.getNivel()), EstadoGeneral.ACTIVO);
            gradoDAO.editar(editado);
            return Response.noContent().build();
        } catch (ExcepcionGradoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar el grado");
        }
    }

    @PUT
    @Path("{id}/activar")
    public Response activar(@PathParam("id") int id) {
        try {
            gradoDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionGradoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar el grado");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    public Response desactivar(@PathParam("id") int id) {
        try {
            gradoDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionGradoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionGradoConEstudiantes e) {
            //409: la peticion es valida, pero choca con una regla de negocio
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar el grado");
        }
    }

    //Metodos de Apoyo

    private String validar(SolicitudGrado s) {
        if (s == null || vacio(s.getNombre()) || vacio(s.getNivel())) {
            return "Todos los campos son obligatorios";
        }
        if (s.getNombre().trim().length() > 50) { 
            return "El nombre no puede pasar de 50 caracteres";
        }
        try {
            Nivel.valueOf(s.getNivel());
        } catch (IllegalArgumentException e) {
            return "El nivel debe ser PRE_PRIMARIA, PRIMARIA, BASICO o DIVERSIFICADO";
        }
        return null;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private RespuestaGrado aRespuesta(Grado g) {
        return new RespuestaGrado(g.getIdGrado(), g.getNombre(), g.getNivel().name(), g.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
