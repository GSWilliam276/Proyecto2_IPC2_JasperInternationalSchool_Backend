/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.NotaCurso;
import com.usac.colegio.jasperschoolbackend.modelo.PagoPropio;
import com.usac.colegio.jasperschoolbackend.modelo.PerfilEstudiante;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface PanelEstudianteDAO {
    Optional<PerfilEstudiante> perfil(int idEstudiante) throws ExcepcionPersistencia;

    //Notas de los cursos de su seccion en el año lectivo activo
    List<NotaCurso> notas(int idEstudiante) throws ExcepcionPersistencia;

    List<PagoPropio> pagos(int idEstudiante) throws ExcepcionPersistencia;
}
