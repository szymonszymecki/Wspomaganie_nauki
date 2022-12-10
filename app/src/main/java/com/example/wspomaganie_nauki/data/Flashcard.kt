package com.example.wspomaganie_nauki.data

data class Flashcard(
    var front: String = "",
    var back: String = "",
    var type: String = FlashcardType.TO_LEARN.toString(),
    var streak: Int = 0,
    var ef: Double = 2.5,
    var interval: Int = 0,
    var time: String = ""
)
