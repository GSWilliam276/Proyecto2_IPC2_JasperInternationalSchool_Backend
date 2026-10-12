/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.usac.colegio.jasperschoolbackend.enums;

/**
 *
 * @author eduar
 */
public enum Puesto {
    //Puesto de un empleado. Define si la persona tiene o no cuenta de acceso al sistema
    MAESTRO,
    SECRETARIA,
    BIBLIOTECARIO,
    ADMINISTRATIVO,
    MANTENIMIENTO,
    LIMPIEZA;
    
    // Estos puestos exigen una cuenta de acceso enlazada a la ficha
    public boolean exigeCuenta() {
        return this == MAESTRO || this == SECRETARIA || this == BIBLIOTECARIO;
    }

    //Estos puestos pueden tener cuenta, pero no es obligatoria (Admin o SuperAdmin)
    public boolean admiteCuenta() {
        return exigeCuenta() || this == ADMINISTRATIVO;
    }
}
