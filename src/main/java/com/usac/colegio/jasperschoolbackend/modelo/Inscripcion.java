/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.enums.EstadoInscripcion;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import java.time.LocalDate;
/**
 *
 * @author eduar
 */
public class Inscripcion {
    //Inscripcion de un estudiante en una seccion. Modelo de lectura: ids y los nombres que muestra la pantalla
    private final int idInscripcion;
    private final int idEstudiante;
    private final String estudianteNombre;
    private final String estudianteCui;
    private final int idSeccion;
    private final String seccionNombre;
    private final int idGrado;
    private final String gradoNombre;
    private final Nivel nivel;
    private final String carreraNombre;   //Null salvo en Diversificado
    private final int idAnioLectivo;
    private final String anioNombre;
    private final EstadoAnioLectivo estadoAnio;
    private final LocalDate fecha;
    private final EstadoInscripcion estado;

    public Inscripcion(int idInscripcion, int idEstudiante, String estudianteNombre, String estudianteCui,
                       int idSeccion, String seccionNombre, int idGrado, String gradoNombre, Nivel nivel,
                       String carreraNombre, int idAnioLectivo, String anioNombre, EstadoAnioLectivo estadoAnio,
                       LocalDate fecha, EstadoInscripcion estado) {
        this.idInscripcion = idInscripcion;
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.estudianteCui = estudianteCui;
        this.idSeccion = idSeccion;
        this.seccionNombre = seccionNombre;
        this.idGrado = idGrado;
        this.gradoNombre = gradoNombre;
        this.nivel = nivel;
        this.carreraNombre = carreraNombre;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.estadoAnio = estadoAnio;
        this.fecha = fecha;
        this.estado = estado;
    }

    public int getIdInscripcion() { return idInscripcion; }
    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public String getEstudianteCui() { return estudianteCui; }
    public int getIdSeccion() { return idSeccion; }
    public String getSeccionNombre() { return seccionNombre; }
    public int getIdGrado() { return idGrado; }
    public String getGradoNombre() { return gradoNombre; }
    public Nivel getNivel() { return nivel; }
    public String getCarreraNombre() { return carreraNombre; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public EstadoAnioLectivo getEstadoAnio() { return estadoAnio; }
    public LocalDate getFecha() { return fecha; }
    public EstadoInscripcion getEstado() { return estado; }
}
