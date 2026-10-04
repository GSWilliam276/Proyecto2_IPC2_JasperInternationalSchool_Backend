/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.CodigoRecuperacion;
import java.time.LocalDateTime;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface CodigoRecuperacionDAO {
    void generar(int idUsuario, String codigo, LocalDateTime expiracion) throws ExcepcionPersistencia;
    Optional<CodigoRecuperacion> buscarVigente(int idUsuario, String codigo) throws ExcepcionPersistencia;
    void marcarComoUsado(int idCodigo) throws ExcepcionPersistencia;
}
