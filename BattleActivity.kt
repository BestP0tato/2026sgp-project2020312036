package com.example.a2026sgp_project2020312036

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BattleActivity : AppCompatActivity() {

    private val cardButtons = mutableListOf<Button>()
    private val cardIcons = mutableListOf("", "", "", "", "", "", "", "", "")
    private val selectedIndices = mutableListOf<Int>()
    private var matchedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        for (i in 0..8) {
            val resId = resources.getIdentifier("card$i", "id", packageName)
            cardButtons.add(findViewById(resId))
        }

        startGame()

        for (i in 0..8) {
            cardButtons[i].setOnClickListener {
                flipCard(i)
            }
        }
    }

    private fun startGame() {
        matchedCount = 0
        selectedIndices.clear()

        val rowBase = listOf("✏️", "📘", "🎓")

        val row1 = rowBase.shuffled()
        val row2 = rowBase.shuffled()
        val row3 = rowBase.shuffled()

        for (i in 0..2) cardIcons[i] = row1[i]
        for (i in 3..5) cardIcons[i] = row2[i - 3]
        for (i in 6..8) cardIcons[i] = row3[i - 6]

        for (btn in cardButtons) {
            btn.text = "?"
            btn.isEnabled = true
            btn.visibility = android.view.View.VISIBLE
        }
    }

    private fun flipCard(index: Int) {
        if (cardButtons[index].text != "?" || selectedIndices.contains(index)) {
            return
        }

        cardButtons[index].text = cardIcons[index]
        selectedIndices.add(index)

        if (selectedIndices.size == 3) {
            for (btn in cardButtons) {
                btn.isEnabled = false
            }
            checkMatch()
        }
    }

    private fun checkMatch() {
        val idx1 = selectedIndices[0]
        val idx2 = selectedIndices[1]
        val idx3 = selectedIndices[2]

        if (cardIcons[idx1] == cardIcons[idx2] && cardIcons[idx2] == cardIcons[idx3]) {
            matchedCount += 3

            cardButtons[idx1].isEnabled = false
            cardButtons[idx2].isEnabled = false
            cardButtons[idx3].isEnabled = false

            selectedIndices.clear()
            enableRemainingCards()

            if (matchedCount == 9) {
                val resultIntent = Intent()
                resultIntent.putExtra("DAMAGE", 20)
                setResult(Activity.RESULT_OK, resultIntent)

                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 500)
            }
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                cardButtons[idx1].text = "?"
                cardButtons[idx2].text = "?"
                cardButtons[idx3].text = "?"

                selectedIndices.clear()
                enableRemainingCards()
            }, 1000)
        }
    }

    private fun enableRemainingCards() {
        for (btn in cardButtons) {
            if (btn.text == "?") {
                btn.isEnabled = true
            }
        }
    }
}
