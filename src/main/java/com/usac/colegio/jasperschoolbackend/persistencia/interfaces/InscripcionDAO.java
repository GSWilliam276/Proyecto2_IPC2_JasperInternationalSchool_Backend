/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionInscripcionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Inscripcion;
import com.usac.colegio.jasperschoolbackend.modelo.ResultadoInscripcion;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface InscripcionDAO {
    //CU059. El correo solo se usa si el estudiante todavia no tiene acceso
    ResultadoInscripcion inscribir(int idEstudiante, int idSeccion, String correo, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida,
            ExcepcionCorreoDuplicado, ExcepcionPersistencia;

    //CU061. Al anular, el cupo queda libre porque el cupo cuenta solo inscripciones ACTIVAS
    void anular(int idInscripcion)
            throws ExcepcionInscripcionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    Optional<Inscripcion> buscarPorId(int idInscripcion) throws ExcepcionPersistencia;

    //CU060. Filtros opcionales (null = sin filtro). Sin idAnio se usa el año lectivo activo
    List<Inscripcion> listar(int pagina, int tamanoPagina, Integer idAnio, Integer idGrado, Integer idSeccion,
                             Integer idEstudiante) throws ExcepcionPersistencia;
    
    //Estudiantes con inscripcion ACTIVA en la seccion (nomina del maestro)
    List<Inscripcion> listarActivasDeSeccion(int idSeccion) throws ExcepcionPersistencia;
}
