CREATE DATABASE IF NOT EXISTS sistema_colegio;
USE sistema_colegio;

-- ========================= BASE =========================

-- correo y contrasena son NULL: el estudiante se registra sin acceso, y la Secretaria se lo crea al inscribirlo
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    cui VARCHAR(13) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NULL UNIQUE,
    telefono VARCHAR(15) NOT NULL,
    direccion VARCHAR(100) NOT NULL,
    contrasena VARCHAR(255) NULL,
    contrasena_provisional BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS admin (
    id_usuario INT PRIMARY KEY,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS superadmin (
    id_usuario INT PRIMARY KEY,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE IF NOT EXISTS maestro (
    id_usuario INT NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_maestro_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE IF NOT EXISTS secretaria (
    id_usuario INT NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_secretaria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE IF NOT EXISTS bibliotecario (
    id_usuario INT NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_bibliotecario_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
);

CREATE TABLE IF NOT EXISTS estudiante (
    id_usuario INT NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    alergias VARCHAR(255) NULL,
    condiciones_medicas VARCHAR(255) NULL,
    encargado_nombre VARCHAR(100) NOT NULL,
    encargado_telefono VARCHAR(15) NOT NULL,
    encargado_parentesco VARCHAR(50) NOT NULL,
    encargado2_nombre VARCHAR(100) NULL,
    encargado2_telefono VARCHAR(15) NULL,
    encargado2_parentesco VARCHAR(50) NULL,
    estado ENUM('ACTIVO','INACTIVO','GRADUADO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_usuario),
    CONSTRAINT fk_estudiante_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    -- el segundo encargado se llena completo o no se llena
    CONSTRAINT ck_encargado2_completo CHECK (
        (encargado2_nombre IS NULL AND encargado2_telefono IS NULL AND encargado2_parentesco IS NULL)
        OR (encargado2_nombre IS NOT NULL AND encargado2_telefono IS NOT NULL AND encargado2_parentesco IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS graduacion_estudiante (
    id_graduacion INT NOT NULL AUTO_INCREMENT,
    id_estudiante INT NOT NULL,
    nivel ENUM('PRE_PRIMARIA','PRIMARIA','BASICO','DIVERSIFICADO') NOT NULL,
    fecha_graduacion DATE NOT NULL,
    PRIMARY KEY (id_graduacion),
    UNIQUE KEY uq_graduacion (id_estudiante, nivel),
    CONSTRAINT fk_graduacion_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_usuario)
);

CREATE TABLE IF NOT EXISTS empleado (
    id_empleado INT NOT NULL AUTO_INCREMENT,
    cui VARCHAR(13) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    direccion VARCHAR(100) NOT NULL,
    puesto ENUM('MAESTRO','SECRETARIA','BIBLIOTECARIO','ADMINISTRATIVO','MANTENIMIENTO','LIMPIEZA') NOT NULL,
    fecha_contratacion DATE NOT NULL,
    salario DECIMAL(10,2) NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    id_usuario INT NULL,
    PRIMARY KEY (id_empleado),
    UNIQUE KEY uq_empleado_cui (cui),
    UNIQUE KEY uq_empleado_usuario (id_usuario),
    CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_empleado_salario CHECK (salario > 0)
);

CREATE TABLE IF NOT EXISTS codigo_recuperacion (
    id_codigo_recuperacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    codigo VARCHAR(10) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    fue_usado BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ========================= ACADEMICO =========================

CREATE TABLE IF NOT EXISTS anio_lectivo (
    id_anio_lectivo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado ENUM('ACTIVO', 'CERRADO') NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE IF NOT EXISTS grado (
    id_grado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    nivel ENUM('PRE_PRIMARIA', 'PRIMARIA', 'BASICO', 'DIVERSIFICADO') NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE IF NOT EXISTS carrera (
    id_carrera INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    estado ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE IF NOT EXISTS curso (
    id_curso INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_curso),
    UNIQUE KEY uq_curso_nombre (nombre)
);

-- carrera_clave convierte el NULL en 0 SOLO para la restriccion UNIQUE:
-- sin ella, MySQL permitiria dos curriculos del mismo grado sin carrera
CREATE TABLE IF NOT EXISTS curriculo (
    id_curriculo INT NOT NULL AUTO_INCREMENT,
    id_grado INT NOT NULL,
    id_carrera INT NULL,
    carrera_clave INT GENERATED ALWAYS AS (IFNULL(id_carrera, 0)) STORED,
    PRIMARY KEY (id_curriculo),
    UNIQUE KEY uq_curriculo (id_grado, carrera_clave),
    CONSTRAINT fk_curriculo_grado FOREIGN KEY (id_grado) REFERENCES grado (id_grado),
    CONSTRAINT fk_curriculo_carrera FOREIGN KEY (id_carrera) REFERENCES carrera (id_carrera)
);

CREATE TABLE IF NOT EXISTS curriculo_curso (
    id_curriculo INT NOT NULL,
    id_curso INT NOT NULL,
    PRIMARY KEY (id_curriculo, id_curso),
    CONSTRAINT fk_cc_curriculo FOREIGN KEY (id_curriculo) REFERENCES curriculo (id_curriculo),
    CONSTRAINT fk_cc_curso FOREIGN KEY (id_curso) REFERENCES curso (id_curso)
);

CREATE TABLE IF NOT EXISTS seccion (
    id_seccion INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(10) NOT NULL,
    id_grado INT NOT NULL,
    id_anio_lectivo INT NOT NULL,
    id_carrera INT NULL,
    carrera_clave INT GENERATED ALWAYS AS (IFNULL(id_carrera, 0)) STORED,
    cupo_maximo INT NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_seccion),
    UNIQUE KEY uq_seccion (id_grado, id_anio_lectivo, carrera_clave, nombre),
    CONSTRAINT fk_seccion_grado FOREIGN KEY (id_grado) REFERENCES grado (id_grado),
    CONSTRAINT fk_seccion_anio FOREIGN KEY (id_anio_lectivo) REFERENCES anio_lectivo (id_anio_lectivo),
    CONSTRAINT fk_seccion_carrera FOREIGN KEY (id_carrera) REFERENCES carrera (id_carrera),
    CONSTRAINT ck_seccion_cupo CHECK (cupo_maximo > 0)
);

-- un solo maestro por curso y seccion
CREATE TABLE IF NOT EXISTS asignacion_maestro (
    id_asignacion INT NOT NULL AUTO_INCREMENT,
    id_seccion INT NOT NULL,
    id_curso INT NOT NULL,
    id_maestro INT NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_asignacion),
    UNIQUE KEY uq_asignacion (id_seccion, id_curso),
    CONSTRAINT fk_asignacion_seccion FOREIGN KEY (id_seccion) REFERENCES seccion (id_seccion),
    CONSTRAINT fk_asignacion_curso FOREIGN KEY (id_curso) REFERENCES curso (id_curso),
    CONSTRAINT fk_asignacion_maestro FOREIGN KEY (id_maestro) REFERENCES maestro (id_usuario)
);

-- ========================= FINANZAS =========================

CREATE TABLE IF NOT EXISTS tarifa (
    id_tarifa INT NOT NULL AUTO_INCREMENT,
    id_grado INT NOT NULL,
    id_anio_lectivo INT NOT NULL,
    concepto ENUM('INSCRIPCION','MENSUALIDAD') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_tarifa),
    UNIQUE KEY uq_tarifa (id_grado, id_anio_lectivo, concepto),
    CONSTRAINT fk_tarifa_grado FOREIGN KEY (id_grado) REFERENCES grado (id_grado),
    CONSTRAINT fk_tarifa_anio FOREIGN KEY (id_anio_lectivo) REFERENCES anio_lectivo (id_anio_lectivo),
    CONSTRAINT ck_tarifa_monto CHECK (monto > 0)
);

-- costo = 0 significa actividad gratuita
CREATE TABLE IF NOT EXISTS actividad_extracurricular (
    id_actividad INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    fecha DATE NOT NULL,
    cupo INT NOT NULL,
    costo DECIMAL(10,2) NOT NULL DEFAULT 0,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_actividad),
    UNIQUE KEY uq_actividad (nombre, fecha),
    CONSTRAINT ck_actividad_cupo CHECK (cupo > 0),
    CONSTRAINT ck_actividad_costo CHECK (costo >= 0)
);

-- clave_activa es NULL en inscripciones anuladas: quien se anula puede reinscribirse en el mismo año
CREATE TABLE IF NOT EXISTS inscripcion (
    id_inscripcion INT NOT NULL AUTO_INCREMENT,
    id_estudiante INT NOT NULL,
    id_seccion INT NOT NULL,
    id_anio_lectivo INT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    id_secretaria INT NOT NULL,
    estado ENUM('ACTIVA','ANULADA') NOT NULL DEFAULT 'ACTIVA',
    clave_activa INT GENERATED ALWAYS AS (IF(estado = 'ACTIVA', id_estudiante, NULL)) STORED,
    PRIMARY KEY (id_inscripcion),
    UNIQUE KEY uq_inscripcion_activa (id_anio_lectivo, clave_activa),
    CONSTRAINT fk_inscripcion_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_usuario),
    CONSTRAINT fk_inscripcion_seccion FOREIGN KEY (id_seccion) REFERENCES seccion (id_seccion),
    CONSTRAINT fk_inscripcion_anio FOREIGN KEY (id_anio_lectivo) REFERENCES anio_lectivo (id_anio_lectivo),
    CONSTRAINT fk_inscripcion_secretaria FOREIGN KEY (id_secretaria) REFERENCES secretaria (id_usuario)
);

-- ========================= BIBLIOTECA =========================

-- una sola fila; los valores iniciales son de ejemplo
CREATE TABLE IF NOT EXISTS parametros_biblioteca (
    id_parametros TINYINT NOT NULL,
    dias_prestamo INT NOT NULL,
    multa_por_dia DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_parametros),
    CONSTRAINT ck_parametros_una_fila CHECK (id_parametros = 1),
    CONSTRAINT ck_parametros_dias CHECK (dias_prestamo > 0),
    CONSTRAINT ck_parametros_multa CHECK (multa_por_dia >= 0)
);

INSERT IGNORE INTO parametros_biblioteca (id_parametros, dias_prestamo, multa_por_dia)
VALUES (1, 7, 5.00);

-- imagen: nombre del archivo de la portada, guardada en C:\jasper-imagenes\libros
CREATE TABLE IF NOT EXISTS libro (
    id_libro INT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(150) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    editorial VARCHAR(100) NOT NULL,
    ejemplares_totales INT NOT NULL,
    ejemplares_disponibles INT NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    imagen VARCHAR(100) NULL,
    PRIMARY KEY (id_libro),
    UNIQUE KEY uq_libro (titulo, autor, editorial),
    CONSTRAINT ck_libro_totales CHECK (ejemplares_totales > 0),
    CONSTRAINT ck_libro_disponibles CHECK (ejemplares_disponibles >= 0 AND ejemplares_disponibles <= ejemplares_totales)
);

-- el prestatario es un usuario: estudiante o maestro (se distingue por la tabla donde esta)
CREATE TABLE IF NOT EXISTS prestamo (
    id_prestamo INT NOT NULL AUTO_INCREMENT,
    id_libro INT NOT NULL,
    id_prestatario INT NOT NULL,
    id_bibliotecario INT NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_esperada DATE NOT NULL,
    fecha_devolucion DATE NULL,
    PRIMARY KEY (id_prestamo),
    KEY ix_prestamo_prestatario (id_prestatario, fecha_devolucion),
    CONSTRAINT fk_prestamo_libro FOREIGN KEY (id_libro) REFERENCES libro (id_libro),
    CONSTRAINT fk_prestamo_prestatario FOREIGN KEY (id_prestatario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_prestamo_bibliotecario FOREIGN KEY (id_bibliotecario) REFERENCES bibliotecario (id_usuario),
    CONSTRAINT ck_prestamo_fechas CHECK (fecha_esperada >= fecha_prestamo)
);

-- una multa por prestamo, solo cuando un estudiante devuelve tarde. El monto se copia al generarla
CREATE TABLE IF NOT EXISTS multa (
    id_multa INT NOT NULL AUTO_INCREMENT,
    id_prestamo INT NOT NULL,
    dias_atraso INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE','PAGADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_generacion DATE NOT NULL,
    PRIMARY KEY (id_multa),
    UNIQUE KEY uq_multa_prestamo (id_prestamo),
    CONSTRAINT fk_multa_prestamo FOREIGN KEY (id_prestamo) REFERENCES prestamo (id_prestamo),
    CONSTRAINT ck_multa_dias CHECK (dias_atraso > 0),
    CONSTRAINT ck_multa_monto CHECK (monto > 0)
);

-- ========================= PAGOS =========================

-- monto = lo cobrado (copiado de la tarifa, actividad o multa): cambiar la tarifa no altera pagos viejos.
-- clave_unica es NULL en pagos anulados: un pago anulado no bloquea volver a cobrar el mismo concepto.
CREATE TABLE IF NOT EXISTS pago (
    id_pago INT NOT NULL AUTO_INCREMENT,
    id_estudiante INT NOT NULL,
    id_anio_lectivo INT NOT NULL,
    concepto ENUM('INSCRIPCION','MENSUALIDAD','ACTIVIDAD','MULTA') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    mes TINYINT NULL,
    id_actividad INT NULL,
    id_multa INT NULL,
    fecha_pago DATE NOT NULL,
    id_secretaria INT NOT NULL,
    estado ENUM('VIGENTE','ANULADO') NOT NULL DEFAULT 'VIGENTE',
    motivo_anulacion VARCHAR(255) NULL,
    fecha_anulacion DATETIME NULL,
    clave_unica VARCHAR(60) GENERATED ALWAYS AS (
        CASE WHEN estado <> 'VIGENTE' THEN NULL
             WHEN concepto = 'INSCRIPCION' THEN CONCAT('I-', id_estudiante, '-', id_anio_lectivo)
             WHEN concepto = 'MENSUALIDAD' THEN CONCAT('M-', id_estudiante, '-', id_anio_lectivo, '-', mes)
             WHEN concepto = 'ACTIVIDAD' THEN CONCAT('A-', id_estudiante, '-', id_actividad)
             ELSE CONCAT('U-', id_multa) END) STORED,
    PRIMARY KEY (id_pago),
    UNIQUE KEY uq_pago_vigente (clave_unica),
    KEY ix_pago_estudiante (id_estudiante, fecha_pago),
    CONSTRAINT fk_pago_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_usuario),
    CONSTRAINT fk_pago_anio FOREIGN KEY (id_anio_lectivo) REFERENCES anio_lectivo (id_anio_lectivo),
    CONSTRAINT fk_pago_actividad FOREIGN KEY (id_actividad) REFERENCES actividad_extracurricular (id_actividad),
    CONSTRAINT fk_pago_multa FOREIGN KEY (id_multa) REFERENCES multa (id_multa),
    CONSTRAINT fk_pago_secretaria FOREIGN KEY (id_secretaria) REFERENCES secretaria (id_usuario),
    CONSTRAINT ck_pago_monto CHECK (monto >= 0),
    CONSTRAINT ck_pago_mes CHECK ((concepto = 'MENSUALIDAD' AND mes BETWEEN 1 AND 12)
                                  OR (concepto <> 'MENSUALIDAD' AND mes IS NULL)),
    CONSTRAINT ck_pago_actividad CHECK ((concepto = 'ACTIVIDAD' AND id_actividad IS NOT NULL)
                                        OR (concepto <> 'ACTIVIDAD' AND id_actividad IS NULL)),
    CONSTRAINT ck_pago_multa CHECK ((concepto = 'MULTA' AND id_multa IS NOT NULL)
                                    OR (concepto <> 'MULTA' AND id_multa IS NULL)),
    CONSTRAINT ck_pago_anulacion CHECK ((estado = 'ANULADO' AND motivo_anulacion IS NOT NULL)
                                        OR (estado = 'VIGENTE' AND motivo_anulacion IS NULL))
);

-- uno por pago; se anula junto con el pago
CREATE TABLE IF NOT EXISTS comprobante (
    id_comprobante INT NOT NULL AUTO_INCREMENT,
    id_pago INT NOT NULL,
    numero VARCHAR(20) NULL,
    fecha_emision DATETIME NOT NULL,
    estado ENUM('EMITIDO','ANULADO') NOT NULL DEFAULT 'EMITIDO',
    PRIMARY KEY (id_comprobante),
    UNIQUE KEY uq_comprobante_pago (id_pago),
    UNIQUE KEY uq_comprobante_numero (numero),
    CONSTRAINT fk_comprobante_pago FOREIGN KEY (id_pago) REFERENCES pago (id_pago)
);

-- se guarda cada emision (una por estudiante y mes, lo controla el backend)
CREATE TABLE IF NOT EXISTS solvencia (
    id_solvencia INT NOT NULL AUTO_INCREMENT,
    id_estudiante INT NOT NULL,
    id_anio_lectivo INT NOT NULL,
    id_secretaria INT NOT NULL,
    fecha_emision DATETIME NOT NULL,
    PRIMARY KEY (id_solvencia),
    CONSTRAINT fk_solvencia_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_usuario),
    CONSTRAINT fk_solvencia_anio FOREIGN KEY (id_anio_lectivo) REFERENCES anio_lectivo (id_anio_lectivo),
    CONSTRAINT fk_solvencia_secretaria FOREIGN KEY (id_secretaria) REFERENCES secretaria (id_usuario)
);

CREATE TABLE IF NOT EXISTS asistencia (
    id_asistencia INT NOT NULL AUTO_INCREMENT,
    id_asignacion INT NOT NULL,
    id_estudiante INT NOT NULL,
    fecha DATE NOT NULL,
    presente BOOLEAN NOT NULL,
    PRIMARY KEY (id_asistencia),
    UNIQUE KEY uq_asistencia (id_asignacion, fecha, id_estudiante),
    CONSTRAINT fk_asistencia_asignacion FOREIGN KEY (id_asignacion) REFERENCES asignacion_maestro (id_asignacion),
    CONSTRAINT fk_asistencia_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_usuario)
);

CREATE TABLE IF NOT EXISTS actividad_zona (
    id_actividad_zona INT NOT NULL AUTO_INCREMENT,
    id_asignacion INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    punteo DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_actividad_zona),
    UNIQUE KEY uq_actividad_zona (id_asignacion, nombre),
    CONSTRAINT fk_zona_asignacion FOREIGN KEY (id_asignacion) REFERENCES asignacion_maestro (id_asignacion),
    CONSTRAINT ck_zona_punteo CHECK (punteo > 0 AND punteo <= 70)
);

CREATE TABLE IF NOT EXISTS nota (
    id_nota INT NOT NULL AUTO_INCREMENT,
    id_actividad_zona INT NOT NULL,
    id_estudiante INT NOT NULL,
    valor DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_nota),
    UNIQUE KEY uq_nota (id_actividad_zona, id_estudiante),
    CONSTRAINT fk_nota_actividad FOREIGN KEY (id_actividad_zona) REFERENCES actividad_zona (id_actividad_zona),
    CONSTRAINT fk_nota_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_usuario),
    CONSTRAINT ck_nota_valor CHECK (valor >= 0)
);

CREATE TABLE IF NOT EXISTS nota_final (
    id_nota_final INT NOT NULL AUTO_INCREMENT,
    id_asignacion INT NOT NULL,
    id_estudiante INT NOT NULL,
    valor DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id_nota_final),
    UNIQUE KEY uq_nota_final (id_asignacion, id_estudiante),
    CONSTRAINT fk_nf_asignacion FOREIGN KEY (id_asignacion) REFERENCES asignacion_maestro (id_asignacion),
    CONSTRAINT fk_nf_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiante (id_usuario),
    CONSTRAINT ck_nf_valor CHECK (valor >= 0 AND valor <= 30)
);
