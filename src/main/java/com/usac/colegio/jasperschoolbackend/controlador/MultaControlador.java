/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.controlador;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Multa;
import com.usac.colegio.jasperschoolbackend.persistencia.implementacion.MultaDAOPersistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.MultaDAO;
import com.usac.colegio.jasperschoolbackend.seguridad.RolesPermitidos;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaError;
import com.usac.colegio.jasperschoolbackend.transferencia.RespuestaMulta;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
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
@Path("multas")
@Produces(MediaType.APPLICATION_JSON)
@RolesPermitidos({"BIBLIOTECARIO", "SECRETARIA"})
public class MultaControlador {
    //Consulta de multas (CU078). Bibliotecario y Secretaria
    private final MultaDAO multaDAO = new MultaDAOPersistencia();

    @GET
    public Response listar(@QueryParam("pagina") @DefaultValue("1") int pagina,
                           @QueryParam("tamano") @DefaultValue("10") int tamano,
                           @QueryParam("estado") String estado,
                           @QueryParam("idEstudiante") Integer idEstudiante) {
        if (pagina < 1 || tamano < 1) {
            return error(Response.Status.BAD_REQUEST, "La página y el tamaño deben ser mayores que cero");
        }
        String filtro = (estado == null || estado.isBlank()) ? null : estado;
        if (filtro != null && !List.of("PENDIENTE", "PAGADA").contains(filtro)) {
            return error(Response.Status.BAD_REQUEST, "El estado debe ser PENDIENTE o PAGADA");
        }
        try {
            List<RespuestaMulta> respuesta = new ArrayList<>();
            for (Multa m : multaDAO.listar(pagina, tamano, filtro, idEstudiante)) {
                respuesta.add(new RespuestaMulta(m.getIdMulta(), m.getIdPrestamo(), m.getLibroTitulo(),
                        m.getIdEstudiante(), m.getEstudianteNombre(), m.getDiasAtraso(), m.getMonto(), m.getEstado(),
                        m.getFechaGeneracion().toString(), m.getIdPago()));
            }
            return Response.ok(respuesta).build();
        } catch (ExcepcionPersistencia e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Error interno al listar las multas");
        }
    }

    private Response error(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new RespuestaError(mensaje)).build();
    }
}
