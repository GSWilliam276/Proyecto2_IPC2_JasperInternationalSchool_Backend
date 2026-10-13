/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCursoEnUso;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCursoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CursoDAO;
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
public class CursoDAOPersistencia implements CursoDAO {
    //Acceso a datos de Curso
    @Override
    public void crear(Curso curso) throws ExcepcionPersistencia {
        String sql = "INSERT INTO curso (nombre, descripcion, estado) VALUES (?, ?, ?)";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, curso.getNombre());
            ps.setString(2, curso.getDescripcion());
            ps.setString(3, curso.getEstado().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    curso.setIdCurso(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al crear el curso");
        }
    }

    @Override
    public void editar(Curso curso) throws ExcepcionPersistencia {
        String sql = "UPDATE curso SET nombre = ?, descripcion = ? WHERE id_curso = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, curso.getNombre());
            ps.setString(2, curso.getDescripcion());
            ps.setInt(3, curso.getIdCurso());
            if (ps.executeUpdate() == 0) {
                throw new ExcepcionCursoNoEncontrado("No existe un curso con ese id");
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al editar el curso");
        }
    }

    @Override
    public Optional<Curso> buscarPorId(int id) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM curso WHERE id_curso = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el curso", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Curso> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        //ORDER BY: sin orden fijo
        String sql = "SELECT * FROM curso WHERE nombre LIKE ? ORDER BY nombre, id_curso LIMIT ? OFFSET ?";
        List<Curso> cursos = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (busqueda == null ? "" : busqueda) + "%");
            ps.setInt(2, tamanoPagina);
            ps.setInt(3, (pagina - 1) * tamanoPagina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cursos.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los cursos", e);
        }
        return cursos;
    }

    @Override
    public void activar(int idCurso) throws ExcepcionCursoNoEncontrado, ExcepcionPersistencia {
        cambiarEstado(idCurso, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int idCurso) throws ExcepcionCursoNoEncontrado, ExcepcionCursoEnUso, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            //Se bloquea la fila del curso: agregarCurso tambien la bloquea,
            //asi nadie lo enlaza a un curriculo entre la revision y el UPDATE
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_curso FROM curso WHERE id_curso = ? FOR UPDATE")) {
                ps.setInt(1, idCurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        throw new ExcepcionCursoNoEncontrado("No existe un curso con ese id");
                    }
                }
            }
            String enUso = "SELECT 1 FROM curriculo_curso cc "
                    + "JOIN curriculo c ON c.id_curriculo = cc.id_curriculo "
                    + "JOIN grado g ON g.id_grado = c.id_grado "
                    + "WHERE cc.id_curso = ? AND g.estado = 'ACTIVO' LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(enUso)) {
                ps.setInt(1, idCurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        con.rollback();
                        throw new ExcepcionCursoEnUso("El curso forma parte del currículo de un grado activo");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE curso SET estado = 'INACTIVO' WHERE id_curso = ?")) {
                ps.setInt(1, idCurso);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al desactivar el curso", e);
        } catch (ExcepcionCursoNoEncontrado | ExcepcionCursoEnUso e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    private void deshacer(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignorada) {
                //si el rollback falla, la conexion se cierra de todos modos
            }
        }
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignorada) {
                //nada que hacer: la conexion ya no se usa
            }
        }
    }

    //Metodos de Apoyo 

    private void cambiarEstado(int idCurso, EstadoGeneral nuevo) throws ExcepcionCursoNoEncontrado, ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE curso SET estado = ? WHERE id_curso = ?")) {
            ps.setString(1, nuevo.name());
            ps.setInt(2, idCurso);
            if (ps.executeUpdate() == 0) {
                throw new ExcepcionCursoNoEncontrado("No existe un curso con ese id");
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al cambiar el estado del curso", e);
        }
    }

    private Curso mapear(ResultSet rs) throws SQLException {
        return new Curso(rs.getInt("id_curso"), rs.getString("nombre"), rs.getString("descripcion"),
                EstadoGeneral.valueOf(rs.getString("estado")));
    }

    //El UNIQUE de la base es la garantia real contra duplicados
    private ExcepcionPersistencia traducir(SQLException e, String mensajeGenerico) {
        if (e.getErrorCode() == 1062) {
            return new ExcepcionNombreDuplicado("Ya existe un curso con ese nombre");
        }
        return new ExcepcionPersistencia(mensajeGenerico, e);
    }
}
