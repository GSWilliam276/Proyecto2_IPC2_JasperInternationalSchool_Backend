/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.FilaIngreso;
import com.usac.colegio.jasperschoolbackend.modelo.FilaMora;
import com.usac.colegio.jasperschoolbackend.modelo.FilaPago;
import com.usac.colegio.jasperschoolbackend.modelo.Inscrito;
import com.usac.colegio.jasperschoolbackend.modelo.NotaTotal;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.ReporteDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author eduar
 */
public class ReporteDAOPersistencia implements ReporteDAO {
    
    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
        "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
    //Consultas de los reportes. Solo lectura
    @Override
    public List<FilaMora> prestamosEnMora(LocalDate desde, LocalDate hasta, Integer pagina, Integer tamano)
            throws ExcepcionPersistencia {
        //Reporte de Mora
        StringBuilder sql = new StringBuilder(
                "SELECT l.titulo, u.nombre, "
                + "CASE WHEN e.id_usuario IS NOT NULL THEN 'Estudiante' ELSE 'Maestro' END AS tipo, "
                + "p.fecha_prestamo, p.fecha_esperada, DATEDIFF(?, p.fecha_esperada) AS dias, "
                + "CASE WHEN e.id_usuario IS NOT NULL THEN DATEDIFF(?, p.fecha_esperada) * pb.multa_por_dia "
                + "ELSE 0 END AS recargo "
                + "FROM prestamo p JOIN libro l ON l.id_libro = p.id_libro "
                + "JOIN usuario u ON u.id_usuario = p.id_prestatario "
                + "LEFT JOIN estudiante e ON e.id_usuario = p.id_prestatario "
                + "JOIN parametros_biblioteca pb ON pb.id_parametros = 1 "
                + "WHERE p.fecha_devolucion IS NULL AND p.fecha_esperada < ? ");
        Date hoy = Date.valueOf(LocalDate.now());
        List<Object> parametros = new ArrayList<>();
        parametros.add(hoy);
        parametros.add(hoy);
        parametros.add(hoy);
        if (desde != null) {
            sql.append("AND p.fecha_prestamo >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND p.fecha_prestamo <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append("ORDER BY dias DESC, u.nombre, p.id_prestamo ");
        if (pagina != null && tamano != null) {
            sql.append("LIMIT ? OFFSET ?");
            parametros.add(tamano);
            parametros.add((pagina - 1) * tamano);
        }

        List<FilaMora> filas = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean maestro = "Maestro".equals(rs.getString("tipo"));
                    filas.add(new FilaMora(rs.getString("titulo"), rs.getString("nombre"), rs.getString("tipo"),
                            rs.getDate("fecha_prestamo").toString(), rs.getDate("fecha_esperada").toString(),
                            maestro ? null : rs.getInt("dias"), maestro ? null : rs.getBigDecimal("recargo")));                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar los préstamos en mora", e);
        }
        return filas;
    }
    
    @Override
    public Object[] totalesMora(LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*), COALESCE(SUM(CASE WHEN e.id_usuario IS NOT NULL "
                + "THEN DATEDIFF(?, p.fecha_esperada) * pb.multa_por_dia ELSE 0 END), 0) "
                + "FROM prestamo p LEFT JOIN estudiante e ON e.id_usuario = p.id_prestatario "
                + "JOIN parametros_biblioteca pb ON pb.id_parametros = 1 "
                + "WHERE p.fecha_devolucion IS NULL AND p.fecha_esperada < ? ");
        Date hoy = Date.valueOf(LocalDate.now());
        List<Object> parametros = new ArrayList<>();
        parametros.add(hoy);
        parametros.add(hoy);
        if (desde != null) {
            sql.append("AND p.fecha_prestamo >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND p.fecha_prestamo <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Object[] {rs.getLong(1), rs.getBigDecimal(2)};
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al totalizar los préstamos en mora", e);
        }
    }
    
    @Override
    public List<FilaIngreso> ingresos(LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia {
        //Reporte de Ingresos
        StringBuilder sql = new StringBuilder(
                "SELECT concepto, COUNT(*) AS cantidad, SUM(monto) AS total FROM pago WHERE estado = 'VIGENTE' ");
        List<Object> parametros = new ArrayList<>();
        if (desde != null) {
            sql.append("AND fecha_pago >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND fecha_pago <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append("GROUP BY concepto");

        //Los cuatro conceptos aparecen siempre, aunque no tengan pagos
        Map<String, FilaIngreso> porConcepto = new LinkedHashMap<>();
        porConcepto.put("INSCRIPCION", new FilaIngreso("Inscripción", 0, BigDecimal.ZERO));
        porConcepto.put("MENSUALIDAD", new FilaIngreso("Mensualidad", 0, BigDecimal.ZERO));
        porConcepto.put("ACTIVIDAD", new FilaIngreso("Actividad", 0, BigDecimal.ZERO));
        porConcepto.put("MULTA", new FilaIngreso("Multa", 0, BigDecimal.ZERO));
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String clave = rs.getString("concepto");
                    FilaIngreso base = porConcepto.get(clave);
                    porConcepto.put(clave, new FilaIngreso(base.concepto(), rs.getInt("cantidad"),
                            rs.getBigDecimal("total")));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar los ingresos", e);
        }
        return new ArrayList<>(porConcepto.values());
    }
    
    //Reporte Historial de Pagos
    @Override
    public String nombreEstudiante(int idEstudiante) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT u.nombre FROM usuario u JOIN estudiante e ON e.id_usuario = u.id_usuario WHERE u.id_usuario = ?")) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el estudiante", e);
        }
    }

    @Override
    public List<FilaPago> historialPagos(int idEstudiante, LocalDate desde, LocalDate hasta, Integer pagina,
                                         Integer tamano) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(
                "SELECT p.fecha_pago, p.concepto, p.mes, ac.nombre AS actividad, p.monto FROM pago p "
                + "LEFT JOIN actividad_extracurricular ac ON ac.id_actividad = p.id_actividad "
                + "WHERE p.id_estudiante = ? AND p.estado = 'VIGENTE' ");
        List<Object> parametros = new ArrayList<>();
        parametros.add(idEstudiante);
        if (desde != null) {
            sql.append("AND p.fecha_pago >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND p.fecha_pago <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        sql.append("ORDER BY p.fecha_pago DESC, p.id_pago DESC ");
        if (pagina != null && tamano != null) {
            sql.append("LIMIT ? OFFSET ?");
            parametros.add(tamano);
            parametros.add((pagina - 1) * tamano);
        }
        List<FilaPago> filas = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String detalle;
                    switch (rs.getString("concepto")) {
                        case "INSCRIPCION" -> detalle = "Inscripción";
                        case "MENSUALIDAD" -> detalle = "Mensualidad de " + MESES[rs.getInt("mes") - 1];
                        case "ACTIVIDAD" -> detalle = "Actividad: " + rs.getString("actividad");
                        default -> detalle = "Multa de biblioteca";
                    }
                    filas.add(new FilaPago(rs.getDate("fecha_pago").toString(), detalle, rs.getBigDecimal("monto")));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar el historial de pagos", e);
        }
        return filas;
    }

    @Override
    public Object[] totalPagos(int idEstudiante, LocalDate desde, LocalDate hasta) throws ExcepcionPersistencia {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*), COALESCE(SUM(monto), 0) FROM pago WHERE id_estudiante = ? AND estado = 'VIGENTE' ");
        List<Object> parametros = new ArrayList<>();
        parametros.add(idEstudiante);
        if (desde != null) {
            sql.append("AND fecha_pago >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND fecha_pago <= ? ");
            parametros.add(Date.valueOf(hasta));
        }
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Object[] {rs.getLong(1), rs.getBigDecimal(2)};
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al totalizar los pagos", e);
        }
    }
    
    //Reporte Solventes y Morosos
    @Override
    public String[] anio(Integer idAnio) throws ExcepcionPersistencia {
        String sql = idAnio == null
                ? "SELECT id_anio_lectivo, nombre, estado FROM anio_lectivo WHERE estado = 'ACTIVO' LIMIT 1"
                : "SELECT id_anio_lectivo, nombre, estado FROM anio_lectivo WHERE id_anio_lectivo = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (idAnio != null) {
                ps.setInt(1, idAnio);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new String[] {String.valueOf(rs.getInt(1)), rs.getString(2), rs.getString(3)} : null;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el año lectivo", e);
        }
    }

    private String filtroInscritos(Integer idGrado, Integer idSeccion, List<Object> parametros) {
        StringBuilder where = new StringBuilder("WHERE i.id_anio_lectivo = ? AND i.estado = 'ACTIVA' ");
        if (idGrado != null) {
            where.append("AND s.id_grado = ? ");
            parametros.add(idGrado);
        }
        if (idSeccion != null) {
            where.append("AND s.id_seccion = ? ");
            parametros.add(idSeccion);
        }
        return where.toString();
    }

    @Override
    public List<Inscrito> inscritos(int idAnio, Integer idGrado, Integer idSeccion, Integer pagina, Integer tamano)
            throws ExcepcionPersistencia {
        List<Object> parametros = new ArrayList<>();
        parametros.add(idAnio);
        StringBuilder sql = new StringBuilder(
                "SELECT u.id_usuario, u.nombre, u.cui, g.nombre AS grado, s.nombre AS seccion "
                + "FROM inscripcion i JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "JOIN grado g ON g.id_grado = s.id_grado JOIN usuario u ON u.id_usuario = i.id_estudiante ");
        sql.append(filtroInscritos(idGrado, idSeccion, parametros));
        sql.append("ORDER BY g.nombre, s.nombre, u.nombre, u.id_usuario ");
        if (pagina != null && tamano != null) {
            sql.append("LIMIT ? OFFSET ?");
            parametros.add(tamano);
            parametros.add((pagina - 1) * tamano);
        }
        List<Inscrito> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Inscrito(rs.getInt("id_usuario"), rs.getString("nombre"), rs.getString("cui"),
                            rs.getString("grado"), rs.getString("seccion")));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los inscritos", e);
        }
        return lista;
    }

    @Override
    public long contarInscritos(int idAnio, Integer idGrado, Integer idSeccion) throws ExcepcionPersistencia {
        List<Object> parametros = new ArrayList<>();
        parametros.add(idAnio);
        String sql = "SELECT COUNT(*) FROM inscripcion i JOIN seccion s ON s.id_seccion = i.id_seccion "
                + filtroInscritos(idGrado, idSeccion, parametros);
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al contar los inscritos", e);
        }
    }
    
    //Reporte de Rendimiento
    @Override
    public String[] seccion(int idSeccion) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT g.nombre, s.nombre, a.nombre FROM seccion s JOIN grado g ON g.id_grado = s.id_grado "
                     + "JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo WHERE s.id_seccion = ?")) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new String[] {rs.getString(1) + " · " + rs.getString(2), rs.getString(3)} : null;
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar la sección", e);
        }
    }

    @Override
    public List<NotaTotal> totalesPorCurso(int idSeccion) throws ExcepcionPersistencia {
        String sql = "SELECT t.id_estudiante, t.estudiante, t.curso, t.total FROM ("
                + "SELECT i.id_estudiante, u.nombre AS estudiante, c.nombre AS curso, "
                + "COALESCE((SELECT SUM(n.valor) FROM nota n JOIN actividad_zona z ON z.id_actividad_zona = n.id_actividad_zona "
                + "WHERE z.id_asignacion = am.id_asignacion AND n.id_estudiante = i.id_estudiante), 0) "
                + "+ COALESCE((SELECT nf.valor FROM nota_final nf WHERE nf.id_asignacion = am.id_asignacion "
                + "AND nf.id_estudiante = i.id_estudiante), 0) AS total, "
                + "(EXISTS (SELECT 1 FROM nota n JOIN actividad_zona z ON z.id_actividad_zona = n.id_actividad_zona "
                + "WHERE z.id_asignacion = am.id_asignacion AND n.id_estudiante = i.id_estudiante) "
                + "OR EXISTS (SELECT 1 FROM nota_final nf WHERE nf.id_asignacion = am.id_asignacion "
                + "AND nf.id_estudiante = i.id_estudiante)) AS tiene "
                + "FROM asignacion_maestro am JOIN curso c ON c.id_curso = am.id_curso "
                + "JOIN inscripcion i ON i.id_seccion = am.id_seccion AND i.estado = 'ACTIVA' "
                + "JOIN usuario u ON u.id_usuario = i.id_estudiante WHERE am.id_seccion = ?) t "
                + "WHERE t.tiene = 1 ORDER BY t.curso, t.estudiante";
        List<NotaTotal> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSeccion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new NotaTotal(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4)));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar el rendimiento", e);
        }
        return lista;
    }
}
