/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.modelo;

import com.usac.colegio.jasperschoolbackend.enums.EstadoGeneral;
/**
 *
 * @author eduar
 */
public class Libro {
    //Libro de la biblioteca. Los disponibles son los que no estan prestados
    private int idLibro;
    private String titulo;
    private String autor;
    private String editorial;
    private int ejemplaresTotales;
    private int ejemplaresDisponibles;
    private EstadoGeneral estado;
    private String imagen;   //Nombre del archivo; null si no tiene portada

    public Libro(int idLibro, String titulo, String autor, String editorial, int ejemplaresTotales,
                 int ejemplaresDisponibles, EstadoGeneral estado) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.ejemplaresTotales = ejemplaresTotales;
        this.ejemplaresDisponibles = ejemplaresDisponibles;
        this.estado = estado;
    }

    public int getEjemplaresPrestados() { return ejemplaresTotales - ejemplaresDisponibles; }

    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getEditorial() { return editorial; }
    public int getEjemplaresTotales() { return ejemplaresTotales; }
    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public EstadoGeneral getEstado() { return estado; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
