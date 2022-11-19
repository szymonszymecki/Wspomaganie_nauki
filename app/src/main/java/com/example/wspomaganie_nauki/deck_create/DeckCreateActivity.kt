package com.example.wspomaganie_nauki.deck_create

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wspomaganie_nauki.data.Deck
import com.example.wspomaganie_nauki.databinding.ActivityDeckCreateBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class DeckCreateActivity : AppCompatActivity() {

    private var deckCollectionRef = Firebase.firestore.collection("decks")

    private lateinit var binding: ActivityDeckCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeckCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnCreateDeckFinish.setOnClickListener {
            val title = binding.etCreateDeckName.text.toString()
            if (title.isEmpty()) {
                Toast.makeText(this, "Wprowadź nazwę talii", Toast.LENGTH_SHORT).show()
            } else {
                addDeck(Deck(title = title, count = 0))
            }
        }
    }

    private fun addDeck(deck: Deck) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val document = deckCollectionRef.document(deck.title).get().await()

            if (!document.exists()) {
                deckCollectionRef.document(deck.title).set(deck).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeckCreateActivity, "Dodano pustą talię", Toast.LENGTH_LONG).show()

                    Intent(this@DeckCreateActivity, FlashcardCreateActivity::class.java).also {
                        it.putExtra("deck_title", deck.title)
                        it.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                        startActivity(it)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DeckCreateActivity, "Talia z podaną nazwą już istnieje", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@DeckCreateActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}