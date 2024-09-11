package xyz.junerver.composehooks.utils

import kotlin.random.Random

object NanoId {
    private const val ALPHABET = "_~0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DEFAULT_SIZE = 8
    fun generate(size: Int = DEFAULT_SIZE): String {
        val builder = StringBuilder(size)
        repeat(size) {
            val randomIndex = Random.nextInt(ALPHABET.length)
            builder.append(ALPHABET[randomIndex])
        }
        return builder.toString()
    }
}
