/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.RegistroAsistencia;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
/**
 *
 * @author eduar
 */
public interface AsistenciaDAO {
    //CU081. Marca a todos los inscritos activos; falla si ya hay asistencia ese dia
    void registrar(int idAsignacion, LocalDate fecha, Map<Integer, Boolean> marcas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //CU082. Corrige una fecha ya tomada; falla si ese dia no tiene asistencia
    void editar(int idAsignacion, LocalDate fecha, Map<Integer, Boolean> marcas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //La nomina actual con lo marcado en esa fecha (presente null = sin registro)
    List<RegistroAsistencia> planilla(int idAsignacion, LocalDate fecha) throws ExcepcionPersistencia;

    //CU083. Historial con filtros opcionales (null = sin filtro)
    List<RegistroAsistencia> listar(int idAsignacion, Integer idEstudiante, LocalDate desde, LocalDate hasta)
            throws ExcepcionPersistencia;
}
