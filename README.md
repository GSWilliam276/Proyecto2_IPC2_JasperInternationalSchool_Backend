# Jasper International School – Backend

API REST del Sistema de Gestión Escolar de Jasper International School, desarrollado para el curso de Introducción a la Programación y Computación 2 (IPC2) CUNOC, USAC.

El sistema administra inscripciones, pagos y solvencias, la gestión académica (cursos, secciones, asistencia y notas) y el servicio de biblioteca (libros, préstamos y multas), con acceso por roles: Super administrador, Administrador, Secretaría, Bibliotecario, Maestro y Estudiante.

## Tecnologías
- Java 17, Jakarta EE y Jersey (JAX-RS)
- Apache Tomcat 11.0.26
- MySQL 8.0
- JWT y BCrypt para la seguridad
- JasperReports para los reportes en PDF
- Maven

## Estructura
- `src/`: código fuente del backend
- `Mapeo y Datos SQL/`: script de la base de datos (`Mapeo Sistema Colegio.sql`) y datos iniciales de prueba
- `Ejecutabel .war/`: archivo `.war` listo para desplegar en Tomcat
- `Recursos/`: material de apoyo y documentación del proyecto

## Cómo ejecutarlo
1. Ejecutar en MySQL los dos scripts de `Mapeo y Datos SQL/`, en este orden: primero el mapeo de la base y luego los datos iniciales.
2. Configurar en `src/main/webapp/META-INF/context.xml` el usuario y la contraseña de MySQL.
3. Abrir el proyecto en NetBeans con Tomcat 11 y ejecutarlo (Clean and Build y Run), o copiar el `.war` a la carpeta `webapps` de Tomcat.
4. El API queda disponible en `http://localhost:8080/JasperSchoolBackend/api/v1`.

El detalle completo de instalación está en el Manual Técnico.

## Cuentas de prueba
Todas usan la contraseña `Colegio2026`.

| Rol | Correo |
|---|---|
| Super administrador | superadmin@colegio.com |
| Administrador | admin@colegio.com |
| Secretaría | secre@colegio.com |
| Bibliotecario | biblio@colegio.com |
| Maestro | maestra@colegio.com |
| Estudiante | diego.aju@alumnos.colegio.com |
