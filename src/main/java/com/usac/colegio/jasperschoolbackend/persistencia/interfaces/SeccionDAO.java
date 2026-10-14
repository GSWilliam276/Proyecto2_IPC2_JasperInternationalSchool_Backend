/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Seccion;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface SeccionDAO {
    //Crea la seccion en el año lectivo ACTIVO. idCarrera es null salvo en Diversificado
    Seccion crear(String nombre, int idGrado, Integer idCarrera, int cupoMaximo)
            throws ExcepcionReferenciaInvalida, ExcepcionNombreDuplicado, ExcepcionPersistencia;

    //Solo cambian nombre y cupo: grado, año y carrera quedan fijos
    void editar(int idSeccion, String nombre, int cupoMaximo)
            throws ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionNombreDuplicado, ExcepcionPersistencia;

    void activar(int idSeccion) throws ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    void desactivar(int idSeccion) throws ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    Optional<Seccion> buscarPorId(int idSeccion) throws ExcepcionPersistencia;

    /**
     * idAnioLectivo y idGrado son filtros opcionales (null = sin filtro)
     * Sin idAnioLectivo, soloAnioActivo decide si se ve solo el año activo o todos los años
     */
    List<Seccion> listar(int pagina, int tamanoPagina, String busqueda, Integer idGrado,
                         Integer idAnioLectivo, boolean soloAnioActivo) throws ExcepcionPersistencia;
}
