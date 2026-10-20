/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.transferencia;

/**
 *
 * @author eduar
 */
public class RespuestaLibro {
        private int idLibro;
    private String titulo;
    private String autor;
    private String editorial;
    private int ejemplaresTotales;
    private int ejemplaresDisponibles;
    private int ejemplaresPrestados;
    private String estado;
    private boolean tieneImagen;   //true si el libro tiene portada

    public RespuestaLibro() {
    }

    public RespuestaLibro(int idLibro, String titulo, String autor, String editorial, int ejemplaresTotales,
                          int ejemplaresDisponibles, int ejemplaresPrestados, String estado, boolean tieneImagen) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.ejemplaresTotales = ejemplaresTotales;
        this.ejemplaresDisponibles = ejemplaresDisponibles;
        this.ejemplaresPrestados = ejemplaresPrestados;
        this.estado = estado;
        this.tieneImagen = tieneImagen;
    }

    public int getIdLibro() { return idLibro; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getEditorial() { return editorial; }
    public int getEjemplaresTotales() { return ejemplaresTotales; }
    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public int getEjemplaresPrestados() { return ejemplaresPrestados; }
    public String getEstado() { return estado; }
    public boolean isTieneImagen() { return tieneImagen; }
}
