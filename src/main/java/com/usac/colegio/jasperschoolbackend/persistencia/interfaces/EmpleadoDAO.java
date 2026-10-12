/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionEmpleadoNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Empleado;
/**
 *
 * @author eduar
 */
public interface EmpleadoDAO extends DAOGenerico<Empleado> {
    //crear(Empleado) viene de DAOGenerico y se usa SOLO para puestos que no exigen
    //cuenta: mantenimiento, limpieza y administrativo (este ultimo, opcionalmente
    //enlazado a una cuenta existente por idUsuario).

    //Crea la ficha junto con su cuenta de acceso, en una sola transaccion:
    //usuario, fila del rol (maestro, secretaria o bibliotecario) y empleado enlazado
    //Solo para puestos que exigen cuenta
    void crearConCuenta(Empleado empleado, String correo, String contrasena) throws ExcepcionPersistencia;

    void activar(int idEmpleado) throws ExcepcionEmpleadoNoEncontrado, ExcepcionPersistencia;

    void desactivar(int idEmpleado) throws ExcepcionEmpleadoNoEncontrado, ExcepcionPersistencia;
}
