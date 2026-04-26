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
import com.yei.dev.controlerinterrapidisimo.domain.utils.cleanCredential
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

        // Clean credentials by removing newlines and trimming
        val cleanUsername = username.cleanCredential()
        val cleanPassword = password.cleanCredential()

        // Authenticate with remote service
        val authResult = networkHandler.safeApiCall {
            apiService.authenticateUser(
                usuario = cleanUsername,
                identificacion = IDENTIFICATION,
                accept = HEADER_ACCEPT_JSON,
                idUsuario = cleanUsername,
                idCentroServicio = DEFAULT_ID_CENTRO_SERVICIO,
                nombreCentroServicio = DEFAULT_NOMBRE_CENTRO_SERVICIO,
                idAplicativoOrigen = DEFAULT_ID_APLICATIVO_ORIGEN,
                contentType = HEADER_CONTENT_TYPE_JSON,
                request = AuthRequestDto(
                    mac = mac,
                    nomApplication = APP_NAME,
                    password = cleanPassword,
                    path = EMPTY_PATH,
                    user = cleanUsername,
                ),
            )
        }

        when (authResult) {
            is Result.Success -> {
                val authResponse = authResponseConverter.convert(authResult.data)
                val userSession = authResponse.toUserSession()

                // Validate user session data - only username is required
                if (userSession.username.isBlank()) {
                    emit(
                        Result.Error(
                            AppError.ValidationError(
                                field = FIELD_USER,
                                message = "Username cannot be empty",
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

    override fun getUserSession(): Flow<Result<UserSession?>> = flow<Result<UserSession?>> {
            val userEntity = userDao.getUser()
            val userSession = userEntity?.let { userEntityToDomainConverter.convert(it) }
            emit(Result.Success(userSession))
    }.catch { e ->
        emit(
            Result.Error(
                AppError.DatabaseError(
                    message = MESSAGE_FAILED_RETRIEVE_SESSION,
                    cause = e,
                ),
            ),
        )
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
        const val HEADER_ACCEPT_JSON = "text/json"
        const val HEADER_CONTENT_TYPE_JSON = "application/json"
        const val DEFAULT_ID_CENTRO_SERVICIO = "1295"
        const val DEFAULT_NOMBRE_CENTRO_SERVICIO = "PTO/BOGOTA/CUND/COL/OF PRINCIPAL - CRA 30 # 7-45"
        const val DEFAULT_ID_APLICATIVO_ORIGEN = "9"
        const val APP_NAME = "Controller APP"
        const val IDENTIFICATION = "987204545"
        const val EMPTY_PATH = ""
    }
}
