/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCursoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
/**
 *
 * @author eduar
 */
public interface CursoDAO extends DAOGenerico<Curso> {
    void activar(int idCurso) throws ExcepcionCursoNoEncontrado, ExcepcionPersistencia;
    void desactivar(int idCurso) throws ExcepcionCursoNoEncontrado, ExcepcionPersistencia;
}
