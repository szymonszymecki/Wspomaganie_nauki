package com.example.wspomaganie_nauki

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wspomaganie_nauki.databinding.ItemDeckListBinding

class DeckListAdapter(
    var decks: List<Deck>
) : RecyclerView.Adapter<DeckListAdapter.DeckListViewHolder>() {


    inner class DeckListViewHolder(private val binding: ItemDeckListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(deck: Deck) {
            binding.tvDeckTitle.text = deck.title
            binding.tvDeckFlashcardsCount.text = deck.count.toString()
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