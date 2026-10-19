/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoPago;
import com.usac.colegio.jasperschoolbackend.enums.EstadoPago;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPagoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Pago;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.PagoDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionActividadNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionMultaNoEncontrada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
/**
 *
 * @author eduar
 */
public class PagoDAOPersistencia implements PagoDAO {
    private static final String SELECCIONAR =
            "SELECT p.id_pago, p.id_estudiante, ue.nombre AS estudiante_nombre, p.id_anio_lectivo, "
            + "a.nombre AS anio_nombre, p.concepto, p.monto, p.mes, ac.nombre AS actividad_nombre, p.fecha_pago, "
            + "us.nombre AS secretaria_nombre, p.estado, p.motivo_anulacion "
            + "FROM pago p "
            + "JOIN usuario ue ON ue.id_usuario = p.id_estudiante "
            + "JOIN anio_lectivo a ON a.id_anio_lectivo = p.id_anio_lectivo "
            + "JOIN usuario us ON us.id_usuario = p.id_secretaria "
            + "LEFT JOIN actividad_extracurricular ac ON ac.id_actividad = p.id_actividad ";

    //Lo que se necesita saber de la inscripcion activa del estudiante en el año activo
    private record Contexto(int idAnio, int idGrado, LocalDate inicio, LocalDate fin) {
    }

    @Override
    public Pago registrarInscripcion(int idEstudiante, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia {
        Connection con = null;
        int idNuevo;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            Contexto ctx = bloquearInscripcionActiva(con, idEstudiante);
            BigDecimal monto = obtenerTarifa(con, ctx, ConceptoPago.INSCRIPCION);
            if (existePagoVigente(con, "concepto = 'INSCRIPCION'", idEstudiante, ctx.idAnio(), null)) {
                throw new ExcepcionOperacionNoPermitida("La inscripción de este año ya está pagada");
            }
            idNuevo = insertarPago(con, idEstudiante, ctx.idAnio(), ConceptoPago.INSCRIPCION, monto, null, idSecretaria);
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionOperacionNoPermitida("La inscripción de este año ya está pagada");
            }
            throw new ExcepcionPersistencia("Error al registrar el pago de inscripción", e);
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        final int id = idNuevo;
        return buscarPorId(id).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el pago creado", null));
    }

    @Override
    public List<Pago> registrarMensualidades(int idEstudiante, List<Integer> meses, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia {
        Connection con = null;
        List<Integer> ids = new ArrayList<>();
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            Contexto ctx = bloquearInscripcionActiva(con, idEstudiante);

            //Los meses validos salen de las fechas del año lectivo (se asume que empieza y termina en el mismo año calendario)
            int primero = ctx.inicio().getMonthValue();
            int ultimo = ctx.fin().getMonthValue();
            for (int mes : meses) {
                if (mes < primero || mes > ultimo) {
                    throw new ExcepcionOperacionNoPermitida("El mes " + nombreMes(mes) + " está fuera del año lectivo (de "
                            + nombreMes(primero) + " a " + nombreMes(ultimo) + ")");
                }
            }

            BigDecimal monto = obtenerTarifa(con, ctx, ConceptoPago.MENSUALIDAD);

            // si alguno ya esta pagado no se guarda ninguno, y se dice cuales
            for (int mes : meses) {
                if (existePagoVigente(con, "concepto = 'MENSUALIDAD' AND mes = ?", idEstudiante, ctx.idAnio(), mes)) {
                    throw new ExcepcionOperacionNoPermitida("La mensualidad de " + nombreMes(mes) + " ya está pagada");
                }
            }
            for (int mes : meses) {
                ids.add(insertarPago(con, idEstudiante, ctx.idAnio(), ConceptoPago.MENSUALIDAD, monto, mes, idSecretaria));
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionOperacionNoPermitida("Alguna de las mensualidades ya estaba pagada");
            }
            throw new ExcepcionPersistencia("Error al registrar el pago de mensualidad", e);
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        List<Pago> creados = new ArrayList<>();
        for (int id : ids) {
            creados.add(buscarPorId(id).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el pago creado", null)));
        }
        return creados;
    }

    @Override
    public Optional<Pago> buscarPorId(int idPago) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE p.id_pago = ?")) {
            ps.setInt(1, idPago);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el pago", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Pago> listar(int pagina, int tamanoPagina, Integer idEstudiante, ConceptoPago concepto,
                             LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (idEstudiante != null) {
            sql.append("AND p.id_estudiante = ? ");
            parametros.add(idEstudiante);
        }
        if (concepto != null) {
            sql.append("AND p.concepto = ? ");
            parametros.add(concepto.name());
        }
        if (desde != null) {
            sql.append("AND p.fecha_pago >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND p.fecha_pago <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append("ORDER BY p.fecha_pago DESC, p.id_pago DESC LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<Pago> lista = new ArrayList<>();
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
            throw new ExcepcionPersistencia("Error al listar los pagos", e);
        }
        return lista;
    }

    @Override
    public Pago registrarActividad(int idEstudiante, int idActividad, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionActividadNoEncontrada, ExcepcionOperacionNoPermitida,
            ExcepcionReferenciaInvalida, ExcepcionPersistencia {
        Connection con = null;
        int idNuevo;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            Contexto ctx = bloquearInscripcionActiva(con, idEstudiante);

            int cupo;
            BigDecimal costo;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT estado, fecha, cupo, costo FROM actividad_extracurricular WHERE id_actividad = ? FOR UPDATE")) {
                ps.setInt(1, idActividad);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionActividadNoEncontrada("No existe una actividad con ese id");
                    }
                    if (!"ACTIVO".equals(rs.getString("estado"))) {
                        throw new ExcepcionOperacionNoPermitida("La actividad está inactiva");
                    }
                    if (rs.getDate("fecha").toLocalDate().isBefore(LocalDate.now())) {
                        throw new ExcepcionOperacionNoPermitida("La actividad ya se realizó");
                    }
                    cupo = rs.getInt("cupo");
                    costo = rs.getBigDecimal("costo");
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM pago WHERE id_estudiante = ? AND id_actividad = ? AND estado = 'VIGENTE'")) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, idActividad);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new ExcepcionOperacionNoPermitida("El estudiante ya pagó esta actividad");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM pago WHERE id_actividad = ? AND estado = 'VIGENTE'")) {
                ps.setInt(1, idActividad);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) >= cupo) {
                        throw new ExcepcionOperacionNoPermitida("La actividad no tiene cupo disponible");
                    }
                }
            }
            String sql = "INSERT INTO pago (id_estudiante, id_anio_lectivo, concepto, monto, id_actividad, fecha_pago, "
                    + "id_secretaria, estado) VALUES (?, ?, 'ACTIVIDAD', ?, ?, ?, ?, 'VIGENTE')";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, ctx.idAnio());
                ps.setBigDecimal(3, costo);
                ps.setInt(4, idActividad);
                ps.setDate(5, Date.valueOf(LocalDate.now()));
                ps.setInt(6, idSecretaria);
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
                throw new ExcepcionOperacionNoPermitida("El estudiante ya pagó esta actividad");
            }
            throw new ExcepcionPersistencia("Error al registrar el pago de actividad", e);
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionActividadNoEncontrada | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        final int id = idNuevo;
        return buscarPorId(id).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el pago creado", null));
    }

    @Override
    public void anular(int idPago, String motivo)
            throws ExcepcionPagoNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            Integer idMulta;
            String sql = "SELECT p.estado, p.id_multa, a.estado AS anio_estado FROM pago p "
                    + "JOIN anio_lectivo a ON a.id_anio_lectivo = p.id_anio_lectivo WHERE p.id_pago = ? FOR UPDATE OF p";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idPago);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionPagoNoEncontrado("No existe un pago con ese id");
                    }
                    if (!"VIGENTE".equals(rs.getString("estado"))) {
                        throw new ExcepcionOperacionNoPermitida("El pago ya está anulado");
                    }
                    if (!"ACTIVO".equals(rs.getString("anio_estado"))) {
                        throw new ExcepcionOperacionNoPermitida("El pago pertenece a un año lectivo cerrado");
                    }
                    int multa = rs.getInt("id_multa");
                    idMulta = rs.wasNull() ? null : multa;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE pago SET estado = 'ANULADO', motivo_anulacion = ?, fecha_anulacion = ? WHERE id_pago = ?")) {
                ps.setString(1, motivo);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, idPago);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE comprobante SET estado = 'ANULADO' WHERE id_pago = ?")) {
                ps.setInt(1, idPago);
                ps.executeUpdate();
            }
            if (idMulta != null) {
                try (PreparedStatement ps = con.prepareStatement("UPDATE multa SET estado = 'PENDIENTE' WHERE id_multa = ?")) {
                    ps.setInt(1, idMulta);
                    ps.executeUpdate();
                }
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al anular el pago", e);
        } catch (ExcepcionPagoNoEncontrado | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }
    
    @Override
    public Pago registrarMulta(int idMulta, int idSecretaria)
            throws ExcepcionMultaNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia {
        Connection con = null;
        int idNuevo;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            //1. la multa: debe existir y estar PENDIENTE. Se bloquea: dos cobros simultaneos no la pagan dos veces
            BigDecimal monto;
            int idEstudiante;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT mu.estado, mu.monto, p.id_prestatario FROM multa mu "
                    + "JOIN prestamo p ON p.id_prestamo = mu.id_prestamo WHERE mu.id_multa = ? FOR UPDATE OF mu")) {
                ps.setInt(1, idMulta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionMultaNoEncontrada("No existe una multa con ese id");
                    }
                    if (!"PENDIENTE".equals(rs.getString("estado"))) {
                        throw new ExcepcionOperacionNoPermitida("La multa ya está pagada");
                    }
                    monto = rs.getBigDecimal("monto");   //El monto se copia de la multa
                    idEstudiante = rs.getInt("id_prestatario");
                }
            }

            //2. el año donde entra el dinero: el activo. No se exige inscripcion
            int idAnio;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_anio_lectivo FROM anio_lectivo WHERE estado = 'ACTIVO' FOR UPDATE");
                 ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("No hay un año lectivo activo");
                }
                idAnio = rs.getInt(1);
            }

            //3. el pago y la multa, juntos
            String sql = "INSERT INTO pago (id_estudiante, id_anio_lectivo, concepto, monto, id_multa, fecha_pago, "
                    + "id_secretaria, estado) VALUES (?, ?, 'MULTA', ?, ?, ?, ?, 'VIGENTE')";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, idAnio);
                ps.setBigDecimal(3, monto);
                ps.setInt(4, idMulta);
                ps.setDate(5, Date.valueOf(LocalDate.now()));
                ps.setInt(6, idSecretaria);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idNuevo = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE multa SET estado = 'PAGADA' WHERE id_multa = ?")) {
                ps.setInt(1, idMulta);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            if (e.getErrorCode() == 1062) {
                throw new ExcepcionOperacionNoPermitida("La multa ya está pagada");
            }
            throw new ExcepcionPersistencia("Error al registrar el pago de la multa", e);
        } catch (ExcepcionMultaNoEncontrada | ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        final int id = idNuevo;
        return buscarPorId(id).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer el pago creado", null));
    }
    
    //Metodos de Apoyo 

    //El estudiante debe existir y tener una inscripcion ACTIVA en el año lectivo activo. Se bloquea esa fila
    private Contexto bloquearInscripcionActiva(Connection con, int idEstudiante)
            throws SQLException, ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM estudiante WHERE id_usuario = ?")) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionEstudianteNoEncontrado("No existe un estudiante con ese id");
                }
            }
        }
        String sql = "SELECT s.id_grado, a.id_anio_lectivo, a.fecha_inicio, a.fecha_fin "
                + "FROM inscripcion i JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN anio_lectivo a ON a.id_anio_lectivo = i.id_anio_lectivo "
                + "WHERE i.id_estudiante = ? AND i.estado = 'ACTIVA' AND a.estado = 'ACTIVO' FOR UPDATE OF i";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionOperacionNoPermitida("El estudiante no está inscrito en el año lectivo activo");
                }
                return new Contexto(rs.getInt("id_anio_lectivo"), rs.getInt("id_grado"),
                        rs.getDate("fecha_inicio").toLocalDate(), rs.getDate("fecha_fin").toLocalDate());
            }
        }
    }

    //Monto de la tarifa del grado en el año activo. Ese monto se COPIA al pago: cambiar la tarifa no altera pagos viejos
    private BigDecimal obtenerTarifa(Connection con, Contexto ctx, ConceptoPago concepto)
            throws SQLException, ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT monto FROM tarifa WHERE id_grado = ? AND id_anio_lectivo = ? AND concepto = ?")) {
            ps.setInt(1, ctx.idGrado());
            ps.setInt(2, ctx.idAnio());
            ps.setString(3, concepto.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("No hay una tarifa de "
                            + (concepto == ConceptoPago.INSCRIPCION ? "inscripción" : "mensualidad")
                            + " definida para el grado del estudiante en el año activo");
                }
                return rs.getBigDecimal("monto");
            }
        }
    }

    //Aviso amable antes del INSERT; la UNIQUE sigue siendo la garantia real
    private boolean existePagoVigente(Connection con, String condicion, int idEstudiante, int idAnio, Integer mes)
            throws SQLException {
        String sql = "SELECT 1 FROM pago WHERE id_estudiante = ? AND id_anio_lectivo = ? AND estado = 'VIGENTE' AND "
                + condicion;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idAnio);
            if (mes != null) {
                ps.setInt(3, mes);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int insertarPago(Connection con, int idEstudiante, int idAnio, ConceptoPago concepto, BigDecimal monto,
                             Integer mes, int idSecretaria) throws SQLException {
        String sql = "INSERT INTO pago (id_estudiante, id_anio_lectivo, concepto, monto, mes, fecha_pago, id_secretaria, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'VIGENTE')";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idAnio);
            ps.setString(3, concepto.name());
            ps.setBigDecimal(4, monto);
            if (mes == null) {
                ps.setNull(5, Types.TINYINT);
            } else {
                ps.setInt(5, mes);
            }
            ps.setDate(6, Date.valueOf(LocalDate.now()));
            ps.setInt(7, idSecretaria);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String nombreMes(int mes) {
        return Month.of(mes).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"));
    }

    private Pago mapear(ResultSet rs) throws SQLException {
        int mes = rs.getInt("mes");
        Integer mesONull = rs.wasNull() ? null : mes;
        return new Pago(rs.getInt("id_pago"), rs.getInt("id_estudiante"), rs.getString("estudiante_nombre"),
                rs.getInt("id_anio_lectivo"), rs.getString("anio_nombre"),
                ConceptoPago.valueOf(rs.getString("concepto")), rs.getBigDecimal("monto"), mesONull,
                rs.getString("actividad_nombre"), rs.getDate("fecha_pago").toLocalDate(),
                rs.getString("secretaria_nombre"), EstadoPago.valueOf(rs.getString("estado")),
                rs.getString("motivo_anulacion"));
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
