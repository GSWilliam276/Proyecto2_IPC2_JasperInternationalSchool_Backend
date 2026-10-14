/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionActividadNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.ActividadExtracurricular;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ActividadDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class ActividadDAOPersistencia implements ActividadDAO {
    @Override
    public void crear(ActividadExtracurricular a) throws ExcepcionPersistencia {
        String sql = "INSERT INTO actividad_extracurricular (nombre, descripcion, fecha, cupo, costo, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNombre());
            ps.setString(2, a.getDescripcion());
            ps.setDate(3, Date.valueOf(a.getFecha()));
            ps.setInt(4, a.getCupo());
            ps.setBigDecimal(5, a.getCosto());
            ps.setString(6, a.getEstado().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setIdActividad(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al crear la actividad");
        }
    }

    @Override
    public void editar(ActividadExtracurricular a) throws ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            bloquear(con, a.getIdActividad());
            //La validacion "el cupo no baja de los participantes" se agrega con las participaciones
            String sql = "UPDATE actividad_extracurricular SET nombre = ?, descripcion = ?, fecha = ?, cupo = ?, costo = ? "
                    + "WHERE id_actividad = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, a.getNombre());
                ps.setString(2, a.getDescripcion());
                ps.setDate(3, Date.valueOf(a.getFecha()));
                ps.setInt(4, a.getCupo());
                ps.setBigDecimal(5, a.getCosto());
                ps.setInt(6, a.getIdActividad());
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw traducir(e, "Error al editar la actividad");
        } catch (ExcepcionActividadNoEncontrada e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<ActividadExtracurricular> buscarPorId(int id) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM actividad_extracurricular WHERE id_actividad = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar la actividad", e);
        }
        return Optional.empty();
    }

    @Override
    public List<ActividadExtracurricular> listarPaginado(int pagina, int tamanoPagina, String busqueda)
            throws ExcepcionPersistencia {
        return listarFiltrado(pagina, tamanoPagina, busqueda, false);
    }

    @Override
    public List<ActividadExtracurricular> listarFiltrado(int pagina, int tamanoPagina, String busqueda, boolean soloActivas)
            throws ExcepcionPersistencia {
        String sql = "SELECT * FROM actividad_extracurricular WHERE nombre LIKE ? "
                + (soloActivas ? "AND estado = 'ACTIVO' " : "")
                + "ORDER BY fecha DESC, nombre, id_actividad LIMIT ? OFFSET ?";
        List<ActividadExtracurricular> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (busqueda == null ? "" : busqueda) + "%");
            ps.setInt(2, tamanoPagina);
            ps.setInt(3, (pagina - 1) * tamanoPagina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar las actividades", e);
        }
        return lista;
    }

    @Override
    public void activar(int idActividad) throws ExcepcionActividadNoEncontrada, ExcepcionPersistencia {
        cambiarEstado(idActividad, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int idActividad) throws ExcepcionActividadNoEncontrada, ExcepcionPersistencia {
        cambiarEstado(idActividad, EstadoGeneral.INACTIVO);
    }

    //Metodos de Apoyo

    private void cambiarEstado(int idActividad, EstadoGeneral nuevo)
            throws ExcepcionActividadNoEncontrada, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            bloquear(con, idActividad);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE actividad_extracurricular SET estado = ? WHERE id_actividad = ?")) {
                ps.setString(1, nuevo.name());
                ps.setInt(2, idActividad);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al cambiar el estado de la actividad", e);
        } catch (ExcepcionActividadNoEncontrada e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    private void bloquear(Connection con, int idActividad) throws SQLException, ExcepcionActividadNoEncontrada {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_actividad FROM actividad_extracurricular WHERE id_actividad = ? FOR UPDATE")) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionActividadNoEncontrada("No existe una actividad con ese id");
                }
            }
        }
    }

    private ActividadExtracurricular mapear(ResultSet rs) throws SQLException {
        return new ActividadExtracurricular(rs.getInt("id_actividad"), rs.getString("nombre"),
                rs.getString("descripcion"), rs.getDate("fecha").toLocalDate(), rs.getInt("cupo"),
                rs.getBigDecimal("costo"), EstadoGeneral.valueOf(rs.getString("estado")));
    }

    //El UNIQUE (nombre, fecha) de la base es la garantia real contra duplicados
    private ExcepcionPersistencia traducir(SQLException e, String mensajeGenerico) {
        if (e.getErrorCode() == 1062) {
            return new ExcepcionNombreDuplicado("Ya existe una actividad con ese nombre en esa fecha");
        }
        return new ExcepcionPersistencia(mensajeGenerico, e);
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
