/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import com.usac.colegio.jasperschoolbackend.modelo.FilaCurso;
import com.usac.colegio.jasperschoolbackend.modelo.FilaMejor;
import java.util.List;
/**
 *
 * @author eduar
 */
public record RespuestaRendimiento(String seccion, String anio, List<FilaCurso> cursos, List<FilaMejor> mejores) {
}
