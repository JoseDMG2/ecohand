package com.example.ecohand.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ecohand.data.local.dao.*
import com.example.ecohand.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        LeccionEntity::class,
        ProgresoLeccionEntity::class,
        ActividadDiariaEntity::class,
        LogroEntity::class,
        LogroUsuarioEntity::class,
        EstadisticasUsuarioEntity::class,
        SenaEntity::class,
        PartidaJuegoEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class EcoHandDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun leccionDao(): LeccionDao
    abstract fun progresoLeccionDao(): ProgresoLeccionDao
    abstract fun actividadDiariaDao(): ActividadDiariaDao
    abstract fun logroDao(): LogroDao
    abstract fun logroUsuarioDao(): LogroUsuarioDao
    abstract fun estadisticasUsuarioDao(): EstadisticasUsuarioDao
    abstract fun senaDao(): SenaDao
    abstract fun partidaJuegoDao(): PartidaJuegoDao

    companion object {
        @Volatile
        private var INSTANCE: EcoHandDatabase? = null
        
        fun getDatabase(context: Context): EcoHandDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EcoHandDatabase::class.java,
                    "ecohand_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database)
                                }
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    // Verificar si las señas ya existen
                                    val senasCount = database.senaDao().getTotalSenas()
                                    if (senasCount == 0) {
                                        // Insertar señas si no existen
                                        insertSenas(database)
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(database: EcoHandDatabase) {
            // Insertar lecciones predeterminadas
            val lecciones = listOf(
                LeccionEntity(
                    titulo = "Saludos Básicos",
                    descripcion = "Aprende los saludos básicos en lengua de señas peruanas. En esta lección aprenderás cómo saludar, despedirte y expresar cortesía básica.",
                    nivel = "BASICO",
                    orden = 1,
                    icono = "👋",
                    videoUrl = "4Pmnh4tRwuk",
                    categoria = "Saludos",
                    bloqueada = false
                ),
                LeccionEntity(
                    titulo = "Alfabeto",
                    descripcion = "Domina el alfabeto dactilológico peruano. Aprende a deletrear palabras letra por letra con tus manos.",
                    nivel = "BASICO",
                    orden = 2,
                    icono = "🔤",
                    videoUrl = "xEsI4vFBLSQ",
                    categoria = "Alfabeto",
                    bloqueada = true
                ),
                LeccionEntity(
                    titulo = "Números",
                    descripcion = "Aprende a contar del 0 al 10 en lengua de señas. Fundamental para expresar cantidades y números en tu comunicación.",
                    nivel = "BASICO",
                    orden = 3,
                    icono = "🔢",
                    videoUrl = "NT70U2YVqG0",
                    categoria = "Números",
                    bloqueada = true
                ),
                LeccionEntity(
                    titulo = "Cortesía",
                    descripcion = "Frases de cortesía esenciales. Aprende a dar las gracias, pedir disculpas y ser cortés en lengua de señas.",
                    nivel = "INTERMEDIO",
                    orden = 4,
                    icono = "🙏",
                    videoUrl = "R5L9bpr3QXM",
                    categoria = "Cortesía",
                    bloqueada = true
                ),
                LeccionEntity(
                    titulo = "Familia",
                    descripcion = "Vocabulario sobre los miembros de la familia. Aprende a referirte a papá, mamá, hermanos y otros familiares.",
                    nivel = "INTERMEDIO",
                    orden = 5,
                    icono = "👨‍👩‍👧‍👦",
                    videoUrl = "dernDK9ipBs",
                    categoria = "Familia",
                    bloqueada = true
                )
            )
            database.leccionDao().insertAll(lecciones)

            // Insertar logros predeterminados
            val logros = listOf(
                LogroEntity(
                    nombre = "Primer Paso",
                    descripcion = "Completa tu primera lección",
                    emoji = "🎯",
                    requisito = "Completar 1 lección"
                ),
                LogroEntity(
                    nombre = "En Racha",
                    descripcion = "Mantén una racha de 7 días",
                    emoji = "🔥",
                    requisito = "7 días consecutivos activos"
                ),
                LogroEntity(
                    nombre = "Experto en Saludos",
                    descripcion = "Completa la lección de Saludos Básicos",
                    emoji = "👋",
                    requisito = "Completar lección de Saludos"
                ),
                LogroEntity(
                    nombre = "Cortés",
                    descripcion = "Completa la lección de Cortesía",
                    emoji = "🙏",
                    requisito = "Completar lección de Cortesía"
                ),
                LogroEntity(
                    nombre = "Maestro del Alfabeto",
                    descripcion = "Domina el alfabeto completo",
                    emoji = "🔤",
                    requisito = "Completar lección de Alfabeto"
                ),
                LogroEntity(
                    nombre = "Contador Experto",
                    descripcion = "Aprende los números",
                    emoji = "🔢",
                    requisito = "Completar lección de Números"
                ),
                LogroEntity(
                    nombre = "Estudiante Dedicado",
                    descripcion = "Completa 3 lecciones",
                    emoji = "📚",
                    requisito = "Completar 3 lecciones"
                ),
                LogroEntity(
                    nombre = "Maestro EcoHand",
                    descripcion = "Completa todas las lecciones",
                    emoji = "🏆",
                    requisito = "Completar todas las lecciones"
                )
            )
            database.logroDao().insertAll(logros)

            // Insertar señas predeterminadas con descripciones
            val senas = listOf(
                SenaEntity(
                    nombre = "Amor",
                    imagenResource = "sena_amor",
                    descripcion = "Cruzar ambos puños en el pecho",
                    categoria = "EMOCIONES"
                ),
                SenaEntity(
                    nombre = "Comedor",
                    imagenResource = "sena_comedor",
                    descripcion = "1. Con los dedos pulgar e índice de ambas manos diseñar un cuadrado (seña de lugar). 2. Configurar la seña del verbo comer.",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Escuela",
                    imagenResource = "sena_escuela",
                    descripcion = "Con ambas manos juntas con los dedos semiflexionados realizar un movimiento circular de izquierda a derecha",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Familia",
                    imagenResource = "sena_familia",
                    descripcion = "1. Configurar 'F' con ambas manos. 2. Realizar un movimiento semicircular hacia adelante.",
                    categoria = "PERSONAS"
                ),
                SenaEntity(
                    nombre = "Agradecer",
                    imagenResource = "sena_agradecer",
                    descripcion = "Llevar la mano derecha extendida a la altura de los labios y bajarla haciendo un movimiento hacia adelante.",
                    categoria = "CORTESIA"
                ),
                SenaEntity(
                    nombre = "Hombre",
                    imagenResource = "sena_hombre",
                    descripcion = "Con el dedo índice sobre el labio superior indicar el lugar del bigote con un movimiento lateral.",
                    categoria = "PERSONAS"
                ),
                SenaEntity(
                    nombre = "Hospital",
                    imagenResource = "sena_hospital",
                    descripcion = "Con los dedos índice y medio de la mano derecha, hacer una cruz en el brazo izquierdo.",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Mamá",
                    imagenResource = "sena_mama",
                    descripcion = "Configurar 'M' con la mano derecha a la altura del hombro y realizar movimientos de izquierda a derecha.",
                    categoria = "FAMILIA"
                ),
                SenaEntity(
                    nombre = "Perú",
                    imagenResource = "sena_peru",
                    descripcion = "Se colocan los dedos índices y medios unidos, apuntando hacia arriba y un poco hacia adelante, de manera que la forma parezca la vincha inca con las plumas",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Trabajo",
                    imagenResource = "sena_trabajo",
                    descripcion = "Debes formar dos puños en forma de 'S'. Coloca la mano no dominante hacia abajo, con la mano dominante tocando la parte superior de la muñeca, y golpea con el puño dominante sobre el no dominante varias veces",
                    categoria = "ACTIVIDADES"
                )
            )
            database.senaDao().insertAll(senas)
        }

        private suspend fun insertSenas(database: EcoHandDatabase) {
            val senas = listOf(
                SenaEntity(
                    nombre = "Amor",
                    imagenResource = "sena_amor",
                    descripcion = "Cruzar ambos puños en el pecho",
                    categoria = "EMOCIONES"
                ),
                SenaEntity(
                    nombre = "Comedor",
                    imagenResource = "sena_comedor",
                    descripcion = "1. Con los dedos pulgar e índice de ambas manos diseñar un cuadrado (seña de lugar). 2. Configurar la seña del verbo comer.",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Escuela",
                    imagenResource = "sena_escuela",
                    descripcion = "Con ambas manos juntas con los dedos semiflexionados realizar un movimiento circular de izquierda a derecha",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Familia",
                    imagenResource = "sena_familia",
                    descripcion = "1. Configurar 'F' con ambas manos. 2. Realizar un movimiento semicircular hacia adelante.",
                    categoria = "PERSONAS"
                ),
                SenaEntity(
                    nombre = "Agradecer",
                    imagenResource = "sena_agradecer",
                    descripcion = "Llevar la mano derecha extendida a la altura de los labios y bajarla haciendo un movimiento hacia adelante.",
                    categoria = "CORTESIA"
                ),
                SenaEntity(
                    nombre = "Hombre",
                    imagenResource = "sena_hombre",
                    descripcion = "Con el dedo índice sobre el labio superior indicar el lugar del bigote con un movimiento lateral.",
                    categoria = "PERSONAS"
                ),
                SenaEntity(
                    nombre = "Hospital",
                    imagenResource = "sena_hospital",
                    descripcion = "Con los dedos índice y medio de la mano derecha, hacer una cruz en el brazo izquierdo.",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Mamá",
                    imagenResource = "sena_mama",
                    descripcion = "Configurar 'M' con la mano derecha a la altura del hombro y realizar movimientos de izquierda a derecha.",
                    categoria = "FAMILIA"
                ),
                SenaEntity(
                    nombre = "Perú",
                    imagenResource = "sena_peru",
                    descripcion = "Se colocan los dedos índices y medios unidos, apuntando hacia arriba y un poco hacia adelante, de manera que la forma parezca la vincha inca con las plumas",
                    categoria = "LUGARES"
                ),
                SenaEntity(
                    nombre = "Trabajo",
                    imagenResource = "sena_trabajo",
                    descripcion = "Debes formar dos puños en forma de 'S'. Coloca la mano no dominante hacia abajo, con la mano dominante tocando la parte superior de la muñeca, y golpea con el puño dominante sobre el no dominante varias veces",
                    categoria = "ACTIVIDADES"
                )
            )
            database.senaDao().insertAll(senas)
        }
    }
}
