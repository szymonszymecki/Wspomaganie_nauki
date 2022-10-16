package com.example.wspomaganie_nauki

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.wspomaganie_nauki.databinding.ActivityMainBinding

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

        }
    }
}