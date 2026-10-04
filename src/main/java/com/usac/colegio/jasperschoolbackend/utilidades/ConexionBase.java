/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.utilidades;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
/**
 *
 * @author eduar
 */
public class ConexionBase {
    private static ConexionBase instancia;
    private DataSource dataSource;

    private static final String URL = "jdbc:mysql://localhost:3306/sistema_colegio";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String USUARIO = "gs_william237";
    private static final String CONTRASENA = "59487059@";

    private ConexionBase() {
        PoolProperties propiedades = new PoolProperties();
        propiedades.setUrl(URL);
        propiedades.setDriverClassName(DRIVER);
        propiedades.setUsername(USUARIO);
        propiedades.setPassword(CONTRASENA);
        propiedades.setMaxActive(20);
        propiedades.setMinIdle(5);
        propiedades.setMaxIdle(10);

        this.dataSource = new DataSource();
        this.dataSource.setPoolProperties(propiedades);
    }

    public static synchronized ConexionBase obtenerInstancia() {
        if (instancia == null) {
            instancia = new ConexionBase();
        }
        return instancia;
    }

    public Connection getConexion() throws SQLException {
        return dataSource.getConnection();
    }
}
