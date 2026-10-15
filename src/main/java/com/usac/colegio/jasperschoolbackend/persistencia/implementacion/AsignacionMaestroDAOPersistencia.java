/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionDuplicada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAsignacionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.AsignacionMaestro;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
import com.usac.colegio.jasperschoolbackend.modelo.Maestro;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.AsignacionMaestroDAO;
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
 * Acceso a datos de AsignacionMaestro. Asignar es una transaccion que bloquea (FOR UPDATE)
 * la seccion, el curriculo y el maestro mientras revisa que todo siga siendo valido
 */
public class AsignacionMaestroDAOPersistencia implements AsignacionMaestroDAO{
    private static final String SELECCIONAR =
            "SELECT am.id_asignacion, s.id_seccion, s.nombre AS seccion_nombre, "
            + "g.id_grado, g.nombre AS grado_nombre, g.nivel, ca.id_carrera, ca.nombre AS carrera_nombre, "
            + "a.id_anio_lectivo, a.nombre AS anio_nombre, a.estado AS anio_estado, "
            + "c.id_curso, c.nombre AS curso_nombre, u.id_usuario AS id_maestro, u.nombre AS maestro_nombre "
            + "FROM asignacion_maestro am "
            + "JOIN seccion s ON s.id_seccion = am.id_seccion "
            + "JOIN grado g ON g.id_grado = s.id_grado "
            + "JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
            + "JOIN curso c ON c.id_curso = am.id_curso "
            + "JOIN usuario u ON u.id_usuario = am.id_maestro "
            + "LEFT JOIN carrera ca ON ca.id_carrera = s.id_carrera ";

    @Override
    public AsignacionMaestro asignar(int idSeccion, int idCurso, int idMaestro)
            throws ExcepcionSeccionNoEncontrada, ExcepcionReferenciaInvalida, ExcepcionOperacionNoPermitida,
            ExcepcionAsignacionDuplicada, ExcepcionPersistencia {
        Connection con = null;
        int idNuevo;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            //1. La seccion: debe existir, estar activa y ser del año lectivo activo
            int idGrado;
            Integer idCarrera;
            String sqlSeccion = "SELECT s.id_grado, s.id_carrera, s.estado, a.estado AS anio_estado "
                    + "FROM seccion s JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
                    + "WHERE s.id_seccion = ? FOR UPDATE OF s";
            try (PreparedStatement ps = con.prepareStatement(sqlSeccion)) {
                ps.setInt(1, idSeccion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionSeccionNoEncontrada("No existe una sección con ese id");
                    }
                    if (EstadoAnioLectivo.valueOf(rs.getString("anio_estado")) != EstadoAnioLectivo.ACTIVO) {
                        throw new ExcepcionOperacionNoPermitida("La sección pertenece a un año lectivo cerrado");
                    }
                    if (EstadoGeneral.valueOf(rs.getString("estado")) != EstadoGeneral.ACTIVO) {
                        throw new ExcepcionReferenciaInvalida("La sección está inactiva");
                    }
                    idGrado = rs.getInt("id_grado");
                    int carrera = rs.getInt("id_carrera");
                    idCarrera = rs.wasNull() ? null : carrera;
                }
            }

            //2. El curso debe formar parte del curriculo de ese grado (y carrera): se bloquea el curriculo
            int idCurriculo;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_curriculo FROM curriculo WHERE id_grado = ? AND carrera_clave = ? FOR UPDATE")) {
                ps.setInt(1, idGrado);
                ps.setInt(2, idCarrera == null ? 0 : idCarrera);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionReferenciaInvalida("El grado no tiene currículo definido");
                    }
                    idCurriculo = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM curriculo_curso WHERE id_curriculo = ? AND id_curso = ?")) {
                ps.setInt(1, idCurriculo);
                ps.setInt(2, idCurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionReferenciaInvalida("El curso no forma parte del currículo de ese grado");
                    }
                }
            }

            //4. El maestro debe existir y estar activo. Se bloquea su fila: desactivar un maestro
            //    la bloquea tambien, asi nadie lo desactiva entre esta revision y el insert
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado FROM maestro WHERE id_usuario = ? FOR UPDATE")) {
                ps.setInt(1, idMaestro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionReferenciaInvalida("No existe un maestro con ese id");
                    }
                    if (EstadoGeneral.valueOf(rs.getString("estado")) != EstadoGeneral.ACTIVO) {
                        throw new ExcepcionReferenciaInvalida("El maestro está inactivo");
                    }
                }
            }

            //4. La asignacion. El UNIQUE (seccion, curso) es la garantia de "un solo maestro por curso"
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO asignacion_maestro (id_seccion, id_curso, id_maestro, estado) VALUES (?, ?, ?, 'ACTIVO')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idSeccion);
                ps.setInt(2, idCurso);
                ps.setInt(3, idMaestro);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionAsignacionDuplicada("Ese curso ya tiene un maestro asignado en esa sección");
            }
            throw new ExcepcionPersistencia("Error al asignar el maestro", e);
        } catch (ExcepcionSeccionNoEncontrada | ExcepcionReferenciaInvalida | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        return buscarPorId(idNuevo).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer la asignación creada", null));
    }

    @Override
    public void quitar(int idAsignacion) throws ExcepcionAsignacionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            String sql = "SELECT a.estado FROM asignacion_maestro am "
                    + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                    + "JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
                    + "WHERE am.id_asignacion = ? FOR UPDATE OF am";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idAsignacion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionAsignacionNoEncontrada("No existe una asignación con ese id");
                    }
                    if (EstadoAnioLectivo.valueOf(rs.getString("estado")) != EstadoAnioLectivo.ACTIVO) {
                        throw new ExcepcionOperacionNoPermitida("La asignación pertenece a un año lectivo cerrado");
                    }
                }
            }
            //La regla "el maestro ya registro notas en ese curso" se agrega en la Iteracion 4, cuando existan las notas
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM asignacion_maestro WHERE id_asignacion = ?")) {
                ps.setInt(1, idAsignacion);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al quitar la asignación", e);
        } catch (ExcepcionAsignacionNoEncontrada | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<AsignacionMaestro> buscarPorId(int idAsignacion) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE am.id_asignacion = ?")) {
            ps.setInt(1, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar la asignación", e);
        }
        return Optional.empty();
    }

    @Override
    public List<AsignacionMaestro> listar(int pagina, int tamanoPagina, Integer idMaestro, Integer idGrado,
                                          Integer idSeccion, boolean todos) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (idMaestro != null) {
            sql.append("AND am.id_maestro = ? ");
            parametros.add(idMaestro);
        }
        if (idGrado != null) {
            sql.append("AND s.id_grado = ? ");
            parametros.add(idGrado);
        }
        if (idSeccion != null) {
            sql.append("AND am.id_seccion = ? ");
            parametros.add(idSeccion);
        }
        if (!todos) {
            sql.append("AND a.estado = 'ACTIVO' ");
        }
        sql.append("ORDER BY g.nombre, ca.nombre, s.nombre, c.nombre, am.id_asignacion LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<AsignacionMaestro> lista = new ArrayList<>();
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
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar las asignaciones", e);
        }
        return lista;
    }

    @Override
    public List<Curso> cursosSinMaestro(int idSeccion) throws ExcepcionSeccionNoEncontrada, ExcepcionPersistencia {
        String sql = "SELECT c.id_curso, c.nombre, c.descripcion, c.estado "
                + "FROM seccion s "
                + "JOIN curriculo cu ON cu.id_grado = s.id_grado AND cu.carrera_clave = s.carrera_clave "
                + "JOIN curriculo_curso cc ON cc.id_curriculo = cu.id_curriculo "
                + "JOIN curso c ON c.id_curso = cc.id_curso "
                + "WHERE s.id_seccion = ? "
                + "AND NOT EXISTS (SELECT 1 FROM asignacion_maestro am WHERE am.id_seccion = s.id_seccion AND am.id_curso = c.id_curso) "
                + "ORDER BY c.nombre, c.id_curso";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM seccion WHERE id_seccion = ?")) {
                ps.setInt(1, idSeccion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionSeccionNoEncontrada("No existe una sección con ese id");
                    }
                }
            }
            List<Curso> cursos = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idSeccion);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cursos.add(new Curso(rs.getInt("id_curso"), rs.getString("nombre"),
                                rs.getString("descripcion"), EstadoGeneral.valueOf(rs.getString("estado"))));
                    }
                }
            }
            return cursos;
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los cursos sin maestro", e);
        }
    }

    @Override
    public List<Maestro> listarMaestrosActivos() throws ExcepcionPersistencia {
        String sql = "SELECT u.id_usuario, u.cui, u.nombre, u.correo, u.telefono, u.direccion, m.estado "
                + "FROM maestro m JOIN usuario u ON u.id_usuario = m.id_usuario "
                + "WHERE m.estado = 'ACTIVO' ORDER BY u.nombre, u.id_usuario";
        List<Maestro> maestros = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                //La contraseña no se lee: este objeto solo sirve para mostrar y elegir
                maestros.add(new Maestro(rs.getInt("id_usuario"), rs.getString("cui"), rs.getString("nombre"),
                        rs.getString("correo"), rs.getString("telefono"), rs.getString("direccion"),
                        null, EstadoGeneral.valueOf(rs.getString("estado"))));
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los maestros", e);
        }
        return maestros;
    }

    //Metodos de Apoyo

    private AsignacionMaestro mapear(ResultSet rs) throws SQLException {
        int carrera = rs.getInt("id_carrera");
        Integer idCarrera = rs.wasNull() ? null : carrera;
        return new AsignacionMaestro(rs.getInt("id_asignacion"), rs.getInt("id_seccion"), rs.getString("seccion_nombre"),
                rs.getInt("id_grado"), rs.getString("grado_nombre"), Nivel.valueOf(rs.getString("nivel")),
                idCarrera, rs.getString("carrera_nombre"), rs.getInt("id_anio_lectivo"), rs.getString("anio_nombre"),
                EstadoAnioLectivo.valueOf(rs.getString("anio_estado")), rs.getInt("id_curso"),
                rs.getString("curso_nombre"), rs.getInt("id_maestro"), rs.getString("maestro_nombre"));
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
