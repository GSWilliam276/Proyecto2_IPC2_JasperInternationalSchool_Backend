/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoEstudiante;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Estudiante;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.EstudianteDAO;
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
public class EstudianteDAOPersistencia implements EstudianteDAO{
    //Acceso a datos de Estudiante. Vive en dos tablas (usuario y estudiante), asi que registrar y editar
    //son transacciones
    private static final String SELECCIONAR =
            "SELECT u.id_usuario, u.cui, u.nombre, u.correo, u.telefono, u.direccion, "
            + "e.fecha_nacimiento, e.alergias, e.condiciones_medicas, "
            + "e.encargado_nombre, e.encargado_telefono, e.encargado_parentesco, "
            + "e.encargado2_nombre, e.encargado2_telefono, e.encargado2_parentesco, e.estado "
            + "FROM estudiante e JOIN usuario u ON u.id_usuario = e.id_usuario ";

    @Override
    public Estudiante registrar(Estudiante estudiante) throws ExcepcionCuiDuplicado, ExcepcionPersistencia {
        Connection con = null;
        int idNuevo = 0;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            //1. La persona: sin correo ni contraseña, por eso no puede iniciar sesion todavia
            String sqlUsuario = "INSERT INTO usuario (cui, nombre, correo, telefono, direccion, contrasena, contrasena_provisional) "
                    + "VALUES (?, ?, NULL, ?, ?, NULL, FALSE)";
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, estudiante.getCui());
                ps.setString(2, estudiante.getNombre());
                ps.setString(3, estudiante.getTelefono());
                ps.setString(4, estudiante.getDireccion());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }

            //2. Sus datos de estudiante, con la misma llave
            String sqlEstudiante = "INSERT INTO estudiante (id_usuario, fecha_nacimiento, alergias, condiciones_medicas, "
                    + "encargado_nombre, encargado_telefono, encargado_parentesco, "
                    + "encargado2_nombre, encargado2_telefono, encargado2_parentesco, estado) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO')";
            try (PreparedStatement ps = con.prepareStatement(sqlEstudiante)) {
                ps.setInt(1, idNuevo);
                llenarDatosDeEstudiante(ps, 2, estudiante);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException ex) {
            deshacer(con);
            if (ex.getErrorCode() == 1062) {
                //El UNIQUE del CUI es la garantia real, tambien frente a dos peticiones simultaneas
                throw new ExcepcionCuiDuplicado("El CUI ya está registrado");
            }
            throw new ExcepcionPersistencia("Error al registrar el estudiante", ex);
        } finally {
            cerrar(con);
        }
        final int id = idNuevo;
        return buscarPorId(id).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el estudiante creado", null));
    }

    @Override
    public void editar(Estudiante estudiante)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            EstadoEstudiante actual = bloquear(con, estudiante.getIdUsuario());
            if (actual == EstadoEstudiante.GRADUADO) {
                throw new ExcepcionOperacionNoPermitida("El estudiante está graduado: no admite cambios");
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE usuario SET nombre = ?, telefono = ?, direccion = ? WHERE id_usuario = ?")) {
                ps.setString(1, estudiante.getNombre());
                ps.setString(2, estudiante.getTelefono());
                ps.setString(3, estudiante.getDireccion());
                ps.setInt(4, estudiante.getIdUsuario());
                ps.executeUpdate();
            }
            String sql = "UPDATE estudiante SET fecha_nacimiento = ?, alergias = ?, condiciones_medicas = ?, "
                    + "encargado_nombre = ?, encargado_telefono = ?, encargado_parentesco = ?, "
                    + "encargado2_nombre = ?, encargado2_telefono = ?, encargado2_parentesco = ? WHERE id_usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                llenarDatosDeEstudiante(ps, 1, estudiante);
                ps.setInt(10, estudiante.getIdUsuario());
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException ex) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al editar el estudiante", ex);
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionOperacionNoPermitida ex) {
            deshacer(con);
            throw ex;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public void activar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        transicion(idUsuario, EstadoEstudiante.INACTIVO, EstadoEstudiante.ACTIVO, "El estudiante no está inactivo");
    }

    @Override
    public void desactivar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        //Desactivar con una inscripcion activa se permite: es justo para quien ya no asiste
        transicion(idUsuario, EstadoEstudiante.ACTIVO, EstadoEstudiante.INACTIVO, "El estudiante no está activo");
    }

    @Override
    public void graduar(int idUsuario) throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        transicion(idUsuario, EstadoEstudiante.ACTIVO, EstadoEstudiante.GRADUADO, "Solo se puede graduar a un estudiante activo");
    }

    @Override
    public Optional<Estudiante> buscarPorId(int idUsuario) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE u.id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("Error al buscar el estudiante", ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Estudiante> listar(int pagina, int tamanoPagina, String busqueda, EstadoEstudiante estado)
            throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE (u.nombre LIKE ? OR u.cui LIKE ?) ");
        List<Object> parametros = new ArrayList<>();
        String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
        parametros.add(filtro);
        parametros.add(filtro);
        if (estado != null) {
            sql.append("AND e.estado = ? ");
            parametros.add(estado.name());
        }
        sql.append("ORDER BY u.nombre, u.id_usuario LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<Estudiante> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException ex) {
            throw new ExcepcionPersistencia("Error al listar los estudiantes", ex);
        }
        return lista;
    }

    //Metodos de Apoyo 

    //Lee el estado de la fila bloqueandola. Un graduado se detecta en cada operacion
    private EstadoEstudiante bloquear(Connection con, int idUsuario) throws SQLException, ExcepcionEstudianteNoEncontrado {
        try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM estudiante WHERE id_usuario = ? FOR UPDATE")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionEstudianteNoEncontrado("No existe un estudiante con ese id");
                }
                return EstadoEstudiante.valueOf(rs.getString("estado"));
            }
        }
    }

    //Cambia de estado solo si el estudiante esta en el estado requerido. Un graduado no admite nada
    private void transicion(int idUsuario, EstadoEstudiante requerido, EstadoEstudiante nuevo, String mensajeSiNo)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            EstadoEstudiante actual = bloquear(con, idUsuario);
            if (actual == EstadoEstudiante.GRADUADO) {
                throw new ExcepcionOperacionNoPermitida("El estudiante está graduado: no admite cambios");
            }
            if (actual != requerido) {
                throw new ExcepcionOperacionNoPermitida(mensajeSiNo);
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE estudiante SET estado = ? WHERE id_usuario = ?")) {
                ps.setString(1, nuevo.name());
                ps.setInt(2, idUsuario);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException ex) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al cambiar el estado del estudiante", ex);
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionOperacionNoPermitida ex) {
            deshacer(con);
            throw ex;
        } finally {
            cerrar(con);
        }
    }

    //Nueve parametros consecutivos desde "desde": fecha, medicos y los dos encargados
    private void llenarDatosDeEstudiante(PreparedStatement ps, int desde, Estudiante e) throws SQLException {
        ps.setDate(desde, Date.valueOf(e.getFechaNacimiento()));
        ps.setString(desde + 1, e.getAlergias());
        ps.setString(desde + 2, e.getCondicionesMedicas());
        ps.setString(desde + 3, e.getEncargadoNombre());
        ps.setString(desde + 4, e.getEncargadoTelefono());
        ps.setString(desde + 5, e.getEncargadoParentesco());
        ps.setString(desde + 6, e.getEncargado2Nombre());
        ps.setString(desde + 7, e.getEncargado2Telefono());
        ps.setString(desde + 8, e.getEncargado2Parentesco());
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        //La contraseña no se lee nunca: este objeto solo sirve para mostrar
        return new Estudiante(rs.getInt("id_usuario"), rs.getString("cui"), rs.getString("nombre"),
                rs.getString("correo"), rs.getString("telefono"), rs.getString("direccion"), null,
                rs.getDate("fecha_nacimiento").toLocalDate(), rs.getString("alergias"),
                rs.getString("condiciones_medicas"), rs.getString("encargado_nombre"),
                rs.getString("encargado_telefono"), rs.getString("encargado_parentesco"),
                rs.getString("encargado2_nombre"), rs.getString("encargado2_telefono"),
                rs.getString("encargado2_parentesco"), EstadoEstudiante.valueOf(rs.getString("estado")));
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
