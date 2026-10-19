/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoPago;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionActividadNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionMultaNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPagoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Pago;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public interface PagoDAO {
    //CU062. El monto se copia de la tarifa de inscripcion del grado, en el año activo
    Pago registrarInscripcion(int idEstudiante, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia;

    //CU063. Uno o varios meses en una sola transaccion: si alguno ya esta pagado, no se guarda ninguno
    List<Pago> registrarMensualidades(int idEstudiante, List<Integer> meses, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia;

    Optional<Pago> buscarPorId(int idPago) throws ExcepcionPersistencia;

    //CU066. Filtros opcionales (null = sin filtro). Incluye los anulados, con su estado
    List<Pago> listar(int pagina, int tamanoPagina, Integer idEstudiante, ConceptoPago concepto,
                      LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia;
    
    //CU065. Cobra una multa PENDIENTE en el año activo y la marca PAGADA, en una sola transaccion
    Pago registrarMulta(int idMulta, int idSecretaria)
            throws ExcepcionMultaNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia;
    
    //CU064. Pagar es inscribirse: valida actividad activa y vigente, cupo y que no este ya pagada
    Pago registrarActividad(int idEstudiante, int idActividad, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionActividadNoEncontrada, ExcepcionOperacionNoPermitida,
            ExcepcionReferenciaInvalida, ExcepcionPersistencia;

    //CU067. Marca el pago como ANULADO con su motivo, sin eliminarlo. Anula tambien su comprobante
    void anular(int idPago, String motivo)
            throws ExcepcionPagoNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia;
}
