package com.example.wspomaganie_nauki.flashcard_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wspomaganie_nauki.data.Flashcard
import com.example.wspomaganie_nauki.data.FlashcardType
import com.example.wspomaganie_nauki.databinding.ItemFlashcardListBinding

class ShowFlashcardListAdapter(
    var flashcards: MutableList<Flashcard>
) : RecyclerView.Adapter<ShowFlashcardListAdapter.ShowFlashcardListViewHolder>() {

    var onItemClick: ((Flashcard) -> (Unit))? = null
    var onItemLongClick: ((Flashcard) -> (Unit))? = null

    inner class ShowFlashcardListViewHolder(private val binding: ItemFlashcardListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(flashcard: Flashcard) {
            binding.tvShowFlashcardFront.text = flashcard.front
            binding.tvShowFlashcardBack.text = flashcard.back
        }

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(flashcards[absoluteAdapterPosition])
            }
            itemView.setOnLongClickListener {
                onItemLongClick?.invoke(flashcards[absoluteAdapterPosition])
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowFlashcardListViewHolder {
        val binding = ItemFlashcardListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShowFlashcardListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowFlashcardListViewHolder, position: Int) {
        holder.bind(flashcards[position])
    }

    override fun getItemCount(): Int {
        return flashcards.size
    }
}