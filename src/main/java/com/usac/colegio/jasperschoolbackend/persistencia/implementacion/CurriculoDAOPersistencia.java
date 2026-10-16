/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCurriculoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCurriculoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Carrera;
import com.usac.colegio.jasperschoolbackend.modelo.Curriculo;
import com.usac.colegio.jasperschoolbackend.modelo.Curso;
import com.usac.colegio.jasperschoolbackend.modelo.Grado;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.CurriculoDAO;
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
 * Acceso a datos de Curriculo. Cada operacion que valida antes de escribir usa una transaccion
 * con FOR UPDATE sobre las filas que revisa, para que un grado desactivado entre la revision y
 * la escritura no deje un curriculo apuntando a algo invalido
 */
public class CurriculoDAOPersistencia implements CurriculoDAO{
    //Grado y carrera de un curriculo, con sus nombres, para armar el objeto sin consultas extra
    private static final String SELECCIONAR =
            "SELECT c.id_curriculo, g.id_grado, g.nombre AS grado_nombre, g.nivel, g.estado AS grado_estado, "
            + "ca.id_carrera, ca.nombre AS carrera_nombre, ca.estado AS carrera_estado "
            + "FROM curriculo c JOIN grado g ON g.id_grado = c.id_grado "
            + "LEFT JOIN carrera ca ON ca.id_carrera = c.id_carrera ";

    @Override
    public Curriculo crear(int idGrado, Integer idCarrera)
            throws ExcepcionReferenciaInvalida, ExcepcionCurriculoDuplicado, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            Grado grado = bloquearGradoActivo(con, idGrado);
            boolean esDiversificado = grado.getNivel() == Nivel.DIVERSIFICADO;

            if (esDiversificado && idCarrera == null) {
                con.rollback();
                throw new ExcepcionReferenciaInvalida("Un grado de Diversificado necesita una carrera");
            }
            if (!esDiversificado && idCarrera != null) {
                con.rollback();
                throw new ExcepcionReferenciaInvalida("Solo los grados de Diversificado llevan carrera");
            }
            Carrera carrera = null;
            if (idCarrera != null) {
                carrera = bloquearCarreraActiva(con, idCarrera);
            }

            int idNuevo;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO curriculo (id_grado, id_carrera) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idGrado);
                if (idCarrera == null) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, idCarrera);
                }
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }
            con.commit();
            return new Curriculo(idNuevo, grado, carrera);

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                //el UNIQUE (id_grado, carrera_clave) es la garantia real: sirve tambien con carrera vacia
                throw new ExcepcionCurriculoDuplicado("Ese grado ya tiene un currículo");
            }
            throw new ExcepcionPersistencia("Error al crear el currículo", e);
        } catch (ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<Curriculo> buscarPorId(int idCurriculo) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE c.id_curriculo = ?")) {
            ps.setInt(1, idCurriculo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Curriculo curriculo = mapear(rs);
                curriculo.setCursos(cursosDe(con, idCurriculo));
                return Optional.of(curriculo);
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el currículo", e);
        }
    }

    @Override
    public List<Curriculo> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        String sql = SELECCIONAR + "WHERE g.nombre LIKE ? OR ca.nombre LIKE ? "
                + "ORDER BY g.nombre, ca.nombre, c.id_curriculo LIMIT ? OFFSET ?";
        List<Curriculo> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setInt(3, tamanoPagina);
            ps.setInt(4, (pagina - 1) * tamanoPagina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            //en el listado no se cargan los cursos: se piden al abrir un curriculo
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los currículos", e);
        }
        return lista;
    }

    @Override
    public void agregarCurso(int idCurriculo, int idCurso)
            throws ExcepcionCurriculoNoEncontrado, ExcepcionReferenciaInvalida, ExcepcionCurriculoDuplicado, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            // el curriculo y el curso se bloquean mientras se revisa que sigan siendo validos
            bloquearCurriculo(con, idCurriculo);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado FROM curso WHERE id_curso = ? FOR UPDATE")) {
                ps.setInt(1, idCurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        throw new ExcepcionReferenciaInvalida("No existe un curso con ese id");
                    }
                    if (EstadoGeneral.valueOf(rs.getString("estado")) != EstadoGeneral.ACTIVO) {
                        con.rollback();
                        throw new ExcepcionReferenciaInvalida("Solo se pueden agregar cursos activos");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO curriculo_curso (id_curriculo, id_curso) VALUES (?, ?)")) {
                ps.setInt(1, idCurriculo);
                ps.setInt(2, idCurso);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionCurriculoDuplicado("El curso ya está en el currículo");
            }
            throw new ExcepcionPersistencia("Error al agregar el curso al currículo", e);
        } catch (ExcepcionCurriculoNoEncontrado | ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public void quitarCurso(int idCurriculo, int idCurso)
            throws ExcepcionCurriculoNoEncontrado, ExcepcionReferenciaInvalida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            //Se bloquea el curriculo: dos "quitar" simultaneos no pueden dejarlo vacio entre los dos
            bloquearCurriculo(con, idCurriculo);
            
            //No se quita un curso que tenga maestro asignado en el año lectivo activo
            //Asignar() bloquea la misma fila del curriculo, asi que las dos operaciones no se cruzan
            String sqlAsignaciones = "SELECT 1 FROM asignacion_maestro am "
                    + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                    + "JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
                    + "JOIN curriculo cu ON cu.id_grado = s.id_grado AND cu.carrera_clave = s.carrera_clave "
                    + "WHERE cu.id_curriculo = ? AND am.id_curso = ? AND a.estado = 'ACTIVO' LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sqlAsignaciones)) {
                ps.setInt(1, idCurriculo);
                ps.setInt(2, idCurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ExcepcionReferenciaInvalida(
                                "El curso tiene maestro asignado en el año lectivo activo: quite la asignación antes");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM curriculo_curso WHERE id_curriculo = ? AND id_curso = ?")) {
                ps.setInt(1, idCurriculo);
                ps.setInt(2, idCurso);
                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    throw new ExcepcionCurriculoNoEncontrado("Ese curso no está en el currículo");
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM curriculo_curso WHERE id_curriculo = ?")) {
                ps.setInt(1, idCurriculo);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) {
                        con.rollback(); // se deshace el DELETE
                        throw new ExcepcionReferenciaInvalida("El grado no puede quedar sin ningún curso");
                    }
                }
            }
            //la regla "ni quitar uno que ya tenga notas" se agrega cuando existan las notas
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al quitar el curso del currículo", e);
        } catch (ExcepcionCurriculoNoEncontrado | ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    //Metodos de Apoyo

    private Grado bloquearGradoActivo(Connection con, int idGrado) throws SQLException, ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_grado, nombre, nivel, estado FROM grado WHERE id_grado = ? FOR UPDATE")) {
            ps.setInt(1, idGrado);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("No existe un grado con ese id");
                }
                Grado grado = new Grado(rs.getInt("id_grado"), rs.getString("nombre"),
                        Nivel.valueOf(rs.getString("nivel")), EstadoGeneral.valueOf(rs.getString("estado")));
                if (grado.getEstado() != EstadoGeneral.ACTIVO) {
                    throw new ExcepcionReferenciaInvalida("El grado está inactivo");
                }
                return grado;
            }
        }
    }

    private Carrera bloquearCarreraActiva(Connection con, int idCarrera) throws SQLException, ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_carrera, nombre, estado FROM carrera WHERE id_carrera = ? FOR UPDATE")) {
            ps.setInt(1, idCarrera);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("No existe una carrera con ese id");
                }
                Carrera carrera = new Carrera(rs.getInt("id_carrera"), rs.getString("nombre"),
                        EstadoGeneral.valueOf(rs.getString("estado")));
                if (carrera.getEstado() != EstadoGeneral.ACTIVO) {
                    throw new ExcepcionReferenciaInvalida("La carrera está inactiva");
                }
                return carrera;
            }
        }
    }

    private void bloquearCurriculo(Connection con, int idCurriculo) throws SQLException, ExcepcionCurriculoNoEncontrado {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_curriculo FROM curriculo WHERE id_curriculo = ? FOR UPDATE")) {
            ps.setInt(1, idCurriculo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionCurriculoNoEncontrado("No existe un currículo con ese id");
                }
            }
        }
    }

    private List<Curso> cursosDe(Connection con, int idCurriculo) throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT c.id_curso, c.nombre, c.descripcion, c.estado FROM curso c "
                + "JOIN curriculo_curso cc ON cc.id_curso = c.id_curso "
                + "WHERE cc.id_curriculo = ? ORDER BY c.nombre")) {
            ps.setInt(1, idCurriculo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cursos.add(new Curso(rs.getInt("id_curso"), rs.getString("nombre"),
                            rs.getString("descripcion"), EstadoGeneral.valueOf(rs.getString("estado"))));
                }
            }
        }
        return cursos;
    }

    private Curriculo mapear(ResultSet rs) throws SQLException {
        Grado grado = new Grado(rs.getInt("id_grado"), rs.getString("grado_nombre"),
                Nivel.valueOf(rs.getString("nivel")), EstadoGeneral.valueOf(rs.getString("grado_estado")));
        Carrera carrera = null;
        int idCarrera = rs.getInt("id_carrera");
        if (!rs.wasNull()) {
            carrera = new Carrera(idCarrera, rs.getString("carrera_nombre"),
                    EstadoGeneral.valueOf(rs.getString("carrera_estado")));
        }
        return new Curriculo(rs.getInt("id_curriculo"), grado, carrera);
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
