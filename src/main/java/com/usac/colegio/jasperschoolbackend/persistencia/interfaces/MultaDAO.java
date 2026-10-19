/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Multa;
import java.util.List;
/**
 *
 * @author eduar
 */
public interface MultaDAO {
    //CU078. estado: null, PENDIENTE o PAGADA. idEstudiante es opcional
    List<Multa> listar(int pagina, int tamanoPagina, String estado, Integer idEstudiante)
            throws ExcepcionPersistencia;
}
