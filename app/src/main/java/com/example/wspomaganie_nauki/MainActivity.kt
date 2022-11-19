package com.example.wspomaganie_nauki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wspomaganie_nauki.databinding.ActivityMainBinding
import com.example.wspomaganie_nauki.deck_create.DeckCreateActivity
import com.example.wspomaganie_nauki.deck_list.DeckListActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnWelcomeReviews.setOnClickListener {

        }

        binding.btnWelcomeDecks.setOnClickListener {
            Intent(this, DeckListActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.btnWelcomeNewDeck.setOnClickListener {
            Intent(this, DeckCreateActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(it)
            }
        }
    }
}