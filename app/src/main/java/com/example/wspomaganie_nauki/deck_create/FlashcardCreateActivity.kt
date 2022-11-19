package com.example.wspomaganie_nauki.deck_create

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wspomaganie_nauki.R
import com.example.wspomaganie_nauki.data.Deck
import com.example.wspomaganie_nauki.data.Flashcard
import com.example.wspomaganie_nauki.databinding.ActivityFlashcardCreateBinding
import com.example.wspomaganie_nauki.deck_create.settings.ShowFlashcardListActivity
import com.example.wspomaganie_nauki.deck_list.DeckListActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FlashcardCreateActivity : AppCompatActivity() {

    private var deckCollectionRef = Firebase.firestore.collection("decks")

    private var deckTitle: String? = null

    private lateinit var binding: ActivityFlashcardCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        deckTitle = intent.getStringExtra("deck_title")

        binding.btnCreateFlashcardAdd.setOnClickListener {
            val front = binding.etCreateFlashcardFront.text.toString()
            val back = binding.etCreateFlashcardBack.text.toString()
            if (front.isEmpty() || back.isEmpty()) {
                Toast.makeText(this, "Uzupełnij dane", Toast.LENGTH_LONG).show()
            } else if (deckTitle != null) {
                addFlashcard(Flashcard(front, back))
            }
        }

        binding.btnCreateFlashcardFinish.setOnClickListener {
            val intent = Intent(this, DeckListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.flashcard_create_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miShowFlashcards -> Intent(this, ShowFlashcardListActivity::class.java).also {
                it.putExtra("deck_title", deckTitle)
                startActivity(it)
            }
        }
        return true
    }

    private fun getDeckMap(deck: Deck): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["flashcards"] = deck.flashcards
        map["count"] = deck.flashcards.size

        return map
    }

    private fun addFlashcard(flashcard: Flashcard) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val document = deckTitle?.let { deckCollectionRef.document(it).get().await() }

            val deck = document?.toObject<Deck>()

            if (deck?.flashcards?.contains(flashcard) == false) {
                deck.flashcards.add(flashcard)
                val deckMap = getDeckMap(deck)
                deckCollectionRef.document(deck.title).set(
                    deckMap,
                    SetOptions.merge()
                ).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FlashcardCreateActivity, "Dodano nową fiszkę", Toast.LENGTH_LONG).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FlashcardCreateActivity, "Taka fiszka już istnieje", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FlashcardCreateActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}