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
        EstadisticasUsuarioEntity::class
    ],
    version = 2,
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
                    descripcion = "Aprende los saludos básicos en lengua de señas",
                    nivel = "BASICO",
                    orden = 1,
                    icono = "👋",
                    bloqueada = false
                ),
                LeccionEntity(
                    titulo = "Alfabeto",
                    descripcion = "Aprende el alfabeto dactilológico",
                    nivel = "BASICO",
                    orden = 2,
                    icono = "🔤",
                    bloqueada = false
                ),
                LeccionEntity(
                    titulo = "Números",
                    descripcion = "Aprende los números del 0 al 10",
                    nivel = "BASICO",
                    orden = 3,
                    icono = "🔢",
                    bloqueada = false
                ),
                LeccionEntity(
                    titulo = "Cortesía",
                    descripcion = "Frases de cortesía en lengua de señas",
                    nivel = "INTERMEDIO",
                    orden = 4,
                    icono = "🙏",
                    bloqueada = false
                ),
                LeccionEntity(
                    titulo = "Familia",
                    descripcion = "Vocabulario sobre la familia",
                    nivel = "INTERMEDIO",
                    orden = 5,
                    icono = "👨‍👩‍👧‍👦",
                    bloqueada = false
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
        }
    }
}
