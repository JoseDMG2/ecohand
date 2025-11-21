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
    version = 3,
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

            // Insertar señas predeterminadas
            val senas = listOf(
                SenaEntity(nombre = "amor", imagenResource = "sena_amor", categoria = "EMOCIONES"),
                SenaEntity(nombre = "comida", imagenResource = "sena_comida", categoria = "NECESIDADES"),
                SenaEntity(nombre = "escuela", imagenResource = "sena_escuela", categoria = "LUGARES"),
                SenaEntity(nombre = "familia", imagenResource = "sena_familia", categoria = "PERSONAS"),
                SenaEntity(nombre = "gracias", imagenResource = "sena_gracias", categoria = "CORTESIA"),
                SenaEntity(nombre = "hola", imagenResource = "sena_hola", categoria = "SALUDOS"),
                SenaEntity(nombre = "hombre", imagenResource = "sena_hombre", categoria = "PERSONAS"),
                SenaEntity(nombre = "hospital", imagenResource = "sena_hospital", categoria = "LUGARES"),
                SenaEntity(nombre = "mama", imagenResource = "sena_mama", categoria = "FAMILIA"),
                SenaEntity(nombre = "peru", imagenResource = "sena_peru", categoria = "LUGARES"),
                SenaEntity(nombre = "trabajo", imagenResource = "sena_trabajo", categoria = "ACTIVIDADES")
            )
            database.senaDao().insertAll(senas)
        }

        private suspend fun insertSenas(database: EcoHandDatabase) {
            val senas = listOf(
                SenaEntity(nombre = "amor", imagenResource = "sena_amor", categoria = "EMOCIONES"),
                SenaEntity(nombre = "comida", imagenResource = "sena_comida", categoria = "NECESIDADES"),
                SenaEntity(nombre = "escuela", imagenResource = "sena_escuela", categoria = "LUGARES"),
                SenaEntity(nombre = "familia", imagenResource = "sena_familia", categoria = "PERSONAS"),
                SenaEntity(nombre = "gracias", imagenResource = "sena_gracias", categoria = "CORTESIA"),
                SenaEntity(nombre = "hola", imagenResource = "sena_hola", categoria = "SALUDOS"),
                SenaEntity(nombre = "hombre", imagenResource = "sena_hombre", categoria = "PERSONAS"),
                SenaEntity(nombre = "hospital", imagenResource = "sena_hospital", categoria = "LUGARES"),
                SenaEntity(nombre = "mama", imagenResource = "sena_mama", categoria = "FAMILIA"),
                SenaEntity(nombre = "peru", imagenResource = "sena_peru", categoria = "LUGARES"),
                SenaEntity(nombre = "trabajo", imagenResource = "sena_trabajo", categoria = "ACTIVIDADES")
            )
            database.senaDao().insertAll(senas)
        }
    }
}
