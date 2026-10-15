/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionDuplicada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.AsignacionMaestro;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
import com.usac.colegio.jasperschoolbackend.modelo.Maestro;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface AsignacionMaestroDAO {
    //Vincula un maestro a un curso de una seccion del año lectivo activo (CU048)
    AsignacionMaestro asignar(int idSeccion, int idCurso, int idMaestro)
            throws ExcepcionSeccionNoEncontrada, ExcepcionReferenciaInvalida, ExcepcionOperacionNoPermitida,
            ExcepcionAsignacionDuplicada, ExcepcionPersistencia;

    //Retira la asignacion (CU049). Reasignar es quitar y volver a asignar
    void quitar(int idAsignacion) throws ExcepcionAsignacionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    Optional<AsignacionMaestro> buscarPorId(int idAsignacion) throws ExcepcionPersistencia;

    //Filtros opcionales (null = sin filtro). Sin "todos", solo las del año lectivo activo (CU050)
    List<AsignacionMaestro> listar(int pagina, int tamanoPagina, Integer idMaestro, Integer idGrado,
                                   Integer idSeccion, boolean todos) throws ExcepcionPersistencia;

    //Cursos del curriculo de la seccion que todavia no tienen maestro: lo que se puede asignar
    List<Curso> cursosSinMaestro(int idSeccion) throws ExcepcionSeccionNoEncontrada, ExcepcionPersistencia;

    //Maestros con cuenta activa, para elegir a quien asignar
    List<Maestro> listarMaestrosActivos() throws ExcepcionPersistencia;
}
