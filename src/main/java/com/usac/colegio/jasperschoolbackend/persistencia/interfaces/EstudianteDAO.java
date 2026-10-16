/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.enums.EstadoEstudiante;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Estudiante;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface EstudianteDAO {
    //Registra al estudiante SIN correo ni contraseña: el acceso lo crea la Secretaria al inscribirloS
    Estudiante registrar(Estudiante estudiante) throws ExcepcionCuiDuplicado, ExcepcionPersistencia;

    //Cambia datos personales, medicos y de encargados. El CUI y el estado no cambian
    void editar(Estudiante estudiante)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //Reactiva a un estudiante INACTIVO
    void activar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //Desactiva a un estudiante ACTIVO, sin tocar su historial
    void desactivar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //Gradua a un estudiante ACTIVO. Un graduado queda cerrado
    void graduar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    Optional<Estudiante> buscarPorId(int idUsuario) throws ExcepcionPersistencia;

    //estado es un filtro opcional (null = todos)
    List<Estudiante> listar(int pagina, int tamanoPagina, String busqueda, EstadoEstudiante estado)
            throws ExcepcionPersistencia;
}
