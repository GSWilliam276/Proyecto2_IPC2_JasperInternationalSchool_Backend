/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAnioLectivoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSolapamientoFechas;
import com.usac.colegio.jasperschoolbackend.modelo.AnioLectivo;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AnioLectivoDAO;
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
public class AnioLectivoDAOPersistencia implements AnioLectivoDAO {
    //Implementacion del acceso a datos de AnioLectivo
    @Override
    public void crear(AnioLectivo anio) throws ExcepcionPersistencia, ExcepcionSolapamientoFechas {
        //Regla de negocio: solo puede haber un año lectivo ACTIVO a la vez
        if (existeSolapamiento(anio.getFechaInicio(), anio.getFechaFin(), null)) {
            throw new ExcepcionSolapamientoFechas("El rango de fechas se solapa con un año lectivo existente");
        }

        String sql = "INSERT INTO anio_lectivo (nombre, fecha_inicio, fecha_fin, estado) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBase.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, anio.getNombre());
            ps.setDate(2, Date.valueOf(anio.getFechaInicio()));
            ps.setDate(3, Date.valueOf(anio.getFechaFin()));
            ps.setString(4, anio.getEstado().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al crear el año lectivo", e);
        }
    }

    @Override
    public void editar(AnioLectivo anio) throws ExcepcionPersistencia, ExcepcionAnioLectivoNoEncontrado, ExcepcionSolapamientoFechas {
        //Al editar, se excluye el propio registro de la busqueda de solapamiento
        //(si no, siempre "chocaria" consigo mismo)
        if (existeSolapamiento(anio.getFechaInicio(), anio.getFechaFin(), anio.getIdAnioLectivo())) {
            throw new ExcepcionSolapamientoFechas("El rango de fechas se solapa con un año lectivo existente");
        }

        String sql = "UPDATE anio_lectivo SET nombre = ?, fecha_inicio = ?, fecha_fin = ? "
                + "WHERE id_anio_lectivo = ?";
        try (Connection con = ConexionBase.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, anio.getNombre());
            ps.setDate(2, Date.valueOf(anio.getFechaInicio()));
            ps.setDate(3, Date.valueOf(anio.getFechaFin()));
            ps.setInt(4, anio.getIdAnioLectivo());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionAnioLectivoNoEncontrado("No existe un año lectivo con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al editar el año lectivo", e);
        }
    }

    @Override
    public Optional<AnioLectivo> buscarPorId(int id) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM anio_lectivo WHERE id_anio_lectivo = ?";
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearAnioLectivo(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el año lectivo", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AnioLectivo> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM anio_lectivo WHERE nombre LIKE ? LIMIT ? OFFSET ?";
        List<AnioLectivo> anios = new ArrayList<>();
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setInt(2, tamanoPagina);
            ps.setInt(3, (pagina - 1) * tamanoPagina);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    anios.add(mapearAnioLectivo(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los años lectivos", e);
        }
        return anios;
    }

    @Override
    public void cerrar(int id) throws ExcepcionAnioLectivoNoEncontrado, ExcepcionPersistencia {
        //Cerrar es irreversible solo cambia a CERRADO,
        //nunca se vuelve a ACTIVO desde aqui
        String sql = "UPDATE anio_lectivo SET estado = 'CERRADO' WHERE id_anio_lectivo = ?";
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionAnioLectivoNoEncontrado("No existe un año lectivo con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al cerrar el año lectivo", e);
        }
    }

    @Override
    public Optional<AnioLectivo> buscarActivo() throws ExcepcionPersistencia {
        //Usado por Crear Seccion y otros CU que necesitan saber cual es
        //el ciclo escolar vigente en este momento
        String sql = "SELECT * FROM anio_lectivo WHERE estado = 'ACTIVO'";
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return Optional.of(mapearAnioLectivo(rs));
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el año lectivo activo", e);
        }
        return Optional.empty();
    }

    //Metodos Privados de Apoyo

    private AnioLectivo mapearAnioLectivo(ResultSet rs) throws SQLException {
        return new AnioLectivo(
                rs.getInt("id_anio_lectivo"),
                rs.getString("nombre"),
                rs.getDate("fecha_inicio").toLocalDate(),
                rs.getDate("fecha_fin").toLocalDate(),
                EstadoAnioLectivo.valueOf(rs.getString("estado"))
        );
    }
    
    //Verifica si el rango de fechas dado se solapa con algun año lectivo
    //ya existente. El parametro idExcluir se usa al editar, para no
    //comparar el registro contra si mismo
    private boolean existeSolapamiento(java.time.LocalDate inicio, java.time.LocalDate fin, Integer idExcluir) throws ExcepcionPersistencia {
        String sql = "SELECT COUNT(*) FROM anio_lectivo WHERE fecha_inicio <= ? AND fecha_fin >= ?";
        if (idExcluir != null) {
            sql += " AND id_anio_lectivo != ?";
        }

        try (Connection con = ConexionBase.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fin));
            ps.setDate(2, Date.valueOf(inicio));
            if (idExcluir != null) {
                ps.setInt(3, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al verificar solapamiento de fechas", e);
        }
    }
}
