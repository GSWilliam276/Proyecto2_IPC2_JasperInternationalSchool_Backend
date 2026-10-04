/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAnioLectivoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.AnioLectivo;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface AnioLectivoDAO extends DAOGenerico<AnioLectivo> {
    void cerrar(int id) throws ExcepcionAnioLectivoNoEncontrado, ExcepcionPersistencia;
    Optional<AnioLectivo> buscarActivo() throws ExcepcionPersistencia;
}
