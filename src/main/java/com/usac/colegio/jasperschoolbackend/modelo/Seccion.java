/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
/**
 *
 * @author eduar
 */
public class Seccion {
    //Seccion de un grado (y de una carrera, en Diversificado) dentro de un año lectivo
    private int idSeccion;
    private String nombre;
    private Grado grado;
    private Carrera carrera;            //null salvo en Diversificado
    private int idAnioLectivo;
    private String anioNombre;
    private EstadoAnioLectivo estadoAnio;
    private int cupoMaximo;
    private EstadoGeneral estado;
    private int inscritos;              //inscripciones ACTIVAS, calculado al leer

    public Seccion(int idSeccion, String nombre, Grado grado, Carrera carrera, int idAnioLectivo,
                   String anioNombre, EstadoAnioLectivo estadoAnio, int cupoMaximo,
                   EstadoGeneral estado, int inscritos) {
        this.idSeccion = idSeccion;
        this.nombre = nombre;
        this.grado = grado;
        this.carrera = carrera;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.estadoAnio = estadoAnio;
        this.cupoMaximo = cupoMaximo;
        this.estado = estado;
        this.inscritos = inscritos;
    }

    public int cuposLibres() {
        return cupoMaximo - inscritos;
    }

    public int getIdSeccion() { return idSeccion; }
    public String getNombre() { return nombre; }
    public Grado getGrado() { return grado; }
    public Carrera getCarrera() { return carrera; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public EstadoAnioLectivo getEstadoAnio() { return estadoAnio; }
    public int getCupoMaximo() { return cupoMaximo; }
    public EstadoGeneral getEstado() { return estado; }
    public int getInscritos() { return inscritos; }
}
