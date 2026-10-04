/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAdminNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Admin;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AdminDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
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
public class AdminDAOPersistencia implements AdminDAO{
    //Implementacion del acceso a datos de Admin
    @Override
    public void crear(Admin admin) throws ExcepcionPersistencia, ExcepcionCorreoDuplicado, ExcepcionCuiDuplicado {
        String sqlUsuario = "INSERT INTO usuario (cui, nombre, correo, telefono, direccion, contrasena) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAdmin = "INSERT INTO admin (id_usuario, estado) VALUES (?, ?)";

        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false); //Arranca la transaccion

            //1. Insertar en usuario, pidiendo que devuelva el id generado
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUsuario.setString(1, admin.getCui());
                psUsuario.setString(2, admin.getNombre());
                psUsuario.setString(3, admin.getCorreo());
                psUsuario.setString(4, admin.getTelefono());
                psUsuario.setString(5, admin.getDireccion());
                psUsuario.setString(6, admin.getContrasena());
                psUsuario.executeUpdate();

                int idGenerado;
                try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                    rs.next();
                    idGenerado = rs.getInt(1);
                }
                admin.setIdUsuario(idGenerado);
            }

            //2. Insertar en admin, usando el mismo id que se acaba de generar
            try (PreparedStatement psAdmin = con.prepareStatement(sqlAdmin)) {
                psAdmin.setInt(1, admin.getIdUsuario());
                psAdmin.setString(2, admin.getEstado().name());
                psAdmin.executeUpdate();
            }

            con.commit(); //Las dos inserciones salieron bien, se confirman juntas

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback(); //Algo fallo, se deshacen ambas inserciones
                } catch (SQLException ex) {
                    throw new ExcepcionPersistencia("Error al revertir la transaccion", ex);
                }
            }
            //El codigo 1062 de MySQL es el de violacion de UNIQUE
            if (e.getErrorCode() == 1062 && e.getMessage().contains("correo")) {
                throw new ExcepcionCorreoDuplicado("El correo ya está registrado");
            }
            if (e.getErrorCode() == 1062 && e.getMessage().contains("cui")) {
                throw new ExcepcionCuiDuplicado("El CUI ya está registrado");
            }
            throw new ExcepcionPersistencia("Error al crear el admin", e);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true); //Regresa la conexion a su estado normal antes de devolverla al pool
                    con.close();
                } catch (SQLException e) {
                    throw new ExcepcionPersistencia("Error al cerrar la conexion", e);
                }
            }
        }
    }

    @Override
    public void editar(Admin admin) throws ExcepcionPersistencia, ExcepcionAdminNoEncontrado {
        //Solo se editan los datos de usuario; el estado se maneja aparte con activar/desactivar
        String sql = "UPDATE usuario SET nombre = ?, telefono = ?, direccion = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, admin.getNombre());
            ps.setString(2, admin.getTelefono());
            ps.setString(3, admin.getDireccion());
            ps.setInt(4, admin.getIdUsuario());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionAdminNoEncontrado("No existe un admin con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al editar el admin", e);
        }
    }

    @Override
    public Optional<Admin> buscarPorId(int id) throws ExcepcionPersistencia {
        String sql = "SELECT u.*, a.estado FROM usuario u "
                + "INNER JOIN admin a ON u.id_usuario = a.id_usuario "
                + "WHERE u.id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearAdmin(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el admin", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Admin> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        String sql = "SELECT u.*, a.estado FROM usuario u "
                + "INNER JOIN admin a ON u.id_usuario = a.id_usuario "
                + "WHERE u.nombre LIKE ? OR u.correo LIKE ? "
                + "LIMIT ? OFFSET ?";
        List<Admin> admins = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setInt(3, tamanoPagina);
            ps.setInt(4, (pagina - 1) * tamanoPagina);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    admins.add(mapearAdmin(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los admins", e);
        }
        return admins;
    }

    @Override
    public void activar(int idUsuario) throws ExcepcionAdminNoEncontrado, ExcepcionPersistencia {
        cambiarEstado(idUsuario, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int idUsuario) throws ExcepcionAdminNoEncontrado, ExcepcionPersistencia {
        cambiarEstado(idUsuario, EstadoGeneral.INACTIVO);
    }

    //Metodos privados de apoyo 
    
    private void cambiarEstado(int idUsuario, EstadoGeneral nuevoEstado) throws ExcepcionAdminNoEncontrado, ExcepcionPersistencia {
        String sql = "UPDATE admin SET estado = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idUsuario);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionAdminNoEncontrado("No existe un admin con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al cambiar el estado del admin", e);
        }
    }

    private Admin mapearAdmin(ResultSet rs) throws SQLException {
        //Convierte una fila del ResultSet en un objeto Admin completo
        return new Admin(
                rs.getInt("id_usuario"),
                rs.getString("cui"),
                rs.getString("nombre"),
                rs.getString("correo"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                rs.getString("contrasena"),
                EstadoGeneral.valueOf(rs.getString("estado"))
        );
    }
}
