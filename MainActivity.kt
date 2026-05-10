package com.example.a2026sgp_project2020312036

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import java.util.Timer
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private var gaugeTimer: Timer? = null

    private var res1Count = 0
    private var res2Count = 0
    private var res3Count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAdventure = findViewById<Button>(R.id.btnAdventure)
        val adventureGauge = findViewById<ProgressBar>(R.id.adventureGauge)
        val tvRes1 = findViewById<TextView>(R.id.tvRes1)
        val tvRes2 = findViewById<TextView>(R.id.tvRes2)
        val tvRes3 = findViewById<TextView>(R.id.tvRes3)
        val btnCraft = findViewById<Button>(R.id.btnCraft)

        btnAdventure.setOnClickListener {
            adventureGauge.progress += 10

            if (adventureGauge.progress >= 100) {
                adventureGauge.progress = 0

                when ((1..3).random()) {
                    1 -> {
                        res1Count++
                        tvRes1.text = "자원1: $res1Count"
                    }
                    2 -> {
                        res2Count++
                        tvRes2.text = "자원2: $res2Count"
                    }
                    3 -> {
                        res3Count++
                        tvRes3.text = "자원3: $res3Count"
                    }
                }
            }
        }

        btnCraft.setOnClickListener {
            val intent = Intent(this, CraftActivity::class.java)
            startActivity(intent)
        }

        startGaugeTimer(adventureGauge)
    }

    private fun startGaugeTimer(gauge: ProgressBar) {
        gaugeTimer = timer(period = 100) {
            runOnUiThread {
                if (gauge.progress > 0) {
                    gauge.progress -= 1
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gaugeTimer?.cancel()
    }
}