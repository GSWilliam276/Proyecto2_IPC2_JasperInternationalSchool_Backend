/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.FilaMora;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ReporteDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
public class ReporteDAOPersistencia implements ReporteDAO {
    //Consultas de los reportes. Solo lectura
    @Override
    public List<FilaMora> prestamosEnMora(LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(
                "SELECT l.titulo, u.nombre, "
                + "CASE WHEN e.id_usuario IS NOT NULL THEN 'Estudiante' ELSE 'Maestro' END AS tipo, "
                + "p.fecha_prestamo, p.fecha_esperada, DATEDIFF(?, p.fecha_esperada) AS dias, "
                + "CASE WHEN e.id_usuario IS NOT NULL THEN DATEDIFF(?, p.fecha_esperada) * pb.multa_por_dia "
                + "ELSE 0 END AS recargo "
                + "FROM prestamo p JOIN libro l ON l.id_libro = p.id_libro "
                + "JOIN usuario u ON u.id_usuario = p.id_prestatario "
                + "LEFT JOIN estudiante e ON e.id_usuario = p.id_prestatario "
                + "JOIN parametros_biblioteca pb ON pb.id_parametros = 1 "
                + "WHERE p.fecha_devolucion IS NULL AND p.fecha_esperada < ? ");
        Date hoy = Date.valueOf(LocalDate.now());
        List<Object> parametros = new ArrayList<>();
        parametros.add(hoy);
        parametros.add(hoy);
        parametros.add(hoy);
        if (desde != null) {
            sql.append("AND p.fecha_prestamo >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND p.fecha_prestamo <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append("ORDER BY dias DESC, u.nombre, p.id_prestamo");

        List<FilaMora> filas = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean maestro = "Maestro".equals(rs.getString("tipo"));
                    filas.add(new FilaMora(rs.getString("titulo"), rs.getString("nombre"), rs.getString("tipo"),
                            rs.getDate("fecha_prestamo").toString(), rs.getDate("fecha_esperada").toString(),
                            maestro ? null : rs.getInt("dias"), maestro ? null : rs.getBigDecimal("recargo")));                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar los préstamos en mora", e);
        }
        return filas;
    }
}
