/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEstudianteNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.modelo.Saldo;
import com.usac.colegio.jasperschoolbackend.modelo.Solvencia;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.SolvenciaDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
/**
 *
 * @author eduar
 */
public class SolvenciaDAOPersistencia implements SolvenciaDAO {
    //Solvencia y saldo. Un mes se EXIGE cuando hoy es posterior al dia DIAS_DE_GRACIA de ese mes, y solo
    //se exigen meses del año lectivo. Pagar adelantado no genera problema: un mes pagado cuenta como pagado
    public static final int DIAS_DE_GRACIA = 10;

    private static final String SELECCIONAR =
            "SELECT sv.id_solvencia, sv.id_estudiante, ue.nombre AS estudiante_nombre, a.nombre AS anio_nombre, "
            + "us.nombre AS secretaria_nombre, sv.fecha_emision FROM solvencia sv "
            + "JOIN usuario ue ON ue.id_usuario = sv.id_estudiante "
            + "JOIN anio_lectivo a ON a.id_anio_lectivo = sv.id_anio_lectivo "
            + "JOIN usuario us ON us.id_usuario = sv.id_secretaria ";

    @Override
    public Saldo consultarSaldo(int idEstudiante)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion()) {
            return calcular(con, idEstudiante, null);
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al calcular el saldo", e);
        }
    }

    @Override
    public Solvencia emitir(int idEstudiante, int idSecretaria)
            throws ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionReferenciaInvalida,
            ExcepcionPersistencia {
        Connection con = null;
        int idResultado;
        boolean yaExistia;   //Se declara aqui para poder usarla despues del try
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            int[] anio = new int[1];
            Saldo saldo = calcular(con, idEstudiante, anio);   //Bloquea la inscripcion: nadie cobra ni emite a mitad
            if (!saldo.isSolvente()) {
                throw new ExcepcionOperacionNoPermitida("El estudiante tiene un saldo pendiente de Q"
                        + String.format(Locale.US, "%,.2f", saldo.getMonto()) + ": no se puede generar la solvencia");
            }

            //Si ya se emitio una este mes, se devuelve esa en vez de crear otra
            Integer existente = null;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_solvencia FROM solvencia WHERE id_estudiante = ? AND id_anio_lectivo = ? "
                    + "AND YEAR(fecha_emision) = YEAR(CURDATE()) AND MONTH(fecha_emision) = MONTH(CURDATE()) "
                    + "ORDER BY id_solvencia DESC LIMIT 1")) {
                ps.setInt(1, idEstudiante);
                ps.setInt(2, anio[0]);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existente = rs.getInt(1);
                    }
                }
            }

            if (existente != null) {
                idResultado = existente;
                yaExistia = true;
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO solvencia (id_estudiante, id_anio_lectivo, id_secretaria, fecha_emision) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idEstudiante);
                    ps.setInt(2, anio[0]);
                    ps.setInt(3, idSecretaria);
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        idResultado = rs.getInt(1);
                    }
                }
                yaExistia = false;
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al emitir la solvencia", e);
        } catch (ExcepcionEstudianteNoEncontrado | ExcepcionOperacionNoPermitida | ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        try (Connection c = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = c.prepareStatement(SELECCIONAR + "WHERE sv.id_solvencia = ?")) {
            ps.setInt(1, idResultado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Solvencia s = mapear(rs);
                s.setYaExistia(yaExistia);   //Le dice al controlador si fue nueva o ya existia
                return s;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("No se pudo leer la solvencia", e);
        }
    }

    @Override
    public List<Solvencia> listar(int pagina, int tamanoPagina, Integer idEstudiante) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE 1 = 1 ");
        if (idEstudiante != null) {
            sql.append("AND sv.id_estudiante = ? ");
        }
        sql.append("ORDER BY sv.fecha_emision DESC, sv.id_solvencia DESC LIMIT ? OFFSET ?");
        List<Solvencia> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (idEstudiante != null) {
                ps.setInt(i++, idEstudiante);
            }
            ps.setInt(i++, tamanoPagina);
            ps.setInt(i, (pagina - 1) * tamanoPagina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar las solvencias", e);
        }
        return lista;
    }

    //Metodos de Apoyo

    //Si anioSalida no es null, bloquea la inscripcion (FOR UPDATE) y devuelve el id del año en anioSalida[0]
    private Saldo calcular(Connection con, int idEstudiante, int[] anioSalida)
            throws SQLException, ExcepcionEstudianteNoEncontrado, ExcepcionOperacionNoPermitida,
            ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM estudiante WHERE id_usuario = ?")) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionEstudianteNoEncontrado("No existe un estudiante con ese id");
                }
            }
        }
        int idGrado;
        int idAnio;
        String anioNombre;
        LocalDate inicio;
        LocalDate fin;
        String sql = "SELECT s.id_grado, a.id_anio_lectivo, a.nombre, a.fecha_inicio, a.fecha_fin "
                + "FROM inscripcion i JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN anio_lectivo a ON a.id_anio_lectivo = i.id_anio_lectivo "
                + "WHERE i.id_estudiante = ? AND i.estado = 'ACTIVA' AND a.estado = 'ACTIVO'"
                + (anioSalida != null ? " FOR UPDATE OF i" : "");
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionOperacionNoPermitida("El estudiante no está inscrito en el año lectivo activo");
                }
                idGrado = rs.getInt("id_grado");
                idAnio = rs.getInt("id_anio_lectivo");
                anioNombre = rs.getString("nombre");
                inicio = rs.getDate("fecha_inicio").toLocalDate();
                fin = rs.getDate("fecha_fin").toLocalDate();
            }
        }
        if (anioSalida != null) {
            anioSalida[0] = idAnio;
        }

        boolean inscripcionPendiente;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM pago WHERE id_estudiante = ? AND id_anio_lectivo = ? AND concepto = 'INSCRIPCION' "
                + "AND estado = 'VIGENTE'")) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idAnio);
            try (ResultSet rs = ps.executeQuery()) {
                inscripcionPendiente = !rs.next();
            }
        }
        Set<Integer> pagados = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT mes FROM pago WHERE id_estudiante = ? AND id_anio_lectivo = ? AND concepto = 'MENSUALIDAD' "
                + "AND estado = 'VIGENTE'")) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idAnio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagados.add(rs.getInt(1));
                }
            }
        }

        LocalDate hoy = LocalDate.now();
        List<Integer> pendientes = new ArrayList<>();
        for (int mes = inicio.getMonthValue(); mes <= fin.getMonthValue(); mes++) {
            boolean vencido = hoy.isAfter(LocalDate.of(inicio.getYear(), mes, DIAS_DE_GRACIA));
            if (vencido && !pagados.contains(mes)) {
                pendientes.add(mes);
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        if (inscripcionPendiente) {
            total = total.add(tarifa(con, idGrado, idAnio, "INSCRIPCION", "inscripción"));
        }
        if (!pendientes.isEmpty()) {
            BigDecimal mensualidad = tarifa(con, idGrado, idAnio, "MENSUALIDAD", "mensualidad");
            total = total.add(mensualidad.multiply(BigDecimal.valueOf(pendientes.size())));
        }
        return new Saldo(anioNombre, total, inscripcionPendiente, pendientes);
    }

    private BigDecimal tarifa(Connection con, int idGrado, int idAnio, String concepto, String nombre)
            throws SQLException, ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT monto FROM tarifa WHERE id_grado = ? AND id_anio_lectivo = ? AND concepto = ?")) {
            ps.setInt(1, idGrado);
            ps.setInt(2, idAnio);
            ps.setString(3, concepto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("No hay una tarifa de " + nombre
                            + " definida para el grado del estudiante: no se puede calcular el saldo");
                }
                return rs.getBigDecimal(1);
            }
        }
    }

    private Solvencia mapear(ResultSet rs) throws SQLException {
        return new Solvencia(rs.getInt("id_solvencia"), rs.getInt("id_estudiante"), rs.getString("estudiante_nombre"),
                rs.getString("anio_nombre"), rs.getString("secretaria_nombre"),
                rs.getTimestamp("fecha_emision").toLocalDateTime());
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
