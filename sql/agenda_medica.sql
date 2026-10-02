CREATE DATABASE IF NOT EXISTS Agenda_Medica
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE Agenda_Medica;

CREATE TABLE IF NOT EXISTS Usuarios (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Usuario VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Nombre VARCHAR(100) NOT NULL,
    Rol VARCHAR(50) NOT NULL DEFAULT 'RECEPCION',
    Activo BOOLEAN NOT NULL DEFAULT TRUE,
    Fecha_Creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Medicos (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Usuario_Id INT UNIQUE,
    Nombre VARCHAR(100) NOT NULL,
    Apellido_Paterno VARCHAR(100) NOT NULL,
    Apellido_Materno VARCHAR(100),
    Especialidad VARCHAR(100),
    Cedula_Profesional VARCHAR(20) UNIQUE,
    Telefono VARCHAR(20),
    Correo VARCHAR(150),
    Activo BOOLEAN NOT NULL DEFAULT TRUE,

    FOREIGN KEY (Usuario_Id)
        REFERENCES Usuarios(Id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Pacientes (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(100) NOT NULL,
    Apellido_Paterno VARCHAR(100) NOT NULL,
    Apellido_Materno VARCHAR(100),
    Fecha_Nacimiento DATE,
    Sexo VARCHAR(20),
    Telefono VARCHAR(20),
    Correo VARCHAR(150),
    Domicilio VARCHAR(250),
    Contacto_Emergencia VARCHAR(150),
    Telefono_Emergencia VARCHAR(20),
    Fecha_Registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Consultorios (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(100) NOT NULL,
    Ubicacion VARCHAR(150),
    Activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Citas (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Paciente_Id INT NOT NULL,
    Medico_Id INT NOT NULL,
    Consultorio_Id INT,
    Fecha DATE NOT NULL,
    Hora TIME NOT NULL,
    Motivo VARCHAR(250),
    Estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    Observaciones VARCHAR(500),
    Creado_Por INT,
    Fecha_Creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (Paciente_Id)
        REFERENCES Pacientes(Id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    FOREIGN KEY (Medico_Id)
        REFERENCES Medicos(Id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    FOREIGN KEY (Consultorio_Id)
        REFERENCES Consultorios(Id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    FOREIGN KEY (Creado_Por)
        REFERENCES Usuarios(Id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Expedientes (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Paciente_Id INT NOT NULL UNIQUE,
    Password_Hash VARCHAR(255) NOT NULL,
    Antecedentes_Medicos TEXT,
    Alergias TEXT,
    Medicamentos_Actuales TEXT,
    Antecedentes_Familiares TEXT,
    Cirugias TEXT,
    Enfermedades_Cronicas TEXT,
    Fecha_Creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Fecha_Actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    Activo BOOLEAN NOT NULL DEFAULT TRUE,

    FOREIGN KEY (Paciente_Id)
        REFERENCES Pacientes(Id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Consultas (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Cita_Id INT,
    Paciente_Id INT NOT NULL,
    Medico_Id INT NOT NULL,
    Fecha_Consulta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Motivo_Consulta TEXT,
    Signos_Vitales TEXT,
    Diagnostico TEXT,
    Tratamiento TEXT,
    Observaciones TEXT,
    Proxima_Cita DATE,
    Fecha_Registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (Cita_Id)
        REFERENCES Citas(Id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    FOREIGN KEY (Paciente_Id)
        REFERENCES Pacientes(Id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    FOREIGN KEY (Medico_Id)
        REFERENCES Medicos(Id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Accesos_Expediente (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Expediente_Id INT NOT NULL,
    Usuario_Id INT,
    Fecha_Hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Resultado VARCHAR(20) NOT NULL,
    Descripcion VARCHAR(250),

    FOREIGN KEY (Expediente_Id)
        REFERENCES Expedientes(Id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (Usuario_Id)
        REFERENCES Usuarios(Id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

SHOW TABLES;
