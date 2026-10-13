/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCurriculoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCurriculoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Curriculo;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface CurriculoDAO {
    //idCarrera es null salvo para un grado de Diversificado 
    Curriculo crear(int idGrado, Integer idCarrera) throws ExcepcionReferenciaInvalida, ExcepcionCurriculoDuplicado, ExcepcionPersistencia;

    Optional<Curriculo> buscarPorId(int idCurriculo) throws ExcepcionPersistencia;

    List<Curriculo> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia;

    void agregarCurso(int idCurriculo, int idCurso)
            throws ExcepcionCurriculoNoEncontrado, ExcepcionReferenciaInvalida, ExcepcionCurriculoDuplicado, ExcepcionPersistencia;

    void quitarCurso(int idCurriculo, int idCurso)
            throws ExcepcionCurriculoNoEncontrado, ExcepcionReferenciaInvalida, ExcepcionPersistencia;
}
