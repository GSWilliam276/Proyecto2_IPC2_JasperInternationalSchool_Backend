/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionLibroNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Libro;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.LibroDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.LibroDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaLibro;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudLibro;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
@Path("libros")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"BIBLIOTECARIO", "ESTUDIANTE"})
public class LibroControlador {
    //Libros (CU070 a CU074). Consultar: Bibliotecario y Estudiante. El resto: Bibliotecario
    private static final int EJEMPLARES_MAXIMO = 1000;   //Tope razonable

    private final LibroDAO libroDAO = new LibroDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda,
                           @Context ContainerRequestContext contexto) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        //El Estudiante solo ve libros activos con ejemplares disponibles: se decide aqui, no en el frontend
        boolean soloDisponibles = "ESTUDIANTE".equals(String.valueOf(contexto.getProperty("rol")));
        try {
            List<RespuestaLibro> respuesta = new ArrayList<>();
            for (Libro l : libroDAO.listarFiltrado(pagina, tamano, busqueda, soloDisponibles)) {
                respuesta.add(aRespuesta(l));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los libros");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"BIBLIOTECARIO"})
    public Response registrar(SolicitudLibro solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        Libro nuevo = construir(0, solicitud, solicitud.getEjemplares());
        try {
            libroDAO.crear(nuevo);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(nuevo)).build();
        } catch (ExcepcionNombreDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al registrar el libro");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"BIBLIOTECARIO"})
    public Response editar(@PathParam("id") int id, SolicitudLibro solicitud) {
        String problema = validar(solicitud);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            //Los disponibles que se mandan aqui no se usan: el DAO los recalcula con los prestados
            libroDAO.editar(construir(id, solicitud, solicitud.getEjemplares()));
            return Response.noContent().build();
        } catch (ExcepcionLibroNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionNombreDuplicado | ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar el libro");
        }
    }

    @PUT
    @Path("{id}/activar")
    @RolesPermitidos({"BIBLIOTECARIO"})
    public Response activar(@PathParam("id") int id) {
        try {
            libroDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionLibroNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar el libro");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @RolesPermitidos({"BIBLIOTECARIO"})
    public Response desactivar(@PathParam("id") int id) {
        try {
            libroDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionLibroNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar el libro");
        }
    }

    //Metodos de Apoyo

    private String validar(SolicitudLibro s) {
        if (s == null || vacio(s.getTitulo()) || vacio(s.getAutor()) || vacio(s.getEditorial())
                || s.getEjemplares() == null) {
            return "El título, el autor, la editorial y los ejemplares son obligatorios";
        }
        if (s.getTitulo().trim().length() > 150) {
            return "El título no puede pasar de 150 caracteres";
        }
        if (s.getAutor().trim().length() > 100 || s.getEditorial().trim().length() > 100) {
            return "El autor y la editorial no pueden pasar de 100 caracteres";
        }
        if (s.getEjemplares() < 1 || s.getEjemplares() > EJEMPLARES_MAXIMO) {
            return "Los ejemplares deben estar entre 1 y " + EJEMPLARES_MAXIMO;
        }
        return null;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private Libro construir(int id, SolicitudLibro s, int ejemplares) {
        return new Libro(id, s.getTitulo().trim(), s.getAutor().trim(), s.getEditorial().trim(), ejemplares,
                ejemplares, EstadoGeneral.ACTIVO);
    }

    private RespuestaLibro aRespuesta(Libro l) {
        return new RespuestaLibro(l.getIdLibro(), l.getTitulo(), l.getAutor(), l.getEditorial(),
                l.getEjemplaresTotales(), l.getEjemplaresDisponibles(), l.getEjemplaresPrestados(),
                l.getEstado().name());
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
