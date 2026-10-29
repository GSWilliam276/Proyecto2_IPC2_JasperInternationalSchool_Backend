/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import com.usac.colegio.jasperschoolbackend.modelo.FilaSolvencia;
import java.util.List;

/**
 *
 * @author eduar
 */
public record RespuestaSolventes(int idAnio, String anio, boolean saldoDisponible, List<FilaSolvencia> filas,
                                 long totalFilas, int pagina, int tamano) {
}
