/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.NotaActividad;
import com.usac.colegio.jasperschoolbackend.modelo.NotaCurso;
import com.usac.colegio.jasperschoolbackend.modelo.PagoPropio;
import com.usac.colegio.jasperschoolbackend.modelo.PerfilEstudiante;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.PanelEstudianteDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author eduar
 */
public class PanelEstudianteDAOPersistencia implements PanelEstudianteDAO {
    //Consultas de solo lectura del panel del Estudiante: todo se filtra por su propio id
    @Override
    public Optional<PerfilEstudiante> perfil(int idEstudiante) throws ExcepcionPersistencia {
        String sql = "SELECT u.nombre, u.cui, u.correo, u.telefono, u.direccion, e.fecha_nacimiento, "
                + "e.encargado_nombre, e.encargado_telefono, e.encargado_parentesco, "
                + "g.nombre AS grado, s.nombre AS seccion, c.nombre AS carrera, a.nombre AS anio "
                + "FROM usuario u JOIN estudiante e ON e.id_usuario = u.id_usuario "
                + "LEFT JOIN anio_lectivo a ON a.estado = 'ACTIVO' "
                + "LEFT JOIN inscripcion i ON i.id_estudiante = u.id_usuario AND i.estado = 'ACTIVA' "
                + "AND i.id_anio_lectivo = a.id_anio_lectivo "
                + "LEFT JOIN seccion s ON s.id_seccion = i.id_seccion "
                + "LEFT JOIN grado g ON g.id_grado = s.id_grado "
                + "LEFT JOIN carrera c ON c.id_carrera = s.id_carrera "
                + "WHERE u.id_usuario = ?";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new PerfilEstudiante(rs.getString("nombre"), rs.getString("cui"),
                        rs.getString("correo"), rs.getString("telefono"), rs.getString("direccion"),
                        rs.getDate("fecha_nacimiento").toString(), rs.getString("encargado_nombre"),
                        rs.getString("encargado_telefono"), rs.getString("encargado_parentesco"),
                        rs.getString("grado"), rs.getString("seccion"), rs.getString("carrera"),
                        rs.getString("anio")));
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar el perfil", e);
        }
    }

    @Override
    public List<NotaCurso> notas(int idEstudiante) throws ExcepcionPersistencia {
        String cursos = "SELECT am.id_asignacion, cu.nombre AS curso, um.nombre AS maestro "
                + "FROM inscripcion i JOIN anio_lectivo a ON a.id_anio_lectivo = i.id_anio_lectivo "
                + "JOIN asignacion_maestro am ON am.id_seccion = i.id_seccion "
                + "JOIN curso cu ON cu.id_curso = am.id_curso JOIN usuario um ON um.id_usuario = am.id_maestro "
                + "WHERE i.id_estudiante = ? AND i.estado = 'ACTIVA' AND a.estado = 'ACTIVO' ORDER BY cu.nombre";
        List<NotaCurso> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(cursos)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(armarCurso(con, idEstudiante, rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar las notas", e);
        }
        return lista;
    }

    private NotaCurso armarCurso(Connection con, int idEstudiante, int idAsignacion, String curso, String maestro)
            throws SQLException {
        List<NotaActividad> actividades = new ArrayList<>();
        BigDecimal maxima = BigDecimal.ZERO;
        BigDecimal obtenida = BigDecimal.ZERO;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT z.nombre, z.punteo, n.valor FROM actividad_zona z "
                + "LEFT JOIN nota n ON n.id_actividad_zona = z.id_actividad_zona AND n.id_estudiante = ? "
                + "WHERE z.id_asignacion = ? ORDER BY z.id_actividad_zona")) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idAsignacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal punteo = rs.getBigDecimal(2);
                    BigDecimal valor = rs.getBigDecimal(3);
                    actividades.add(new NotaActividad(rs.getString(1), punteo, valor));
                    maxima = maxima.add(punteo);
                    if (valor != null) {
                        obtenida = obtenida.add(valor);
                    }
                }
            }
        }
        BigDecimal examen = null;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT valor FROM nota_final WHERE id_asignacion = ? AND id_estudiante = ?")) {
            ps.setInt(1, idAsignacion);
            ps.setInt(2, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    examen = rs.getBigDecimal(1);
                }
            }
        }
        BigDecimal total = obtenida.add(examen == null ? BigDecimal.ZERO : examen);
        return new NotaCurso(idAsignacion, curso, maestro, actividades, maxima, obtenida, examen, total);
    }

    @Override
    public List<PagoPropio> pagos(int idEstudiante) throws ExcepcionPersistencia {
        String sql = "SELECT p.id_pago, p.concepto, p.monto, p.mes, ac.nombre AS actividad, p.fecha_pago, p.estado "
                + "FROM pago p LEFT JOIN actividad_extracurricular ac ON ac.id_actividad = p.id_actividad "
                + "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC, p.id_pago DESC LIMIT 100";
        List<PagoPropio> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int mes = rs.getInt("mes");
                    Integer mesPago = rs.wasNull() ? null : mes;
                    lista.add(new PagoPropio(rs.getInt("id_pago"), rs.getString("concepto"), rs.getBigDecimal("monto"),
                            mesPago, rs.getString("actividad"), rs.getDate("fecha_pago").toString(),
                            rs.getString("estado")));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al consultar los pagos", e);
        }
        return lista;
    }
}
