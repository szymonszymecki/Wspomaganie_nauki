package com.example.wspomaganie_nauki.data

data class Deck(
    var title: String = "",
    var count: Int = 0,
    var flashcards: MutableList<Flashcard> = mutableListOf()
)
