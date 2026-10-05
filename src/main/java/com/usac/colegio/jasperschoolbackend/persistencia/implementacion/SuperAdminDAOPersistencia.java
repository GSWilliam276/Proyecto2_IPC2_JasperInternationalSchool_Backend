/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSuperAdminNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionUltimoSuperAdmin;
import com.usac.colegio.jasperschoolbackend.modelo.SuperAdmin;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SuperAdminDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.usac.colegio.jasperschoolbackend.utilidades.ContrasenaUtil;
/**
 *
 * @author eduar
 */
public class SuperAdminDAOPersistencia implements SuperAdminDAO{
    //Implementacion del acceso a datos de SuperAdmin
    @Override
    public void crear(SuperAdmin superAdmin) throws ExcepcionPersistencia, ExcepcionCorreoDuplicado, ExcepcionCuiDuplicado {
        String sqlUsuario = "INSERT INTO usuario (cui, nombre, correo, telefono, direccion, contrasena) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlSuperAdmin = "INSERT INTO superadmin (id_usuario, estado) VALUES (?, ?)";

        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();

            //Arranca la transaccion
            con.setAutoCommit(false);

            //1. Insertar en la tabla usuario (datos comunes a todos los roles).
            //RETURN_GENERATED_KEYS permite recuperar el id autoincremental
            //que MySQL le asigno a esta fila recien creada.
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psUsuario.setString(1, superAdmin.getCui());
                psUsuario.setString(2, superAdmin.getNombre());
                psUsuario.setString(3, superAdmin.getCorreo());
                psUsuario.setString(4, superAdmin.getTelefono());
                psUsuario.setString(5, superAdmin.getDireccion());
                psUsuario.setString(6, ContrasenaUtil.hashear(superAdmin.getContrasena()));
                psUsuario.executeUpdate();

                int idGenerado;
                try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                    rs.next();
                    idGenerado = rs.getInt(1);
                }
                //El objeto en memoria se actualiza con el id real que acaba
                //de generar la base de datos
                superAdmin.setIdUsuario(idGenerado);
            }

            //2. Insertar en la tabla superadmin, usando el MISMO id que se
            // genero en el paso anterior 
            try (PreparedStatement psSuperAdmin = con.prepareStatement(sqlSuperAdmin)) {
                psSuperAdmin.setInt(1, superAdmin.getIdUsuario());
                psSuperAdmin.setString(2, superAdmin.getEstado().name());
                psSuperAdmin.executeUpdate();
            }

            //Fin de la transaccion
            con.commit();

        } catch (SQLException e) {
            //Si cualquiera de las dos inserciones fallo, se deshacen ambas
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new ExcepcionPersistencia("Error al revertir la transaccion", ex);
                }
            }
            //1062 es el codigo que MySQL usa para violacion de restriccion UNIQUE
            if (e.getErrorCode() == 1062 && e.getMessage().contains("correo")) {
                throw new ExcepcionCorreoDuplicado("El correo ya está registrado");
            }
            if (e.getErrorCode() == 1062 && e.getMessage().contains("cui")) {
                throw new ExcepcionCuiDuplicado("El CUI ya está registrado");
            }
            throw new ExcepcionPersistencia("Error al crear el superadmin", e);
        } finally {
            //Sin importar si salio bien o mal, hay que regresar la conexion
            //a su estado normal (autocommit true) antes de devolverla al pool,
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    throw new ExcepcionPersistencia("Error al cerrar la conexion", e);
                }
            }
        }
    }

    @Override
    public void editar(SuperAdmin superAdmin) throws ExcepcionPersistencia, ExcepcionSuperAdminNoEncontrado {
        //Esta operacion toca una sola tabla (usuario)
        String sql = "UPDATE usuario SET nombre = ?, telefono = ?, direccion = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, superAdmin.getNombre());
            ps.setString(2, superAdmin.getTelefono());
            ps.setString(3, superAdmin.getDireccion());
            ps.setInt(4, superAdmin.getIdUsuario());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionSuperAdminNoEncontrado("No existe un superadmin con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al editar el superadmin", e);
        }
    }

    @Override
    public Optional<SuperAdmin> buscarPorId(int id) throws ExcepcionPersistencia {
        //INNER JOIN trae en una sola consulta los datos de usuario
        //combinados con el estado especifico de superadmin
        String sql = "SELECT u.*, s.estado FROM usuario u "
                + "INNER JOIN superadmin s ON u.id_usuario = s.id_usuario "
                + "WHERE u.id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearSuperAdmin(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el superadmin", e);
        }
        return Optional.empty();
    }

    @Override
    public List<SuperAdmin> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        String sql = "SELECT u.*, s.estado FROM usuario u "
                + "INNER JOIN superadmin s ON u.id_usuario = s.id_usuario "
                + "WHERE u.nombre LIKE ? OR u.correo LIKE ? "
                + "LIMIT ? OFFSET ?";
        List<SuperAdmin> superAdmins = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            //Si no viene texto de busqueda, el filtro queda como "%%",
            //que en SQL hace match con cualquier texto (no filtra nada)
            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setInt(3, tamanoPagina);                       //LIMIT: cuantas filas traer
            ps.setInt(4, (pagina - 1) * tamanoPagina);         //OFFSET: cuantas filas saltar

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    superAdmins.add(mapearSuperAdmin(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los superadmins", e);
        }
        return superAdmins;
    }

    @Override
    public void activar(int idUsuario) throws ExcepcionSuperAdminNoEncontrado, ExcepcionPersistencia {
        cambiarEstado(idUsuario, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int idUsuario) throws ExcepcionSuperAdminNoEncontrado, ExcepcionUltimoSuperAdmin, ExcepcionPersistencia {
        //Siempre debe existir al menos un SuperAdmin activo en el sistema 
        //Se valida antes de desactivar, no despues, para no dejar el sistema en un estado invalido
        if (contarActivos() <= 1) {
            throw new ExcepcionUltimoSuperAdmin("No se puede desactivar al único SuperAdmin activo del sistema");
        }
        cambiarEstado(idUsuario, EstadoGeneral.INACTIVO);
    }

    @Override
    public int contarActivos() throws ExcepcionPersistencia {
        String sql = "SELECT COUNT(*) FROM superadmin WHERE estado = 'ACTIVO'";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al contar los superadmins activos", e);
        }
    }

    //Metodos Privados de Apoyo

    private void cambiarEstado(int idUsuario, EstadoGeneral nuevoEstado) throws ExcepcionSuperAdminNoEncontrado, ExcepcionPersistencia {
        //Esta operacion toca una sola tabla (superadmin), no necesita transaccion
        String sql = "UPDATE superadmin SET estado = ? WHERE id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idUsuario);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new ExcepcionSuperAdminNoEncontrado("No existe un superadmin con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al cambiar el estado del superadmin", e);
        }
    }

    private SuperAdmin mapearSuperAdmin(ResultSet rs) throws SQLException {
        //Convierte una fila del ResultSet en un objeto SuperAdmin completo
        return new SuperAdmin(
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
