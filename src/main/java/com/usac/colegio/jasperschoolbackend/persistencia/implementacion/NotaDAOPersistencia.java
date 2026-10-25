/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.RegistroNota;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.NotaDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author eduar
 */
public class NotaDAOPersistencia implements NotaDAO{
    //Notas de zona y examen final. Guardar bloquea la asignacion (FOR UPDATE)
    private static final BigDecimal MAXIMO_FINAL = new BigDecimal("30");

    @Override
    public List<RegistroNota> planillaZona(int idAsignacion, int idActividadZona)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion()) {
            punteoDeActividad(con, idAsignacion, idActividadZona);
            String sql = "SELECT u.id_usuario, u.nombre, n.valor FROM asignacion_maestro am "
                    + "JOIN inscripcion i ON i.id_seccion = am.id_seccion AND i.estado = 'ACTIVA' "
                    + "JOIN usuario u ON u.id_usuario = i.id_estudiante "
                    + "LEFT JOIN nota n ON n.id_actividad_zona = ? AND n.id_estudiante = i.id_estudiante "
                    + "WHERE am.id_asignacion = ? ORDER BY u.nombre, u.id_usuario";
            return leer(con, sql, idActividadZona, idAsignacion);
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar las notas", e);
        }
    }

    @Override
    public void guardarZona(int idAsignacion, int idActividadZona, Map<Integer, BigDecimal> notas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);
            bloquear(con, idAsignacion);
            BigDecimal punteo = punteoDeActividad(con, idAsignacion, idActividadZona);
            validar(con, idAsignacion, notas, punteo);
            String sql = "INSERT INTO nota (id_actividad_zona, id_estudiante, valor) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE valor = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Map.Entry<Integer, BigDecimal> n : notas.entrySet()) {
                    ps.setInt(1, idActividadZona);
                    ps.setInt(2, n.getKey());
                    ps.setBigDecimal(3, n.getValue());
                    ps.setBigDecimal(4, n.getValue());
                    ps.executeUpdate();
                }
            }
            con.commit();
        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al guardar las notas", e);
        } catch (ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public List<RegistroNota> planillaFinal(int idAsignacion) throws ExcepcionPersistencia {
        String sql = "SELECT u.id_usuario, u.nombre, n.valor FROM asignacion_maestro am "
                + "JOIN inscripcion i ON i.id_seccion = am.id_seccion AND i.estado = 'ACTIVA' "
                + "JOIN usuario u ON u.id_usuario = i.id_estudiante "
                + "LEFT JOIN nota_final n ON n.id_asignacion = am.id_asignacion AND n.id_estudiante = i.id_estudiante "
                + "WHERE am.id_asignacion = ? AND ? = ? ORDER BY u.nombre, u.id_usuario";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion()) {
            return leer(con, sql, idAsignacion, 1, 1);
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar el examen final", e);
        }
    }

    @Override
    public void guardarFinal(int idAsignacion, Map<Integer, BigDecimal> notas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);
            bloquear(con, idAsignacion);
            validar(con, idAsignacion, notas, MAXIMO_FINAL);
            String sql = "INSERT INTO nota_final (id_asignacion, id_estudiante, valor) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE valor = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Map.Entry<Integer, BigDecimal> n : notas.entrySet()) {
                    ps.setInt(1, idAsignacion);
                    ps.setInt(2, n.getKey());
                    ps.setBigDecimal(3, n.getValue());
                    ps.setBigDecimal(4, n.getValue());
                    ps.executeUpdate();
                }
            }
            con.commit();
        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al guardar el examen final", e);
        } catch (ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    //Metodos de Apoyo 

    private List<RegistroNota> leer(Connection con, String sql, int p1, int p2) throws SQLException {
        return leer(con, sql, p1, p2, null);
    }

    private List<RegistroNota> leer(Connection con, String sql, int p1, int p2, Integer p3) throws SQLException {
        List<RegistroNota> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p1);
            ps.setInt(2, p2);
            if (p3 != null) {
                ps.setInt(3, p3);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RegistroNota(rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)));
                }
            }
        }
        return lista;
    }

    private void bloquear(Connection con, int idAsignacion) throws SQLException, ExcepcionOperacionNoPermitida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM asignacion_maestro WHERE id_asignacion = ? FOR UPDATE")) {
            ps.setInt(1, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionOperacionNoPermitida("No existe la asignación");
                }
            }
        }
    }

    private BigDecimal punteoDeActividad(Connection con, int idAsignacion, int idActividadZona)
            throws SQLException, ExcepcionOperacionNoPermitida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT punteo FROM actividad_zona WHERE id_actividad_zona = ? AND id_asignacion = ?")) {
            ps.setInt(1, idActividadZona);
            ps.setInt(2, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionOperacionNoPermitida("No existe esa actividad en este curso");
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    //Cada estudiante debe estar inscrito (activo) en la seccion y cada valor entre 0 y el maximo
    private void validar(Connection con, int idAsignacion, Map<Integer, BigDecimal> notas, BigDecimal maximo)
            throws SQLException, ExcepcionOperacionNoPermitida {
        Set<Integer> nomina = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT i.id_estudiante FROM asignacion_maestro am JOIN inscripcion i "
                + "ON i.id_seccion = am.id_seccion AND i.estado = 'ACTIVA' WHERE am.id_asignacion = ?")) {
            ps.setInt(1, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nomina.add(rs.getInt(1));
                }
            }
        }
        for (Map.Entry<Integer, BigDecimal> n : notas.entrySet()) {
            if (!nomina.contains(n.getKey())) {
                throw new ExcepcionOperacionNoPermitida("El estudiante " + n.getKey() + " no está inscrito en la sección");
            }
            BigDecimal v = n.getValue();
            if (v.signum() < 0 || v.compareTo(maximo) > 0) {
                throw new ExcepcionOperacionNoPermitida("La nota debe estar entre 0 y " + maximo.toPlainString());
            }
            if (v.stripTrailingZeros().scale() > 2) {
                throw new ExcepcionOperacionNoPermitida("La nota admite máximo 2 decimales");
            }
        }
    }

    private void deshacer(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignorada) {
                //La conexion se cierra de todos modos
            }
        }
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignorada) {
                //Nada que hacer
            }
        }
    }
}
