package com.testnobody.freegame

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MainActivity : AppCompatActivity() {

    private var allGames: List<Game> = emptyList()
    private lateinit var adapter: GameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        allGames = GameRepository.loadGames(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_about) {
                showAbout()
                true
            } else false
        }

        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(this, 3)
        adapter = GameAdapter(allGames) { openGame(it) }
        recycler.adapter = adapter

        buildCategoryChips()
    }

    private fun buildCategoryChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.chips)
        val order = listOf("街机", "益智", "棋牌", "射击", "动作", "休闲", "卡牌", "策略")
        val present = allGames.map { it.category }.toSet()
        val categories = listOf("全部") + order.filter { it in present } +
            (present - order.toSet()).sorted()

        categories.forEachIndexed { index, category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = index == 0
                setOnClickListener { filterBy(category) }
            }
            chipGroup.addView(chip)
        }
    }

    private fun filterBy(category: String) {
        val list = if (category == "全部") allGames else allGames.filter { it.category == category }
        adapter.update(list)
    }

    private fun openGame(game: Game) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(GameActivity.EXTRA_GAME, GameRepository.toJson(game))
        startActivity(intent)
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_text)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private class GameAdapter(
        private var games: List<Game>,
        private val onClick: (Game) -> Unit,
    ) : RecyclerView.Adapter<GameAdapter.Holder>() {

        fun update(newGames: List<Game>) {
            games = newGames
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_game, parent, false)
            return Holder(view, onClick)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) =
            holder.bind(games[position])

        override fun getItemCount(): Int = games.size

        private class Holder(view: View, onClick: (Game) -> Unit) :
            RecyclerView.ViewHolder(view) {
            private val card: MaterialCardView = view as MaterialCardView
            private val icon: TextView = view.findViewById(R.id.game_icon)
            private val name: TextView = view.findViewById(R.id.game_name)
            private var game: Game? = null

            init {
                card.setOnClickListener { game?.let(onClick) }
            }

            fun bind(g: Game) {
                game = g
                icon.text = g.icon
                name.text = g.name
            }
        }
    }
}
