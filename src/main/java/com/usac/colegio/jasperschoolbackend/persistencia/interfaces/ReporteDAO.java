/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.FilaIngreso;
import com.usac.colegio.jasperschoolbackend.modelo.FilaMora;
import com.usac.colegio.jasperschoolbackend.modelo.FilaPago;
import java.time.LocalDate;
import java.util.List;
/**
 *
 * @author eduar
 */
public interface ReporteDAO {
    //CU100. desde y hasta filtran por fecha de prestamo; null = sin limite
    List<FilaMora> prestamosEnMora(LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia;
    
    //CU099. Solo pagos vigentes, por concepto; siempre devuelve los cuatro conceptos
    List<FilaIngreso> ingresos(LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia;
    
    //Nombre del estudiante, o null si no existe
    String nombreEstudiante(int idEstudiante) throws ExcepcionPersistencia;

    //CU098. Pagos vigentes del estudiante. pagina/tamano null = todos (para el PDF)
    List<FilaPago> historialPagos(int idEstudiante, LocalDate desde, LocalDate hasta, Integer pagina, Integer tamano)
            throws ExcepcionPersistencia;

    //{cantidad, monto} de los pagos que cumplen el filtro
    Object[] totalPagos(int idEstudiante, LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia;
}
