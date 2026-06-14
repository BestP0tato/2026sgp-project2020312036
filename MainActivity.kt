package com.example.a2026sgp_project2020312036

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.math.cos
import kotlin.math.exp

class MainActivity : AppCompatActivity() {

    private var gaugeTimer: Timer? = null
    private var rampageAnimator: ValueAnimator? = null
    private var isHurtOrDead = false

    private lateinit var sharedPreferences: SharedPreferences

    private var bgmPlayer: MediaPlayer? = null
    private lateinit var soundPool: SoundPool
    private var soundClick = 0
    private var soundThrow = 0
    private var soundWin = 0

    private var requiredMajor = 0
    private var electiveMajor = 0
    private var liberalArts = 0
    private var ammobooks = 0
    private var bossHp = 100

    private val battleResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val damage = result.data?.getIntExtra("DAMAGE", 0) ?: 0
            if (damage > 0) {
                imgMainCharacter.postDelayed({
                    launchBookPhysics(damage)
                }, 500)
            }
        }
    }

    private lateinit var imgMainCharacter: ImageView
    private lateinit var tvRes1: TextView
    private lateinit var tvRes2: TextView
    private lateinit var tvRes3: TextView
    private lateinit var tvBookCount: TextView
    private lateinit var btnBattle: Button
    private lateinit var bossHpBar: ProgressBar
    private lateinit var adventureGauge: ProgressBar

    private var lastFlipTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("TinoGamePrefs", Context.MODE_PRIVATE)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundClick = soundPool.load(this, R.raw.click, 1)
        soundThrow = soundPool.load(this, R.raw.toss, 1)
        soundWin = soundPool.load(this, R.raw.win, 1)

        tvRes1 = findViewById(R.id.tvRes1)
        tvRes2 = findViewById(R.id.tvRes2)
        tvRes3 = findViewById(R.id.tvRes3)
        tvBookCount = findViewById<TextView>(R.id.tvBookCount)
        adventureGauge = findViewById<ProgressBar>(R.id.adventureGauge)

        val btnAdventure = findViewById<Button>(R.id.btnAdventure)
        val btnCraft = findViewById<Button>(R.id.btnCraft)
        btnBattle = findViewById<Button>(R.id.btnBattle)

        val btnCheatAdd = findViewById<Button>(R.id.btnCheatAdd)
        val btnCheatClear = findViewById<Button>(R.id.btnCheatClear)
        val btnCheatDirectAttack = findViewById<Button>(R.id.btnCheatDirectAttack)

        imgMainCharacter = findViewById<ImageView>(R.id.imgMainCharacter)
        bossHpBar = findViewById<ProgressBar>(R.id.bossHpBar)

        loadGameData()

        btnAdventure.setOnClickListener {
            soundPool.play(soundClick, 1f, 1f, 0, 0, 1f)
            adventureGauge.progress += 10

            if (adventureGauge.progress >= 100) {
                adventureGauge.progress = 0
                when ((1..3).random()) {
                    1 -> requiredMajor++
                    2 -> electiveMajor++
                    3 -> liberalArts++
                }
            }
            updateUI()
            saveGameData()
        }

        btnCraft.setOnClickListener {
            soundPool.play(soundClick, 1f, 1f, 0, 0, 1f)
            if (requiredMajor >= 1 && electiveMajor >= 1 && liberalArts >= 1) {
                val intent = Intent(this, CraftActivity::class.java)
                craftResultLauncher.launch(intent)
            } else {
                Toast.makeText(this, "지식이 부족합니다! (전필, 전선, 교양 각 1개 필요)", Toast.LENGTH_SHORT).show()
            }
        }

        btnBattle.setOnClickListener {
            soundPool.play(soundClick, 1f, 1f, 0, 0, 1f)
            if (ammobooks >= 1) {
                ammobooks--
                updateUI()
                saveGameData()

                val intent = Intent(this, BattleActivity::class.java)
                battleResultLauncher.launch(intent)
            } else {
                Toast.makeText(this, "던질 전공책이 없습니다! [시험]을 치러 책을 마련하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCheatAdd.setOnClickListener {
            soundPool.play(soundClick, 1f, 1f, 0, 0, 1f)
            requiredMajor += 5
            electiveMajor += 5
            liberalArts += 5
            ammobooks += 5
            updateUI()
            saveGameData()
        }

        btnCheatClear.setOnClickListener {
            soundPool.play(soundClick, 1f, 1f, 0, 0, 1f)
            requiredMajor = 0
            electiveMajor = 0
            liberalArts = 0
            ammobooks = 0
            bossHp = 100
            isHurtOrDead = false

            bossHpBar.progress = bossHp
            updateUI()
            saveGameData()

            imgMainCharacter.visibility = View.VISIBLE
            imgMainCharacter.translationX = 0f
            imgMainCharacter.translationY = 0f
            imgMainCharacter.rotation = 0f
            imgMainCharacter.scaleX = 1.0f
            imgMainCharacter.scaleY = 1.0f
            imgMainCharacter.setImageResource(R.drawable.tino_boss)

            startTinoRampage()
            playBGM()
        }

        btnCheatDirectAttack.setOnClickListener {
            launchBookPhysics(20)
        }

        startGaugeTimer(adventureGauge)
        startTinoRampage()
    }

    private fun playBGM() {
        if (bossHp <= 0) return
        if (bgmPlayer == null) {
            bgmPlayer = MediaPlayer.create(this, R.raw.bgm).apply {
                isLooping = true
                setVolume(0.7f, 0.7f)
            }
        }
        if (bgmPlayer?.isPlaying == false) {
            bgmPlayer?.start()
        }
    }

    private fun stopBGM() {
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
    }

    private val craftResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        requiredMajor--
        electiveMajor--
        liberalArts--

        if (result.resultCode == Activity.RESULT_OK) {
            ammobooks++
        }
        updateUI()
        saveGameData()
    }

    override fun onResume() {
        super.onResume()
        loadGameData()
        playBGM()
        if (!isHurtOrDead) {
            startTinoRampage()
        }
    }

    override fun onPause() {
        super.onPause()
        if (bgmPlayer?.isPlaying == true) {
            bgmPlayer?.pause()
        }
    }

    private fun saveGameData() {
        val editor = sharedPreferences.edit()
        editor.putInt("BOSS_HP", bossHp)
        editor.putInt("BOOK_COUNT", ammobooks)
        editor.putInt("RES_1", requiredMajor)
        editor.putInt("RES_2", electiveMajor)
        editor.putInt("RES_3", liberalArts)
        editor.putInt("ADVENTURE_PROGRESS", adventureGauge.progress)
        editor.apply()
    }

    private fun loadGameData() {
        bossHp = sharedPreferences.getInt("BOSS_HP", 100)
        ammobooks = sharedPreferences.getInt("BOOK_COUNT", 0)
        requiredMajor = sharedPreferences.getInt("RES_1", 0)
        electiveMajor = sharedPreferences.getInt("RES_2", 0)
        liberalArts = sharedPreferences.getInt("RES_3", 0)
        adventureGauge.progress = sharedPreferences.getInt("ADVENTURE_PROGRESS", 0)

        bossHpBar.progress = bossHp
        updateUI()

        if (bossHp <= 0) {
            imgMainCharacter.visibility = View.GONE
            isHurtOrDead = true
        }
    }

    private fun updateUI() {
        bossHpBar.progress = bossHp
        tvBookCount.text = "📘 : $ammobooks"
        tvRes1.text = "전필: $requiredMajor"
        tvRes2.text = "전선: $electiveMajor"
        tvRes3.text = "교양: $liberalArts"
    }

    private fun startTinoRampage() {
        if (isHurtOrDead) return
        rampageAnimator?.cancel()

        rampageAnimator = ValueAnimator.ofFloat(-40f, 40f).apply {
            duration = 500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animation ->
                if (!isHurtOrDead) {
                    val value = animation.animatedValue as Float
                    imgMainCharacter.translationY = value

                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastFlipTime > 250) {
                        if ((1..100).random() <= 15) {
                            imgMainCharacter.scaleX = if (imgMainCharacter.scaleX > 0) -1.0f else 1.0f
                            lastFlipTime = currentTime
                        }
                    }
                }
            }
        }
        rampageAnimator?.start()
    }

    private fun launchBookPhysics(damage: Int) {

        soundPool.play(soundThrow, 1f, 1f, 0, 0, 1f)

        val flyingBook = TextView(this)
        flyingBook.text = "📘"
        flyingBook.textSize = 36f
        flyingBook.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val mainLayout = findViewById<ViewGroup>(android.R.id.content)
        mainLayout.addView(flyingBook)

        val displayMetrics = resources.displayMetrics
        val startX = displayMetrics.widthPixels.toFloat() - 50f
        val startY = displayMetrics.heightPixels.toFloat() - 50f

        val endX = imgMainCharacter.x + (imgMainCharacter.width / 2f) - 40f
        val endY = imgMainCharacter.y + (imgMainCharacter.height / 2f) - 40f

        flyingBook.x = startX
        flyingBook.y = startY
        flyingBook.scaleX = 2.2f
        flyingBook.scaleY = 2.2f

        val flyAnimator = ValueAnimator.ofFloat(0f, 1f)
        flyAnimator.duration = 650
        flyAnimator.interpolator = AccelerateDecelerateInterpolator()

        flyAnimator.addUpdateListener { animation ->
            val f = animation.animatedFraction
            flyingBook.x = startX + (endX - startX) * f
            flyingBook.y = startY + (endY - startY) * f
            flyingBook.rotation = f * 720f

            val currentScale = 2.2f - (1.4f * f)
            flyingBook.scaleX = currentScale
            flyingBook.scaleY = currentScale
        }

        flyAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                damageBoss(damage)

                val bounceAnimator = ValueAnimator.ofFloat(0f, 1f)
                bounceAnimator.duration = 600
                bounceAnimator.interpolator = DecelerateInterpolator()

                val bounceDirection = if ((1..2).random() == 1) -280f else 280f
                val startBounceX = flyingBook.x
                val startBounceY = flyingBook.y

                bounceAnimator.addUpdateListener { bAnimation ->
                    val t = bAnimation.animatedFraction
                    flyingBook.x = startBounceX + (bounceDirection * t)
                    val peakHeight = -180f
                    flyingBook.y = startBounceY + (peakHeight * 4 * t * (1 - t)) + (450f * t * t)

                    flyingBook.rotation = 720f + (t * 360f)
                    flyingBook.alpha = 1f - t
                }

                bounceAnimator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(anim: Animator) {
                        mainLayout.removeView(flyingBook)
                    }
                })
                bounceAnimator.start()
            }
        })

        flyAnimator.start()
    }

    private fun damageBoss(damage: Int) {
        rampageAnimator?.cancel()
        isHurtOrDead = true

        bossHp -= damage
        if (bossHp < 0) bossHp = 0
        bossHpBar.progress = bossHp
        saveGameData()

        imgMainCharacter.setImageResource(R.drawable.tino_hit)
        imgMainCharacter.scaleX = 1.0f

        if (bossHp <= 0) {
            triggerBossKnockout()
            return
        }

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 800
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val t = animation.animatedFraction
            val amplitude = 200f * exp(-3f * t)
            val shakeX = amplitude * cos(t * 2f * Math.PI.toFloat() * 5f)

            imgMainCharacter.translationX = shakeX
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                imgMainCharacter.translationX = 0f
                imgMainCharacter.setImageResource(R.drawable.tino_boss)

                isHurtOrDead = false
                startTinoRampage()
            }
        })

        animator.start()
    }

    private fun triggerBossKnockout() {
        isHurtOrDead = true
        rampageAnimator?.cancel()

        stopBGM()

        val koAnimator = ValueAnimator.ofFloat(0f, 1f)
        koAnimator.duration = 1600
        koAnimator.interpolator = LinearInterpolator()

        val startY = imgMainCharacter.y
        val startScaleX = 1.0f
        val startScaleY = imgMainCharacter.scaleY

        koAnimator.addUpdateListener { animation ->
            val t = animation.animatedFraction
            imgMainCharacter.y = startY - (1500f * t)
            imgMainCharacter.rotation = t * 720f
            imgMainCharacter.scaleX = startScaleX * (1f - t)
            imgMainCharacter.scaleY = startScaleY * (1f - t)
        }

        koAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                imgMainCharacter.visibility = View.GONE

                soundPool.play(soundWin, 1f, 1f, 0, 0, 1f)
                showVictoryDialog()
            }
        })

        koAnimator.start()
    }

    private fun showVictoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("You Win!")
            .setMessage("티노를 물리치고 학교를 구했습니다!")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
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
        rampageAnimator?.cancel()
        stopBGM()
        soundPool.release()
    }
}
