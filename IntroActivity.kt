package com.example.a2026sgp_project2020312036

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private var currentCut = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val imgIntroCartoon = findViewById<ImageView>(R.id.imgIntroCartoon)
        val introMainLayout = findViewById<View>(R.id.introMainLayout)

        introMainLayout.setOnClickListener {

            val clickPlayer = MediaPlayer.create(this, R.raw.click)
            clickPlayer.setOnCompletionListener { mp -> mp.release() }
            clickPlayer.start()

            currentCut++

            when (currentCut) {
                2 -> imgIntroCartoon.setImageResource(R.drawable.intro_page2)
                3 -> imgIntroCartoon.setImageResource(R.drawable.intro_page3)
                4 -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
