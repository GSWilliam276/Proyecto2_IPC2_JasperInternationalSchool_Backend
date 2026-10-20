/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionLibroNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionNombreDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Libro;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.LibroDAO;
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
public class LibroDAOPersistencia implements LibroDAO {
    //Acceso a datos de Libro. Editar y cambiar de estado bloquean la fila (FOR UPDATE): prestar y devolver
    //(Iteracion 3) bloquean la misma, asi los ejemplares nunca quedan descuadrados
    @Override
    public void crear(Libro l) throws ExcepcionPersistencia {
        String sql = "INSERT INTO libro (titulo, autor, editorial, ejemplares_totales, ejemplares_disponibles, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getEditorial());
            ps.setInt(4, l.getEjemplaresTotales());
            ps.setInt(5, l.getEjemplaresDisponibles());
            ps.setString(6, l.getEstado().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    l.setIdLibro(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al registrar el libro");
        }
    }

    @Override
    public void editar(Libro l) throws ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            int prestados;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT ejemplares_totales - ejemplares_disponibles FROM libro WHERE id_libro = ? FOR UPDATE")) {
                ps.setInt(1, l.getIdLibro());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionLibroNoEncontrado("No existe un libro con ese id");
                    }
                    prestados = rs.getInt(1);
                }
            }
            if (l.getEjemplaresTotales() < prestados) {
                throw new ExcepcionOperacionNoPermitida("Hay " + prestados
                        + " ejemplares prestados: la cantidad no puede quedar por debajo");
            }
            String sql = "UPDATE libro SET titulo = ?, autor = ?, editorial = ?, ejemplares_totales = ?, "
                    + "ejemplares_disponibles = ? WHERE id_libro = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, l.getTitulo());
                ps.setString(2, l.getAutor());
                ps.setString(3, l.getEditorial());
                ps.setInt(4, l.getEjemplaresTotales());
                ps.setInt(5, l.getEjemplaresTotales() - prestados);
                ps.setInt(6, l.getIdLibro());
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw traducir(e, "Error al editar el libro");
        } catch (ExcepcionLibroNoEncontrado | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<Libro> buscarPorId(int id) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM libro WHERE id_libro = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el libro", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Libro> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        return listarFiltrado(pagina, tamanoPagina, busqueda, false);
    }

    @Override
    public List<Libro> listarFiltrado(int pagina, int tamanoPagina, String busqueda, boolean soloDisponibles)
            throws ExcepcionPersistencia {
        String sql = "SELECT * FROM libro WHERE (titulo LIKE ? OR autor LIKE ? OR editorial LIKE ?) "
                + (soloDisponibles ? "AND estado = 'ACTIVO' AND ejemplares_disponibles > 0 " : "")
                + "ORDER BY titulo, id_libro LIMIT ? OFFSET ?";
        List<Libro> lista = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            ps.setInt(4, tamanoPagina);
            ps.setInt(5, (pagina - 1) * tamanoPagina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los libros", e);
        }
        return lista;
    }

    @Override
    public void activar(int idLibro) throws ExcepcionLibroNoEncontrado, ExcepcionPersistencia {
        try {
            cambiarEstado(idLibro, EstadoGeneral.ACTIVO);
        } catch (ExcepcionOperacionNoPermitida e) {
            throw new ExcepcionPersistencia(e.getMessage(), e);   //Activar nunca la lanza
        }
    }

    @Override
    public void desactivar(int idLibro)
            throws ExcepcionLibroNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        cambiarEstado(idLibro, EstadoGeneral.INACTIVO);
    }

    @Override
    public void guardarImagen(int idLibro, String nombreArchivo) throws ExcepcionLibroNoEncontrado, ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE libro SET imagen = ? WHERE id_libro = ?")) {
            ps.setString(1, nombreArchivo);
            ps.setInt(2, idLibro);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al guardar la imagen del libro", e);
        }
    }
    
    //Metodos de Apoyo

    private void cambiarEstado(int idLibro, EstadoGeneral nuevo)
            throws ExcepcionLibroNoEncontrado, ExcepcionOperacionNoPermitida, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM libro WHERE id_libro = ? FOR UPDATE")) {
                ps.setInt(1, idLibro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new ExcepcionLibroNoEncontrado("No existe un libro con ese id");
                    }
                }
            }
            if (nuevo == EstadoGeneral.INACTIVO) {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM prestamo WHERE id_libro = ? AND fecha_devolucion IS NULL LIMIT 1")) {
                    ps.setInt(1, idLibro);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new ExcepcionOperacionNoPermitida("El libro tiene préstamos sin devolver");
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE libro SET estado = ? WHERE id_libro = ?")) {
                ps.setString(1, nuevo.name());
                ps.setInt(2, idLibro);
                ps.executeUpdate();
            }
            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al cambiar el estado del libro", e);
        } catch (ExcepcionLibroNoEncontrado | ExcepcionOperacionNoPermitida e) {
            deshacer(con);
            throw e;
        } finally {
            cerrar(con);
        }
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        Libro l = new Libro(rs.getInt("id_libro"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("editorial"), rs.getInt("ejemplares_totales"), rs.getInt("ejemplares_disponibles"),
                EstadoGeneral.valueOf(rs.getString("estado")));
        l.setImagen(rs.getString("imagen"));
        return l;
    }

    //El UNIQUE (titulo, autor, editorial) es la garantia real contra duplicados
    private ExcepcionPersistencia traducir(SQLException e, String mensajeGenerico) {
        if (e.getErrorCode() == 1062) {
            return new ExcepcionNombreDuplicado("Ya existe un libro con ese título, autor y editorial");
        }
        return new ExcepcionPersistencia(mensajeGenerico, e);
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
