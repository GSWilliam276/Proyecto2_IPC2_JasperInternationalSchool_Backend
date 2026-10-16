/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.enums.EstadoEstudiante;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Estudiante;
import com.usac.colegio.jasperschoolbackend.modelo.Graduacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface EstudianteDAO {
    //Registra al estudiante SIN correo ni contraseña: el acceso lo crea la Secretaria al inscribirlo
    Estudiante registrar(Estudiante estudiante) throws ExcepcionCuiDuplicado, ExcepcionPersistencia;

    //Cambia datos personales, medicos y de encargados. El CUI, la fecha de nacimiento y el estado no cambian
    void editar(Estudiante estudiante)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //Reactiva a un estudiante INACTIVO
    void activar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //Desactiva a un estudiante ACTIVO, sin tocar su historial
    void desactivar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    //Egreso: gradua de Diversificado a un estudiante ACTIVO. Queda cerrado y el nivel entra al historial
    void graduar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    
    //Registra que el estudiante termino un nivel (pre-primaria, primaria o basico) sin cambiar su estado
    //Se permite a estudiantes ACTIVOS o INACTIVOS, pero no a un graduado
    void registrarGraduacionNivel(int idUsuario, Nivel nivel, LocalDate fecha)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;

    List<Graduacion> listarGraduaciones(int idUsuario) throws ExcepcionPersistencia;

    Optional<Estudiante> buscarPorId(int idUsuario) throws ExcepcionPersistencia;

    //estado es un filtro opcional (null = todos)
    List<Estudiante> listar(int pagina, int tamanoPagina, String busqueda, EstadoEstudiante estado)
            throws ExcepcionPersistencia;
}
