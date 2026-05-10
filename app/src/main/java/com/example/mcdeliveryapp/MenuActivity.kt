package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {

    private companion object {
        const val MENU_CATEGORIES_COLLECTION = "menu_categories"
        const val FOODS_COLLECTION = "foods"
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var foodAdapter: FoodAdapter
    private val categoryList = mutableListOf<MenuCategory>()
    private val foodList = mutableListOf<Food>()
    private var showingFoods = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_menu)

        db = FirebaseFirestore.getInstance()
        setupMenuRecyclerView()
        fetchMenuFromFirestore()
        setupBottomNav()
        setupBackNavigation()
    }

    private fun setupMenuRecyclerView() {
        recyclerMenu = findViewById(R.id.recyclerMenu)
        recyclerMenu.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        recyclerMenu.setHasFixedSize(true)

        categoryAdapter = CategoryAdapter(categoryList) { category ->
            fetchFoodsForCategory(category)
        }
        foodAdapter = FoodAdapter(foodList) { food ->
            openFoodDetails(food)
        }

        recyclerMenu.adapter = categoryAdapter
    }

    private fun fetchMenuFromFirestore() {
        db.collection(MENU_CATEGORIES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val categories = result.documents
                    .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                    .mapNotNull { it.toMenuCategory() }
                val existingIds = categories.map { it.id }.toSet()
                val missingDefaultIds = defaultCategories()
                    .map { it["id"].toString() }
                    .filterNot { it in existingIds }

                if (missingDefaultIds.isNotEmpty()) {
                    createMissingMenuCategories(existingIds)
                    if (categories.isEmpty()) return@addOnSuccessListener
                }

                categoryList.clear()
                categoryList.addAll(categories)
                showingFoods = false
                recyclerMenu.adapter = categoryAdapter
                categoryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching menu: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createMissingMenuCategories(existingIds: Set<String>) {
        val missingCategories = defaultCategories().filter { it["id"] !in existingIds }
        if (missingCategories.isEmpty()) return

        val batch = db.batch()
        val collection = db.collection(MENU_CATEGORIES_COLLECTION)

        missingCategories.forEach { category ->
            val documentId = category["id"].toString()
            batch.set(collection.document(documentId), category)
        }

        batch.commit()
            .addOnSuccessListener { fetchMenuFromFirestore() }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error creating categories: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFoodsForCategory(category: MenuCategory, createIfEmpty: Boolean = true) {
        db.collection(FOODS_COLLECTION)
            .whereEqualTo("categoryId", category.id)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty && createIfEmpty) {
                    createDefaultFoodsForCategory(category) {
                        fetchFoodsForCategory(category, false)
                    }
                    return@addOnSuccessListener
                }

                foodList.clear()
                foodList.addAll(
                    result.documents
                        .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                        .mapNotNull { it.toFood() }
                )
                showingFoods = true
                recyclerMenu.adapter = foodAdapter
                foodAdapter.notifyDataSetChanged()

                if (foodList.isEmpty()) {
                    Toast.makeText(this, "No foods found for ${category.name}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching foods: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createDefaultFoodsForCategory(category: MenuCategory, onCreated: () -> Unit) {
        val foods = defaultCategories().filter { it["categoryId"] == category.id }
        if (foods.isEmpty()) {
            onCreated()
            return
        }

        val batch = db.batch()
        val collection = db.collection(FOODS_COLLECTION)

        foods.forEach { food ->
            val documentId = food["id"].toString()
            batch.set(collection.document(documentId), food)
        }

        batch.commit()
            .addOnSuccessListener { onCreated() }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error creating foods: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openFoodDetails(food: Food) {
        val intent = Intent(this, OrderDetailsActivity::class.java).apply {
            putExtra("FOOD_NAME", food.name)
            putExtra("FOOD_PRICE", food.price)
            putExtra("FOOD_IMAGE", food.image)
        }
        startActivity(intent)
    }

    private fun showCategories() {
        showingFoods = false
        recyclerMenu.adapter = categoryAdapter
    }

    private fun DocumentSnapshot.toMenuCategory(): MenuCategory? {
        val name = getString("name").orEmpty()
        val image = getString("image").orEmpty()

        if (name.isBlank() || image.isBlank()) return null

        return MenuCategory(
            id = getString("id") ?: id,
            name = name,
            image = image
        )
    }

    private fun DocumentSnapshot.toFood(): Food? {
        val name = getString("name").orEmpty()
        val image = getString("image").orEmpty()
        val categoryId = getString("categoryId").orEmpty()
        val price = getDouble("price") ?: getLong("price")?.toDouble() ?: 0.0

        if (name.isBlank() || image.isBlank() || categoryId.isBlank()) return null

        return Food(
            id = getString("id") ?: id,
            name = name,
            price = price,
            image = image,
            categoryId = categoryId
        )
    }

    private fun defaultCategories(): List<Map<String, Any>> {
        return listOf(
            mapOf("id" to "zodiacmeals", "name" to "Zodiac Meals", "image" to "zodiacmeals", "order" to 1),
            mapOf("id" to "groupmeals", "name" to "Group Meals", "image" to "groupmeals", "order" to 2),
            mapOf("id" to "featured", "name" to "Featured", "image" to "featured", "order" to 3),
            mapOf("id" to "chickfish", "name" to "Chicken & Fish", "image" to "chickfish", "order" to 4),
            mapOf("id" to "burgers", "name" to "Burgers", "image" to "burgers", "order" to 5),
            mapOf("id" to "spag", "name" to "McSpaghetti", "image" to "spag", "order" to 6),
            mapOf("id" to "ricebowls", "name" to "Rice Bowls", "image" to "ricebowls", "order" to 7),
            mapOf("id" to "d&d", "name" to "Desserts & Drinks", "image" to "dd", "order" to 8),
            mapOf("id" to "cafe", "name" to "McCafe", "image" to "cafe", "order" to 9),
            mapOf("id" to "f&e", "name" to "Fries & Extras", "image" to "fe", "order" to 10),
            mapOf("id" to "happymeal", "name" to "Happy Meal", "image" to "happymeal", "order" to 11),
            mapOf("id" to "sulit", "name" to "Sulit-Busog Meals", "image" to "burgermenu", "order" to 12),
            mapOf("id" to "mcdopb", "name" to "Mcdo Party Box", "image" to "mcdopb", "order" to 13)
        )
    }



    private fun foodMap(
        id: String,
        categoryId: String,
        name: String,
        image: String,
        price: Double,
        order: Int
    ): Map<String, Any> {
        return mapOf(
            "id" to id,
            "categoryId" to categoryId,
            "name" to name,
            "image" to image,
            "price" to price,
            "order" to order
        )
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener { showCategories() }

        findViewById<LinearLayout>(R.id.navOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navCoupons).setOnClickListener {
            startActivity(Intent(this, CouponsActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navMore).setOnClickListener { /* TODO */ }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (showingFoods) {
                    showCategories()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
