package com.example.wspomaganie_nauki.flashcard_list

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wspomaganie_nauki.data.Deck
import com.example.wspomaganie_nauki.data.Flashcard
import com.example.wspomaganie_nauki.data.FlashcardType
import com.example.wspomaganie_nauki.data.utils.AccountDataParser
import com.example.wspomaganie_nauki.databinding.ActivityShowFlashcardDetailsBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ShowFlashcardDetailsActivity : AppCompatActivity() {

    private var deckCollectionRef = Firebase.firestore.collection("decks")

    private var flashcardList = mutableListOf<Flashcard>()

    private lateinit var binding: ActivityShowFlashcardDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowFlashcardDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val deckTitle = intent.getStringExtra("deck_title")
        val flashcardIndex = intent.getIntExtra("flashcard_index", -1)

        getFlashcard(deckTitle, flashcardIndex)

        binding.btnFlashcardSettingsUpdate.setOnClickListener {
            updateFlashcard(deckTitle, flashcardIndex)
        }
    }

    private fun getFlashcard(deckTitle: String?, flashcardIndex: Int) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val id = deckTitle?.let { AccountDataParser.getDeckID(it) }
            val document = id?.let { deckCollectionRef.document(it).get().await() }
            flashcardList = document?.toObject<Deck>()?.flashcards!!
            val flashcard = flashcardList[flashcardIndex]
            withContext(Dispatchers.Main) {
                binding.etFlashcardDetailsFrontText.setText(flashcard.front)
                binding.etFlashcardDetailsBackText.setText(flashcard.back)
                binding.tvFlashcardDetailsTimeText.text = if (flashcard.type != FlashcardType.BURNED.toString()) flashcard.time else "-"
                binding.tvFlashcardDetailsStatusText.text = when (flashcard.type) {
                    FlashcardType.TO_LEARN.toString() -> "Nieotwarte"
                    FlashcardType.TO_REVIEW.toString() -> "Aktywne"
                    FlashcardType.BURNED.toString() -> "Zakończone"
                    else -> "Niezdefiniowane"
                }
                binding.tvFlashcardDetailsStreakText.text = flashcard.streak.toString()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShowFlashcardDetailsActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateFlashcard(deckTitle: String?, flashcardIndex: Int) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val front = binding.etFlashcardDetailsFrontText.text.toString()
            val back = binding.etFlashcardDetailsBackText.text.toString()
            if (flashcardList[flashcardIndex].front != front || flashcardList[flashcardIndex].back != back) {
                flashcardList[flashcardIndex].front = front
                flashcardList[flashcardIndex].back = back

                if (deckTitle != null) {
                    val id = AccountDataParser.getDeckID(deckTitle)
                    deckCollectionRef.document(id).update(mapOf(
                        "flashcards" to flashcardList
                    )).await()
                }
            }
            finish()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShowFlashcardDetailsActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}