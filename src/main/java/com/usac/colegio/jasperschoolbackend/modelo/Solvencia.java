/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.time.LocalDateTime;
/**
 *
 * @author eduar
 */
public class Solvencia {
    //Solvencia emitida. Se guarda cada emision
    private final int idSolvencia;
    private final int idEstudiante;
    private final String estudianteNombre;
    private final String anioNombre;
    private final String secretariaNombre;
    private final LocalDateTime fechaEmision;
    private boolean yaExistia;

    public Solvencia(int idSolvencia, int idEstudiante, String estudianteNombre, String anioNombre,
                     String secretariaNombre, LocalDateTime fechaEmision) {
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
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public boolean isYaExistia() { return yaExistia; }
    public void setYaExistia(boolean yaExistia) { this.yaExistia = yaExistia; }
}
