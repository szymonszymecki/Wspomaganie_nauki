package com.example.wspomaganie_nauki.deck_list

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wspomaganie_nauki.data.Deck
import com.example.wspomaganie_nauki.data.Flashcard
import com.example.wspomaganie_nauki.data.FlashcardType
import com.example.wspomaganie_nauki.data.utils.AccountDataParser
import com.example.wspomaganie_nauki.data.utils.TimeParser
import com.example.wspomaganie_nauki.databinding.ActivityShowDeckDetailsBinding
import com.example.wspomaganie_nauki.flashcard_list.ShowFlashcardListActivity
import com.example.wspomaganie_nauki.review.FlashcardsReviewActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ShowDeckDetailsActivity : AppCompatActivity() {

    private var deckCollectionRef = Firebase.firestore.collection("decks")

    private lateinit var deckTitle: String

    private lateinit var binding: ActivityShowDeckDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowDeckDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        deckTitle = intent.getStringExtra("deck_title").toString()
        deckDetailsRealtimeUpdates()

        binding.btnDeckDetailsShowFlashcards.setOnClickListener {
            val intent = Intent(this@ShowDeckDetailsActivity, ShowFlashcardListActivity::class.java)
            intent.putExtra("deck_title", deckTitle)
            startActivity(intent)
        }
        binding.btnDeckDetailsStartReviews.setOnClickListener {
            if (binding.tvDeckDetailsFlashcardsCountNumber.text.toString() == "0") {
                Toast.makeText(this, "Aktualnie nie ma nic do powtórki, wróć później", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this@ShowDeckDetailsActivity, FlashcardsReviewActivity::class.java)
                intent.putExtra("deck_title", deckTitle)
                startActivity(intent)
            }
        }
        binding.btnDeckDetailsConfirm.setOnClickListener {
            updateDeckTitle(deckTitle)
            finish()
        }
    }

    private fun getReviewCount(flashcards: MutableList<Flashcard>) : Int {
        val currentTime = LocalDateTime.now()
        return flashcards.count { it.type != FlashcardType.BURNED.toString() &&
                TimeParser.getTimeFromString(it.time).isBefore(currentTime) }
    }

    private fun deckDetailsRealtimeUpdates() {
        val id = AccountDataParser.getDeckID(deckTitle)
        deckCollectionRef.document(id).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this@ShowDeckDetailsActivity, it.message, Toast.LENGTH_LONG).show()
            }
            documentSnapshot?.let { documentSnapshot ->
                val deck = documentSnapshot.toObject<Deck>()
                val flashcards = deck?.flashcards
                binding.etDeckDetailsTitle.setText(deckTitle)
                if (flashcards != null) {
                    val reviewCount = getReviewCount(flashcards)
                    binding.tvDeckDetailsFlashcardsCountNumber.text = reviewCount.toString()
                    val frequencies = flashcards.groupingBy { it.type }.eachCount()
                    frequencies["TO_LEARN"]?.let {
                        binding.tvDeckDetailsFlashcardsToLearnNumber.text = it.toString()
                    }
                    frequencies["TO_REVIEW"]?.let {
                        binding.tvDeckDetailsFlashcardsToReviewNumber.text = it.toString()
                    }
                    frequencies["BURNED"]?.let {
                        binding.tvDeckDetailsFlashcardsBurnedNumber.text = it.toString()
                    }
                }
            }
        }
    }

    private fun updateDeckTitle(deckTitle: String?) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val newDeckTitle = binding.etDeckDetailsTitle.text.toString()
            if (newDeckTitle != deckTitle) {

                val id = deckTitle?.let { AccountDataParser.getDeckID(it) }
                val document = id?.let { deckCollectionRef.document(it).get().await() }
                val deck = document?.toObject<Deck>()
                val newId = AccountDataParser.getDeckID(newDeckTitle)
                val newDocument = deckCollectionRef.document(newId).get().await()

                if (!newDocument.exists() && deck != null) {
                    deck.title = newDeckTitle
                    deckCollectionRef.document(newId).set(deck).await()
                    deckCollectionRef.document(id).delete().await()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ShowDeckDetailsActivity, "Talia z podaną nazwą już istnieje", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShowDeckDetailsActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}