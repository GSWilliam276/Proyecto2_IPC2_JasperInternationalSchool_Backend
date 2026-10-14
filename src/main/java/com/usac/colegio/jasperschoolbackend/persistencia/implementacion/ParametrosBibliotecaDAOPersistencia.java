/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ParametrosBiblioteca;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ParametrosBibliotecaDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class ParametrosBibliotecaDAOPersistencia implements ParametrosBibliotecaDAO {
    //Acceso a datos de los parametros de biblioteca
    @Override
    public Optional<ParametrosBiblioteca> consultar() throws ExcepcionPersistencia {
        String sql = "SELECT dias_prestamo, multa_por_dia FROM parametros_biblioteca WHERE id_parametros = 1";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new ParametrosBiblioteca(rs.getInt("dias_prestamo"), rs.getBigDecimal("multa_por_dia")));
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar los parámetros de biblioteca", e);
        }
        return Optional.empty();
    }

    //Si la fila 1 no existe la crea, y si existe la actualiza
    //Asi el sistema se recupera solo aunque alguien haya borrado la fila a mano
    @Override
    public void actualizar(int diasPrestamo, BigDecimal multaPorDia) throws ExcepcionPersistencia {
        String sql = "INSERT INTO parametros_biblioteca (id_parametros, dias_prestamo, multa_por_dia) VALUES (1, ?, ?) "
                + "ON DUPLICATE KEY UPDATE dias_prestamo = ?, multa_por_dia = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, diasPrestamo);
            ps.setBigDecimal(2, multaPorDia);
            ps.setInt(3, diasPrestamo);
            ps.setBigDecimal(4, multaPorDia);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al actualizar los parámetros de biblioteca", e);
        }
    }
}
