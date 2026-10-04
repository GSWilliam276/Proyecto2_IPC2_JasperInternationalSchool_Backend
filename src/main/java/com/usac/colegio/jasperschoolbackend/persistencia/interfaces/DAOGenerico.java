/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface DAOGenerico<T> {
    //Contrato base para los DAO que siguen el patron CRUD tipico:
    //crear, editar, buscar por id y listar de forma paginada
    void crear(T objeto) throws ExcepcionPersistencia;
    void editar(T objeto) throws ExcepcionPersistencia;
    Optional<T> buscarPorId(int id) throws ExcepcionPersistencia;
    List<T> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia;
}
