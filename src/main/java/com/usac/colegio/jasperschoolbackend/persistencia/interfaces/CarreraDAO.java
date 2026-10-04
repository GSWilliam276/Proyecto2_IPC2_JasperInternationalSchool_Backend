/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCarreraConEstudiantes;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCarreraNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Carrera;
/**
 *
 * @author eduar
 */
public interface CarreraDAO extends DAOGenerico<Carrera>{
    void activar(int id) throws ExcepcionCarreraNoEncontrada, ExcepcionPersistencia;
    void desactivar(int id) throws ExcepcionCarreraNoEncontrada, ExcepcionCarreraConEstudiantes, ExcepcionPersistencia;
}
