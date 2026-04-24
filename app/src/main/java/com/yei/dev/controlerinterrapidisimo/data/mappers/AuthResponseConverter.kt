package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.remote.dto.AuthResponseDto
import com.yei.dev.controlerinterrapidisimo.domain.models.AuthResponse

/**
 * Converter para transformar AuthResponseDto a AuthResponse (modelo de dominio).
 */
class AuthResponseConverter : Converter<AuthResponseDto, AuthResponse> {
    override fun convert(input: AuthResponseDto): AuthResponse = AuthResponse(
        username = input.username,
        identification = input.identification,
        name = input.name,
        token = null
    )
}
