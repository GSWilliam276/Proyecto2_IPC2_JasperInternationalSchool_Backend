/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.ConceptoTarifa;
import com.usac.colegio.jasperschoolbackend.enums.EstadoAnioLectivo;
import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Nivel;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionReferenciaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionTarifaDuplicada;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionTarifaNoEncontrada;
import com.usac.colegio.jasperschoolbackend.modelo.Grado;
import com.usac.colegio.jasperschoolbackend.modelo.Tarifa;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.TarifaDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
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
public class TarifaDAOPersistencia implements TarifaDAO {
    private static final String SELECCIONAR =
            "SELECT t.id_tarifa, t.concepto, t.monto, "
            + "g.id_grado, g.nombre AS grado_nombre, g.nivel, g.estado AS grado_estado, "
            + "a.id_anio_lectivo, a.nombre AS anio_nombre, a.estado AS anio_estado "
            + "FROM tarifa t JOIN grado g ON g.id_grado = t.id_grado "
            + "JOIN anio_lectivo a ON a.id_anio_lectivo = t.id_anio_lectivo ";

    @Override
    public Tarifa crear(int idGrado, ConceptoTarifa concepto, BigDecimal monto)
            throws ExcepcionReferenciaInvalida, ExcepcionTarifaDuplicada, ExcepcionPersistencia {
        Connection con = null;
        int idNuevo;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            int idAnio = bloquearAnioActivo(con);
            verificarGradoActivo(con, idGrado);

            String sql = "INSERT INTO tarifa (id_grado, id_anio_lectivo, concepto, monto, estado) VALUES (?, ?, ?, ?, 'ACTIVO')";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idGrado);
                ps.setInt(2, idAnio);
                ps.setString(3, concepto.name());
                ps.setBigDecimal(4, monto);
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
                //El UNIQUE (grado, año, concepto) es la garantia real
                throw new ExcepcionTarifaDuplicada("Ya existe una tarifa de ese concepto para ese grado en el año activo");
            }
            throw new ExcepcionPersistencia("Error al crear la tarifa", e);
        } catch (ExcepcionReferenciaInvalida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
        return buscarPorId(idNuevo).orElseThrow(() -> new ExcepcionPersistencia("No se pudo leer la tarifa creada", null));
    }

    @Override
    public void editar(int idTarifa, BigDecimal monto)
            throws ExcepcionTarifaNoEncontrada, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            String sql = "SELECT a.estado FROM tarifa t JOIN anio_lectivo a ON a.id_anio_lectivo = t.id_anio_lectivo "
                    + "WHERE t.id_tarifa = ? FOR UPDATE OF t";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idTarifa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionTarifaNoEncontrada("No existe una tarifa con ese id");
                    }
                    if (EstadoAnioLectivo.valueOf(rs.getString("estado")) != EstadoAnioLectivo.ACTIVO) {
                        throw new ExcepcionOperacionNoPermitida("La tarifa pertenece a un año lectivo cerrado");
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE tarifa SET monto = ? WHERE id_tarifa = ?")) {
                ps.setBigDecimal(1, monto);
                ps.setInt(2, idTarifa);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al editar la tarifa", e);
        } catch (ExcepcionTarifaNoEncontrada | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<Tarifa> buscarPorId(int idTarifa) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SELECCIONAR + "WHERE t.id_tarifa = ?")) {
            ps.setInt(1, idTarifa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar la tarifa", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tarifa> listar(int pagina, int tamanoPagina, Integer idGrado, boolean todos) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(SELECCIONAR).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();
        if (idGrado != null) {
            sql.append("AND t.id_grado = ? ");
            parametros.add(idGrado);
        }
        if (!todos) {
            sql.append("AND a.estado = 'ACTIVO' ");
        }
        sql.append("ORDER BY g.nombre, t.concepto, t.id_tarifa LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<Tarifa> lista = new ArrayList<>();
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
            throw new ExcepcionPersistencia("Error al listar las tarifas", e);
        }
        return lista;
    }

    //Metodos de Apoyo

    //Bloquea el año activo mientras se crea la tarifa: nadie puede cerrarlo a mitad
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

    private void verificarGradoActivo(Connection con, int idGrado) throws SQLException, ExcepcionReferenciaInvalida {
        try (PreparedStatement ps = con.prepareStatement("SELECT estado FROM grado WHERE id_grado = ? FOR UPDATE")) {
            ps.setInt(1, idGrado);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionReferenciaInvalida("No existe un grado con ese id");
                }
                if (EstadoGeneral.valueOf(rs.getString("estado")) != EstadoGeneral.ACTIVO) {
                    throw new ExcepcionReferenciaInvalida("El grado está inactivo");
                }
            }
        }
    }

    private Tarifa mapear(ResultSet rs) throws SQLException {
        Grado grado = new Grado(rs.getInt("id_grado"), rs.getString("grado_nombre"),
                Nivel.valueOf(rs.getString("nivel")), EstadoGeneral.valueOf(rs.getString("grado_estado")));
        return new Tarifa(rs.getInt("id_tarifa"), grado, rs.getInt("id_anio_lectivo"), rs.getString("anio_nombre"),
                EstadoAnioLectivo.valueOf(rs.getString("anio_estado")),
                ConceptoTarifa.valueOf(rs.getString("concepto")), rs.getBigDecimal("monto"));
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
