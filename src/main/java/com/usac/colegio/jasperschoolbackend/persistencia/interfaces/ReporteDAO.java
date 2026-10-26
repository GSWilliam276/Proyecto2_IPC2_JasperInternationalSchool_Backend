/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.FilaMora;
import java.time.LocalDate;
import java.util.List;
/**
 *
 * @author eduar
 */
public interface ReporteDAO {
    //CU100. desde y hasta filtran por fecha de prestamo; null = sin limite
    List<FilaMora> prestamosEnMora(LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia;
}
