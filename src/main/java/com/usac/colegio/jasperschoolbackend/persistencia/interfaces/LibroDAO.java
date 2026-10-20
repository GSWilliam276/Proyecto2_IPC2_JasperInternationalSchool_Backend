/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionLibroNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Libro;
import java.util.List;
/**
 *
 * @author eduar
 */
public interface LibroDAO extends DAOGenerico<Libro> {
    void activar(int idLibro) throws ExcepcionLibroNoEncontrado, ExcepcionPersistencia;

    //No se permite si el libro tiene prestamos sin devolver
    void desactivar(int idLibro) throws ExcepcionLibroNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //soloDisponibles = true es la vista del Estudiante: solo libros activos con ejemplares disponibles
    List<Libro> listarFiltrado(int pagina, int tamanoPagina, String busqueda, boolean soloDisponibles)
            throws ExcepcionPersistencia;
    
    void guardarImagen(int idLibro, String nombreArchivo) throws ExcepcionLibroNoEncontrado, ExcepcionPersistencia;
}
