/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.RegistroAsistencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AsistenciaDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 *
 * @author eduar
 */
public class AsistenciaDAOPersistencia implements AsistenciaDAO{
    @Override
    public void registrar(int idAsignacion, LocalDate fecha, Map<Integer, Boolean> marcas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            int idSeccion = bloquearAsignacion(con, idAsignacion, fecha);
            Set<Integer> nomina = nomina(con, idSeccion);
            if (nomina.isEmpty()) {
                throw new ExcepcionOperacionNoPermitida("La sección no tiene estudiantes inscritos");
            }
            if (hayAsistencia(con, idAsignacion, fecha)) {
                throw new ExcepcionOperacionNoPermitida("Ya hay asistencia registrada en esa fecha: use editar");
            }
            if (!nomina.containsAll(marcas.keySet())) {
                throw new ExcepcionOperacionNoPermitida("Hay estudiantes que no están inscritos en la sección");
            }
            if (!marcas.keySet().containsAll(nomina)) {
                throw new ExcepcionOperacionNoPermitida("Debe marcar a todos los estudiantes inscritos");
            }
            guardarMarcas(con, idAsignacion, fecha, marcas);
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionOperacionNoPermitida("Ya hay asistencia registrada en esa fecha: use editar");
            }
            throw new ExcepcionPersistencia("Error al registrar la asistencia", e);
        } catch (ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public void editar(int idAsignacion, LocalDate fecha, Map<Integer, Boolean> marcas)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            int idSeccion = bloquearAsignacion(con, idAsignacion, fecha);
            if (!hayAsistencia(con, idAsignacion, fecha)) {
                throw new ExcepcionOperacionNoPermitida("No hay asistencia registrada en esa fecha: use registrar");
            }
            if (!nomina(con, idSeccion).containsAll(marcas.keySet())) {
                throw new ExcepcionOperacionNoPermitida("Hay estudiantes que no están inscritos en la sección");
            }
            guardarMarcas(con, idAsignacion, fecha, marcas);   //Actualiza lo marcado y agrega a quien estaba "sin registro"
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al editar la asistencia", e);
        } catch (ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public List<RegistroAsistencia> planilla(int idAsignacion, LocalDate fecha) throws ExcepcionPersistencia {
        String sql = "SELECT u.id_usuario, u.nombre, u.cui, a.presente "
                + "FROM asignacion_maestro am "
                + "JOIN inscripcion i ON i.id_seccion = am.id_seccion AND i.estado = 'ACTIVA' "
                + "JOIN usuario u ON u.id_usuario = i.id_estudiante "
                + "LEFT JOIN asistencia a ON a.id_asignacion = am.id_asignacion AND a.id_estudiante = i.id_estudiante "
                + "AND a.fecha = ? WHERE am.id_asignacion = ? ORDER BY u.nombre, u.id_usuario";
        List<RegistroAsistencia> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            ps.setInt(2, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean marcado = rs.getBoolean("presente");
                    Boolean presente = rs.wasNull() ? null : marcado;
                    lista.add(new RegistroAsistencia(rs.getInt("id_usuario"), rs.getString("nombre"),
                            rs.getString("cui"), fecha, presente));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar la planilla de asistencia", e);
        }
        return lista;
    }

    @Override
    public List<RegistroAsistencia> listar(int idAsignacion, Integer idEstudiante, LocalDate desde, LocalDate hasta)
            throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(
                "SELECT u.id_usuario, u.nombre, u.cui, a.fecha, a.presente FROM asistencia a "
                + "JOIN usuario u ON u.id_usuario = a.id_estudiante WHERE a.id_asignacion = ? ");
        List<Object> parametros = new ArrayList<>();
        parametros.add(idAsignacion);
        if (idEstudiante != null) {
            sql.append("AND a.id_estudiante = ? ");
            parametros.add(idEstudiante);
        }
        if (desde != null) {
            sql.append("AND a.fecha >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND a.fecha <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append("ORDER BY a.fecha DESC, u.nombre, u.id_usuario LIMIT 500");
        List<RegistroAsistencia> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new RegistroAsistencia(rs.getInt("id_usuario"), rs.getString("nombre"),
                            rs.getString("cui"), rs.getDate("fecha").toLocalDate(), rs.getBoolean("presente")));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar la asistencia", e);
        }
        return lista;
    }

    //Metodos de Apoyo

    //Bloquea la asignacion y valida el año activo y que la fecha no sea anterior a su inicio. Devuelve la seccion
    private int bloquearAsignacion(Connection con, int idAsignacion, LocalDate fecha)
            throws SQLException, ExcepcionOperacionNoPermitida {
        String sql = "SELECT am.id_seccion, a.estado, a.fecha_inicio FROM asignacion_maestro am "
                + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                + "JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
                + "WHERE am.id_asignacion = ? FOR UPDATE OF am";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionOperacionNoPermitida("No existe la asignación");
                }
                if (!"ACTIVO".equals(rs.getString("estado"))) {
                    throw new ExcepcionOperacionNoPermitida("La asignación pertenece a un año lectivo cerrado");
                }
                if (fecha.isBefore(rs.getDate("fecha_inicio").toLocalDate())) {
                    throw new ExcepcionOperacionNoPermitida("La fecha es anterior al inicio del año lectivo");
                }
                return rs.getInt("id_seccion");
            }
        }
    }

    private Set<Integer> nomina(Connection con, int idSeccion) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_estudiante FROM inscripcion WHERE id_seccion = ? AND estado = 'ACTIVA'")) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    private boolean hayAsistencia(Connection con, int idAsignacion, LocalDate fecha) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM asistencia WHERE id_asignacion = ? AND fecha = ? LIMIT 1")) {
            ps.setInt(1, idAsignacion);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    //Inserta cada marca; si ya existia, actualiza. Sirve para registrar y para editar
    private void guardarMarcas(Connection con, int idAsignacion, LocalDate fecha, Map<Integer, Boolean> marcas)
            throws SQLException {
        String sql = "INSERT INTO asistencia (id_asignacion, id_estudiante, fecha, presente) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE presente = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map.Entry<Integer, Boolean> m : marcas.entrySet()) {
                ps.setInt(1, idAsignacion);
                ps.setInt(2, m.getKey());
                ps.setDate(3, Date.valueOf(fecha));
                ps.setBoolean(4, m.getValue());
                ps.setBoolean(5, m.getValue());
                ps.executeUpdate();
            }
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
