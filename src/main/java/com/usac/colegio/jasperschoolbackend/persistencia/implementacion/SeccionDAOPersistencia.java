/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Carrera;
import com.usac.colegio.jasperschoolbackend.modelo.Grado;
import com.usac.colegio.jasperschoolbackend.modelo.Seccion;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SeccionDAO;
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
public class SeccionDAOPersistencia implements SeccionDAO {
    //Acceso a datos de Seccion. Crear, editar y cambiar de estado son transacciones que bloquean
    private static final String SELECCIONAR =
            "SELECT s.id_seccion, s.nombre, s.cupo_maximo, s.estado, "
            + "g.id_grado, g.nombre AS grado_nombre, g.nivel, g.estado AS grado_estado, "
            + "ca.id_carrera, ca.nombre AS carrera_nombre, ca.estado AS carrera_estado, "
            + "a.id_anio_lectivo, a.nombre AS anio_nombre, a.estado AS anio_estado, "
            + "(SELECT COUNT(*) FROM inscripcion i WHERE i.id_seccion = s.id_seccion AND i.estado = 'ACTIVA') AS inscritos "
            + "FROM seccion s JOIN grado g ON g.id_grado = s.id_grado "
            + "JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
            + "LEFT JOIN carrera ca ON ca.id_carrera = s.id_carrera ";

    @Override
    public Seccion crear(String nombre, int idGrado, Integer idCarrera, int cupoMaximo)
            throws ExcepcionReferenciaInvalida, ExcepcionNombreDuplicado, ExcepcionPersistencia {
        Connection con = null;
        int idNuevo = 0;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            int idAnio = bloquearAnioActivo(con);
            Grado grado = bloquearGradoActivo(con, idGrado);

            boolean esDiversificado = grado.getNivel() == Nivel.DIVERSIFICADO;
            if (esDiversificado && idCarrera == null) {
                throw new ExcepcionReferenciaInvalida("Un grado de Diversificado necesita una carrera");
            }
            if (!esDiversificado && idCarrera != null) {
                throw new ExcepcionReferenciaInvalida("Solo los grados de Diversificado llevan carrera");
            }
            if (idCarrera != null) {
                verificarCarreraActiva(con, idCarrera);
            }
            verificarCurriculoConCursos(con, idGrado, idCarrera);

            String sql = "INSERT INTO seccion (nombre, id_grado, id_anio_lectivo, id_carrera, cupo_maximo, estado) "
                    + "VALUES (?, ?, ?, ?, ?, 'ACTIVO')";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setInt(2, idGrado);
                ps.setInt(3, idAnio);
                if (idCarrera == null) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(4, idCarrera);
                }
                ps.setInt(5, cupoMaximo);
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
                //El UNIQUE (grado, año, carrera_clave, nombre) es la garantia real
                throw new ExcepcionNombreDuplicado("Ya existe una sección con ese nombre en ese grado, año y carrera");
            }
            throw new ExcepcionPersistencia("Error al crear la sección", e);
        } catch (ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        return buscarPorId(idNuevo).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer la sección creada", null));
    }

    @Override
    public void editar(int idSeccion, String nombre, int cupoMaximo)
            throws ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionNombreDuplicado, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            bloquearSeccionDeAnioAbierto(con, idSeccion);
            int inscritos = contarInscritos(con, idSeccion);
            if (cupoMaximo < inscritos) {
                throw new ExcepcionOperacionNoPermitida(
                        "El cupo no puede ser menor que los " + inscritos + " estudiantes inscritos");
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE seccion SET nombre = ?, cupo_maximo = ? WHERE id_seccion = ?")) {
                ps.setString(1, nombre);
                ps.setInt(2, cupoMaximo);
                ps.setInt(3, idSeccion);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionNombreDuplicado("Ya existe una sección con ese nombre en ese grado, año y carrera");
            }
            throw new ExcepcionPersistencia("Error al editar la sección", e);
        } catch (ExcepcionSeccionNoEncontrada | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public void activar(int idSeccion) throws ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        cambiarEstado(idSeccion, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int idSeccion) throws ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        cambiarEstado(idSeccion, EstadoGeneral.INACTIVO);
    }

    private void cambiarEstado(int idSeccion, EstadoGeneral nuevo)
            throws ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            bloquearSeccionDeAnioAbierto(con, idSeccion);
            if (nuevo == EstadoGeneral.INACTIVO && contarInscritos(con, idSeccion) > 0) {
                throw new ExcepcionOperacionNoPermitida("La sección tiene estudiantes inscritos");
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE seccion SET estado = ? WHERE id_seccion = ?")) {
                ps.setString(1, nuevo.name());
                ps.setInt(2, idSeccion);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al cambiar el estado de la sección", e);
        } catch (ExcepcionSeccionNoEncontrada | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<Seccion> buscarPorId(int idSeccion) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE s.id_seccion = ?")) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar la sección", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Seccion> listar(int pagina, int tamanoPagina, String busqueda, Integer idGrado,
                                Integer idAnioLectivo, boolean soloAnioActivo) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE (s.nombre LIKE ? OR g.nombre LIKE ?) ");
        List<Object> parametros = new ArrayList<>();
        String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
        parametros.add(filtro);
        parametros.add(filtro);
        if (idGrado != null) {
            sql.append("AND s.id_grado = ? ");
            parametros.add(idGrado);
        }
        if (idAnioLectivo != null) {
            sql.append("AND s.id_anio_lectivo = ? ");
            parametros.add(idAnioLectivo);
        } else if (soloAnioActivo) {
            sql.append("AND a.estado = 'ACTIVO' ");
        }
        sql.append("ORDER BY g.nombre, ca.nombre, s.nombre, s.id_seccion LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<Seccion> lista = new ArrayList<>();
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
            throw new ExcepcionPersistencia("Error al listar las secciones", e);
        }
        return lista;
    }

    //Metodos de Apoyo

    //Bloquea el año activo mientras se crea la seccion: nadie puede cerrarlo a mitad
    private int bloquearAnioActivo(Connection con) throws SQLException, ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_anio_lectivo FROM anio_lectivo WHERE estado = 'ACTIVO' FOR UPDATE");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new ExcepcionReferenciaInvalida("No hay un año lectivo activo");
            }
            return rs.getInt(1);
        }
    }

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

    private void verificarCarreraActiva(Connection con, int idCarrera) throws SQLException, ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM carrera WHERE id_carrera = ? FOR UPDATE")) {
            ps.setInt(1, idCarrera);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("No existe una carrera con ese id");
                }
                if (EstadoGeneral.valueOf(rs.getString("estado")) != EstadoGeneral.ACTIVO) {
                    throw new ExcepcionReferenciaInvalida("La carrera está inactiva");
                }
            }
        }
    }

    //El grado (y carrera) debe tener un curriculo con al menos un curso: eso es "tener curriculo definido"
    private void verificarCurriculoConCursos(Connection con, int idGrado, Integer idCarrera)
            throws SQLException, ExcepcionReferenciaInvalida {
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
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM curriculo_curso WHERE id_curriculo = ?")) {
            ps.setInt(1, idCurriculo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    throw new ExcepcionReferenciaInvalida("El currículo del grado todavía no tiene cursos");
                }
            }
        }
    }

    //Bloquea la seccion (no el año) y exige que su año lectivo siga activo
    private void bloquearSeccionDeAnioAbierto(Connection con, int idSeccion)
            throws SQLException, ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida {
        String sql = "SELECT a.estado FROM seccion s JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
                + "WHERE s.id_seccion = ? FOR UPDATE OF s";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionSeccionNoEncontrada("No existe una sección con ese id");
                }
                if (EstadoAnioLectivo.valueOf(rs.getString("estado")) != EstadoAnioLectivo.ACTIVO) {
                    throw new ExcepcionOperacionNoPermitida("La sección pertenece a un año lectivo cerrado");
                }
            }
        }
    }

    private int contarInscritos(Connection con, int idSeccion) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM inscripcion WHERE id_seccion = ? AND estado = 'ACTIVA'")) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Seccion mapear(ResultSet rs) throws SQLException {
        Grado grado = new Grado(rs.getInt("id_grado"), rs.getString("grado_nombre"),
                Nivel.valueOf(rs.getString("nivel")), EstadoGeneral.valueOf(rs.getString("grado_estado")));
        Carrera carrera = null;
        int idCarrera = rs.getInt("id_carrera");
        if (!rs.wasNull()) {
            carrera = new Carrera(idCarrera, rs.getString("carrera_nombre"),
                    EstadoGeneral.valueOf(rs.getString("carrera_estado")));
        }
        return new Seccion(rs.getInt("id_seccion"), rs.getString("nombre"), grado, carrera,
                rs.getInt("id_anio_lectivo"), rs.getString("anio_nombre"),
                EstadoAnioLectivo.valueOf(rs.getString("anio_estado")), rs.getInt("cupo_maximo"),
                EstadoGeneral.valueOf(rs.getString("estado")), rs.getInt("inscritos"));
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
}
