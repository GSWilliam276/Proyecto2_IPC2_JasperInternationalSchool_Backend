/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionAdminNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.modelo.Admin;
/**
 *
 * @author eduar
 */
public interface AdminDAO extends DAOGenerico<Admin> {
    void activar(int idUsuario) throws ExcepcionAdminNoEncontrado, ExcepcionPersistencia;
    void desactivar(int idUsuario) throws ExcepcionAdminNoEncontrado, ExcepcionPersistencia;
}
