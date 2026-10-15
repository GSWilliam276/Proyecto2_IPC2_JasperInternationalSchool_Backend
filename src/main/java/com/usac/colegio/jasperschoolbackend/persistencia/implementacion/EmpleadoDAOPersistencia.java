/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.implementacion;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
import com.usac.colegio.jasperschoolbackend.enums.Puesto;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCorreoDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuentaInvalida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionCuiDuplicado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEmpleadoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionOperacionNoPermitida;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Empleado;
import com.usac.colegio.jasperschoolbackend.persistencia.interfaces.EmpleadoDAO;
import com.usac.colegio.jasperschoolbackend.utilidades.ConexionBase;
import com.usac.colegio.jasperschoolbackend.utilidades.ContrasenaUtil;
import java.sql.Connection;
import java.sql.Date;
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
public class EmpleadoDAOPersistencia implements EmpleadoDAO{
    private static final String INSERTAR_EMPLEADO =
            "INSERT INTO empleado (cui, nombre, telefono, direccion, puesto, fecha_contratacion, salario, estado, id_usuario) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    //Se crea la ficha de un puesto sin cuenta propia: mantenimiento, limpieza o administrativo
    //(este ultimo, opcionalmente enlazado a una cuenta de Admin o SuperAdmin ya existente)
    @Override
    public void crear(Empleado empleado) throws ExcepcionPersistencia {
        if (empleado.getPuesto().exigeCuenta()) {
            throw new IllegalArgumentException("Este puesto exige cuenta: use crearConCuenta");
        }
        if (!empleado.getPuesto().admiteCuenta() && empleado.tieneCuenta()) {
            throw new IllegalArgumentException("Este puesto no admite cuenta de acceso");
        }

        try (Connection con = ConexionBase.obtenerInstancia().getConexion()) {
            if (empleado.tieneCuenta()) {
                validarCuentaDeAdministracion(con, empleado.getIdUsuario(), empleado.getCui());
            }
            try (PreparedStatement ps = con.prepareStatement(INSERTAR_EMPLEADO, Statement.RETURN_GENERATED_KEYS)) {
                llenarInsercionEmpleado(ps, empleado);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        empleado.setIdEmpleado(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al crear el empleado");
        }
    }

    //Crea ficha + cuenta en una transaccion: usuario, fila del rol y empleado enlazado
    @Override
    public void crearConCuenta(Empleado empleado, String correo, String contrasena) throws ExcepcionPersistencia {
        if (!empleado.getPuesto().exigeCuenta()) {
            throw new IllegalArgumentException("Este puesto no lleva cuenta: use crear");
        }
        String tablaRol = tablaRol(empleado.getPuesto());

        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false); //Iniicio de la transaccion

            //1. la cuenta; contrasena_provisional = TRUE porque la entrega quien la crea
            int idUsuario;
            String sqlUsuario = "INSERT INTO usuario (cui, nombre, correo, telefono, direccion, contrasena, contrasena_provisional) "
                    + "VALUES (?, ?, ?, ?, ?, ?, TRUE)";
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, empleado.getCui());
                ps.setString(2, empleado.getNombre());
                ps.setString(3, correo);
                ps.setString(4, empleado.getTelefono());
                ps.setString(5, empleado.getDireccion());
                ps.setString(6, ContrasenaUtil.hashear(contrasena));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    idUsuario = rs.getInt(1);
                }
            }

            //2. la fila del rol
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO " + tablaRol + " (id_usuario, estado) VALUES (?, 'ACTIVO')")) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }

            //3. la ficha, enlazada a la cuenta recien creada
            empleado.setIdUsuario(idUsuario);
            try (PreparedStatement ps = con.prepareStatement(INSERTAR_EMPLEADO, Statement.RETURN_GENERATED_KEYS)) {
                llenarInsercionEmpleado(ps, empleado);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    empleado.setIdEmpleado(rs.getInt(1));
                }
            }

            con.commit(); //las tres inserciones se confirman juntas

        } catch (SQLException e) {
            deshacer(con);
            empleado.setIdUsuario(null); //el objeto no debe quedar apuntando a una cuenta que se deshizo
            throw traducir(e, "Error al crear el empleado con su cuenta");
        } finally {
            cerrar(con);
        }
    }

    //Edita nombre, telefono, direccion, fecha de contratacion y salario
    //El CUI y el puesto no cambian. Si la ficha tiene cuenta enlazada, sus datos personales
    //se actualizan tambien, en la misma transaccion, para que las dos tablas no se desincronicen
    @Override
    public void editar(Empleado empleado) throws ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            //bloquea la fila y lee la cuenta enlazada desde la base, no desde el objeto recibido
            Integer idUsuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id_usuario FROM empleado WHERE id_empleado = ? FOR UPDATE")) {
                ps.setInt(1, empleado.getIdEmpleado());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        throw new ExcepcionEmpleadoNoEncontrado("No existe un empleado con ese id");
                    }
                    int id = rs.getInt(1);
                    idUsuario = rs.wasNull() ? null : id;
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE empleado SET nombre = ?, telefono = ?, direccion = ?, fecha_contratacion = ?, salario = ? "
                    + "WHERE id_empleado = ?")) {
                ps.setString(1, empleado.getNombre());
                ps.setString(2, empleado.getTelefono());
                ps.setString(3, empleado.getDireccion());
                ps.setDate(4, Date.valueOf(empleado.getFechaContratacion()));
                ps.setBigDecimal(5, empleado.getSalario());
                ps.setInt(6, empleado.getIdEmpleado());
                ps.executeUpdate();
            }

            if (idUsuario != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario SET nombre = ?, telefono = ?, direccion = ? WHERE id_usuario = ?")) {
                    ps.setString(1, empleado.getNombre());
                    ps.setString(2, empleado.getTelefono());
                    ps.setString(3, empleado.getDireccion());
                    ps.setInt(4, idUsuario);
                    ps.executeUpdate();
                }
            }

            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al editar el empleado", e);
        } finally {
            cerrar(con);
        }
    }

    @Override
    public Optional<Empleado> buscarPorId(int id) throws ExcepcionPersistencia {
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM empleado WHERE id_empleado = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEmpleado(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al buscar el empleado", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Empleado> listarPaginado(int pagina, int tamanoPagina, String busqueda) throws ExcepcionPersistencia {
        String sql = "SELECT * FROM empleado WHERE nombre LIKE ? OR cui LIKE ? "
                + "ORDER BY id_empleado LIMIT ? OFFSET ?";
        List<Empleado> empleados = new ArrayList<>();
        try (Connection con = ConexionBase.obtenerInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String filtro = "%" + (busqueda == null ? "" : busqueda) + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setInt(3, tamanoPagina);
            ps.setInt(4, (pagina - 1) * tamanoPagina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    empleados.add(mapearEmpleado(rs));
                }
            }
        } catch (SQLException e) {
            throw new ExcepcionPersistencia("Error al listar los empleados", e);
        }
        return empleados;
    }

    @Override
    public void activar(int idEmpleado) throws ExcepcionEmpleadoNoEncontrado, ExcepcionPersistencia {
        cambiarEstado(idEmpleado, EstadoGeneral.ACTIVO);
    }

    @Override
    public void desactivar(int idEmpleado) throws ExcepcionEmpleadoNoEncontrado, ExcepcionPersistencia {
        cambiarEstado(idEmpleado, EstadoGeneral.INACTIVO);
    }

    //Metodos de Apoyo

    //Cambia el estado de la ficha y, si el puesto tiene cuenta propia (maestro, secretaria o
    //bibliotecario), tambien el de su cuenta: un maestro dado de baja no debe poder seguir entrando
    //Un administrativo enlazado a un Admin o SuperAdmin no arrastra a su cuenta: esa se gestiona
    //en su propia pantalla, y desactivar a un SuperAdmin tiene su propia regla
    private void cambiarEstado(int idEmpleado, EstadoGeneral nuevoEstado)
            throws ExcepcionEmpleadoNoEncontrado, ExcepcionPersistencia {
        Connection con = null;
        try {
            con = ConexionBase.obtenerInstancia().getConexion();
            con.setAutoCommit(false);

            Puesto puesto;
            Integer idUsuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT puesto, id_usuario FROM empleado WHERE id_empleado = ? FOR UPDATE")) {
                ps.setInt(1, idEmpleado);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        throw new ExcepcionEmpleadoNoEncontrado("No existe un empleado con ese id");
                    }
                    puesto = Puesto.valueOf(rs.getString("puesto"));
                    int id = rs.getInt("id_usuario");
                    idUsuario = rs.wasNull() ? null : id;
                }
            }
            if (nuevoEstado == EstadoGeneral.INACTIVO && puesto == Puesto.MAESTRO && idUsuario != null) {
                //Se bloquea la fila del maestro: asignar() tambien la bloquea,
                //asi nadie lo asigna entre esta revision y el UPDATE
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_usuario FROM maestro WHERE id_usuario = ? FOR UPDATE")) {
                    ps.setInt(1, idUsuario);
                    ps.executeQuery().close();
                }
                String sql = "SELECT 1 FROM asignacion_maestro am "
                        + "JOIN seccion s ON s.id_seccion = am.id_seccion "
                        + "JOIN anio_lectivo a ON a.id_anio_lectivo = s.id_anio_lectivo "
                        + "WHERE am.id_maestro = ? AND a.estado = 'ACTIVO' LIMIT 1";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idUsuario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            con.rollback();
                            throw new ExcepcionOperacionNoPermitida(
                                    "El maestro tiene asignaciones en el año lectivo activo: reasígnelas antes de desactivarlo");
                        }
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE empleado SET estado = ? WHERE id_empleado = ?")) {
                ps.setString(1, nuevoEstado.name());
                ps.setInt(2, idEmpleado);
                ps.executeUpdate();
            }

            if (puesto.exigeCuenta() && idUsuario != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE " + tablaRol(puesto) + " SET estado = ? WHERE id_usuario = ?")) {
                    ps.setString(1, nuevoEstado.name());
                    ps.setInt(2, idUsuario);
                    ps.executeUpdate();
                }
            }

            con.commit();

        } catch (SQLException e) {
            deshacer(con);
            throw new ExcepcionPersistencia("Error al cambiar el estado del empleado", e);
        } finally {
            cerrar(con);
        }
    }

    //Para enlazar un administrativo: la cuenta debe existir, ser de Admin o SuperAdmin,
    //y ser de la misma persona (mismo CUI que la ficha)
    private void validarCuentaDeAdministracion(Connection con, int idUsuario, String cuiFicha)
            throws SQLException, ExcepcionCuentaInvalida {
        String sql = "SELECT u.cui FROM usuario u WHERE u.id_usuario = ? AND ("
                + "EXISTS (SELECT 1 FROM admin a WHERE a.id_usuario = u.id_usuario) "
                + "OR EXISTS (SELECT 1 FROM superadmin s WHERE s.id_usuario = u.id_usuario))";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ExcepcionCuentaInvalida("La cuenta indicada no existe o no es de Admin ni SuperAdmin");
                }
                if (!rs.getString("cui").equals(cuiFicha)) {
                    throw new ExcepcionCuentaInvalida("El CUI de la ficha debe coincidir con el de la cuenta");
                }
            }
        }
    }

    //Tabla del rol que corresponde al puesto. Solo los tres puestos con cuenta propia tienen una
    private String tablaRol(Puesto puesto) {
        switch (puesto) {
            case MAESTRO:
                return "maestro";
            case SECRETARIA:
                return "secretaria";
            case BIBLIOTECARIO:
                return "bibliotecario";
            default:
                throw new IllegalArgumentException("El puesto " + puesto + " no tiene tabla de rol");
        }
    }

    private void llenarInsercionEmpleado(PreparedStatement ps, Empleado e) throws SQLException {
        ps.setString(1, e.getCui());
        ps.setString(2, e.getNombre());
        ps.setString(3, e.getTelefono());
        ps.setString(4, e.getDireccion());
        ps.setString(5, e.getPuesto().name());
        ps.setDate(6, Date.valueOf(e.getFechaContratacion()));
        ps.setBigDecimal(7, e.getSalario());
        ps.setString(8, e.getEstado().name());
        if (e.getIdUsuario() == null) {
            ps.setNull(9, java.sql.Types.INTEGER);
        } else {
            ps.setInt(9, e.getIdUsuario());
        }
    }

    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        int idU = rs.getInt("id_usuario");
        Integer idUsuario = rs.wasNull() ? null : idU;
        return new Empleado(
                rs.getInt("id_empleado"),
                rs.getString("cui"),
                rs.getString("nombre"),
                rs.getString("telefono"),
                rs.getString("direccion"),
                Puesto.valueOf(rs.getString("puesto")),
                rs.getDate("fecha_contratacion").toLocalDate(),
                rs.getBigDecimal("salario"),
                EstadoGeneral.valueOf(rs.getString("estado")),
                idUsuario);
    }

    private ExcepcionPersistencia traducir(SQLException e, String mensajeGenerico) {
        if (e.getErrorCode() == 1062) {
            String mensaje = e.getMessage() == null ? "" : e.getMessage();
            int pos = mensaje.indexOf("for key");
            String clave = pos >= 0 ? mensaje.substring(pos) : "";
            if (clave.contains("uq_empleado_usuario")) {
                return new ExcepcionCuentaInvalida("La cuenta ya está enlazada a otro empleado");
            }
            if (clave.contains("correo")) {
                return new ExcepcionCorreoDuplicado("El correo ya está registrado");
            }
            if (clave.contains("cui")) {
                return new ExcepcionCuiDuplicado("El CUI ya está registrado");
            }
        }
        return new ExcepcionPersistencia(mensajeGenerico, e);
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
                con.setAutoCommit(true); //Se deja la conexion en su estado normal antes de devolverla al pool
                con.close();
            } catch (SQLException ignorada) {
                //nada que hacer
            }
        }
    }
}
