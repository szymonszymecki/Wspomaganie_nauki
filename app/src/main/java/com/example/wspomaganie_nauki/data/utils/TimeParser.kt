package com.example.wspomaganie_nauki.data.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeParser {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun getCurrentTimeToString() : String {
            val current = LocalDateTime.now()
            return current.format(formatter)
        }

        fun getTimeAfterPeriodToString(numberOfDays: Int) : String {
            val current = LocalDateTime.now()
            val modifiedDate = current.plusDays(numberOfDays.toLong())
            return modifiedDate.format(formatter)
        }

        fun getTimeFromString(date: String) : LocalDateTime {
            return LocalDateTime.parse(date, formatter)
        }
    }
}