/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.persistencia.interfaces;

import com.usac.colegio.jasperschoolbackend.modelo.Usuario;
import java.util.Optional;
import com.usac.colegio.jasperschoolbackend.excepciones.ExcepcionPersistencia;
/**
 *
 * @author eduar
 */
public interface UsuarioDAO {
    Optional<Usuario> buscarPorCorreo(String correo) throws ExcepcionPersistencia;
    Optional<Usuario> buscarPorCui(String cui) throws ExcepcionPersistencia;
    boolean existeCorreo(String correo) throws ExcepcionPersistencia;
    boolean existeCui(String cui) throws ExcepcionPersistencia;
    void actualizarContrasena(int idUsuario, String nuevaContrasena) throws ExcepcionPersistencia;
    String determinarRol(int idUsuario) throws ExcepcionPersistencia;
}
