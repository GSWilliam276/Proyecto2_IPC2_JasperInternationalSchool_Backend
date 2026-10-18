/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPagoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Comprobante;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface ComprobanteDAO {
    //CU068. Un comprobante por pago vigente
    Comprobante emitir(int idPago)
            throws ExcepcionPagoNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    Optional<Comprobante> buscarPorPago(int idPago) throws ExcepcionPersistencia;
}
