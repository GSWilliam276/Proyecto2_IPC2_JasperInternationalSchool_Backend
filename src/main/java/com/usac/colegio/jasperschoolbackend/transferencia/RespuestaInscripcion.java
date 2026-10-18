/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaInscripcion {
    private int idInscripcion;
    private int idEstudiante;
    private String estudianteNombre;
    private String estudianteCui;
    private int idSeccion;
    private String seccionNombre;
    private int idGrado;
    private String gradoNombre;
    private String nivel;
    private String carreraNombre;
    private int idAnioLectivo;
    private String anioNombre;
    private String anioEstado;
    private String fecha;
    private String estado;
    private String contrasenaProvisional;  //Solo al inscribir con acceso nuevo; null en los listados

    public RespuestaInscripcion() {
    }

    public RespuestaInscripcion(int idInscripcion, int idEstudiante, String estudianteNombre, String estudianteCui,
                                int idSeccion, String seccionNombre, int idGrado, String gradoNombre, String nivel,
                                String carreraNombre, int idAnioLectivo, String anioNombre, String anioEstado,
                                String fecha, String estado, String contrasenaProvisional) {
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
        this.anioEstado = anioEstado;
        this.fecha = fecha;
        this.estado = estado;
        this.contrasenaProvisional = contrasenaProvisional;
    }

    public int getIdInscripcion() { return idInscripcion; }
    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public String getEstudianteCui() { return estudianteCui; }
    public int getIdSeccion() { return idSeccion; }
    public String getSeccionNombre() { return seccionNombre; }
    public int getIdGrado() { return idGrado; }
    public String getGradoNombre() { return gradoNombre; }
    public String getNivel() { return nivel; }
    public String getCarreraNombre() { return carreraNombre; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public String getAnioEstado() { return anioEstado; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public String getContrasenaProvisional() { return contrasenaProvisional; }
}
