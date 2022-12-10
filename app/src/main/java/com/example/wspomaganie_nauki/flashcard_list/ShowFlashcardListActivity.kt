package com.example.wspomaganie_nauki.flashcard_list

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.FieldValue
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

        deckTitle = intent.getStringExtra("deck_title").toString()
        setupRecyclerView()
        flashcardListRealtimeUpdates()
    }

    private fun setupRecyclerView() {
        showFlashcardListAdapter = ShowFlashcardListAdapter(flashcardList)
        Log.i("ADAPTER_INFO", showFlashcardListAdapter.toString())
        showFlashcardListAdapter.onItemClick = {
            val intent = Intent(this@ShowFlashcardListActivity, ShowFlashcardDetailsActivity::class.java)
            intent.putExtra("deck_title", deckTitle)
            intent.putExtra("flashcard_index", flashcardList.indexOf(it))
            startActivity(intent)
        }
        showFlashcardListAdapter.onItemLongClick = {
            val clickedViewIndex = flashcardList.indexOf(it)
            val clickedView = binding.root.rvFlashcardList.layoutManager?.findViewByPosition(clickedViewIndex)
            flashcardPopupMenu(clickedView, it)
        }
        binding.rvFlashcardList.apply {
            adapter = showFlashcardListAdapter
            layoutManager = LinearLayoutManager(this@ShowFlashcardListActivity)
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

    @SuppressLint("NotifyDataSetChanged")
    private fun flashcardListRealtimeUpdates() {
        deckCollectionRef.document(deckTitle).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this@ShowFlashcardListActivity, it.message, Toast.LENGTH_LONG).show()
            }
            documentSnapshot?.let {
                val deck = it.toObject<Deck>()
                if (deck != null) {
                    flashcardList.clear()
                    for (flashcard in deck.flashcards) {
                        flashcardList.add(flashcard)
                    }
                    showFlashcardListAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun deleteFlashCard(flashcard: Flashcard) = CoroutineScope(Dispatchers.IO).launch {
        try {
            deckCollectionRef.document(deckTitle).update(mapOf(
                "flashcards" to FieldValue.arrayRemove(flashcard),
                "count" to flashcardList.size - 1
            )).await()

            val flashcardIndex = flashcardList.indexOf(flashcard)
            flashcardList.remove(flashcard)
            withContext(Dispatchers.Main) {
                showFlashcardListAdapter.notifyItemRemoved(flashcardIndex)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@ShowFlashcardListActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}