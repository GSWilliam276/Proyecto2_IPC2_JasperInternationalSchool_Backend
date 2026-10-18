/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionLibroNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPrestamoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Prestamo;
import com.usac.colegio.jasperschoolbackend.modelo.Prestatario;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.PrestamoDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class PrestamoDAOPersistencia implements PrestamoDAO {
    //Acceso a datos de Prestamo. Prestar y devolver son transacciones que bloquean el libro (FOR UPDATE),
    //la misma fila que bloquean editar y desactivar libro: los ejemplares nunca quedan descuadrados
    private static final String SELECCIONAR =
            "SELECT p.id_prestamo, p.id_libro, l.titulo AS libro_titulo, p.id_prestatario, "
            + "u.nombre AS prestatario_nombre, "
            + "CASE WHEN e.id_usuario IS NOT NULL THEN 'ESTUDIANTE' ELSE 'MAESTRO' END AS tipo, "
            + "ub.nombre AS bibliotecario_nombre, p.fecha_prestamo, p.fecha_esperada, p.fecha_devolucion, "
            + "mu.monto AS multa_monto, mu.estado AS multa_estado "
            + "FROM prestamo p "
            + "JOIN libro l ON l.id_libro = p.id_libro "
            + "JOIN usuario u ON u.id_usuario = p.id_prestatario "
            + "JOIN usuario ub ON ub.id_usuario = p.id_bibliotecario "
            + "LEFT JOIN estudiante e ON e.id_usuario = p.id_prestatario "
            + "LEFT JOIN multa mu ON mu.id_prestamo = p.id_prestamo ";

    @Override
    public Prestamo registrar(int idLibro, int idPrestatario, int idBibliotecario)
            throws ExcepcionLibroNoEncontrado, ExcepcionReferenciaInvalida, ExcepcionOperacionNoPermitida,
            ExcepcionPersistencia {
        Connection con = null;
        int idNuevo;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);
            LocalDate hoy = LocalDate.now();

            //1. el libro: debe existir, estar activo y tener ejemplares. Se bloquea
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado, ejemplares_disponibles FROM libro WHERE id_libro = ? FOR UPDATE")) {
                ps.setInt(1, idLibro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionLibroNoEncontrado("No existe un libro con ese id");
                    }
                    if (!"ACTIVO".equals(rs.getString("estado"))) {
                        throw new ExcepcionOperacionNoPermitida("El libro está inactivo");
                    }
                    if (rs.getInt("ejemplares_disponibles") <= 0) {
                        throw new ExcepcionOperacionNoPermitida("No hay ejemplares disponibles de este libro");
                    }
                }
            }

            //2. el prestatario: estudiante o maestro con estado ACTIVO
            if (tipoPrestatarioActivo(con, idPrestatario) == null) {
                throw new ExcepcionReferenciaInvalida("No existe un estudiante o maestro activo con ese id");
            }

            //3. no puede tener prestamos en mora
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM prestamo WHERE id_prestatario = ? AND fecha_devolucion IS NULL "
                    + "AND fecha_esperada < ? LIMIT 1")) {
                ps.setInt(1, idPrestatario);
                ps.setDate(2, Date.valueOf(hoy));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ExcepcionOperacionNoPermitida("El prestatario tiene préstamos en mora: debe devolverlos primero");
                    }
                }
            }

            //4. el plazo vigente
            int dias;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT dias_prestamo FROM parametros_biblioteca WHERE id_parametros = 1");
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("Los parámetros de biblioteca no están configurados");
                }
                dias = rs.getInt(1);
            }

            //5. el prestamo y el ejemplar que sale
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO prestamo (id_libro, id_prestatario, id_bibliotecario, fecha_prestamo, fecha_esperada) "
                    + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idLibro);
                ps.setInt(2, idPrestatario);
                ps.setInt(3, idBibliotecario);
                ps.setDate(4, Date.valueOf(hoy));
                ps.setDate(5, Date.valueOf(hoy.plusDays(dias)));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE libro SET ejemplares_disponibles = ejemplares_disponibles - 1 WHERE id_libro = ?")) {
                ps.setInt(1, idLibro);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al registrar el préstamo", e);
        } catch (ExcepcionPersistencia e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        final int id = idNuevo;
        return buscarPorId(id).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el préstamo creado", null));
    }

    @Override
    public Prestamo devolver(int idPrestamo)
            throws ExcepcionPrestamoNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);
            LocalDate hoy = LocalDate.now();

            int idLibro;
            int idPrestatario;
            LocalDate esperada;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_libro, id_prestatario, fecha_esperada, fecha_devolucion FROM prestamo "
                    + "WHERE id_prestamo = ? FOR UPDATE")) {
                ps.setInt(1, idPrestamo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionPrestamoNoEncontrado("No existe un préstamo con ese id");
                    }
                    if (rs.getDate("fecha_devolucion") != null) {
                        throw new ExcepcionOperacionNoPermitida("El préstamo ya fue devuelto");
                    }
                    idLibro = rs.getInt("id_libro");
                    idPrestatario = rs.getInt("id_prestatario");
                    esperada = rs.getDate("fecha_esperada").toLocalDate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM libro WHERE id_libro = ? FOR UPDATE")) {
                ps.setInt(1, idLibro);
                ps.executeQuery().close();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE prestamo SET fecha_devolucion = ? WHERE id_prestamo = ?")) {
                ps.setDate(1, Date.valueOf(hoy));
                ps.setInt(2, idPrestamo);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE libro SET ejemplares_disponibles = ejemplares_disponibles + 1 WHERE id_libro = ?")) {
                ps.setInt(1, idLibro);
                ps.executeUpdate();
            }

            //La multa: solo estudiantes, y solo si hubo atraso. A un maestro se le registra el atraso y nada mas
            long diasAtraso = ChronoUnit.DAYS.between(esperada, hoy);
            if (diasAtraso > 0 && esEstudiante(con, idPrestatario)) {
                BigDecimal multaPorDia;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT multa_por_dia FROM parametros_biblioteca WHERE id_parametros = 1");
                     ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionPersistencia("Los parámetros de biblioteca no están configurados", null);
                    }
                    multaPorDia = rs.getBigDecimal(1);
                }
                BigDecimal monto = multaPorDia.multiply(BigDecimal.valueOf(diasAtraso));
                if (monto.signum() > 0) {   //La tabla exige monto > 0
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO multa (id_prestamo, dias_atraso, monto, estado, fecha_generacion) "
                            + "VALUES (?, ?, ?, 'PENDIENTE', ?)")) {
                        ps.setInt(1, idPrestamo);
                        ps.setInt(2, (int) diasAtraso);
                        ps.setBigDecimal(3, monto);   //El monto se COPIA: cambiar la multa despues no lo altera
                        ps.setDate(4, Date.valueOf(hoy));
                        ps.executeUpdate();
                    }
                }
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al registrar la devolución", e);
        } catch (ExcepcionPersistencia e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        return buscarPorId(idPrestamo)
                .orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el préstamo", null));
    }

    @Override
    public Optional<Prestamo> buscarPorId(int idPrestamo) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE p.id_prestamo = ?")) {
            ps.setInt(1, idPrestamo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el préstamo", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Prestamo> listar(int pagina, int tamanoPagina, String estado, Integer idLibro, Integer idPrestatario,
                                 LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if ("PRESTADO".equals(estado)) {
            sql.append("AND p.fecha_devolucion IS NULL ");
        } else if ("DEVUELTO".equals(estado)) {
            sql.append("AND p.fecha_devolucion IS NOT NULL ");
        } else if ("EN_MORA".equals(estado)) {
            sql.append("AND p.fecha_devolucion IS NULL AND p.fecha_esperada < ? ");
            parametros.add(Date.valueOf(LocalDate.now()));
        }
        if (idLibro != null) {
            sql.append("AND p.id_libro = ? ");
            parametros.add(idLibro);
        }
        if (idPrestatario != null) {
            sql.append("AND p.id_prestatario = ? ");
            parametros.add(idPrestatario);
        }
        if (desde != null) {
            sql.append("AND p.fecha_prestamo >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND p.fecha_prestamo <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append("ORDER BY p.fecha_prestamo DESC, p.id_prestamo DESC LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<Prestamo> lista = new ArrayList<>();
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
            throw new ExcepcionPersistencia("Error al listar los préstamos", e);
        }
        return lista;
    }

    @Override
    public List<Prestatario> listarPrestatarios(String busqueda) throws ExcepcionPersistencia {
        String sql = "SELECT u.id_usuario, u.nombre, u.cui, 'ESTUDIANTE' AS tipo FROM estudiante e "
                + "JOIN usuario u ON u.id_usuario = e.id_usuario "
                + "WHERE e.estado = 'ACTIVO' AND (u.nombre LIKE ? OR u.cui LIKE ?) "
                + "UNION ALL "
                + "SELECT u.id_usuario, u.nombre, u.cui, 'MAESTRO' AS tipo FROM maestro m "
                + "JOIN usuario u ON u.id_usuario = m.id_usuario "
                + "WHERE m.estado = 'ACTIVO' AND (u.nombre LIKE ? OR u.cui LIKE ?) "
                + "ORDER BY nombre LIMIT 100";
        String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
        List<Prestatario> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, filtro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Prestatario(rs.getInt("id_usuario"), rs.getString("nombre"), rs.getString("cui"),
                            rs.getString("tipo")));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los prestatarios", e);
        }
        return lista;
    }

    //Metodos de Apoyo

    //ESTUDIANTE, MAESTRO, o null si no existe o no esta activo
    private String tipoPrestatarioActivo(Connection con, int idUsuario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 'ESTUDIANTE' FROM estudiante WHERE id_usuario = ? AND estado = 'ACTIVO' "
                + "UNION ALL SELECT 'MAESTRO' FROM maestro WHERE id_usuario = ? AND estado = 'ACTIVO'")) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private boolean esEstudiante(Connection con, int idUsuario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM estudiante WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Date devolucion = rs.getDate("fecha_devolucion");
        return new Prestamo(rs.getInt("id_prestamo"), rs.getInt("id_libro"), rs.getString("libro_titulo"),
                rs.getInt("id_prestatario"), rs.getString("prestatario_nombre"), rs.getString("tipo"),
                rs.getString("bibliotecario_nombre"), rs.getDate("fecha_prestamo").toLocalDate(),
                rs.getDate("fecha_esperada").toLocalDate(), devolucion == null ? null : devolucion.toLocalDate(),
                rs.getBigDecimal("multa_monto"), rs.getString("multa_estado"));
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
