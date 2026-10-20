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
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.glassfish.jersey.media.multipart.FormDataParam;
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
    //Se escribe con el nombre completo porque "Path" ya es la anotacion de JAX-RS
    private static final java.nio.file.Path CARPETA_IMAGENES = Paths.get("C:\\jasper-imagenes\\libros");
    private static final long IMAGEN_MAXIMA = 2L * 1024 * 1024;   //2 MB

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

    //Portada del libro: subir y ver

    @POST
    @Path("{id}/imagen")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesPermitidos({"BIBLIOTECARIO"})
    public Response subirImagen(@PathParam("id") int id,
                                @FormDataParam("archivo") InputStream archivo) {
        if (archivo == null) {
            return error(Response.Status.BAD_REQUEST, "Debe enviar un archivo");
        }
        try {
            if (libroDAO.buscarPorId(id).isEmpty()) {
                return error(Response.Status.NOT_FOUND, "No existe un libro con ese id");
            }
            //Se lee con tope (2 MB + 1 byte): asi se rechaza sin cargar todo en memoria
            byte[] datos = archivo.readNBytes((int) IMAGEN_MAXIMA + 1);
            if (datos.length > IMAGEN_MAXIMA) {
                return error(Response.Status.BAD_REQUEST, "La imagen no puede pasar de 2 MB");
            }
            String extension = detectarExtension(datos);
            if (extension == null) {
                return error(Response.Status.BAD_REQUEST, "La imagen debe ser JPG, PNG o WebP");
            }
            Files.createDirectories(CARPETA_IMAGENES);
            String nombre = "libro-" + id + "." + extension;
            //Si ya habia una portada con otra extension, se borra
            for (String e : new String[] {"jpg", "png", "webp"}) {
                Files.deleteIfExists(CARPETA_IMAGENES.resolve("libro-" + id + "." + e));
            }
            Files.write(CARPETA_IMAGENES.resolve(nombre), datos);
            libroDAO.guardarImagen(id, nombre);
            return Response.noContent().build();
        } catch (IOException | ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al guardar la imagen");
        }
    }

    @GET
    @Path("{id}/imagen")
    @Produces({"image/jpeg", "image/png", "image/webp", MediaType.APPLICATION_JSON})
    public Response verImagen(@PathParam("id") int id) {
        try {
            Optional<Libro> libro = libroDAO.buscarPorId(id);
            if (libro.isEmpty() || libro.get().getImagen() == null) {
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
                        .entity(new RespuestaError("El libro no tiene imagen")).build();
            }
            //El nombre sale de la base y se limpia otra vez: nunca se arma una ruta con datos del usuario
            java.nio.file.Path ruta = CARPETA_IMAGENES.resolve(
                    Paths.get(libro.get().getImagen()).getFileName().toString());
            if (!Files.exists(ruta)) {
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
                        .entity(new RespuestaError("No se encontró el archivo de la imagen")).build();
            }
            String nombreRuta = ruta.toString();
            String tipo = nombreRuta.endsWith(".png") ? "image/png"
                    : nombreRuta.endsWith(".webp") ? "image/webp" : "image/jpeg";
            StreamingOutput salida = out -> Files.copy(ruta, out);
            return Response.ok(salida, tipo).build();
        } catch (ExcepcionPersistencia e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
                    .entity(new RespuestaError("Error interno al leer la imagen")).build();
        }
    }

    //Metodos de Apoyo

    //Reconoce el formato por los primeros bytes, no por el nombre que mande el usuario
    private String detectarExtension(byte[] d) {
        if (d.length > 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (d.length > 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "png";
        }
        if (d.length > 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "webp";
        }
        return null;
    }

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
                l.getEstado().name(), l.getImagen() != null);
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
