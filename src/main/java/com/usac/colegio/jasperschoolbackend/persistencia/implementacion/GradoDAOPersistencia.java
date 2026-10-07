/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionGradoConEstudiantes;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionGradoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Grado;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.GradoDAO;
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
public class GradoDAOPersistencia implements GradoDAO {
    //Implementacion del acceso a datos de Grado
    @Override
    public void crear(Grado grado) throws ExcepcionPersistencia, ExcepcionNombreDuplicado {
        if (existeNombre(grado.getNombre(), null)) {
            throw new ExcepcionNombreDuplicado("Ya existe un grado con ese nombre");
        }

        String sql = "INSERT INTO grado (nombre, nivel, estado) VALUES (?, ?, ?)";
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, grado.getNombre());
            ps.setString(2, grado.getNivel().name());
            ps.setString(3, grado.getEstado().name());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al crear el grado", e);
        }
    }

    @Override
    public void editar(Grado grado) throws ExcepcionPersistencia, ExcepcionGradoNoEncontrado, ExcepcionNombreDuplicado {
        if (existeNombre(grado.getNombre(), grado.getIdGrado())) {
            throw new ExcepcionNombreDuplicado("Ya existe otro grado con ese nombre");
        }

        String sql = "UPDATE grado SET nombre = ?, nivel = ? WHERE id_grado = ?";
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, grado.getNombre());
            ps.setString(2, grado.getNivel().name());
            ps.setInt(3, grado.getIdGrado());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionGradoNoEncontrado("No existe un grado con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al editar el grado", e);
        }
    }

    @Override
    public Optional<Grado> buscarPorId(int id) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM grado WHERE id_grado = ?";
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearGrado(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el grado", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Grado> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM grado WHERE nombre LIKE ? LIMIT ? OFFSET ?";
        List<Grado> grados = new ArrayList<>();
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setInt(2, tamanoPagina);
            ps.setInt(3, (pagina - 1) * tamanoPagina);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grados.add(mapearGrado(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los grados", e);
        }
        return grados;
    }

    @Override
    public void activar(int id) throws ExcepcionGradoNoEncontrado, ExcepcionPersistencia {
        cambiarEstado(id, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int id) throws ExcepcionGradoNoEncontrado, ExcepcionGradoConEstudiantes, ExcepcionPersistencia {
        //NOTA: la validacion real de "tiene estudiantes inscritos" se
        //completa cuando exista la tabla de secciones/inscripciones,
        //en la Iteracion 2. Por ahora el metodo queda listo con la
        //excepcion declarada, pendiente de esa consulta
        cambiarEstado(id, EstadoGeneral.INACTIVO);
    }

    //Metodos Privados de Apoyo

    private void cambiarEstado(int id, EstadoGeneral nuevoEstado) throws ExcepcionGradoNoEncontrado, ExcepcionPersistencia {
        String sql = "UPDATE grado SET estado = ? WHERE id_grado = ?";
        try (Connection con = ConexionBase.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, id);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionGradoNoEncontrado("No existe un grado con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al cambiar el estado del grado", e);
        }
    }

    private boolean existeNombre(String nombre, Integer idExcluir) throws ExcepcionPersistencia {
        String sql = "SELECT COUNT(*) FROM grado WHERE nombre = ?";
        if (idExcluir != null) {
            sql += " AND id_grado != ?";
        }

        try (Connection con = ConexionBase.getConexion();
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

    private Grado mapearGrado(ResultSet rs) throws SQLException {
        return new Grado(
                rs.getInt("id_grado"),
                rs.getString("nombre"),
                Nivel.valueOf(rs.getString("nivel")),
                EstadoGeneral.valueOf(rs.getString("estado"))
        );
    }
}
