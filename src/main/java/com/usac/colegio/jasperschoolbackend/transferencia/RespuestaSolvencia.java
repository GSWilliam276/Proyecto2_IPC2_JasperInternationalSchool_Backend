/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaSolvencia {
    private int idSolvencia;
    private int idEstudiante;
    private String estudianteNombre;
    private String anioNombre;
    private String secretariaNombre;
    private String fechaEmision;

    public RespuestaSolvencia() {
    }

    public RespuestaSolvencia(int idSolvencia, int idEstudiante, String estudianteNombre, String anioNombre,
                              String secretariaNombre, String fechaEmision) {
        this.idSolvencia = idSolvencia;
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.anioNombre = anioNombre;
        this.secretariaNombre = secretariaNombre;
        this.fechaEmision = fechaEmision;
    }

    public int getIdSolvencia() { return idSolvencia; }
    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public String getAnioNombre() { return anioNombre; }
    public String getSecretariaNombre() { return secretariaNombre; }
    public String getFechaEmision() { return fechaEmision; }
}
