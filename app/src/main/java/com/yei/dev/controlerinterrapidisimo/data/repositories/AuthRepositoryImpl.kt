package com.yei.dev.controlerinterrapidisimo.data.repositories

import com.yei.dev.controlerinterrapidisimo.data.local.dao.UserDao
import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity
import com.yei.dev.controlerinterrapidisimo.data.mappers.Converter
import com.yei.dev.controlerinterrapidisimo.data.remote.NetworkHandler
import com.yei.dev.controlerinterrapidisimo.data.remote.api.AuthApiService
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthRequestDto
import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthResponseDto
import com.yei.dev.controlerinterrapidisimo.domain.models.AppError
import com.yei.dev.controlerinterrapidisimo.domain.models.AuthResponse
import com.yei.dev.controlerinterrapidisimo.domain.models.Result
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession
import com.yei.dev.controlerinterrapidisimo.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of AuthRepository.
 *
 * Handles user authentication with the remote service and manages user session
 * persistence in the local database.
 */
class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userDao: UserDao,
    private val networkHandler: NetworkHandler,
    private val authResponseConverter: Converter<AuthResponseDto, AuthResponse>,
    private val userEntityToDomainConverter: Converter<UserEntity, UserSession>,
    private val userSessionToEntityConverter: Converter<UserSession, UserEntity>,
) : AuthRepository {

    override fun login(
        username: String,
        password: String,
        mac: String,
    ): Flow<Result<UserSession>> = flow {
        // Validate input
        if (username.isBlank() || password.isBlank()) {
            emit(
                Result.Error(
                    AppError.ValidationError(
                        field = FIELD_CREDENTIALS,
                        message = MESSAGE_EMPTY_CREDENTIALS,
                    ),
                ),
            )
            return@flow
        }

        // Authenticate with remote service
        val authResult = networkHandler.safeApiCall {
            apiService.authenticateUser(
                usuario = username,
                identificacion = username,
                accept = HEADER_ACCEPT_JSON,
                idUsuario = DEFAULT_ID_USUARIO,
                idCentroServicio = DEFAULT_ID_CENTRO_SERVICIO,
                nombreCentroServicio = APP_NAME,
                idAplicativoOrigen = DEFAULT_ID_APLICATIVO_ORIGEN,
                contentType = HEADER_CONTENT_TYPE_JSON,
                request = AuthRequestDto(
                    mac = mac,
                    nomApplication = APP_NAME,
                    password = password,
                    path = EMPTY_PATH,
                    user = username,
                ),
            )
        }

        when (authResult) {
            is Result.Success -> {
                val authResponse = authResponseConverter.convert(authResult.data)
                val userSession = authResponse.toUserSession()

                // Validate user session data
                if (userSession.username.isBlank() || userSession.name.isBlank()) {
                    emit(
                        Result.Error(
                            AppError.ValidationError(
                                field = FIELD_USER,
                                message = MESSAGE_EMPTY_USER_FIELDS,
                            ),
                        ),
                    )
                    return@flow
                }

                // Save session to database
                try {
                    userDao.insertUser(userSessionToEntityConverter.convert(userSession))
                    emit(Result.Success(userSession))
                } catch (e: Exception) {
                    emit(
                        Result.Error(
                            AppError.DatabaseError(
                                message = MESSAGE_FAILED_SAVE_SESSION,
                                cause = e,
                            ),
                        ),
                    )
                }
            }

            is Result.Error -> emit(authResult)
        }
    }

    override fun getUserSession(): Flow<Result<UserSession?>> = flow {
        try {
            val userEntity = userDao.getUser()
            val userSession = userEntity?.let { userEntityToDomainConverter.convert(it) }
            emit(Result.Success(userSession))
        } catch (e: Exception) {
            emit(
                Result.Error(
                    AppError.DatabaseError(
                        message = MESSAGE_FAILED_RETRIEVE_SESSION,
                        cause = e,
                    ),
                ),
            )
        }
    }

    override fun clearUserSession(): Flow<Result<Unit>> = flow {
        try {
            userDao.clearUsers()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(
                Result.Error(
                    AppError.DatabaseError(
                        message = MESSAGE_FAILED_CLEAR_SESSION,
                        cause = e,
                    ),
                ),
            )
        }
    }

    internal companion object {
        const val FIELD_CREDENTIALS = "credentials"
        const val FIELD_USER = "user"
        const val MESSAGE_EMPTY_CREDENTIALS = "Username and password cannot be empty"
        const val MESSAGE_EMPTY_USER_FIELDS = "All user fields must be non-empty"
        const val MESSAGE_FAILED_SAVE_SESSION = "Failed to save user session"
        const val MESSAGE_FAILED_RETRIEVE_SESSION = "Failed to retrieve user session"
        const val MESSAGE_FAILED_CLEAR_SESSION = "Failed to clear user session"
        const val HEADER_ACCEPT_JSON = "application/json"
        const val HEADER_CONTENT_TYPE_JSON = "application/json"
        const val DEFAULT_ID_USUARIO = "0"
        const val DEFAULT_ID_CENTRO_SERVICIO = "0"
        const val DEFAULT_ID_APLICATIVO_ORIGEN = "1"
        const val APP_NAME = "ControllerApp"
        const val EMPTY_PATH = ""
    }
}
