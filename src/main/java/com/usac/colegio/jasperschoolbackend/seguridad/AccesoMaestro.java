/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.seguridad;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionAjena;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.AsignacionMaestro;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AsignacionMaestroDAO;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class AccesoMaestro {
    //Unica regla de "esta asignacion es mia": la usan nomina, asistencia, zona y notas
    private AccesoMaestro() {
    }

    public static AsignacionMaestro asignacionPropia(AsignacionMaestroDAO dao, int idAsignacion, int idMaestro)
            throws ExcepcionAsignacionNoEncontrada, ExcepcionAsignacionAjena, ExcepcionOperacionNoPermitida,
            ExcepcionPersistencia {
        Optional<AsignacionMaestro> asignacion = dao.buscarPorId(idAsignacion);
        if (asignacion.isEmpty()) {
            throw new ExcepcionAsignacionNoEncontrada("No existe una asignación con ese id");
        }
        if (asignacion.get().getIdMaestro() != idMaestro) {
            throw new ExcepcionAsignacionAjena("Esa asignación no le pertenece");
        }
        if (asignacion.get().getEstadoAnio() != EstadoAnioLectivo.ACTIVO) {
            throw new ExcepcionOperacionNoPermitida("La asignación pertenece a un año lectivo cerrado");
        }
        return asignacion.get();
    }
}
