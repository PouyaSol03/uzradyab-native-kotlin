package com.example.uzradyab.data.repository

import com.example.uzradyab.data.local.dao.UserSessionDao
import com.example.uzradyab.data.mapper.toDomain
import com.example.uzradyab.data.mapper.toEntity
import com.example.uzradyab.data.remote.api.AuthHelperApi
import com.example.uzradyab.data.remote.api.TraccarApi
import com.example.uzradyab.data.remote.dto.CreateUserRequestDto
import com.example.uzradyab.data.remote.dto.OtpRequestDto
import com.example.uzradyab.domain.model.UserSession
import com.example.uzradyab.domain.repository.RegistrationRepository
import javax.inject.Inject

class RegistrationRepositoryImpl @Inject constructor(
    private val authHelperApi: AuthHelperApi,
    private val traccarApi: TraccarApi,
    private val userSessionDao: UserSessionDao,
) : RegistrationRepository {
    override suspend fun sendOtp(phoneNumber: String): Result<Unit> = runCatching {
        authHelperApi.sendOtp(OtpRequestDto(phone = phoneNumber))
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): Result<Unit> = runCatching {
        authHelperApi.verifyOtp(OtpRequestDto(phone = phoneNumber, otp = otp))
    }

    override suspend fun createUserAndLogin(
        name: String,
        phoneNumber: String,
        password: String,
    ): Result<UserSession> = runCatching {
        traccarApi.createUser(
            CreateUserRequestDto(
                login = phoneNumber,
                email = phoneNumber,
                password = password,
                name = name,
            ),
        )
        val session = traccarApi.login(phoneNumber = phoneNumber, password = password)
        val entity = session.toEntity()
        userSessionDao.upsert(entity)
        entity.toDomain()
    }
}
