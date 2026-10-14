/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ParametrosBiblioteca;
import java.math.BigDecimal;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface ParametrosBibliotecaDAO {
    Optional<ParametrosBiblioteca> consultar() throws ExcepcionPersistencia;
    void actualizar(int diasPrestamo, BigDecimal multaPorDia) throws ExcepcionPersistencia;
}
