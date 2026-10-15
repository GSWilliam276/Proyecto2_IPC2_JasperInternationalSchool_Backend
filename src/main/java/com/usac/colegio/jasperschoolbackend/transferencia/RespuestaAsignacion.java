/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaAsignacion {
    private int idAsignacion;
    private int idSeccion;
    private String seccionNombre;
    private int idGrado;
    private String gradoNombre;
    private String nivel;
    private Integer idCarrera;
    private String carreraNombre;
    private int idAnioLectivo;
    private String anioNombre;
    private String anioEstado;
    private int idCurso;
    private String cursoNombre;
    private int idMaestro;
    private String maestroNombre;

    public RespuestaAsignacion() {
    }

    public RespuestaAsignacion(int idAsignacion, int idSeccion, String seccionNombre, int idGrado, String gradoNombre,
                               String nivel, Integer idCarrera, String carreraNombre, int idAnioLectivo,
                               String anioNombre, String anioEstado, int idCurso, String cursoNombre,
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
        this.anioEstado = anioEstado;
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
    public String getNivel() { return nivel; }
    public Integer getIdCarrera() { return idCarrera; }
    public String getCarreraNombre() { return carreraNombre; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public String getAnioEstado() { return anioEstado; }
    public int getIdCurso() { return idCurso; }
    public String getCursoNombre() { return cursoNombre; }
    public int getIdMaestro() { return idMaestro; }
    public String getMaestroNombre() { return maestroNombre; }
}
