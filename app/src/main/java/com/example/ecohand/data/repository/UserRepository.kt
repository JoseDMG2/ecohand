package com.example.ecohand.data.repository

import com.example.ecohand.data.local.dao.UserDao
import com.example.ecohand.data.local.entity.UserEntity

class UserRepository(private val userDao: UserDao) {
    
    suspend fun registerUser(username: String, email: String, password: String): Result<Long> {
        return try {
            // Verificar si el email ya existe
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                Result.failure(Exception("El correo electrónico ya está registrado"))
            } else {
                val user = UserEntity(
                    username = username,
                    email = email,
                    password = password
                )
                val userId = userDao.insertUser(user)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.login(email, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: Int): UserEntity? {
        return try {
            userDao.getUserById(userId)
        } catch (e: Exception) {
            null
        }
    }
}
