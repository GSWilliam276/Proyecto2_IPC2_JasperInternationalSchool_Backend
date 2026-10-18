/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoComprobante;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPagoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Comprobante;
import com.usac.colegio.jasperschoolbackend.modelo.Pago;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ComprobanteDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class ComprobanteDAOPersistencia implements ComprobanteDAO {
    //Acceso a datos de Comprobante. Emitir bloquea la fila del pago, la misma que bloquea anular
    @Override
    public Comprobante emitir(int idPago)
            throws ExcepcionPagoNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM pago WHERE id_pago = ? FOR UPDATE")) {
                ps.setInt(1, idPago);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionPagoNoEncontrado("No existe un pago con ese id");
                    }
                    if (!"VIGENTE".equals(rs.getString("estado"))) {
                        throw new ExcepcionOperacionNoPermitida("No se emite comprobante de un pago anulado");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM comprobante WHERE id_pago = ?")) {
                ps.setInt(1, idPago);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ExcepcionOperacionNoPermitida("El pago ya tiene comprobante");
                    }
                }
            }
            int id;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO comprobante (id_pago, numero, fecha_emision, estado) VALUES (?, NULL, ?, 'EMITIDO')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idPago);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    id = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE comprobante SET numero = ? WHERE id_comprobante = ?")) {
                ps.setString(1, String.format("C-%06d", id));
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionOperacionNoPermitida("El pago ya tiene comprobante");
            }
            throw new ExcepcionPersistencia("Error al emitir el comprobante", e);
        } catch (ExcepcionPagoNoEncontrado | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        return buscarPorPago(idPago)
                .orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el comprobante creado", null));
    }

    @Override
    public Optional<Comprobante> buscarPorPago(int idPago) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_comprobante, numero, fecha_emision, estado FROM comprobante WHERE id_pago = ?")) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Optional<Pago> pago = new PagoDAOPersistencia().buscarPorId(idPago);
                if (pago.isEmpty()) {
                    return Optional.empty();
                }
                return Optional.of(new Comprobante(rs.getInt("id_comprobante"), rs.getString("numero"),
                        rs.getTimestamp("fecha_emision").toLocalDateTime(),
                        EstadoComprobante.valueOf(rs.getString("estado")), pago.get()));
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el comprobante", e);
        }
    }

    private void deshacer(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignorada) {
                //Si el rollback falla, la conexion se cierra de todos modos
            }
        }
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignorada) {
                //Nada que hacer: la conexion ya no se usa
            }
        }
    }
}
