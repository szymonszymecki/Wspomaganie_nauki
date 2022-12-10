package com.example.wspomaganie_nauki.deck_list

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
import com.example.wspomaganie_nauki.data.utils.AccountDataParser
import com.example.wspomaganie_nauki.databinding.ActivityDeckListBinding
import com.example.wspomaganie_nauki.deck_create.FlashcardCreateActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_deck_list.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DeckListActivity : AppCompatActivity() {

    private var deckCollectionRef = Firebase.firestore.collection("decks")

    private lateinit var deckListAdapter: DeckListAdapter
    private val deckList = mutableListOf<Deck>()

    private lateinit var binding: ActivityDeckListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeckListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupRecyclerView()
        deckListRealtimeUpdates()
    }

    private fun setupRecyclerView() {
        deckListAdapter = DeckListAdapter(deckList)
        deckListAdapter.onItemClick = {
            val intent = Intent(this@DeckListActivity, ShowDeckDetailsActivity::class.java)
            intent.putExtra("deck_title", it.title)
            startActivity(intent)
        }
        deckListAdapter.onItemLongClick = {
            val clickedViewIndex = deckList.indexOf(it)
            val clickedView = binding.root.rvDeckList.layoutManager?.findViewByPosition(clickedViewIndex)
            deckPopupMenu(clickedView, it)
        }
        binding.rvDeckList.apply {
            adapter = deckListAdapter
            layoutManager = LinearLayoutManager(this@DeckListActivity)
        }
    }

    private fun deckPopupMenu(view: View?, deck: Deck) = PopupMenu(this@DeckListActivity, view).apply {
        setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cmDeleteDeck -> {
                    val deleteDeckDialog = deleteDeckDialog(deck)
                    deleteDeckDialog.show()
                }
                R.id.cmAddFlashcard -> {
                    Intent(this@DeckListActivity, FlashcardCreateActivity::class.java).also {
                        it.putExtra("deck_title", deck.title)
                        startActivity(it)
                    }
                }
            }
            true
        }
        inflate(R.menu.decklist_floating_context_menu)
        show()
    }

    private fun deleteDeckDialog(deck: Deck) = AlertDialog.Builder(this@DeckListActivity).apply {
        setTitle("Usuwanie talii")
        setMessage("Czy na pewno chcesz usunąć talię '${deck.title}'?")
        setPositiveButton("Tak") { _, _ ->
            deleteDeck(deck)
        }
        setNegativeButton("Nie") { _, _ ->}
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deckListRealtimeUpdates() {
        deckCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this@DeckListActivity, it.message, Toast.LENGTH_LONG).show()
            }
            querySnapshot?.let {
                deckList.clear()
                for (document in it.documents) {
                    if (AccountDataParser.getEmailFromID(document.id) == AccountDataParser.getEmail()) {
                        val deck = document.toObject<Deck>()
                        if (deck != null) {
                            deckList.add(deck)
                        }
                    }
                }
                deckListAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteDeck(deck: Deck) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val id = AccountDataParser.getDeckID(deck.title)
            deckCollectionRef.document(id).delete().await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@DeckListActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}