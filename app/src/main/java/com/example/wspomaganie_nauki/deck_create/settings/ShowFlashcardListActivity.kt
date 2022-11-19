package com.example.wspomaganie_nauki.deck_create.settings

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wspomaganie_nauki.R
import com.example.wspomaganie_nauki.data.Deck
import com.example.wspomaganie_nauki.data.Flashcard
import com.example.wspomaganie_nauki.databinding.ActivityShowFlashcardListBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_show_flashcard_list.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ShowFlashcardListActivity : AppCompatActivity() {

    private var deckCollectionRef = Firebase.firestore.collection("decks")

    private lateinit var showFlashcardListAdapter: ShowFlashcardListAdapter
    private var flashcardList = mutableListOf<Flashcard>()
    private lateinit var deckTitle: String

    private lateinit var binding: ActivityShowFlashcardListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowFlashcardListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        getFlashcards()
    }

    private fun setupRecyclerView() {
        showFlashcardListAdapter = ShowFlashcardListAdapter(flashcardList)
        showFlashcardListAdapter.onItemLongClick = {
            val clickedViewIndex = flashcardList.indexOf(it)
            val clickedView = binding.root.rvFlashcardList.layoutManager?.findViewByPosition(clickedViewIndex)
            flashcardPopupMenu(clickedView, it)
        }
        binding.rvFlashcardList.apply {
            adapter = showFlashcardListAdapter
            layoutManager = LinearLayoutManager(this@ShowFlashcardListActivity)
            addItemDecoration(DividerItemDecoration(this@ShowFlashcardListActivity, DividerItemDecoration.VERTICAL))
        }
    }

    private fun flashcardPopupMenu(view: View?, flashcard: Flashcard) = PopupMenu(this@ShowFlashcardListActivity, view).apply {
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cmDeleteFlashcard -> {
                    val deleteFlashcardDialog = deleteFlashcardDialog(flashcard)
                    deleteFlashcardDialog.show()
                }
            }
            true
        }
        inflate(R.menu.flashcardlist_floating_context_menu)
        show()
    }

    private fun deleteFlashcardDialog(flashcard: Flashcard) = AlertDialog.Builder(this@ShowFlashcardListActivity).apply {
        setTitle("Usuwanie fiszki")
        setMessage("Czy na pewno chcesz usunąć tę fiszkę?")
        setPositiveButton("Tak") { _, _ ->
            deleteFlashCard(flashcard)
        }
        setNegativeButton("Nie") { _, _ ->}
    }

    private fun getFlashcards() = CoroutineScope(Dispatchers.IO).launch {
        try {
            deckTitle = intent.getStringExtra("deck_title").toString()
            val document = deckTitle.let { deckCollectionRef.document(it).get().await() }

            val deck = document?.toObject<Deck>()
            if (deck != null) {
                withContext(Dispatchers.Main) {
                    flashcardList = deck.flashcards
                    setupRecyclerView()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShowFlashcardListActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteFlashCard(flashcard: Flashcard) = CoroutineScope(Dispatchers.IO).launch {
        val flashcardIndex = flashcardList.indexOf(flashcard)
        try {
            flashcardList.remove(flashcard)
            deckCollectionRef.document(deckTitle).update(mapOf(
                "flashcards" to flashcardList,
                "count" to flashcardList.size
            )).await()

            withContext(Dispatchers.Main) {
                showFlashcardListAdapter.notifyItemRemoved(flashcardIndex)
            }
        } catch (e: Exception) {
            flashcardList.add(flashcardIndex, flashcard)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShowFlashcardListActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}