package com.example.wspomaganie_nauki.review

import kotlin.math.roundToInt

class SRS {

    companion object {
        private const val minEF = 1.3
        private const val defaultEF = 2.5

        fun sm02(grade: Int, n: Int, EF: Double = defaultEF, interval: Int) : Triple<Int, Double, Int> {
            val newEF = if (EF < minEF) minEF else EF + (0.1f - (5 - grade) * (0.08f + (5 - grade) * 0.02f))

            return if (grade >= 3) {
                val newInterval: Int = when (n) {
                    0 -> 1
                    1 -> 6
                    else -> (interval * EF).roundToInt()
                }
                Triple(n + 1, newEF, newInterval)
            } else {
                Triple(0, newEF, 1)
            }
        }
    }
}