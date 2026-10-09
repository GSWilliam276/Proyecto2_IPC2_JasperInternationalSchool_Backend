/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
/**
 *
 * @author eduar
 * Singleton que guarda el DataSource del pool que administra Tomcat (context.xml).
 * El pool se obtiene por JNDI una sola vez; cada getConexion() presta una conexion.
 */
public class ConexionBase {

    private static final String RECURSO = "java:comp/env/jdbc/colegio";

    //Se crea al cargar la clase: Java garantiza que eso es seguro entre hilos
    private static final ConexionBase instancia = new ConexionBase();

    private final DataSource pool;

    private ConexionBase() {
        try {
            pool = (DataSource) new InitialContext().lookup(RECURSO);
        } catch (NamingException e) {
            throw new IllegalStateException("No se pudo resolver el DataSource JNDI: " + RECURSO, e);
        }
    }

    public static ConexionBase obtenerInstancia() {
        return instancia;
    }

    public Connection getConexion() throws SQLException {
        return pool.getConnection();
    }
}
