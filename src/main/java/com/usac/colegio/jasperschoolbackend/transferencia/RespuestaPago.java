/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public class RespuestaPago {
    private int idPago;
    private int idEstudiante;
    private String estudianteNombre;
    private int idAnioLectivo;
    private String anioNombre;
    private String concepto;
    private BigDecimal monto;
    private Integer mes;
    private String actividadNombre;
    private String fecha;
    private String secretariaNombre;
    private String estado;
    private String motivoAnulacion;

    public RespuestaPago() {
    }

    public RespuestaPago(int idPago, int idEstudiante, String estudianteNombre, int idAnioLectivo, String anioNombre,
                         String concepto, BigDecimal monto, Integer mes, String actividadNombre, String fecha,
                         String secretariaNombre, String estado, String motivoAnulacion) {
        this.idPago = idPago;
        this.idEstudiante = idEstudiante;
        this.estudianteNombre = estudianteNombre;
        this.idAnioLectivo = idAnioLectivo;
        this.anioNombre = anioNombre;
        this.concepto = concepto;
        this.monto = monto;
        this.mes = mes;
        this.actividadNombre = actividadNombre;
        this.fecha = fecha;
        this.secretariaNombre = secretariaNombre;
        this.estado = estado;
        this.motivoAnulacion = motivoAnulacion;
    }

    public int getIdPago() { return idPago; }
    public int getIdEstudiante() { return idEstudiante; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public int getIdAnioLectivo() { return idAnioLectivo; }
    public String getAnioNombre() { return anioNombre; }
    public String getConcepto() { return concepto; }
    public BigDecimal getMonto() { return monto; }
    public Integer getMes() { return mes; }
    public String getActividadNombre() { return actividadNombre; }
    public String getFecha() { return fecha; }
    public String getSecretariaNombre() { return secretariaNombre; }
    public String getEstado() { return estado; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
}
