/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.CodigoRecuperacion;
import com.usac.colegio.jasperschoolbackend.modelo.Usuario;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CodigoRecuperacionDAO;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.UsuarioDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class CodigoRecuperacionDAOPersistencia implements CodigoRecuperacionDAO {
    //Implementacion del acceso a datos de CodigoRecuperacion
    private final UsuarioDAO usuarioDAO = new UsuarioDAOPersistencia();

    @Override
    public void generar(int idUsuario, String codigo, LocalDateTime expiracion) throws ExcepcionPersistencia {
        String sql = "INSERT INTO codigo_recuperacion (id_usuario, codigo, fecha_creacion, fecha_expiracion, fue_usado) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, codigo);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(4, Timestamp.valueOf(expiracion));
            ps.setBoolean(5, false);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al generar el código de recuperación", e);
        }
    }

    @Override
    public Optional<CodigoRecuperacion> buscarVigente(int idUsuario, String codigo) throws ExcepcionPersistencia {
        //vigente = mismo codigo, no usado todavia, y la fecha de expiracion
        //es posterior al momento actual
        String sql = "SELECT * FROM codigo_recuperacion "
                + "WHERE id_usuario = ? AND codigo = ? AND fue_usado = FALSE AND fecha_expiracion > ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, codigo);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = usuarioDAO.buscarPorId(idUsuario)
                            .orElseThrow(() -> new ExcepcionPersistencia("El usuario del código no existe", null));
                    return Optional.of(mapearCodigo(rs, usuario));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el código de recuperación", e);
        }
        return Optional.empty();
    }

    @Override
    public void marcarComoUsado(int idCodigo) throws ExcepcionPersistencia {
        String sql = "UPDATE codigo_recuperacion SET fue_usado = TRUE WHERE id_codigo_recuperacion = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCodigo);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al marcar el código como usado", e);
        }
    }

    private CodigoRecuperacion mapearCodigo(ResultSet rs, Usuario usuario) throws SQLException {
        return new CodigoRecuperacion(
                rs.getInt("id_codigo_recuperacion"),
                rs.getString("codigo"),
                rs.getTimestamp("fecha_creacion").toLocalDateTime(),
                rs.getTimestamp("fecha_expiracion").toLocalDateTime(),
                rs.getBoolean("fue_usado"),
                usuario
        );
    }
}
