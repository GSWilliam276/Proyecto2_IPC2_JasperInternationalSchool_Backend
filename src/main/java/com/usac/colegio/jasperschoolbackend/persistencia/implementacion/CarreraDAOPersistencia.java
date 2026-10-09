/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCarreraConEstudiantes;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCarreraNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Carrera;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CarreraDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class CarreraDAOPersistencia implements CarreraDAO{
    //Implementacion del acceso a datos de Carrera
    @Override
    public void crear(Carrera carrera) throws ExcepcionPersistencia, ExcepcionNombreDuplicado {
        if (existeNombre(carrera.getNombre(), null)) {
            throw new ExcepcionNombreDuplicado("Ya existe una carrera con ese nombre");
        }

        String sql = "INSERT INTO carrera (nombre, estado) VALUES (?, ?)";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrera.getNombre());
            ps.setString(2, carrera.getEstado().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al crear la carrera", e);
        }
    }

    @Override
    public void editar(Carrera carrera) throws ExcepcionPersistencia, ExcepcionCarreraNoEncontrada, ExcepcionNombreDuplicado {
        if (existeNombre(carrera.getNombre(), carrera.getIdCarrera())) {
            throw new ExcepcionNombreDuplicado("Ya existe otra carrera con ese nombre");
        }

        String sql = "UPDATE carrera SET nombre = ? WHERE id_carrera = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, carrera.getNombre());
            ps.setInt(2, carrera.getIdCarrera());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionCarreraNoEncontrada("No existe una carrera con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al editar la carrera", e);
        }
    }

    @Override
    public Optional<Carrera> buscarPorId(int id) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM carrera WHERE id_carrera = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCarrera(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar la carrera", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Carrera> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM carrera WHERE nombre LIKE ? LIMIT ? OFFSET ?";
        List<Carrera> carreras = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setInt(2, tamanoPagina);
            ps.setInt(3, (pagina - 1) * tamanoPagina);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    carreras.add(mapearCarrera(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar las carreras", e);
        }
        return carreras;
    }

    @Override
    public void activar(int id) throws ExcepcionCarreraNoEncontrada, ExcepcionPersistencia {
        cambiarEstado(id, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int id) throws ExcepcionCarreraNoEncontrada, ExcepcionCarreraConEstudiantes, ExcepcionPersistencia {
        //Misma nota que Grado: la validacion de estudiantes inscritos
        //se completa en la Iteracion 2
        cambiarEstado(id, EstadoGeneral.INACTIVO);
    }

    //Metodos privados de apoyo 

    private void cambiarEstado(int id, EstadoGeneral nuevoEstado) throws ExcepcionCarreraNoEncontrada, ExcepcionPersistencia {
        String sql = "UPDATE carrera SET estado = ? WHERE id_carrera = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, id);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionCarreraNoEncontrada("No existe una carrera con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al cambiar el estado de la carrera", e);
        }
    }

    private boolean existeNombre(String nombre, Integer idExcluir) throws ExcepcionPersistencia {
        String sql = "SELECT COUNT(*) FROM carrera WHERE nombre = ?";
        if (idExcluir != null) {
            sql += " AND id_carrera != ?";
        }

        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al verificar nombre duplicado", e);
        }
    }

    private Carrera mapearCarrera(ResultSet rs) throws SQLException {
        return new Carrera(
                rs.getInt("id_carrera"),
                rs.getString("nombre"),
                EstadoGeneral.valueOf(rs.getString("estado"))
        );
    }
}
