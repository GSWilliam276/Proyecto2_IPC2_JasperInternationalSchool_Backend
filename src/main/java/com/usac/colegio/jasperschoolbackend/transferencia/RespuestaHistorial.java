/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

import com.usac.colegio.jasperschoolbackend.modelo.FilaPago;
import java.math.BigDecimal;
import java.util.List;
/**
 *
 * @author eduar
 */
public record RespuestaHistorial
        //Historial paginado. solvente/saldo son null si no se pudo calcular
        (String estudiante, List<FilaPago> filas, long totalFilas, int pagina, int tamano,
                                 BigDecimal montoTotal, Boolean solvente, BigDecimal saldo,
                                 boolean inscripcionPendiente, List<Integer> mesesPendientes, String avisoSaldo) {
}
