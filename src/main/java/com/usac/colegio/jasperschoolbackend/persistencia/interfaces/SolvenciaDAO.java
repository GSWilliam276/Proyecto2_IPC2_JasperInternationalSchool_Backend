/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Saldo;
import com.usac.colegio.jasperschoolbackend.modelo.Solvencia;
import java.util.List;
/**
 *
 * @author eduar
 */
public interface SolvenciaDAO {
    //Lo que debe el estudiante en el año activo: inscripcion y mensualidades vencidas sin pagar
    Saldo consultarSaldo(int idEstudiante)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia;

    //CU069. Solo si no hay saldo; si lo hay, rechaza e indica el monto
    Solvencia emitir(int idEstudiante, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia;

    List<Solvencia> listar(int pagina, int tamanoPagina, Integer idEstudiante) throws ExcepcionPersistencia;
}
