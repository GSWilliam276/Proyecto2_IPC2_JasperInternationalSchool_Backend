/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ActividadZona;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ZonaDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author eduar
 */
public class ZonaDAOPersistencia implements ZonaDAO {
    //Zona. Crear, editar y eliminar bloquean la asignacion (FOR UPDATE): la suma de 70 no se pasa por concurrencia
    private static final BigDecimal LIMITE = new BigDecimal("70");

    @Override
    public ActividadZona crear(int idAsignacion, String nombre, BigDecimal punteo)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        int id;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);
            bloquearAsignacion(con, idAsignacion);
            validarSuma(con, idAsignacion, punteo, 0);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO actividad_zona (id_asignacion, nombre, punteo) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idAsignacion);
                ps.setString(2, nombre);
                ps.setBigDecimal(3, punteo);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    id = rs.getInt(1);
                }
            }
            con.commit();
        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionOperacionNoPermitida("Ya existe una actividad con ese nombre en este curso");
            }
            throw new ExcepcionPersistencia("Error al crear la actividad de zona", e);
        } catch (ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        return new ActividadZona(id, idAsignacion, nombre, punteo);
    }

    @Override
    public void editar(int idAsignacion, int idActividadZona, String nombre, BigDecimal punteo)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);
            bloquearAsignacion(con, idAsignacion);
            existeEnAsignacion(con, idAsignacion, idActividadZona);
            validarSuma(con, idAsignacion, punteo, idActividadZona);
            //El punteo no puede quedar por debajo de la nota mas alta ya registrada
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT MAX(valor) FROM nota WHERE id_actividad_zona = ?")) {
                ps.setInt(1, idActividadZona);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    BigDecimal maxima = rs.getBigDecimal(1);
                    if (maxima != null && punteo.compareTo(maxima) < 0) {
                        throw new ExcepcionOperacionNoPermitida(
                                "Ya hay una nota de " + maxima.toPlainString() + ": el punteo no puede ser menor");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE actividad_zona SET nombre = ?, punteo = ? WHERE id_actividad_zona = ?")) {
                ps.setString(1, nombre);
                ps.setBigDecimal(2, punteo);
                ps.setInt(3, idActividadZona);
                ps.executeUpdate();
            }
            con.commit();
        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionOperacionNoPermitida("Ya existe una actividad con ese nombre en este curso");
            }
            throw new ExcepcionPersistencia("Error al editar la actividad de zona", e);
        } catch (ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public void eliminar(int idAsignacion, int idActividadZona)
            throws ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);
            bloquearAsignacion(con, idAsignacion);
            existeEnAsignacion(con, idAsignacion, idActividadZona);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM nota WHERE id_actividad_zona = ? LIMIT 1")) {
                ps.setInt(1, idActividadZona);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ExcepcionOperacionNoPermitida("La actividad ya tiene notas registradas: no se puede eliminar");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM actividad_zona WHERE id_actividad_zona = ?")) {
                ps.setInt(1, idActividadZona);
                ps.executeUpdate();
            }
            con.commit();
        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al eliminar la actividad de zona", e);
        } catch (ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public List<ActividadZona> listar(int idAsignacion) throws ExcepcionPersistencia {
        List<ActividadZona> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id_actividad_zona, nombre, punteo FROM actividad_zona WHERE id_asignacion = ? "
                     + "ORDER BY id_actividad_zona")) {
            ps.setInt(1, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ActividadZona(rs.getInt(1), idAsignacion, rs.getString(2), rs.getBigDecimal(3)));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar las actividades de zona", e);
        }
        return lista;
    }

    //Metodos de apoyo 

    private void bloquearAsignacion(Connection con, int idAsignacion) throws SQLException, ExcepcionOperacionNoPermitida {
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

    private void existeEnAsignacion(Connection con, int idAsignacion, int idActividadZona)
            throws SQLException, ExcepcionOperacionNoPermitida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM actividad_zona WHERE id_actividad_zona = ? AND id_asignacion = ?")) {
            ps.setInt(1, idActividadZona);
            ps.setInt(2, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionOperacionNoPermitida("No existe esa actividad en este curso");
                }
            }
        }
    }

    //Suma de los demas punteos (sin contar excluirId) mas el nuevo: no puede pasar de 70
    private void validarSuma(Connection con, int idAsignacion, BigDecimal nuevo, int excluirId)
            throws SQLException, ExcepcionOperacionNoPermitida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COALESCE(SUM(punteo), 0) FROM actividad_zona WHERE id_asignacion = ? AND id_actividad_zona <> ?")) {
            ps.setInt(1, idAsignacion);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                BigDecimal total = rs.getBigDecimal(1).add(nuevo);
                if (total.compareTo(LIMITE) > 0) {
                    throw new ExcepcionOperacionNoPermitida("La suma de punteos sería " + total.toPlainString()
                            + ": no puede pasar de 70");
                }
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
