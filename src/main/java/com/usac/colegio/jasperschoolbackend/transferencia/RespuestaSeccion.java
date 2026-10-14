/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaSeccion {
    private int idSeccion;
    private String nombre;
    private int idGrado;
    private String gradoNombre;
    private String nivel;
    private Integer idCarrera;
    private String carreraNombre;
    private int idAnioLectivo;
    private String anioNombre;
    private String anioEstado;
    private int cupoMaximo;
    private int inscritos;
    private String estado;

    public RespuestaSeccion() {
    }

    public RespuestaSeccion(int idSeccion, String nombre, int idGrado, String gradoNombre, String nivel,
                            Integer idCarrera, String carreraNombre, int idAnioLectivo, String anioNombre,
                            String anioEstado, int cupoMaximo, int inscritos, String estado) {
        this.idSeccion = idSeccion;
        this.nombre = nombre;
        this.idGrado = idGrado;
        this.gradoNombre = gradoNombre;
        this.nivel = nivel;
        this.idCarrera = idCarrera;
        this.carreraNombre = carreraNombre;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.anioEstado = anioEstado;
        this.cupoMaximo = cupoMaximo;
        this.inscritos = inscritos;
        this.estado = estado;
    }

    public int getIdSeccion() { return idSeccion; }
    public String getNombre() { return nombre; }
    public int getIdGrado() { return idGrado; }
    public String getGradoNombre() { return gradoNombre; }
    public String getNivel() { return nivel; }
    public Integer getIdCarrera() { return idCarrera; }
    public String getCarreraNombre() { return carreraNombre; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public String getAnioEstado() { return anioEstado; }
    public int getCupoMaximo() { return cupoMaximo; }
    public int getInscritos() { return inscritos; }
    public String getEstado() { return estado; }
}
