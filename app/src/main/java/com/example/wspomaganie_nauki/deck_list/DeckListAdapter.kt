package com.example.wspomaganie_nauki.deck_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wspomaganie_nauki.data.Deck
import com.example.wspomaganie_nauki.databinding.ItemDeckListBinding

class DeckListAdapter(
    var decks: MutableList<Deck>
) : RecyclerView.Adapter<DeckListAdapter.DeckListViewHolder>() {

    var onItemClick: ((Deck) -> Unit)? = null
    var onItemLongClick: ((Deck) -> Unit)? = null

    inner class DeckListViewHolder(private val binding: ItemDeckListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(deck: Deck) {
            binding.tvDeckTitle.text = deck.title
            binding.tvDeckFlashcardsCount.text = deck.count.toString()
        }

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(decks[absoluteAdapterPosition])
            }
            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(decks[absoluteAdapterPosition])
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckListViewHolder {
        val binding = ItemDeckListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckListViewHolder, position: Int) {
        holder.bind(decks[position])
    }

    override fun getItemCount(): Int {
        return decks.size
    }
}