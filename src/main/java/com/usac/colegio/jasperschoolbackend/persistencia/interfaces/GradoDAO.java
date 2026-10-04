/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionGradoConEstudiantes;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionGradoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Grado;
/**
 *
 * @author eduar
 */
public interface GradoDAO extends DAOGenerico<Grado> {
    void activar(int id) throws ExcepcionGradoNoEncontrado, ExcepcionPersistencia;
    void desactivar(int id) throws ExcepcionGradoNoEncontrado, ExcepcionGradoConEstudiantes, ExcepcionPersistencia;
}
