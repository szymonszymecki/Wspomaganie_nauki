package com.example.wspomaganie_nauki.review

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wspomaganie_nauki.data.Deck
import com.example.wspomaganie_nauki.data.Flashcard
import com.example.wspomaganie_nauki.data.FlashcardType
import com.example.wspomaganie_nauki.data.utils.TimeParser
import com.example.wspomaganie_nauki.databinding.ActivityFlashcardsReviewBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class FlashcardsReviewActivity : AppCompatActivity() {

    private var deckCollectionRef = Firebase.firestore.collection("decks")

    private lateinit var flashcards: MutableList<Flashcard>
    private lateinit var actualFlashcard: Flashcard

    private lateinit var binding: ActivityFlashcardsReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardsReviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val deckTitle = intent.getStringExtra("deck_title")
        getFlashcardListForReview(deckTitle)

        val defaultGradeDec = "Oceń swoją odpowiedź"

        binding.rbFlashcardReviewGrade.setOnRatingBarChangeListener { ratingBar, fl, b ->
            binding.tvFlashcardReviewGradeDesc.text = when (fl) {
                0.0f -> "Nie miałem/am zielonego pojęcia"
                1.0f -> "Nie wiedziałem/am, ale wygląda znajomo"
                2.0f -> "Nie wiedziałem/am, ale było blisko"
                3.0f -> "Wiedziałem/am ale trzeba było się natrudzić"
                4.0f -> "Wiedziałem/am po chwili zastanowienia"
                5.0f -> "Wiedziałem/am od razu"
                else -> defaultGradeDec
            }
        }
        binding.btnFlashcardReviewNext.setOnClickListener {
            val rating = binding.rbFlashcardReviewGrade.rating.toInt()

            binding.tvFlashcardReviewBack.visibility = View.INVISIBLE

            binding.btnFlashcardReviewShowAnswer.visibility = View.VISIBLE
            binding.btnFlashcardReviewFinish.visibility = View.VISIBLE
            binding.rbFlashcardReviewGrade.visibility = View.INVISIBLE
            binding.btnFlashcardReviewNext.visibility = View.INVISIBLE
            binding.tvFlashcardReviewGradeDesc.visibility = View.INVISIBLE

            binding.tvFlashcardReviewGradeDesc.text = defaultGradeDec
            binding.rbFlashcardReviewGrade.rating = 0.0f

            updateFlashcard(deckTitle, rating)
        }

        binding.btnFlashcardReviewShowAnswer.setOnClickListener {
            binding.tvFlashcardReviewBack.visibility = View.VISIBLE

            binding.btnFlashcardReviewShowAnswer.visibility = View.INVISIBLE
            binding.btnFlashcardReviewFinish.visibility = View.INVISIBLE
            binding.rbFlashcardReviewGrade.visibility = View.VISIBLE
            binding.btnFlashcardReviewNext.visibility = View.VISIBLE
            binding.tvFlashcardReviewGradeDesc.visibility = View.VISIBLE
        }
        binding.btnFlashcardReviewFinish.setOnClickListener {
            finish()
        }
    }

    private fun setNewActualFlashcard() {
        actualFlashcard = flashcards[0]
        binding.tvFlashcardReviewFront.text = actualFlashcard.front
        binding.tvFlashcardReviewBack.text = actualFlashcard.back
        flashcards.removeAt(0)
    }

    private fun setResultsForSRS(grade: Int): Flashcard {

        val (newStreak, newEF, newInterval) = SRS.sm02(
            grade = grade,
            n = actualFlashcard.streak,
            EF = actualFlashcard.ef,
            interval = actualFlashcard.interval)

        return Flashcard(
            front = actualFlashcard.front,
            back = actualFlashcard.back,
            type = if ((grade == 4 && newStreak >= 5) || (grade == 5 && newStreak >= 3))
                FlashcardType.BURNED.toString() else FlashcardType.TO_REVIEW.toString(),
            streak = newStreak,
            ef = newEF,
            interval = newInterval,
            time = TimeParser.getTimeAfterPeriodToString(newInterval),
        )
    }

    private fun getFlashcardListForReview(deckTitle: String?) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val document = deckTitle?.let { deckCollectionRef.document(it).get().await() }
            val deck = document?.toObject<Deck>()
            flashcards = if (deck?.flashcards != null) deck.flashcards else mutableListOf()

            withContext(Dispatchers.Main) {
                setNewActualFlashcard()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FlashcardsReviewActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateFlashcard(deckTitle: String?, grade: Int) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val document = deckTitle?.let { deckCollectionRef.document(it).get().await() }
            val deck = document?.toObject<Deck>()
            val allFlashcards = if (deck?.flashcards != null) deck.flashcards else mutableListOf()

            val flashcardIndex = allFlashcards.indexOf(actualFlashcard)
            val newFlashcard = setResultsForSRS(grade)
            allFlashcards.remove(actualFlashcard)
            allFlashcards.add(flashcardIndex, newFlashcard)

            if (deckTitle != null) {
                deckCollectionRef.document(deckTitle).update(mapOf(
                    "flashcards" to allFlashcards,
                )).await()
            }

            withContext(Dispatchers.Main) {
                if (flashcards.isEmpty()) {
                    finish()
                } else {
                    setNewActualFlashcard()
                    if (flashcards.isEmpty()) {
                        val finishText = "Zakończ"
                        binding.btnFlashcardReviewFinish.text = finishText
                    }
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FlashcardsReviewActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}