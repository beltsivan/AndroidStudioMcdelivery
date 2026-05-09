package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.util.CollectionUtils.listOf

class MainActivity : AppCompatActivity() {

    // Banner carousel state
    private lateinit var bannerViewPager: ViewPager2
    private lateinit var dotsIndicator: LinearLayout
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var currentBannerPage = 0

    // All banner drawables that have "banner" in the filename
    private val bannerImages = listOf(
        R.drawable.cearealbanner,
        R.drawable.chickbanner,
        R.drawable.galaxybanner,
        R.drawable.goldenbanner,
        R.drawable.mariobanner,
        R.drawable.mcfloatbanner,
        R.drawable.sulitbanner,
        R.drawable.twisterbanner
    )

    // Auto-advance every 3 seconds; wraps back to 0
    private val bannerRunnable = object : Runnable {
        override fun run() {
            currentBannerPage = (currentBannerPage + 1) % bannerImages.size
            bannerViewPager.setCurrentItem(currentBannerPage, true)
            bannerHandler.postDelayed(this, 3000)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Banner Carousel Setup ---
        bannerViewPager = findViewById(R.id.bannerViewPager)
        dotsIndicator   = findViewById(R.id.dotsIndicator)

        bannerViewPager.adapter = BannerAdapter(bannerImages)

        // Build the dot indicators
        setupDots(bannerImages.size)

        // Track manual swipes and reset the auto-slide timer
        bannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentBannerPage = position
                updateDots(position)
                // Reset timer so it doesn't skip shortly after a manual swipe
                bannerHandler.removeCallbacks(bannerRunnable)
                bannerHandler.postDelayed(bannerRunnable, 3000)
            }
        })

        // Start auto-slide
        bannerHandler.postDelayed(bannerRunnable, 3000)

        // --- Bottom Navigation ---
        // Home — already here, do nothing (active tab)
        findViewById<android.widget.LinearLayout>(R.id.navHome).setOnClickListener { /* active */ }

        // Menu
        findViewById<android.widget.LinearLayout>(R.id.navMenu).setOnClickListener {
            startActivity(android.content.Intent(this, MenuActivity::class.java))
            finish()
        }

        // Orders
        findViewById<android.widget.LinearLayout>(R.id.navOrders).setOnClickListener {
            startActivity(android.content.Intent(this, OrdersActivity::class.java))
            finish()
        }

        // Coupons
        findViewById<android.widget.LinearLayout>(R.id.navCoupons).setOnClickListener {
            startActivity(android.content.Intent(this, CouponsActivity::class.java))
            finish()
        }

        // More — placeholder (no activity yet)
        findViewById<android.widget.LinearLayout>(R.id.navMore).setOnClickListener { /* TODO */ }
    }

    // Create one dot ImageView per banner slide
    private fun setupDots(count: Int) {
        dotsIndicator.removeAllViews()
        for (i in 0 until count) {
            val dot = ImageView(this)
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(6, 0, 6, 0)
            dot.layoutParams = params
            dotsIndicator.addView(dot)
        }
    }

    // Swap drawable on each dot to reflect the active page
    private fun updateDots(activePosition: Int) {
        for (i in 0 until dotsIndicator.childCount) {
            val dot = dotsIndicator.getChildAt(i) as ImageView
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == activePosition) R.drawable.dot_active else R.drawable.dot_inactive
                )
            )
        }
    }

    // Pause auto-slide when the app goes to background
    override fun onPause() {
        super.onPause()
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    // Resume auto-slide when the app comes back to foreground
    override fun onResume() {
        super.onResume()
        bannerHandler.postDelayed(bannerRunnable, 3000)
    }
}