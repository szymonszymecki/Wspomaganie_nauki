package com.example.wspomaganie_nauki

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wspomaganie_nauki.databinding.ActivityDeckListBinding

class DeckListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeckListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeckListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val deckList = mutableListOf(
            Deck("Talia 1", 10),
            Deck("Talia 2", 123),
            Deck("Talia 3", 76),
            Deck("Talia 4", 36),
            Deck("Talia 5", 86),
            Deck("Talia 6", 108),
            Deck("Talia 7", 33),
        )

        val adapter = DeckListAdapter(deckList)
        binding.rvDeckList.adapter = adapter
        binding.rvDeckList.layoutManager = LinearLayoutManager(this)
    }
}