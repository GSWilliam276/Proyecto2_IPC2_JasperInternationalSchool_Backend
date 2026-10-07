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
 * Entrega conexiones del pool que administra Tomcat, configurado en context.xml
 * Al cerrar la conexion, regresa al pool en lugar de destruirse
 */
public class ConexionBase {

    //Nombre del recurso declarado en context.xml
    private static final String RECURSO = "java:comp/env/jdbc/colegio";

    private ConexionBase() {
    }

    public static Connection getConexion() throws SQLException {
        try {
            DataSource pool = (DataSource) new InitialContext().lookup(RECURSO);
            return pool.getConnection();
        } catch (NamingException e) {
            throw new SQLException("No se encontró el pool configurado en context.xml", e);
        }
    }
}
