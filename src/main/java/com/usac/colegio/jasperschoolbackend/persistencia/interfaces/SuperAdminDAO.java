/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionSuperAdminNoEncontrado;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionUltimoSuperAdmin;
import com.usac.colegio.jasperschoolbackend.modelo.SuperAdmin;
/**
 *
 * @author eduar
 */
public interface SuperAdminDAO extends DAOGenerico<SuperAdmin> {
    void activar(int idUsuario) throws ExcepcionSuperAdminNoEncontrado, ExcepcionPersistencia;
    void desactivar(int idUsuario) throws ExcepcionSuperAdminNoEncontrado, ExcepcionUltimoSuperAdmin, ExcepcionPersistencia;
    int contarActivos() throws ExcepcionPersistencia;
}
