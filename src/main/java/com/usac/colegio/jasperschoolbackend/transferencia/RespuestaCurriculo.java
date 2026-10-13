/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.util.List;
/**
 *
 * @author eduar
 */
public class RespuestaCurriculo {
    //Lo que el backend devuelve de un curriculo. En el listado, "cursos" viene en null:
    //la lista de cursos se pide al abrir un curriculo
    private int idCurriculo;
    private int idGrado;
    private String gradoNombre;
    private String nivel;
    private Integer idCarrera;
    private String carreraNombre;
    private List<RespuestaCurso> cursos;

    public RespuestaCurriculo() {
    }

    public RespuestaCurriculo(int idCurriculo, int idGrado, String gradoNombre, String nivel,
                              Integer idCarrera, String carreraNombre, List<RespuestaCurso> cursos) {
        this.idCurriculo = idCurriculo;
        this.idGrado = idGrado;
        this.gradoNombre = gradoNombre;
        this.nivel = nivel;
        this.idCarrera = idCarrera;
        this.carreraNombre = carreraNombre;
        this.cursos = cursos;
    }

    public int getIdCurriculo() { return idCurriculo; }
    public int getIdGrado() { return idGrado; }
    public String getGradoNombre() { return gradoNombre; }
    public String getNivel() { return nivel; }
    public Integer getIdCarrera() { return idCarrera; }
    public String getCarreraNombre() { return carreraNombre; }
    public List<RespuestaCurso> getCursos() { return cursos; }
}
