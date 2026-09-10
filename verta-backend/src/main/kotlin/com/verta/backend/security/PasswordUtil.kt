package com.verta.backend.security

import at.favre.lib.crypto.bcrypt.BCrypt

/** Hash e verificacao de senha para public.usuarios.senha. */
object PasswordUtil {

    fun hash(plainPassword: String): String =
        BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())

    fun verify(plainPassword: String, hashed: String): Boolean =
        BCrypt.verifyer().verify(plainPassword.toCharArray(), hashed).verified
}
