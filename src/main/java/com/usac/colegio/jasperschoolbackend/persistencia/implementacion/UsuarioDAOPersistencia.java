/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Admin;
import com.usac.colegio.jasperschoolbackend.modelo.SuperAdmin;
import com.usac.colegio.jasperschoolbackend.modelo.Usuario;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.UsuarioDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class UsuarioDAOPersistencia implements UsuarioDAO {
    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM usuario WHERE correo = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Si se encontro la fila en usuario, hay que armar el objeto
                    //concreto correcto consultando las tablas hijas
                    return Optional.of(construirUsuarioConRol(con, rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar usuario por correo", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorCui(String cui) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM usuario WHERE cui = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cui);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(construirUsuarioConRol(con, rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar usuario por CUI", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existeCorreo(String correo) throws ExcepcionPersistencia {
        //Se usa para validar duplicados 
        String sql = "SELECT COUNT(*) FROM usuario WHERE correo = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al verificar correo", e);
        }
    }

    @Override
    public boolean existeCui(String cui) throws ExcepcionPersistencia {
        String sql = "SELECT COUNT(*) FROM usuario WHERE cui = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cui);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al verificar CUI", e);
        }
    }

    @Override
    public void actualizarContrasena(int idUsuario, String nuevaContrasena) throws ExcepcionPersistencia {
        //Usado por Cambiar Contraseña (CU102) y Restablecer Contraseña (CU003)
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevaContrasena);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al actualizar contraseña", e);
        }
    }

    @Override
    public String determinarRol(int idUsuario) throws ExcepcionPersistencia {
        //Revisa en cada tabla hija hasta encontrar en cual vive este usuario
        try (Connection con = ConexionBase.obtenerInstancia().getConexion()) {
            if (existeEnTabla(con, "admin", idUsuario)) {
                return "ADMIN";
            }
            if (existeEnTabla(con, "superadmin", idUsuario)) {
                return "SUPERADMIN";
            }
            return "DESCONOCIDO";
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al determinar rol", e);
        }
    }

    //Metodos privados propios de apoyo

    
    //Arma el objeto Usuario concreto (Admin o SuperAdmin) a partir de una fila
    //de la tabla usuario, consultando ademas la tabla hija correspondiente
    //para saber el rol y su estado
    private Usuario construirUsuarioConRol(Connection con, ResultSet rsUsuario) throws SQLException {
        int idUsuario = rsUsuario.getInt("id_usuario");
        String cui = rsUsuario.getString("cui");
        String nombre = rsUsuario.getString("nombre");
        String correo = rsUsuario.getString("correo");
        String telefono = rsUsuario.getString("telefono");
        String direccion = rsUsuario.getString("direccion");
        String contrasena = rsUsuario.getString("contrasena");

        EstadoGeneral estadoAdmin = buscarEstado(con, "admin", idUsuario);
        if (estadoAdmin != null) {
            return new Admin(idUsuario, cui, nombre, correo, telefono, direccion, contrasena, estadoAdmin);
        }

        EstadoGeneral estadoSuperAdmin = buscarEstado(con, "superadmin", idUsuario);
        if (estadoSuperAdmin != null) {
            return new SuperAdmin(idUsuario, cui, nombre, correo, telefono, direccion, contrasena, estadoSuperAdmin);
        }

        //Si llega aca, hay una fila en usuario sin fila correspondiente en ninguna
        //tabla hija, lo cual nunca deberia pasar si el flujo de creacion es correcto
        throw new RuntimeException("El usuario no tiene un rol asignado en ninguna tabla hija");
    }

    private EstadoGeneral buscarEstado(Connection con, String tabla, int idUsuario) throws SQLException {
        String sql = "SELECT estado FROM " + tabla + " WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return EstadoGeneral.valueOf(rs.getString("estado"));
                }
            }
        }
        return null;
    }

    private boolean existeEnTabla(Connection con, String tabla, int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tabla + " WHERE id_usuario = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
