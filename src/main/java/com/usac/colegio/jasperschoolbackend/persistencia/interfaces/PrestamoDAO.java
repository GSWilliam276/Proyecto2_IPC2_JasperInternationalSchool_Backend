/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionLibroNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPrestamoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Prestamo;
import com.usac.colegio.jasperschoolbackend.modelo.Prestatario;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface PrestamoDAO {
    //CU075. Valida libro, prestatario y mora; descuenta un ejemplar, todo en una transaccion
    Prestamo registrar(int idLibro, int idPrestatario, int idBibliotecario)
            throws ExcepcionLibroNoEncontrado, ExcepcionReferenciaInvalida, ExcepcionOperacionNoPermitida,
            ExcepcionPersistencia;

    //CU076. Repone el ejemplar y, si un estudiante devuelve tarde, genera la multa pendiente
    Prestamo devolver(int idPrestamo)
            throws ExcepcionPrestamoNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    Optional<Prestamo> buscarPorId(int idPrestamo) throws ExcepcionPersistencia;

    //CU077. estado: null, PRESTADO (sin devolver), DEVUELTO o EN_MORA. Los demas filtros son opcionales
    List<Prestamo> listar(int pagina, int tamanoPagina, String estado, Integer idLibro, Integer idPrestatario,
                          LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia;

    //Estudiantes y maestros activos, para elegir a quien prestar
    List<Prestatario> listarPrestatarios(String busqueda) throws ExcepcionPersistencia;
}
