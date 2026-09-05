package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

class NairaGuardRepository(private val dao: NairaGuardDao) {

    val allCommodities: Flow<List<Commodity>> = dao.getAllCommodities()
    val allPrices: Flow<List<MarketPrice>> = dao.getAllMarketPrices()
    val allHistory: Flow<List<PriceHistory>> = dao.getAllPriceHistory()
    val allAlerts: Flow<List<PriceAlert>> = dao.getAllAlerts()
    val subscriptionState: Flow<SubscriptionState?> = dao.getSubscriptionState()
    val allFeedback: Flow<List<UserFeedback>> = dao.getAllFeedback()
    val allWaitlist: Flow<List<WaitlistEntry>> = dao.getAllWaitlist()
    val allPlusSubscriptions: Flow<List<PlusSubscriptionRecord>> = dao.getAllPlusSubscriptionsHistory()

    suspend fun insertWaitlistEntry(entry: WaitlistEntry) = dao.insertWaitlistEntry(entry)
    suspend fun deleteWaitlistEntry(id: Int) = dao.deleteWaitlistEntry(id)

    suspend fun insertPlusSubscription(record: PlusSubscriptionRecord) = dao.insertPlusSubscriptionRecord(record)

    suspend fun insertFeedback(feedback: UserFeedback) = dao.insertFeedback(feedback)
    suspend fun updateFeedbackStatus(id: Int, status: String) = dao.updateFeedbackStatus(id, status)
    suspend fun deleteFeedback(id: Int) = dao.deleteFeedback(id)


    fun getPricesForCommodity(commodityId: Int): Flow<List<MarketPrice>> =
        dao.getPricesForCommodity(commodityId)

    fun getHistoryForCommodity(commodityId: Int): Flow<List<PriceHistory>> =
        dao.getHistoryForCommodity(commodityId)

    suspend fun getPricesForCommodityList(commodityId: Int): List<MarketPrice> =
        dao.getPricesForCommodityList(commodityId)

    suspend fun getPriceForCommodityAndMarket(commodityId: Int, market: String): MarketPrice? =
        dao.getPriceForCommodityAndMarket(commodityId, market)

    suspend fun updateMarketPrice(marketPrice: MarketPrice) {
        dao.deletePriceForCommodityAndMarket(marketPrice.commodityId, marketPrice.marketLocation)
        dao.insertMarketPrice(marketPrice)
        // Add to history
        dao.insertHistory(
            PriceHistory(
                commodityId = marketPrice.commodityId,
                marketLocation = marketPrice.marketLocation,
                wholesalePrice = marketPrice.wholesalePrice,
                retailPrice = marketPrice.retailPrice,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateConversionFactor(commodityId: Int, factor: Double) {
        val commodities = dao.getAllCommoditiesList()
        val c = commodities.find { it.id == commodityId }
        if (c != null) {
            val updated = c.copy(conversionFactor = factor)
            dao.updateCommodity(updated)
            
            // Recalculate retail prices for all matching market prices
            val prices = dao.getPricesForCommodityList(commodityId)
            prices.forEach { price ->
                val newRetail = price.wholesalePrice / factor
                dao.insertMarketPrice(price.copy(retailPrice = newRetail, lastUpdated = System.currentTimeMillis(), updatedBy = "System (Conversion Update)"))
            }
        }
    }

    suspend fun insertAlert(alert: PriceAlert) = dao.insertAlert(alert)
    suspend fun deleteAlert(id: Int) = dao.deleteAlert(id)
    suspend fun markAlertTriggered(id: Int) = dao.markAlertTriggered(id)

    suspend fun getAdminByUsername(username: String): AdminAccount? = dao.getAdminByUsername(username)
    suspend fun getAdminByEmail(email: String): AdminAccount? = dao.getAdminByEmail(email)
    suspend fun insertAdminAccount(account: AdminAccount) = dao.insertAdminAccount(account)

    suspend fun updateSubscription(state: SubscriptionState) =
        dao.insertOrUpdateSubscription(state)

    suspend fun getSubscriptionStateOnce(): SubscriptionState? = dao.getSubscriptionStateOnce()

    suspend fun getCommodityByNameAndLocation(commodityNameQuery: String, locationQuery: String): Pair<Commodity, String>? {
        val commodities = dao.getAllCommoditiesList()
        
        // Find best match for commodity name
        val matchedComm = commodities.find { 
            it.name.contains(commodityNameQuery, ignoreCase = true) || 
            commodityNameQuery.contains(it.name, ignoreCase = true) 
        } ?: commodities.find {
            it.description.contains(commodityNameQuery, ignoreCase = true)
        }

        if (matchedComm == null) return null

        // Find best match for market location (Only Mile 12 or Isale Eko)
        val resolvedMarket = when {
            locationQuery.contains("island", ignoreCase = true) || 
            locationQuery.contains("eko", ignoreCase = true) || 
            locationQuery.contains("isale", ignoreCase = true) ||
            locationQuery.contains("lekki", ignoreCase = true) -> "Isale Eko (Island)"
            else -> "Mile 12 (Mainland)" // Default mainland hub
        }

        return Pair(matchedComm, resolvedMarket)
    }

    suspend fun seedDatabaseIfEmpty() {
        // Clean legacy markets (Ikorodu, Lekki) from database
        dao.cleanLegacyMarkets()

        // Seeding and category synchronization checks
        val commoditiesList = dao.getAllCommoditiesList()
        if (commoditiesList.isNotEmpty()) {
            Log.d("NairaGuardRepo", "Database already seeded with ${commoditiesList.size} commodities. Syncing categories...")
            val agroItemNames = listOf("Beef", "Yam", "Local Eggs", "Hard Chicken (Frozen)", "Hard Chicken")
            commoditiesList.forEach { comm ->
                if (agroItemNames.any { comm.name.contains(it, ignoreCase = true) } && comm.category != "Agro Products") {
                    dao.updateCommodity(comm.copy(category = "Agro Products"))
                }
            }

            val hasChickenGlory = commoditiesList.any { it.name.equals("Chicken Glory", ignoreCase = true) }
            if (!hasChickenGlory) {
                val newCg = Commodity(
                    name = "Chicken Glory",
                    category = "Seasoning Cubes",
                    wholesaleUnit = "Carton of 40 Packs",
                    microUnit = "1 Pack",
                    conversionFactor = 40.0,
                    description = "Chicken Glory chicken seasoning cubes"
                )
                dao.insertCommodities(listOf(newCg))
                val refreshedList = dao.getAllCommoditiesList()
                val cgSaved = refreshedList.find { it.name.equals("Chicken Glory", ignoreCase = true) }
                if (cgSaved != null) {
                    val markets = listOf(
                        "Mile 12 (Mainland)" to 0.85,
                        "Isale Eko (Island)" to 1.10
                    )
                    val baseWholesale = 6500.0
                    val cgPrices = markets.map { (marketName, multiple) ->
                        val finalWholesale = baseWholesale * multiple
                        val finalRetail = finalWholesale / cgSaved.conversionFactor
                        MarketPrice(
                            commodityId = cgSaved.id,
                            marketLocation = marketName,
                            wholesalePrice = finalWholesale,
                            retailPrice = finalRetail,
                            lastUpdated = System.currentTimeMillis() - 1000 * 60,
                            updatedBy = "Initial Import"
                        )
                    }
                    dao.insertMarketPrices(cgPrices)
                }
            }
            return
        }

        Log.d("NairaGuardRepo", "Seeding database with Nigerian commodities, geographical prices, and history...")

        val baseCommodities = listOf(
            Commodity(name = "Local Rice", category = "Grains", wholesaleUnit = "50kg Bag", microUnit = "Cup", conversionFactor = 128.0, description = "Locally grown parboiled long grain rice"),
            Commodity(name = "Imported Foreign Rice", category = "Grains", wholesaleUnit = "50kg Bag", microUnit = "Cup", conversionFactor = 128.0, description = "Imported double polished parboiled long grain rice"),
            Commodity(name = "Abuja Golden Beans (Oloyin)", category = "Beans", wholesaleUnit = "50kg Bag", microUnit = "Derica", conversionFactor = 60.0, description = "Sweet brown honey beans from northern Nigeria"),
            Commodity(name = "White Beans (Iron Beans)", category = "Beans", wholesaleUnit = "50kg Bag", microUnit = "Derica", conversionFactor = 60.0, description = "White iron beans from northern hubs"),
            Commodity(name = "Garri (White)", category = "Processed Tubers", wholesaleUnit = "50kg Bag", microUnit = "Paint Bucket", conversionFactor = 12.0, description = "Finely processed cassava flakes"),
            Commodity(name = "Garri (Yellow)", category = "Processed Tubers", wholesaleUnit = "50kg Bag", microUnit = "Paint Bucket", conversionFactor = 12.0, description = "Cassava flakes fried with red palm oil"),
            Commodity(name = "Agege Bread", category = "Bakery", wholesaleUnit = "Crate of 30 Loaves", microUnit = "1 Loaf", conversionFactor = 30.0, description = "Freshly baked local unsliced Lagos sweet bread"),
            Commodity(name = "Semovita", category = "Bakery", wholesaleUnit = "Carton of 10 Packs", microUnit = "1 Pack (1kg)", conversionFactor = 10.0, description = "Golden Penny premium Semovita"),
            Commodity(name = "Wheat Flour", category = "Bakery", wholesaleUnit = "50kg Bag", microUnit = "Paint Bucket", conversionFactor = 12.0, description = "Wheat flour for baking and swallows"),
            Commodity(name = "Hard Chicken (Frozen)", category = "Agro Products", wholesaleUnit = "Carton of 10kg", microUnit = "1kg Piece", conversionFactor = 10.0, description = "Imported frozen hard chicken blocks"),
            Commodity(name = "Beef", category = "Agro Products", wholesaleUnit = "Quarter Side (50kg)", microUnit = "1kg Piece", conversionFactor = 50.0, description = "Freshly butchered grass-fed local beef"),
            Commodity(name = "Local Eggs", category = "Agro Products", wholesaleUnit = "Crate of 30 Eggs", microUnit = "1 Egg", conversionFactor = 30.0, description = "Fresh farm large brown chicken eggs"),
            Commodity(name = "Vegetable Oil", category = "Oils", wholesaleUnit = "25L Jerrican", microUnit = "1L Bottle", conversionFactor = 25.0, description = "Refined vegetable peanut cooking oil"),
            Commodity(name = "Palm Oil", category = "Oils", wholesaleUnit = "25L Jerrican", microUnit = "1L Bottle", conversionFactor = 25.0, description = "Locally processed organic red palm oil"),
            Commodity(name = "Yam", category = "Agro Products", wholesaleUnit = "100 Tubers (Heap)", microUnit = "1 Tuber", conversionFactor = 100.0, description = "Premium medium sized Abuja yams"),
            Commodity(name = "Tomatoes", category = "Vegetables", wholesaleUnit = "40kg Raffia Basket", microUnit = "Derica", conversionFactor = 40.0, description = "Red plum tomatoes from northern irrigation farms"),
            Commodity(name = "Rodo", category = "Vegetables", wholesaleUnit = "30kg Raffia Basket", microUnit = "Paint Bucket", conversionFactor = 10.0, description = "Very hot scotch bonnet habanero pepper"),
            Commodity(name = "Onions", category = "Vegetables", wholesaleUnit = "100kg Bag", microUnit = "Plate", conversionFactor = 25.0, description = "Sharp red onions imported from Aliero, Kebbi"),
            Commodity(name = "Dano Milk", category = "Dairy", wholesaleUnit = "Carton of 72 Sachets", microUnit = "1 Sachet", conversionFactor = 72.0, description = "Highly nutritious full cream instant powdered milk"),
            // Instant Noodles Section
            Commodity(name = "Indomie Indomitables", category = "Instant Noodles", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Indomie noodles Indomitables"),
            Commodity(name = "Indomie Super Pack", category = "Instant Noodles", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Indomie noodles Super Pack"),
            Commodity(name = "Indomie Onion Flavour", category = "Instant Noodles", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Indomie noodles Onion Flavour"),
            Commodity(name = "Indomie Hungry Man Size", category = "Instant Noodles", wholesaleUnit = "Carton of 24 Packs", microUnit = "1 Pack", conversionFactor = 24.0, description = "Indomie noodles Hungry Man Size"),
            Commodity(name = "Indomie Belle Full", category = "Instant Noodles", wholesaleUnit = "Carton of 16 Packs", microUnit = "1 Pack", conversionFactor = 16.0, description = "Indomie noodles Belle Full"),
            Commodity(name = "Golden Penny Noodles", category = "Instant Noodles", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Golden Penny premium noodles"),

            // Salts Section
            Commodity(name = "Dangote Salt", category = "Salt", wholesaleUnit = "Carton of 30 Packs", microUnit = "1 Sachet", conversionFactor = 30.0, description = "Dangote white iodized table salt"),
            Commodity(name = "Mr. Chef Salt", category = "Salt", wholesaleUnit = "Carton of 30 Packs", microUnit = "1 Sachet", conversionFactor = 30.0, description = "Mr. Chef iodized table salt"),
            Commodity(name = "Royal Salt", category = "Salt", wholesaleUnit = "Carton of 30 Packs", microUnit = "1 Sachet", conversionFactor = 30.0, description = "Royal refined white cooking salt"),

            // Sugars Section
            Commodity(name = "Dangote Sugar", category = "Sugar", wholesaleUnit = "Carton of 50 Packs", microUnit = "1 Pack", conversionFactor = 50.0, description = "Dangote refined granulated white sugar"),
            Commodity(name = "St. Louis Sugar", category = "Sugar", wholesaleUnit = "Carton of 50 Packs", microUnit = "1 Pack", conversionFactor = 50.0, description = "Premium St. Louis white sugar cubes"),
            Commodity(name = "Golden Penny Sugar", category = "Sugar", wholesaleUnit = "Carton of 50 Packs", microUnit = "1 Pack", conversionFactor = 50.0, description = "Golden Penny refined sugar"),

            // Seasoning Cubes Section
            Commodity(name = "Maggi Star", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Maggi Star umami seasoning cubes"),
            Commodity(name = "Maggi Chicken", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Maggi Chicken flavour seasoning cubes"),
            Commodity(name = "Maggi Crayfish", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Maggi Crayfish flavour seasoning cubes"),
            Commodity(name = "Knorr Beef", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Knorr Beef broth seasoning cubes"),
            Commodity(name = "Knorr Chicken", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Knorr Chicken broth seasoning cubes"),
            Commodity(name = "Royco", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Royco rich seasoning cubes"),
            Commodity(name = "Terra", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Terra rich taste seasoning stock cubes"),
            Commodity(name = "Kitchen Glory", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Kitchen Glory cooking seasoning cubes"),
            Commodity(name = "Chicken Glory", category = "Seasoning Cubes", wholesaleUnit = "Carton of 40 Packs", microUnit = "1 Pack", conversionFactor = 40.0, description = "Chicken Glory chicken seasoning cubes"),

            // Other items matching the standard baseline moved out of decommissioned category
            Commodity(name = "Spaghetti (Golden Penny)", category = "Grains", wholesaleUnit = "Carton of 20 Packs", microUnit = "1 Pack", conversionFactor = 20.0, description = "Premium gold standard spaghetti strand noodles"),
            Commodity(name = "Sachet Tomato Paste (Gino and Tasty Tom)", category = "Vegetables", wholesaleUnit = "Carton of 50 Sachets", microUnit = "1 Sachet", conversionFactor = 50.0, description = "Red concentrated tomato paste sachets")
        )

        dao.insertCommodities(baseCommodities)

        // Refetch to get IDs
        val savedComm = dao.getAllCommoditiesList()
        val markets = listOf(
            "Mile 12 (Mainland)" to 0.85,  // Base price multiple
            "Isale Eko (Island)" to 1.10
        )

        // Custom wholesale base prices in NGN (Mainland references)
        val wholesaleBases = mapOf(
            "Local Rice" to 75000.0,
            "Imported Foreign Rice" to 85000.0,
            "Abuja Golden Beans (Oloyin)" to 80000.0,
            "White Beans (Iron Beans)" to 70000.0,
            "Garri (White)" to 42000.0,
            "Garri (Yellow)" to 47000.0,
            "Agege Bread" to 15000.0,
            "Semovita" to 12000.0,
            "Wheat Flour" to 65000.0,
            "Hard Chicken (Frozen)" to 32000.0,
            "Beef" to 180000.0,
            "Local Eggs" to 5400.0,
            "Vegetable Oil" to 58000.0,
            "Palm Oil" to 38000.0,
            "Yam" to 220000.0,
            "Tomatoes" to 35000.0,
            "Rodo" to 30000.0,
            "Onions" to 75000.0,
            "Dano Milk" to 18000.0,
            "Spaghetti (Golden Penny)" to 14000.0,
            "Sachet Tomato Paste (Gino and Tasty Tom)" to 12500.0,
            
            // Instant Noodles
            "Indomie Indomitables" to 6500.0,
            "Indomie Super Pack" to 8500.0,
            "Indomie Onion Flavour" to 7200.0,
            "Indomie Hungry Man Size" to 9500.0,
            "Indomie Belle Full" to 11000.0,
            "Golden Penny Noodles" to 6800.0,
            
            // Salt
            "Dangote Salt" to 12000.0,
            "Mr. Chef Salt" to 11000.0,
            "Royal Salt" to 10500.0,
            
            // Sugar
            "Dangote Sugar" to 92000.0,
            "St. Louis Sugar" to 98000.0,
            "Golden Penny Sugar" to 94000.0,
            
            // Seasoning Cubes
            "Maggi Star" to 7500.0,
            "Maggi Chicken" to 8200.0,
            "Maggi Crayfish" to 8500.0,
            "Knorr Beef" to 9000.0,
            "Knorr Chicken" to 9500.0,
            "Royco" to 6500.0,
            "Terra" to 6000.0,
            "Kitchen Glory" to 6200.0,
            "Chicken Glory" to 6500.0
        )

        val priceList = mutableListOf<MarketPrice>()
        val historyList = mutableListOf<PriceHistory>()

        // Generate current prices and historical price trails (stretching 4 periods back for charts)
        savedComm.forEach { comm ->
            val baseWholesale = wholesaleBases[comm.name] ?: 50000.0
            
            markets.forEach { (marketName, multiple) ->
                val finalWholesale = baseWholesale * multiple
                val finalRetail = finalWholesale / comm.conversionFactor
                
                priceList.add(
                    MarketPrice(
                        commodityId = comm.id,
                        marketLocation = marketName,
                        wholesalePrice = finalWholesale,
                        retailPrice = finalRetail,
                        lastUpdated = System.currentTimeMillis() - 1000 * 60, // 1 min ago
                        updatedBy = "Initial Import"
                    )
                )

                // Add 4-day trailing history to create wonderful volatility data
                val msPerDay = 24 * 60 * 60 * 1000L
                for (i in 4 downTo 1) {
                    // Introduce a random price swing of -10% to +10%
                    val randomChange = 1.0 + ((i % 3 - 1) * 0.05) + ((comm.id % 2 - 0.5) * 0.03)
                    val histWholesale = finalWholesale * randomChange
                    val histRetail = histWholesale / comm.conversionFactor
                    historyList.add(
                        PriceHistory(
                            commodityId = comm.id,
                            marketLocation = marketName,
                            wholesalePrice = histWholesale,
                            retailPrice = histRetail,
                            timestamp = System.currentTimeMillis() - i * msPerDay
                        )
                    )
                }
            }
        }

        dao.insertMarketPrices(priceList)
        dao.insertHistories(historyList)

        // Seed Default Subscription
        if (dao.getSubscriptionStateOnce() == null) {
            dao.insertOrUpdateSubscription(
                SubscriptionState(
                    id = 1,
                    tier = "FREE",
                    status = "inactive",
                    trialStartDate = null,
                    expiryDate = null,
                    dailySmsAlertEnabled = false,
                    phoneNumber = ""
                )
            )
        }

        // Seed Historic PLUS Subscription Records (Never Deleted)
        val existingPlusSubs = dao.getAllPlusSubscriptionsHistory().firstOrNull()
        if (existingPlusSubs.isNullOrEmpty()) {
            dao.insertPlusSubscriptionRecord(PlusSubscriptionRecord(name = "Yusuf Ibrahim", contact = "+234 803 111 2222", status = "Active", timestamp = System.currentTimeMillis() - 15 * 24 * 3600 * 1000L))
            dao.insertPlusSubscriptionRecord(PlusSubscriptionRecord(name = "Chioma Okafor", contact = "+234 812 333 4444", status = "Active", timestamp = System.currentTimeMillis() - 10 * 24 * 3600 * 1000L))
            dao.insertPlusSubscriptionRecord(PlusSubscriptionRecord(name = "Fatima Umar", contact = "+234 905 555 6666", status = "Expired", timestamp = System.currentTimeMillis() - 40 * 24 * 3600 * 1000L))
            dao.insertPlusSubscriptionRecord(PlusSubscriptionRecord(name = "Sesan Balogun", contact = "+234 701 777 8888", status = "Cancelled", timestamp = System.currentTimeMillis() - 5 * 24 * 3600 * 1000L))
        }

        // Seed Standard Admin Account
        if (dao.getAdminByUsername("admin") == null) {
            dao.insertAdminAccount(
                AdminAccount(
                    username = "admin",
                    passwordHash = "admin123",
                    email = "kenennakingsleychukwuma@gmail.com",
                    isGoogleAccount = false
                )
            )
        }
    }
}
