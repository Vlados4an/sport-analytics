package com.sportanalytics.core.repository

import com.sportanalytics.core.model.User
import com.sportanalytics.jooq.tables.Users.USERS
import org.jooq.DSLContext
import org.jooq.Record

class UserRepository(private val dsl: DSLContext) {

    fun findByLogin(login: String): User? =
        dsl.selectFrom(USERS).where(USERS.LOGIN.eq(login)).fetchOne()?.let { mapUser(it) }

    fun insert(user: User): Int =
        dsl.insertInto(USERS)
            .set(USERS.LOGIN, user.login)
            .set(USERS.PASSWORD_HASH, user.passwordHash)
            .set(USERS.ROLE, user.role)
            .returningResult(USERS.ID)
            .fetchOne()!!
            .getValue(USERS.ID)

    fun existsByLogin(login: String): Boolean =
        dsl.fetchExists(dsl.selectFrom(USERS).where(USERS.LOGIN.eq(login)))

    private fun mapUser(r: Record) = User(
        id = r[USERS.ID], login = r[USERS.LOGIN], passwordHash = r[USERS.PASSWORD_HASH],
        role = r[USERS.ROLE], createdAt = r[USERS.CREATED_AT],
    )
}
