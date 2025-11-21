# Diseño de Base de Datos - EcoHand

## Descripción General
Base de datos SQLite (Room) para la aplicación EcoHand, enfocada en el aprendizaje lúdico de lengua de señas peruanas.

## Tablas Actuales

### 1. Tabla: `users`
**Descripción:** Almacena la información de los usuarios de la aplicación.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único del usuario | PRIMARY KEY, AUTO_INCREMENT |
| username | TEXT | Nombre de usuario | NOT NULL |
| email | TEXT | Correo electrónico | NOT NULL, UNIQUE |
| password | TEXT | Contraseña encriptada | NOT NULL |
| createdAt | LONG | Timestamp de creación | NOT NULL, DEFAULT: System.currentTimeMillis() |

**Índices:**
- PRIMARY KEY en `id`
- UNIQUE en `email`

---

## Tablas Propuestas para Futuras Implementaciones

### 2. Tabla: `lecciones`
**Descripción:** Almacena las lecciones disponibles en la aplicación.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único de la lección | PRIMARY KEY, AUTO_INCREMENT |
| titulo | TEXT | Título de la lección | NOT NULL |
| descripcion | TEXT | Descripción de la lección | NOT NULL |
| nivel | TEXT | Nivel de dificultad (BASICO, INTERMEDIO, AVANZADO) | NOT NULL |
| orden | INTEGER | Orden de la lección en el curso | NOT NULL |
| icono | TEXT | URL o recurso del ícono | NULL |
| bloqueada | BOOLEAN | Indica si está bloqueada | DEFAULT: true |
| createdAt | LONG | Timestamp de creación | NOT NULL |

### 3. Tabla: `senias`
**Descripción:** Almacena las señas individuales que se enseñan.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único de la seña | PRIMARY KEY, AUTO_INCREMENT |
| palabra | TEXT | Palabra o frase en español | NOT NULL |
| categoria | TEXT | Categoría (ALFABETO, NUMEROS, SALUDOS, etc.) | NOT NULL |
| videoUrl | TEXT | URL del video demostrativo | NULL |
| imagenUrl | TEXT | URL de la imagen | NULL |
| descripcion | TEXT | Descripción de cómo realizar la seña | NOT NULL |
| dificultad | TEXT | Nivel de dificultad | NOT NULL |

### 4. Tabla: `leccion_senias`
**Descripción:** Relación muchos a muchos entre lecciones y señas.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único | PRIMARY KEY, AUTO_INCREMENT |
| leccionId | INTEGER | ID de la lección | FOREIGN KEY (lecciones.id) |
| seniaId | INTEGER | ID de la seña | FOREIGN KEY (senias.id) |
| orden | INTEGER | Orden de la seña en la lección | NOT NULL |

### 5. Tabla: `progreso_usuario`
**Descripción:** Almacena el progreso del usuario en cada lección.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único | PRIMARY KEY, AUTO_INCREMENT |
| usuarioId | INTEGER | ID del usuario | FOREIGN KEY (users.id) |
| leccionId | INTEGER | ID de la lección | FOREIGN KEY (lecciones.id) |
| completada | BOOLEAN | Si la lección fue completada | DEFAULT: false |
| puntuacion | INTEGER | Puntuación obtenida (0-100) | DEFAULT: 0 |
| intentos | INTEGER | Número de intentos | DEFAULT: 0 |
| fechaInicio | LONG | Timestamp de inicio | NOT NULL |
| fechaCompletado | LONG | Timestamp de completado | NULL |
| ultimaActualizacion | LONG | Última actualización | NOT NULL |

### 6. Tabla: `progreso_senias`
**Descripción:** Almacena el progreso del usuario en cada seña individual.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único | PRIMARY KEY, AUTO_INCREMENT |
| usuarioId | INTEGER | ID del usuario | FOREIGN KEY (users.id) |
| seniaId | INTEGER | ID de la seña | FOREIGN KEY (senias.id) |
| dominada | BOOLEAN | Si domina la seña | DEFAULT: false |
| vecesRepasada | INTEGER | Veces que repasó | DEFAULT: 0 |
| ultimaFechaRepaso | LONG | Última vez que repasó | NULL |

### 7. Tabla: `juegos`
**Descripción:** Almacena los diferentes juegos disponibles.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único del juego | PRIMARY KEY, AUTO_INCREMENT |
| nombre | TEXT | Nombre del juego | NOT NULL |
| descripcion | TEXT | Descripción del juego | NOT NULL |
| tipo | TEXT | Tipo (MEMORIA, QUIZ, RECONOCIMIENTO, etc.) | NOT NULL |
| dificultad | TEXT | Nivel de dificultad | NOT NULL |
| icono | TEXT | URL o recurso del ícono | NULL |

### 8. Tabla: `puntuaciones_juegos`
**Descripción:** Almacena las puntuaciones de los juegos.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único | PRIMARY KEY, AUTO_INCREMENT |
| usuarioId | INTEGER | ID del usuario | FOREIGN KEY (users.id) |
| juegoId | INTEGER | ID del juego | FOREIGN KEY (juegos.id) |
| puntuacion | INTEGER | Puntuación obtenida | NOT NULL |
| tiempoJuego | INTEGER | Tiempo en segundos | NOT NULL |
| fecha | LONG | Timestamp de la partida | NOT NULL |

### 9. Tabla: `estadisticas_usuario`
**Descripción:** Almacena estadísticas generales del usuario.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único | PRIMARY KEY, AUTO_INCREMENT |
| usuarioId | INTEGER | ID del usuario | FOREIGN KEY (users.id), UNIQUE |
| puntosTotales | INTEGER | Puntos acumulados totales | DEFAULT: 0 |
| leccionesCompletadas | INTEGER | Total de lecciones completadas | DEFAULT: 0 |
| seniasAprendidas | INTEGER | Total de señas aprendidas | DEFAULT: 0 |
| diasRacha | INTEGER | Días consecutivos de uso | DEFAULT: 0 |
| ultimaActividad | LONG | Última actividad registrada | NOT NULL |
| nivelActual | TEXT | Nivel del usuario | DEFAULT: 'PRINCIPIANTE' |

### 10. Tabla: `logros`
**Descripción:** Almacena los logros disponibles en la aplicación.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único del logro | PRIMARY KEY, AUTO_INCREMENT |
| nombre | TEXT | Nombre del logro | NOT NULL |
| descripcion | TEXT | Descripción | NOT NULL |
| icono | TEXT | URL o recurso del ícono | NULL |
| categoria | TEXT | Categoría del logro | NOT NULL |
| puntos | INTEGER | Puntos que otorga | NOT NULL |

### 11. Tabla: `logros_usuario`
**Descripción:** Relación de logros desbloqueados por usuarios.

| Campo | Tipo | Descripción | Restricciones |
|-------|------|-------------|---------------|
| id | INTEGER | Identificador único | PRIMARY KEY, AUTO_INCREMENT |
| usuarioId | INTEGER | ID del usuario | FOREIGN KEY (users.id) |
| logroId | INTEGER | ID del logro | FOREIGN KEY (logros.id) |
| fechaDesbloqueo | LONG | Timestamp de desbloqueo | NOT NULL |

---

## Relaciones

### Diagrama de Relaciones

```
users (1) ──────────── (*) progreso_usuario
users (1) ──────────── (*) progreso_senias
users (1) ──────────── (*) puntuaciones_juegos
users (1) ──────────── (1) estadisticas_usuario
users (1) ──────────── (*) logros_usuario

lecciones (1) ──────── (*) progreso_usuario
lecciones (1) ──────── (*) leccion_senias

senias (1) ──────────── (*) progreso_senias
senias (1) ──────────── (*) leccion_senias

juegos (1) ──────────── (*) puntuaciones_juegos

logros (1) ──────────── (*) logros_usuario
```

---

## Índices Recomendados

Para optimizar consultas frecuentes:

```sql
-- Índice para búsqueda de progreso por usuario
CREATE INDEX idx_progreso_usuario ON progreso_usuario(usuarioId);

-- Índice para búsqueda de progreso por lección
CREATE INDEX idx_progreso_leccion ON progreso_usuario(leccionId);

-- Índice compuesto para progreso de señas
CREATE INDEX idx_progreso_senias_usuario ON progreso_senias(usuarioId, seniaId);

-- Índice para puntuaciones de juegos
CREATE INDEX idx_puntuaciones_usuario ON puntuaciones_juegos(usuarioId, juegoId);

-- Índice para búsqueda de señas por categoría
CREATE INDEX idx_senias_categoria ON senias(categoria);

-- Índice para logros de usuario
CREATE INDEX idx_logros_usuario ON logros_usuario(usuarioId);
```

---

## Consideraciones de Diseño

### Seguridad
- Las contraseñas deben almacenarse encriptadas (usar bcrypt o similar)
- Implementar validación de datos antes de insertar
- Sanitizar inputs para prevenir SQL injection (Room maneja esto automáticamente)

### Rendimiento
- Usar índices en campos frecuentemente consultados
- Implementar paginación para listas largas
- Considerar cache en memoria para datos frecuentes

### Escalabilidad
- Diseño preparado para migración futura a Firebase
- Estructura normalizada para facilitar sincronización
- Timestamps para control de versiones y sincronización

### Migraciones Futuras
- Planificar estrategia de migración de SQLite a Firebase
- Mantener compatibilidad hacia atrás en actualizaciones
- Implementar versionado de base de datos con Room

---

## Migración a Firebase (Futuro)

Cuando se implemente Firebase:

### Colecciones Firestore Propuestas
- `users/` - Datos de usuarios
- `lecciones/` - Catálogo de lecciones
- `senias/` - Catálogo de señas
- `userProgress/{userId}/lecciones/` - Progreso por lección
- `userProgress/{userId}/senias/` - Progreso por seña
- `userProgress/{userId}/statistics/` - Estadísticas generales
- `juegos/` - Catálogo de juegos
- `userProgress/{userId}/gameScores/` - Puntuaciones de juegos
- `logros/` - Catálogo de logros
- `userProgress/{userId}/achievements/` - Logros desbloqueados

### Sincronización
- Implementar sincronización bidireccional
- Manejar conflictos con timestamps
- Modo offline con sincronización posterior
