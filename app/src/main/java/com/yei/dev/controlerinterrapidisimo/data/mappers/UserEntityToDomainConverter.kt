package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession

/**
 * Converter para transformar UserEntity a UserSession (modelo de dominio).
 */
class UserEntityToDomainConverter : Converter<UserEntity, UserSession> {
    override fun convert(input: UserEntity): UserSession = UserSession(
        username = input.username,
        name = input.name
    )
}
