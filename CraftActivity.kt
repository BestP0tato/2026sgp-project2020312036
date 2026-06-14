package com.example.a2026sgp_project2020312036

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CraftActivity : AppCompatActivity() {

    private val arrowList = mutableListOf<String>()
    private val userInputs = mutableListOf<String>()
    private var currentIndex = 0

    private lateinit var tvArrow0: TextView
    private lateinit var tvArrow1: TextView
    private lateinit var tvArrow2: TextView
    private lateinit var tvArrow3: TextView
    private lateinit var tvArrow4: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_craft)

        tvArrow0 = findViewById(R.id.tvArrow0)
        tvArrow1 = findViewById(R.id.tvArrow1)
        tvArrow2 = findViewById(R.id.tvArrow2)
        tvArrow3 = findViewById(R.id.tvArrow3)
        tvArrow4 = findViewById(R.id.tvArrow4)

        val btnUp = findViewById<Button>(R.id.btnUp)
        val btnDown = findViewById<Button>(R.id.btnDown)
        val btnLeft = findViewById<Button>(R.id.btnLeft)
        val btnRight = findViewById<Button>(R.id.btnRight)

        generateArrows()

        btnUp.setOnClickListener { handleInput("↑") }
        btnDown.setOnClickListener { handleInput("↓") }
        btnLeft.setOnClickListener { handleInput("←") }
        btnRight.setOnClickListener { handleInput("→") }
    }

    private fun generateArrows() {
        val arrows = listOf("↑", "↓", "←", "→")
        arrowList.clear()
        userInputs.clear()
        currentIndex = 0

        for (i in 0..4) {
            arrowList.add(arrows.random())
        }

        tvArrow0.text = arrowList[0]
        tvArrow1.text = arrowList[1]
        tvArrow2.text = arrowList[2]
        tvArrow3.text = arrowList[3]
        tvArrow4.text = arrowList[4]

        resetArrowColors()
    }

    private fun handleInput(input: String) {
        if (currentIndex < arrowList.size) {
            if (arrowList[currentIndex] == input) {
                highlightArrow(currentIndex, android.graphics.Color.GREEN)
                currentIndex++

                if (currentIndex == arrowList.size) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun highlightArrow(index: Int, color: Int) {
        when (index) {
            0 -> tvArrow0.setTextColor(color)
            1 -> tvArrow1.setTextColor(color)
            2 -> tvArrow2.setTextColor(color)
            3 -> tvArrow3.setTextColor(color)
            4 -> tvArrow4.setTextColor(color)
        }
    }

    private fun resetArrowColors() {
        val defaultColor = android.graphics.Color.BLACK
        tvArrow0.setTextColor(defaultColor)
        tvArrow1.setTextColor(defaultColor)
        tvArrow2.setTextColor(defaultColor)
        tvArrow3.setTextColor(defaultColor)
        tvArrow4.setTextColor(defaultColor)
    }
}
