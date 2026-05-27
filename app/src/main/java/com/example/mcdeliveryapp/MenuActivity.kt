package com.example.mcdeliveryapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
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
    private var branchId = ""
    private var branchLoaded = false

    private val branchAvail = mutableMapOf<String, Boolean>()
    private var branchItemsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_menu)

        db = FirebaseFirestore.getInstance()
        branchId = intent.getStringExtra("BRANCH_ID") ?: ""

        setupMenuRecyclerView()

        if (branchId.isNotEmpty()) {
            branchLoaded = true
            fetchCategories()
            loadBranchAvailability()
        } else {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                db.collection("users").document(user.uid).get()
                    .addOnSuccessListener { doc ->
                        branchId = doc.getString("branchId") ?: ""
                        branchLoaded = true
                        fetchCategories()
                        loadBranchAvailability()
                    }
                    .addOnFailureListener { fetchCategories(); loadBranchAvailability() }
            } else {
                fetchCategories()
                loadBranchAvailability()
            }
        }

        setupBottomNav()
        setupBackNavigation()
    }

    private fun loadBranchAvailability() {
        if (branchId.isEmpty()) return
        db.collection("branchMenuItems")
            .whereEqualTo("branchId", branchId)
            .get()
            .addOnSuccessListener { result ->
                branchAvail.clear()
                for (doc in result.documents) {
                    val itemId = doc.getString("menuItemId") ?: doc.getString("menultemid") ?: continue
                    val v = doc.get("isAvailable") ?: doc.get("is Available") ?: doc.get("available")
                    branchAvail[itemId] = parseBoolean(v)
                }
                branchItemsLoaded = true
                Toast.makeText(this, "branchAvail: ${branchAvail.size} items", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { branchItemsLoaded = true }
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
        if (branchId.isEmpty() || !branchItemsLoaded) {
            Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("menuItems")
            .whereEqualTo("categoryId", category.id)
            .get()
            .addOnSuccessListener { result ->
                foodList.clear()
                foodList.addAll(
                    result.documents
                        .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                        .mapNotNull { doc ->
                            val name = doc.getString("name") ?: return@mapNotNull null
                            val menuDocId = doc.getString("id") ?: doc.getString("itemId") ?: doc.id
                            val isAvail = branchAvail[menuDocId] ?: true
                            Food(
                                id = menuDocId,
                                name = name,
                                price = (doc.getDouble("price") ?: doc.getLong("price")?.toDouble()) ?: 0.0,
                                image = doc.getString("image") ?: "",
                                categoryId = category.id,
                                order = doc.getLong("order")?.toInt() ?: 0,
                                isAvailable = isAvail
                            )
                        }
                )
                showingFoods = true
                foodAdapter = FoodAdapter(foodList) { food ->
                    openFoodDetails(food)
                }
                recyclerMenu.adapter = foodAdapter
                foodAdapter.notifyDataSetChanged()
                if (foodList.isEmpty()) {
                    Toast.makeText(this, "No items in ${category.name}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { }
    }

    private fun parseBoolean(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> value.equals("true", ignoreCase = true)
            is Number -> value.toInt() != 0
            else -> true
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
            putExtra("FOOD_AVAILABLE", food.isAvailable)
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
