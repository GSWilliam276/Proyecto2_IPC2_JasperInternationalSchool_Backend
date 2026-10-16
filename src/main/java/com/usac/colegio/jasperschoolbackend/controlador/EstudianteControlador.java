/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.enums.EstadoEstudiante;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Estudiante;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.EstudianteDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.EstudianteDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaEstudiante;
import com.usac.colegio.jasperschoolbackend.transferencia.SolicitudEstudiante;
import com.usac.colegio.jasperschoolbackend.transferencia.ValidacionEstudiante;
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
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
@Path("estudiantes")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"ADMIN", "SUPERADMIN", "SECRETARIA"})
public class EstudianteControlador {
    //Estudiantes (CU029 a CU034). Consultar: Admin y Secretaria. Registrar, editar y cambiar de estado:
    //Admin y, por herencia, SuperAdmin
    private final EstudianteDAO estudianteDAO = new EstudianteDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("busqueda") String busqueda,
                           @QueryParam("estado") String estado) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        EstadoEstudiante filtro = null;
        if (estado != null && !estado.isBlank()) {
            try {
                filtro = EstadoEstudiante.valueOf(estado);
            } catch (IllegalArgumentException e) {
                return error(Response.Status.BAD_REQUEST, "El estado debe ser ACTIVO, INACTIVO o GRADUADO");
            }
        }
        try {
            List<RespuestaEstudiante> respuesta = new ArrayList<>();
            for (Estudiante e : estudianteDAO.listar(pagina, tamano, busqueda, filtro)) {
                respuesta.add(aRespuesta(e));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar los estudiantes");
        }
    }

    /** El expediente de un estudiante. */
    @GET
    @Path("{id}")
    public Response consultar(@PathParam("id") int id) {
        try {
            Optional<Estudiante> estudiante = estudianteDAO.buscarPorId(id);
            if (estudiante.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "No existe un estudiante con ese id");
            }
            return Response.ok(aRespuesta(estudiante.get())).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al consultar el estudiante");
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response registrar(SolicitudEstudiante solicitud) {
        String problema = ValidacionEstudiante.validar(solicitud, true);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        Estudiante nuevo = construir(0, solicitud.getCui().trim(), null, solicitud, EstadoEstudiante.ACTIVO);
        try {
            Estudiante creado = estudianteDAO.registrar(nuevo);
            return Response.status(Response.Status.CREATED).entity(aRespuesta(creado)).build();
        } catch (ExcepcionCuiDuplicado e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al registrar el estudiante");
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response editar(@PathParam("id") int id, SolicitudEstudiante solicitud) {
        String problema = ValidacionEstudiante.validar(solicitud, false);
        if (problema != null) {
            return error(Response.Status.BAD_REQUEST, problema);
        }
        try {
            Optional<Estudiante> actual = estudianteDAO.buscarPorId(id);
            if (actual.isEmpty()) {
                return error(Response.Status.NOT_FOUND, "No existe un estudiante con ese id");
            }
            // se conservan CUI, correo y estado: el PUT reemplaza el resto de los datos
            Estudiante a = actual.get();
            estudianteDAO.editar(construir(id, a.getCui(), a.getCorreo(), solicitud, a.getEstado()));
            return Response.noContent().build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al editar el estudiante");
        }
    }

    @PUT
    @Path("{id}/activar")
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response activar(@PathParam("id") int id) {
        try {
            estudianteDAO.activar(id);
            return Response.noContent().build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al activar el estudiante");
        }
    }

    @PUT
    @Path("{id}/desactivar")
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response desactivar(@PathParam("id") int id) {
        try {
            estudianteDAO.desactivar(id);
            return Response.noContent().build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al desactivar el estudiante");
        }
    }

    @PUT
    @Path("{id}/graduar")
    @RolesPermitidos({"ADMIN", "SUPERADMIN"})
    public Response graduar(@PathParam("id") int id) {
        try {
            estudianteDAO.graduar(id);
            return Response.noContent().build();
        } catch (ExcepcionEstudianteNoEncontrado e) {
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (ExcepcionOperacionNoPermitida e) {
            return error(Response.Status.CONFLICT, e.getMessage());
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al graduar el estudiante");
        }
    }

    // ----- apoyo -----

    private Estudiante construir(int id, String cui, String correo, SolicitudEstudiante s, EstadoEstudiante estado) {
        return new Estudiante(id, cui, s.getNombre().trim(), correo, s.getTelefono().trim(), s.getDireccion().trim(),
                null, LocalDate.parse(s.getFechaNacimiento().trim()), limpio(s.getAlergias()),
                limpio(s.getCondicionesMedicas()), s.getEncargadoNombre().trim(), s.getEncargadoTelefono().trim(),
                s.getEncargadoParentesco().trim(), limpio(s.getEncargado2Nombre()), limpio(s.getEncargado2Telefono()),
                limpio(s.getEncargado2Parentesco()), estado);
    }

    /** Un opcional vacio se guarda como null: asi queda una sola forma de "sin dato". */
    private String limpio(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private RespuestaEstudiante aRespuesta(Estudiante e) {
        int edad = Period.between(e.getFechaNacimiento(), LocalDate.now()).getYears();
        return new RespuestaEstudiante(e.getIdUsuario(), e.getCui(), e.getNombre(), e.getCorreo(), e.getTelefono(),
                e.getDireccion(), e.getFechaNacimiento().toString(), edad, e.getAlergias(), e.getCondicionesMedicas(),
                e.getEncargadoNombre(), e.getEncargadoTelefono(), e.getEncargadoParentesco(),
                e.getEncargado2Nombre(), e.getEncargado2Telefono(), e.getEncargado2Parentesco(),
                e.getEstado().name(), e.getCorreo() != null);
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
