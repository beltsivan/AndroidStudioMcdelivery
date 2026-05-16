package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var bannerViewPager: ViewPager2
    private lateinit var dotsIndicator: LinearLayout
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var currentBannerPage = 0

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

    private val bannerRunnable = object : Runnable {
        override fun run() {
            currentBannerPage = (currentBannerPage + 1) % bannerImages.size
            bannerViewPager.setCurrentItem(currentBannerPage, true)
            bannerHandler.postDelayed(this, 3000)
        }
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var categoryRecycler: RecyclerView
    private lateinit var categoryFoodsContainer: LinearLayout
    private val categoryList = mutableListOf<Category>()

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

        bannerViewPager = findViewById(R.id.bannerViewPager)
        dotsIndicator = findViewById(R.id.dotsIndicator)
        bannerViewPager.adapter = BannerAdapter(bannerImages)
        setupDots(bannerImages.size)
        bannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentBannerPage = position
                updateDots(position)
                bannerHandler.removeCallbacks(bannerRunnable)
                bannerHandler.postDelayed(bannerRunnable, 3000)
            }
        })
        bannerHandler.postDelayed(bannerRunnable, 3000)

        findViewById<FrameLayout>(R.id.cartIcon).setOnClickListener {
            startActivity(Intent(this, BagActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { /* active */ }
        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navCoupons).setOnClickListener {
            startActivity(Intent(this, CouponsActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener { /* TODO */ }

        db = FirebaseFirestore.getInstance()
        categoryRecycler = findViewById(R.id.categoryRecycler)
        categoryRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryFoodsContainer = findViewById(R.id.categoryFoodsContainer)

        fetchData()
    }

    private fun fetchData() {
        db.collection("categories")
            .get()
            .addOnSuccessListener { catResult ->
                val categories = catResult.documents
                    .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                    .mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        Category(
                            id = doc.getString("id") ?: doc.id,
                            name = name,
                            image = doc.getString("image") ?: ""
                        )
                    }

                categoryList.clear()
                categoryList.addAll(categories)
                categoryRecycler.adapter = AdapterCat(categoryList) { category ->
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                db.collection("menuItems")
                    .get()
                    .addOnSuccessListener { foodResult ->
                        val allFoods = foodResult.documents
                            .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                            .mapNotNull { doc ->
                                val name = doc.getString("name") ?: return@mapNotNull null
                                val categoryId = doc.getString("categoryId") ?: return@mapNotNull null
                                Food(
                                    id = doc.getString("id") ?: doc.id,
                                    name = name,
                                    price = (doc.getDouble("price") ?: doc.getLong("price")?.toDouble()) ?: 0.0,
                                    image = doc.getString("image") ?: "",
                                    categoryId = categoryId,
                                    order = doc.getLong("order")?.toInt() ?: 0
                                )
                            }

                        buildCategorySections(categories, allFoods)
                    }
            }
    }

    private fun buildCategorySections(categories: List<Category>, allFoods: List<Food>) {
        categoryFoodsContainer.removeAllViews()

        for (cat in categories) {
            val foods = allFoods.filter { it.categoryId == cat.id }
            if (foods.isEmpty()) continue

            val sectionView = layoutInflater.inflate(R.layout.item_home_food_section, categoryFoodsContainer, false)
            val categoryName = sectionView.findViewById<TextView>(R.id.sectionCategoryName)
            val foodRowContainer = sectionView.findViewById<LinearLayout>(R.id.foodRowContainer)

            categoryName.text = cat.name

            for (food in foods) {
                val cardView = layoutInflater.inflate(R.layout.item_home_food_card, foodRowContainer, false)
                val foodImage = cardView.findViewById<ImageView>(R.id.foodImage)
                val foodName = cardView.findViewById<TextView>(R.id.foodName)
                val foodPrice = cardView.findViewById<TextView>(R.id.foodPrice)

                foodName.text = food.name
                foodPrice.text = String.format("\u20B1%.2f", food.price)

                loadImage(foodImage, food.image)

                cardView.setOnClickListener { openFoodDetails(food) }
                foodRowContainer.addView(cardView)
            }

            categoryFoodsContainer.addView(sectionView)
        }
    }

    private fun openFoodDetails(food: Food) {
        val intent = Intent(this, OrderDetailsActivity::class.java).apply {
            putExtra("FOOD_ID", food.id)
            putExtra("FOOD_NAME", food.name)
            putExtra("FOOD_PRICE", food.price)
            putExtra("FOOD_IMAGE", food.image)
            putExtra("FOOD_CATEGORY_ID", food.categoryId)
            putExtra("FOOD_ORDER", food.order)
        }
        startActivity(intent)
    }

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

    override fun onPause() {
        super.onPause()
        bannerHandler.removeCallbacks(bannerRunnable)
    }

    override fun onResume() {
        super.onResume()
        bannerHandler.postDelayed(bannerRunnable, 3000)
    }
}
