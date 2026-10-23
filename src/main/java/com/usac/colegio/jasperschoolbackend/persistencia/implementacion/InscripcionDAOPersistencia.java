/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.enums.EstadoEstudiante;
import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.EstadoInscripcion;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionInscripcionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSeccionNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Inscripcion;
import com.usac.colegio.jasperschoolbackend.modelo.ResultadoInscripcion;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.InscripcionDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import com.usac.colegio.jasperschoolbackend.utilidades.ContrasenaUtil;
import com.usac.colegio.jasperschoolbackend.utilidades.GeneradorContrasena;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class InscripcionDAOPersistencia implements InscripcionDAO{
    //Acceso a datos de Inscripcion. Inscribir es una sola transaccion: bloquea al estudiante y a la seccion
    //(FOR UPDATE) y revisa estado, año, duplicado, cupo y correo antes de escribir
    private static final String SELECCIONAR =
            "SELECT i.id_inscripcion, i.fecha_inscripcion, i.estado, "
            + "u.id_usuario, u.cui, u.nombre AS estudiante_nombre, "
            + "s.id_seccion, s.nombre AS seccion_nombre, g.id_grado, g.nombre AS grado_nombre, g.nivel, "
            + "ca.nombre AS carrera_nombre, a.id_anio_lectivo, a.nombre AS anio_nombre, a.estado AS anio_estado "
            + "FROM inscripcion i "
            + "JOIN usuario u ON u.id_usuario = i.id_estudiante "
            + "JOIN seccion s ON s.id_seccion = i.id_seccion "
            + "JOIN grado g ON g.id_grado = s.id_grado "
            + "JOIN anio_lectivo a ON a.id_anio_lectivo = i.id_anio_lectivo "
            + "LEFT JOIN carrera ca ON ca.id_carrera = s.id_carrera ";

    @Override
    public ResultadoInscripcion inscribir(int idEstudiante, int idSeccion, String correo, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionSeccionNoEncontrada, ExcepcionOperacionNoPermitida,
            ExcepcionCorreoDuplicado, ExcepcionPersistencia {
        Connection con = null;
        int idNueva;
        String contrasenaProvisional = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            //1. El estudiante: debe existir y estar ACTIVO. Se bloquea su fila (y su cuenta)
            boolean tieneAcceso;
            String sqlEstudiante = "SELECT e.estado, u.correo FROM estudiante e JOIN usuario u ON u.id_usuario = e.id_usuario "
                    + "WHERE e.id_usuario = ? FOR UPDATE";
            try (PreparedStatement ps = con.prepareStatement(sqlEstudiante)) {
                ps.setInt(1, idEstudiante);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionEstudianteNoEncontrado("No existe un estudiante con ese id");
                    }
                    if (EstadoEstudiante.valueOf(rs.getString("estado")) != EstadoEstudiante.ACTIVO) {
                        throw new ExcepcionOperacionNoPermitida("El estudiante no está activo");
                    }
                    tieneAcceso = rs.getString("correo") != null;
                }
            }

            //2. La seccion: debe existir, estar activa y ser del año lectivo activo. Se bloquea: serializa el cupo
            int idAnio;
            int cupo;
            String sqlSeccion = "SELECT s.estado, s.id_anio_lectivo, s.cupo_maximo, a.estado AS anio_estado "
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
                        throw new ExcepcionOperacionNoPermitida("La sección está inactiva");
                    }
                    idAnio = rs.getInt("id_anio_lectivo");
                    cupo = rs.getInt("cupo_maximo");
                }
            }

            //3. No puede estar ya inscrito en ese año
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM inscripcion WHERE id_estudiante = ? AND id_anio_lectivo = ? AND estado = 'ACTIVA'")) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, idAnio);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ExcepcionOperacionNoPermitida("El estudiante ya está inscrito en este año lectivo");
                    }
                }
            }

            //4. Cupo: inscripciones ACTIVAS de la seccion
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM inscripcion WHERE id_seccion = ? AND estado = 'ACTIVA'")) {
                ps.setInt(1, idSeccion);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) >= cupo) {
                        throw new ExcepcionOperacionNoPermitida("La sección no tiene cupo disponible");
                    }
                }
            }

            //5. El acceso: solo si todavia no lo tiene. En una reinscripcion la cuenta no se toca
            if (!tieneAcceso) {
                if (correo == null) {
                    throw new ExcepcionOperacionNoPermitida("El estudiante todavía no tiene acceso: indique su correo");
                }
                try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM usuario WHERE correo = ?")) {
                    ps.setString(1, correo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new ExcepcionCorreoDuplicado("El correo ya está registrado");
                        }
                    }
                }
                contrasenaProvisional = GeneradorContrasena.generar();
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario SET correo = ?, contrasena = ?, contrasena_provisional = TRUE WHERE id_usuario = ?")) {
                    ps.setString(1, correo);
                    ps.setString(2, ContrasenaUtil.hashear(contrasenaProvisional)); // SUPUESTO: nombre del metodo
                    ps.setInt(3, idEstudiante);
                    ps.executeUpdate();
                }
            }

            //6. La inscripcion
            String sqlInscripcion = "INSERT INTO inscripcion (id_estudiante, id_seccion, id_anio_lectivo, fecha_inscripcion, "
                    + "id_secretaria, estado) VALUES (?, ?, ?, ?, ?, 'ACTIVA')";
            try (PreparedStatement ps = con.prepareStatement(sqlInscripcion, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, idSeccion);
                ps.setInt(3, idAnio);
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.setInt(5, idSecretaria);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idNueva = rs.getInt(1);
                }
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                //las UNIQUE son la garantia real frente a dos peticiones simultaneas
                String mensaje = e.getMessage() == null ? "" : e.getMessage();
                if (mensaje.contains("uq_inscripcion_activa")) {
                    throw new ExcepcionOperacionNoPermitida("El estudiante ya está inscrito en este año lectivo");
                }
                throw new ExcepcionCorreoDuplicado("El correo ya está registrado");
            }
            throw new ExcepcionPersistencia("Error al inscribir al estudiante", e);
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionSeccionNoEncontrada | ExcepcionOperacionNoPermitida
                 | ExcepcionCorreoDuplicado e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        final int id = idNueva;
        Inscripcion creada = buscarPorId(id)
                .orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer la inscripción creada", null));
        return new ResultadoInscripcion(creada, contrasenaProvisional);
    }

    @Override
    public void anular(int idInscripcion)
            throws ExcepcionInscripcionNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            String sql = "SELECT i.estado, a.estado AS anio_estado FROM inscripcion i "
                    + "JOIN anio_lectivo a ON a.id_anio_lectivo = i.id_anio_lectivo "
                    + "WHERE i.id_inscripcion = ? FOR UPDATE OF i";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idInscripcion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionInscripcionNoEncontrada("No existe una inscripción con ese id");
                    }
                    if (EstadoAnioLectivo.valueOf(rs.getString("anio_estado")) != EstadoAnioLectivo.ACTIVO) {
                        throw new ExcepcionOperacionNoPermitida("La inscripción pertenece a un año lectivo cerrado");
                    }
                    if (EstadoInscripcion.valueOf(rs.getString("estado")) != EstadoInscripcion.ACTIVA) {
                        throw new ExcepcionOperacionNoPermitida("La inscripción ya está anulada");
                    }
                }
            }
            //la regla "el estudiante ya tiene notas en esa seccion" se agrega en la Iteracion 4, cuando existan las notas
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE inscripcion SET estado = 'ANULADA' WHERE id_inscripcion = ?")) {
                ps.setInt(1, idInscripcion);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al anular la inscripción", e);
        } catch (ExcepcionInscripcionNoEncontrada | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<Inscripcion> buscarPorId(int idInscripcion) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE i.id_inscripcion = ?")) {
            ps.setInt(1, idInscripcion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar la inscripción", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Inscripcion> listar(int pagina, int tamanoPagina, Integer idAnio, Integer idGrado, Integer idSeccion,
                                    Integer idEstudiante) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (idAnio != null) {
            sql.append("AND i.id_anio_lectivo = ? ");
            parametros.add(idAnio);
        } else {
            sql.append("AND a.estado = 'ACTIVO' ");
        }
        if (idGrado != null) {
            sql.append("AND s.id_grado = ? ");
            parametros.add(idGrado);
        }
        if (idSeccion != null) {
            sql.append("AND i.id_seccion = ? ");
            parametros.add(idSeccion);
        }
        if (idEstudiante != null) {
            sql.append("AND i.id_estudiante = ? ");
            parametros.add(idEstudiante);
        }
        sql.append("ORDER BY g.nombre, s.nombre, u.nombre, i.id_inscripcion LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<Inscripcion> lista = new ArrayList<>();
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
            throw new ExcepcionPersistencia("Error al listar las inscripciones", e);
        }
        return lista;
    }
    
    @Override
    public List<Inscripcion> listarActivasDeSeccion(int idSeccion) throws ExcepcionPersistencia {
        String sql = SELECCIONAR + "WHERE i.id_seccion = ? AND i.estado = 'ACTIVA' ORDER BY u.nombre, i.id_inscripcion";
        List<Inscripcion> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar la nómina", e);
        }
        return lista;
    }

    //Metodos de Apoyo

    private Inscripcion mapear(ResultSet rs) throws SQLException {
        return new Inscripcion(rs.getInt("id_inscripcion"), rs.getInt("id_usuario"), rs.getString("estudiante_nombre"),
                rs.getString("cui"), rs.getInt("id_seccion"), rs.getString("seccion_nombre"), rs.getInt("id_grado"),
                rs.getString("grado_nombre"), Nivel.valueOf(rs.getString("nivel")), rs.getString("carrera_nombre"),
                rs.getInt("id_anio_lectivo"), rs.getString("anio_nombre"),
                EstadoAnioLectivo.valueOf(rs.getString("anio_estado")), rs.getDate("fecha_inscripcion").toLocalDate(),
                EstadoInscripcion.valueOf(rs.getString("estado")));
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
