/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoTarifa;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionTarifaDuplicada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionTarifaNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Tarifa;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface TarifaDAO {
    //Crea la tarifa en el año lectivo ACTIVO
    Tarifa crear(int idGrado, ConceptoTarifa concepto, BigDecimal monto)
            throws ExcepcionReferenciaInvalida, ExcepcionTarifaDuplicada, ExcepcionPersistencia;

    //Solo cambia el monto. Los pagos ya registrados conservan el monto con el que se cobraron
    void editar(int idTarifa, BigDecimal monto)
            throws ExcepcionTarifaNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    Optional<Tarifa> buscarPorId(int idTarifa) throws ExcepcionPersistencia;

    //idGrado es filtro opcional. Sin "todos", solo se ven las del año lectivo activo
    List<Tarifa> listar(int pagina, int tamanoPagina, Integer idGrado, boolean todos) throws ExcepcionPersistencia;
}
