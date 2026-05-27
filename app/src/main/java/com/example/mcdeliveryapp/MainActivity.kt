package com.example.mcdeliveryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
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
    private var branchId: String? = null
    private var loadedCategories = listOf<Category>()
    private var loadedFoods = listOf<Food>()
    private var dataLoaded = false
    private var itemsLoaded = false
    private var branchLoaded = false

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
            startActivity(Intent(this, MenuActivity::class.java).apply {
                putExtra("BRANCH_ID", branchId ?: "")
            })
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
        findViewById<LinearLayout>(R.id.navMore).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        db = FirebaseFirestore.getInstance()
        categoryRecycler = findViewById(R.id.categoryRecycler)
        categoryRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoryFoodsContainer = findViewById(R.id.categoryFoodsContainer)

        fetchCategories()
        checkBranch()
    }

    private fun checkBranch() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val txtCompanyName = findViewById<TextView>(R.id.txtCompanyName)

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val bid = doc.getString("branchId")
                val branchAddress = doc.getString("branchAddress")
                val branchName = doc.getString("branchName")
                if (!bid.isNullOrEmpty()) {
                    branchId = bid
                    branchLoaded = true
                    loadBranchItems()
                }
                if (!branchAddress.isNullOrEmpty()) {
                    txtCompanyName.text = branchAddress
                    txtCompanyName.setOnClickListener { promptBranchSelection() }
                } else if (!branchName.isNullOrEmpty()) {
                    txtCompanyName.text = branchName
                    txtCompanyName.setOnClickListener { promptBranchSelection() }
                } else {
                    promptBranchSelection()
                }
            }
            .addOnFailureListener { promptBranchSelection() }
    }

    private fun promptBranchSelection() {
        db.collection("branches").get()
            .addOnSuccessListener { result ->
                val branchNames = mutableListOf<String>()
                val branchData = mutableListOf<Map<String, Any>>()

                if (result.documents.isEmpty()) {
                    Toast.makeText(this, "No branches found in Firestore.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                for (doc in result.documents) {
                    val data = doc.data
                    val name = doc.getString("name")
                        ?: doc.getString("branchName")
                        ?: doc.getString("branch_name")
                        ?: data?.entries?.firstOrNull { it.value is String }?.value as? String
                        ?: doc.id

                    val addrMap = (doc.get("address") as? Map<String, Any>)
                        ?: (doc.get("location") as? Map<String, Any>)
                        ?: emptyMap()
                    val street = addrMap["street"] as? String ?: doc.getString("street") ?: ""
                    val barangay = addrMap["barangay"] as? String ?: doc.getString("barangay") ?: ""
                    val municipality = addrMap["municipality"] as? String ?: doc.getString("municipality") ?: ""
                    val province = addrMap["province"] as? String ?: doc.getString("province") ?: ""
                    val zipCode = addrMap["zipCode"] as? String ?: doc.getString("zipCode") ?: ""
                    val componentAddress = listOfNotNull(
                        street.ifEmpty { null },
                        barangay.ifEmpty { null },
                        municipality.ifEmpty { null },
                        province.ifEmpty { null },
                        zipCode.ifEmpty { null }
                    ).joinToString(", ")
                    val singleFieldAddress = doc.getString("address")
                        ?: doc.getString("branchAddress")
                        ?: doc.getString("branch_address")
                        ?: doc.getString("location")
                        ?: doc.getString("storeAddress")
                        ?: doc.getString("store_address")
                        ?: doc.getString("fullAddress")
                        ?: doc.getString("branchLocation")
                    val address = componentAddress.ifEmpty { singleFieldAddress ?: "" }
                    branchNames.add(name)
                    branchData.add(
                        mapOf(
                            "id" to (doc.getString("id") ?: doc.getString("branchId") ?: doc.id),
                            "name" to name,
                            "address" to address
                        )
                    )
                }

                AlertDialog.Builder(this)
                    .setTitle("Select Branch")
                    .setItems(branchNames.toTypedArray()) { _, which ->
                        val selected = branchData[which]
                        saveBranch(selected)
                    }
                    .setCancelable(false)
                    .show()
            }
    }

    private fun saveBranch(selected: Map<String, Any>) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val bid = selected["id"] as? String ?: ""
        val branchAddress = selected["address"] as? String ?: ""
        val branchName = selected["name"] as? String ?: ""

        val displayText = branchAddress.ifEmpty { branchName }

        branchId = bid
        branchLoaded = true
        loadBranchItems()

        val updates = hashMapOf<String, Any>(
            "branchId" to bid,
            "branchName" to branchName,
            "branchAddress" to branchAddress
        )

        db.collection("users")
            .document(user.uid)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                val txtCompanyName = findViewById<TextView>(R.id.txtCompanyName)
                txtCompanyName.text = displayText
                txtCompanyName.setOnClickListener { promptBranchSelection() }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save branch: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchCategories() {
        db.collection("categories")
            .get()
            .addOnSuccessListener { catResult ->
                val categories = catResult.documents
                    .filter { it.getBoolean("isArchived") != true }
                    .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                    .mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        Category(
                            id = doc.getString("id") ?: doc.id,
                            name = name,
                            image = doc.getString("image") ?: ""
                        )
                    }

                loadedCategories = categories
                categoryList.clear()
                categoryList.addAll(categories)
                categoryRecycler.adapter = AdapterCat(categoryList) { category ->
                    startActivity(Intent(this, MenuActivity::class.java).apply {
                        putExtra("BRANCH_ID", branchId ?: "")
                        putExtra("CATEGORY_ID", category.id)
                        putExtra("CATEGORY_NAME", category.name)
                    })
                    finish()
                }

                dataLoaded = true
                renderHomeScreen()
            }
    }

    private fun getAvail(doc: com.google.firebase.firestore.DocumentSnapshot): Boolean {
        val v = doc.get("isAvailable") ?: doc.get("is Available") ?: doc.get("available")
        return parseBoolean(v)
    }

    private fun loadBranchItems() {
        val bid = branchId ?: return

        db.collection("branchMenuItems")
            .whereEqualTo("branchId", bid)
            .get()
            .addOnSuccessListener { branchResult ->
                val branchAvail = mutableMapOf<String, Boolean>()
                for (branchDoc in branchResult.documents) {
                    val itemId = branchDoc.getString("menuItemId") ?: branchDoc.getString("menultemid") ?: continue
                    branchAvail[itemId] = getAvail(branchDoc)
                }

                db.collection("menuItems")
                    .get()
                    .addOnSuccessListener { menuResult ->
                        loadedFoods = menuResult.documents
                            .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                            .mapNotNull { doc ->
                                val name = doc.getString("name") ?: return@mapNotNull null
                                val categoryId = doc.getString("categoryId") ?: return@mapNotNull null
                                val menuDocId = doc.getString("id") ?: doc.getString("itemId") ?: doc.id
                                val isAvail = branchAvail[menuDocId] ?: true
                                Food(
                                    id = menuDocId,
                                    name = name,
                                    price = (doc.getDouble("price") ?: doc.getLong("price")?.toDouble()) ?: 0.0,
                                    image = doc.getString("image") ?: "",
                                    categoryId = categoryId,
                                    order = doc.getLong("order")?.toInt() ?: 0,
                                    isAvailable = isAvail
                                )
                            }
                        itemsLoaded = true
                        renderHomeScreen()
                    }
                    .addOnFailureListener { itemsLoaded = true; renderHomeScreen() }
            }
            .addOnFailureListener {
                itemsLoaded = true
                loadedFoods = emptyList()
                renderHomeScreen()
            }
    }

    private fun parseBoolean(value: Any?): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> value.equals("true", ignoreCase = true)
            is Number -> value.toInt() != 0
            else -> true
        }
    }

    private fun renderHomeScreen() {
        if (!dataLoaded || !branchLoaded || !itemsLoaded) return

        val bid = branchId ?: ""
        buildCategorySections(loadedCategories, loadedFoods, bid)
    }

    private fun buildCategorySections(categories: List<Category>, allFoods: List<Food>, bid: String) {
        categoryFoodsContainer.removeAllViews()

        val allowed = categories.filter { it.name == "Featured" || it.name == "Sulit-Busog Meals" }

        for (cat in allowed) {
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
                val overlay = cardView.findViewById<View>(R.id.overlayUnavailable)
                val txtUnavailable = cardView.findViewById<TextView>(R.id.txtUnavailableLabel)

                foodName.text = food.name
                foodPrice.text = String.format("\u20B1%.2f", food.price)

                loadImage(foodImage, food.image)

                if (food.isAvailable) {
                    overlay.visibility = View.GONE
                    txtUnavailable.visibility = View.GONE
                    foodName.setTextColor(0xFF000000.toInt())
                    foodName.text = food.name
                    foodPrice.text = String.format("\u20B1%.2f", food.price)
                    cardView.setOnClickListener { openFoodDetails(food) }
                } else {
                    overlay.visibility = View.VISIBLE
                    txtUnavailable.visibility = View.VISIBLE
                    foodName.setTextColor(0xFF000000.toInt())
                    foodName.text = food.name
                    foodPrice.text = String.format("\u20B1%.2f", food.price)
                    cardView.setOnClickListener(null)
                }
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
            putExtra("FOOD_AVAILABLE", food.isAvailable)
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
