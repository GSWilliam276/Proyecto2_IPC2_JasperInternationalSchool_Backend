/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionActividadNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ActividadExtracurricular;
import java.util.List;
/**
 *
 * @author eduar
 */
public interface ActividadDAO extends DAOGenerico<ActividadExtracurricular>{
    void activar(int idActividad) throws ExcepcionActividadNoEncontrada, ExcepcionPersistencia;

    void desactivar(int idActividad) throws ExcepcionActividadNoEncontrada, ExcepcionPersistencia;

    //soloActivas = true es lo que usa la Secretaria al cobrar: solo actividades disponibles
    List<ActividadExtracurricular> listarFiltrado(int pagina, int tamanoPagina, String busqueda, boolean soloActivas)
            throws ExcepcionPersistencia;
}
