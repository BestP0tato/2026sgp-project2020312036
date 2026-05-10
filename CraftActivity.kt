package com.example.a2026sgp_project2020312036

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CraftActivity : AppCompatActivity() {

    private val arrowOptions = listOf("↑", "↓", "←", "→")
    private var targetSequence = mutableListOf<String>()

    private lateinit var tvTargetArrows: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_craft)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTargetArrows = findViewById(R.id.tvTargetArrows)
        val btnBackToMain = findViewById<Button>(R.id.btnBackToMain)
        val btnUp = findViewById<Button>(R.id.btnUp)
        val btnDown = findViewById<Button>(R.id.btnDown)
        val btnLeft = findViewById<Button>(R.id.btnLeft)
        val btnRight = findViewById<Button>(R.id.btnRight)

        btnBackToMain.setOnClickListener {
            finish()
        }

        generateNewSequence()

        btnUp.setOnClickListener { checkInput("↑") }
        btnDown.setOnClickListener { checkInput("↓") }
        btnLeft.setOnClickListener { checkInput("←") }
        btnRight.setOnClickListener { checkInput("→") }
    }

    private fun generateNewSequence() {
        targetSequence.clear()
        for (i in 1..5) {
            targetSequence.add(arrowOptions.random())
        }
        updateTargetUI()
    }

    private fun updateTargetUI() {
        tvTargetArrows.text = targetSequence.joinToString(" ")
    }

    private fun checkInput(input: String) {
        if (targetSequence.isNotEmpty()) {
            val currentTarget = targetSequence[0]

            if (input == currentTarget) {
                targetSequence.removeAt(0)
                updateTargetUI()

                if (targetSequence.isEmpty()) {
                    Toast.makeText(this, "제작 성공! 소모품을 획득했습니다.", Toast.LENGTH_SHORT).show()
                    generateNewSequence()
                }
            } else {
                Toast.makeText(this, "틀렸습니다! 다시 입력하세요.", Toast.LENGTH_SHORT).show()
                generateNewSequence()
            }
        }
    }
}