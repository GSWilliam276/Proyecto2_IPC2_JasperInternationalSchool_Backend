/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Multa;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.MultaDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
public class MultaDAOPersistencia implements MultaDAO {
    //Consulta de multas. El idPago es el del pago VIGENTE que la cubre, para poder emitir su comprobante
    @Override
    public List<Multa> listar(int pagina, int tamanoPagina, String estado, Integer idEstudiante)
            throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(
                "SELECT mu.id_multa, mu.id_prestamo, l.titulo AS libro_titulo, p.id_prestatario, "
                + "u.nombre AS estudiante_nombre, mu.dias_atraso, mu.monto, mu.estado, mu.fecha_generacion, "
                + "pg.id_pago "
                + "FROM multa mu "
                + "JOIN prestamo p ON p.id_prestamo = mu.id_prestamo "
                + "JOIN libro l ON l.id_libro = p.id_libro "
                + "JOIN usuario u ON u.id_usuario = p.id_prestatario "
                + "LEFT JOIN pago pg ON pg.id_multa = mu.id_multa AND pg.estado = 'VIGENTE' "
                + "WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (estado != null) {
            sql.append("AND mu.estado = ? ");
            parametros.add(estado);
        }
        if (idEstudiante != null) {
            sql.append("AND p.id_prestatario = ? ");
            parametros.add(idEstudiante);
        }
        sql.append("ORDER BY mu.fecha_generacion DESC, mu.id_multa DESC LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<Multa> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pago = rs.getInt("id_pago");
                    Integer idPago = rs.wasNull() ? null : pago;
                    lista.add(new Multa(rs.getInt("id_multa"), rs.getInt("id_prestamo"), rs.getString("libro_titulo"),
                            rs.getInt("id_prestatario"), rs.getString("estudiante_nombre"), rs.getInt("dias_atraso"),
                            rs.getBigDecimal("monto"), rs.getString("estado"),
                            rs.getDate("fecha_generacion").toLocalDate(), idPago));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar las multas", e);
        }
        return lista;
    }
}
