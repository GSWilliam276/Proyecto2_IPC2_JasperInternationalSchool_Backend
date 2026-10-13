/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCurriculoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCurriculoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Curriculo;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.CurriculoDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CurriculoDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaCurriculo;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaCurso;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudAgregarCurso;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudCurriculo;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
@Path("curriculos")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN"})
public class CurriculoControlador {
    //Curriculos: la lista de cursos de cada grado. Admin y, por herencia, SuperAdmin
    private final CurriculoDAO curriculoDAO = new CurriculoDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        try {
            List<RespuestaCurriculo> respuesta = new ArrayList<>();
            for (Curriculo c : curriculoDAO.listarPaginado(pagina, tamano, busqueda)) {
                respuesta.add(aRespuesta(c, false));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los currículos");
        }
    }

    @GET
    @Path("{id}")
    public Response consultar(@PathParam("id") int id) {
        try {
            Optional<Curriculo> curriculo = curriculoDAO.buscarPorId(id);
            if (curriculo.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "No existe un currículo con ese id");
            }
            return Response.ok(aRespuesta(curriculo.get(), true)).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar el currículo");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudCurriculo solicitud) {
        if (solicitud == null || solicitud.getIdGrado() == null) {
            return error(Response.Status.BAD_REQUEST, "El grado es obligatorio");
        }
        try {
            Curriculo nuevo = curriculoDAO.crear(solicitud.getIdGrado(), solicitud.getIdCarrera());
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nuevo, true)).build();
        } catch (ExcepcionCurriculoDuplicado | ExcepcionReferenciaInvalida e) {
            //409: la peticion esta bien armada, pero choca con lo que ya existe o con el estado de los datos
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al crear el currículo");
        }
    }

    @POST
    @Path("{id}/cursos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response agregarCurso(@PathParam("id") int id, SolicitudAgregarCurso solicitud) {
        if (solicitud == null || solicitud.getIdCurso() == null) {
            return error(Response.Status.BAD_REQUEST, "El curso es obligatorio");
        }
        try {
            curriculoDAO.agregarCurso(id, solicitud.getIdCurso());
            return Response.status(Response.Status.CREATED).build();
        } catch (ExcepcionCurriculoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionCurriculoDuplicado | ExcepcionReferenciaInvalida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al agregar el curso");
        }
    }

    @DELETE
    @Path("{id}/cursos/{idCurso}")
    public Response quitarCurso(@PathParam("id") int id, @PathParam("idCurso") int idCurso) {
        try {
            curriculoDAO.quitarCurso(id, idCurso);
            return Response.noContent().build();
        } catch (ExcepcionCurriculoNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al quitar el curso");
        }
    }

    //Metodos de Apoyo

    private RespuestaCurriculo aRespuesta(Curriculo c, boolean incluirCursos) {
        List<RespuestaCurso> cursos = null;
        if (incluirCursos) {
            cursos = new ArrayList<>();
            for (Curso curso : c.getCursos()) {
                cursos.add(new RespuestaCurso(curso.getIdCurso(), curso.getNombre(),
                        curso.getDescripcion(), curso.getEstado().name()));
            }
        }
        return new RespuestaCurriculo(c.getIdCurriculo(), c.getGrado().getIdGrado(), c.getGrado().getNombre(),
                c.getGrado().getNivel().name(),
                c.getCarrera() == null ? null : c.getCarrera().getIdCarrera(),
                c.getCarrera() == null ? null : c.getCarrera().getNombre(), cursos);
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
