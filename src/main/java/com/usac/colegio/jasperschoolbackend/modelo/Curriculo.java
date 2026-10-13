/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
public class Curriculo {
    //Curriculo: la lista de cursos que un grado imparte. En Diversificado hay uno por carrera
    private int idCurriculo;
    private Grado grado;
    private Carrera carrera;        //null salvo en Diversificado
    private List<Curso> cursos = new ArrayList<>();

    public Curriculo(int idCurriculo, Grado grado, Carrera carrera) {
        this.idCurriculo = idCurriculo;
        this.grado = grado;
        this.carrera = carrera;
    }

    public int getIdCurriculo() { return idCurriculo; }
    public void setIdCurriculo(int idCurriculo) { this.idCurriculo = idCurriculo; }
    public Grado getGrado() { return grado; }
    public Carrera getCarrera() { return carrera; }
    public List<Curso> getCursos() { return cursos; }
    public void setCursos(List<Curso> cursos) { this.cursos = cursos; }
}
