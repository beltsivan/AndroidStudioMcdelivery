package com.example.mcdeliveryapp

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

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var foodAdapter: FoodAdapter
    private val categoryList = mutableListOf<MenuCategory>()
    private val foodList = mutableListOf<Food>()
    private var showingFoods = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_menu)

        db = FirebaseFirestore.getInstance()
        setupMenuRecyclerView()
        fetchCategories()
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

    private fun fetchCategories() {
        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                categoryList.clear()
                categoryList.addAll(
                    result.documents
                        .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                        .mapNotNull { it.toMenuCategory() }
                )
                showingFoods = false
                recyclerMenu.adapter = categoryAdapter
                categoryAdapter.notifyDataSetChanged()

                if (categoryList.isEmpty()) {
                    Toast.makeText(this, "No categories found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFoodsForCategory(category: MenuCategory) {
        db.collection("menuItems")
            .whereEqualTo("categoryId", category.id)
            .get()
            .addOnSuccessListener { result ->
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
                    Toast.makeText(this, "No items in ${category.name}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCategories() {
        showingFoods = false
        recyclerMenu.adapter = categoryAdapter
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

    private fun DocumentSnapshot.toMenuCategory(): MenuCategory? {
        val name = getString("name") ?: return null
        return MenuCategory(
            id = getString("id") ?: id,
            name = name,
            image = getString("image") ?: "",
            order = getLong("order")?.toInt() ?: 0
        )
    }

    private fun DocumentSnapshot.toFood(): Food? {
        val name = getString("name") ?: return null
        val categoryId = getString("categoryId") ?: return null
        return Food(
            id = getString("id") ?: id,
            name = name,
            price = (getDouble("price") ?: getLong("price")?.toDouble()) ?: 0.0,
            image = getString("image") ?: "",
            categoryId = categoryId,
            order = getLong("order")?.toInt() ?: 0
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
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
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
