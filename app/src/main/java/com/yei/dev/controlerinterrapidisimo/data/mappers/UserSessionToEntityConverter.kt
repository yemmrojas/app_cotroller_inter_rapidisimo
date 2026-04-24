package com.yei.dev.controlerinterrapidisimo.data.mappers

import com.yei.dev.controlerinterrapidisimo.data.local.entity.UserEntity
import com.yei.dev.controlerinterrapidisimo.domain.models.UserSession

/**
 * Converter para transformar UserSession (modelo de dominio) a UserEntity.
 */
class UserSessionToEntityConverter : Converter<UserSession, UserEntity> {
    override fun convert(input: UserSession): UserEntity = UserEntity(
        id = 1,
        username = input.username,
        identification = input.identification,
        name = input.name
    )
}
