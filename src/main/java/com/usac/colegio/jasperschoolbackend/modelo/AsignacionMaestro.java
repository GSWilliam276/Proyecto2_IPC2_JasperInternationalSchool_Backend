/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
/**
 *
 * @author eduar
 */
public class AsignacionMaestro {
    //Maestro asignado a un curso de una seccion. Modelo de lectura: ids y los nombres que muestra la pantalla
    private final int idAsignacion;
    private final int idSeccion;
    private final String seccionNombre;
    private final int idGrado;
    private final String gradoNombre;
    private final Nivel nivel;
    private final Integer idCarrera;      //null salvo en Diversificado
    private final String carreraNombre;
    private final int idAnioLectivo;
    private final String anioNombre;
    private final EstadoAnioLectivo estadoAnio;
    private final int idCurso;
    private final String cursoNombre;
    private final int idMaestro;          //id_usuario de la cuenta del maestro
    private final String maestroNombre;

    public AsignacionMaestro(int idAsignacion, int idSeccion, String seccionNombre, int idGrado, String gradoNombre,
                             Nivel nivel, Integer idCarrera, String carreraNombre, int idAnioLectivo,
                             String anioNombre, EstadoAnioLectivo estadoAnio, int idCurso, String cursoNombre,
                             int idMaestro, String maestroNombre) {
        this.idAsignacion = idAsignacion;
        this.idSeccion = idSeccion;
        this.seccionNombre = seccionNombre;
        this.idGrado = idGrado;
        this.gradoNombre = gradoNombre;
        this.nivel = nivel;
        this.idCarrera = idCarrera;
        this.carreraNombre = carreraNombre;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.estadoAnio = estadoAnio;
        this.idCurso = idCurso;
        this.cursoNombre = cursoNombre;
        this.idMaestro = idMaestro;
        this.maestroNombre = maestroNombre;
    }

    public int getIdAsignacion() { return idAsignacion; }
    public int getIdSeccion() { return idSeccion; }
    public String getSeccionNombre() { return seccionNombre; }
    public int getIdGrado() { return idGrado; }
    public String getGradoNombre() { return gradoNombre; }
    public Nivel getNivel() { return nivel; }
    public Integer getIdCarrera() { return idCarrera; }
    public String getCarreraNombre() { return carreraNombre; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public EstadoAnioLectivo getEstadoAnio() { return estadoAnio; }
    public int getIdCurso() { return idCurso; }
    public String getCursoNombre() { return cursoNombre; }
    public int getIdMaestro() { return idMaestro; }
    public String getMaestroNombre() { return maestroNombre; }
}
