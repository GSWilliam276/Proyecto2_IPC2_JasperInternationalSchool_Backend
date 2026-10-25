/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ActividadZona;
import java.util.List;
/**
 *
 * @author eduar
 */
public interface ZonaDAO {
    //CU084. Valida que la suma de punteos de la asignacion no pase de 70
    ActividadZona crear(int idAsignacion, String nombre, java.math.BigDecimal punteo)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //CU085. La actividad debe ser de la asignacion indicada
    void editar(int idAsignacion, int idActividadZona, String nombre, java.math.BigDecimal punteo)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //CU086. No se elimina si tiene notas
    void eliminar(int idAsignacion, int idActividadZona)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //CU087
    List<ActividadZona> listar(int idAsignacion) throws ExcepcionPersistencia;
}
