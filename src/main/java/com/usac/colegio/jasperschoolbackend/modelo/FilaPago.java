/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import java.math.BigDecimal;
/**
 *
 * @author eduar
 */
public record FilaPago(String fecha, String detalle, BigDecimal monto) {
}
