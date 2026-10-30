/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.RegistroNota;
import com.usac.colegio.jasperschoolbackend.modelo.ResumenAlumno;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
/**
 *
 * @author eduar
 */
public interface NotaDAO {
    //Nomina con la nota de cada uno en la actividad de zona (null = sin nota)
    List<RegistroNota> planillaZona(int idAsignacion, int idActividadZona) throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //CU088/CU089. Inserta o corrige. Cada valor debe estar entre 0 y el punteo de la actividad
    void guardarZona(int idAsignacion, int idActividadZona, Map<Integer, BigDecimal> notas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    List<RegistroNota> planillaFinal(int idAsignacion) throws ExcepcionPersistencia;

    //CU090. Examen final, de 0 a 30
    void guardarFinal(int idAsignacion, Map<Integer, BigDecimal> notas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia;
    
    //Resumen de cada estudiante inscrito: zona, examen, total y estado
    List<ResumenAlumno> resumen(int idAsignacion) throws ExcepcionPersistencia;
}
