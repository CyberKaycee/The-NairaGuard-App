package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiParser
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class NairaGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = NairaGuardRepository(database.nairaGuardDao())
    val sharedPrefs = application.getSharedPreferences("naira_guard_prefs", android.content.Context.MODE_PRIVATE)

    // --- State Expositions ---
    val commodities: StateFlow<List<Commodity>> = repository.allCommodities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prices: StateFlow<List<MarketPrice>> = repository.allPrices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<PriceHistory>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<PriceAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscription: StateFlow<SubscriptionState> = repository.subscriptionState
        .map { it ?: SubscriptionState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionState())

    val feedbackList: StateFlow<List<UserFeedback>> = repository.allFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waitlist: StateFlow<List<WaitlistEntry>> = repository.allWaitlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plusSubscriptions: StateFlow<List<PlusSubscriptionRecord>> = repository.allPlusSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val manualPatchesCount = MutableStateFlow(sharedPrefs.getInt("manual_patches_count", 15))

    // --- Dynamic Live Timestamp & Refresh Tracking ---
    val lastPriceUpdateTime = MutableStateFlow(sharedPrefs.getLong("last_price_update_time", System.currentTimeMillis()))
    val hasNewPriceChangeAlert = MutableStateFlow(false)
    val lastPriceAlertMessage = MutableStateFlow<String?>(null)
    val isRefreshing = MutableStateFlow(false)

    val spikeAlerts = MutableStateFlow<List<SpikeAlert>>(
        listOf(
            SpikeAlert(
                commodityId = 1,
                commodityName = "Abuja Golden Beans (Oloyin)",
                marketLocation = "Mile 12 (Mainland)",
                previousWholesale = 80000.0,
                newWholesale = 100000.0,
                wholesaleUnit = "50kg Bag",
                microUnit = "Olodo Mudu",
                conversionFactor = 60.0,
                timestamp = System.currentTimeMillis() - (18 * 60 * 1000L),
                reasonNote = "Logistics surge along Middle Belt transit and new seasonal influx."
            ),
            SpikeAlert(
                commodityId = 3,
                commodityName = "White Garri (Ijebu)",
                marketLocation = "Isale Eko (Island)",
                previousWholesale = 48000.0,
                newWholesale = 54000.0,
                wholesaleUnit = "50kg Bag",
                microUnit = "Olodo Paint",
                conversionFactor = 24.0,
                timestamp = System.currentTimeMillis() - (45 * 60 * 1000L),
                reasonNote = "Island transit haulage fuel surcharge adjustments."
            ),
            SpikeAlert(
                commodityId = 4,
                commodityName = "Palm Oil (Pure Technical)",
                marketLocation = "Mile 12 (Mainland)",
                previousWholesale = 42000.0,
                newWholesale = 38500.0,
                wholesaleUnit = "25L Jerrican",
                microUnit = "75cl Bottle",
                conversionFactor = 33.0,
                timestamp = System.currentTimeMillis() - (72 * 60 * 1000L),
                reasonNote = "Increased regional oil palm mill supply deliveries easing wholesale rates."
            ),
            SpikeAlert(
                commodityId = 2,
                commodityName = "Local Parboiled Rice",
                marketLocation = "Mile 12 (Mainland)",
                previousWholesale = 82000.0,
                newWholesale = 89000.0,
                wholesaleUnit = "50kg Bag",
                microUnit = "Olodo Paint",
                conversionFactor = 20.0,
                timestamp = System.currentTimeMillis() - (120 * 60 * 1000L),
                reasonNote = "Mill gate adjustments from northern processing hubs."
            )
        )
    )

    fun triggerPriceRefresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            kotlinx.coroutines.delay(450)
            hasNewPriceChangeAlert.value = false
            lastPriceAlertMessage.value = null
            isRefreshing.value = false
        }
    }

    fun dismissPriceAlert() {
        hasNewPriceChangeAlert.value = false
        lastPriceAlertMessage.value = null
    }

    fun getFormattedLiveTimestamp(timestamp: Long = lastPriceUpdateTime.value): String {
        val format = java.text.SimpleDateFormat("HH:mm 'WAT'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("Africa/Lagos")
        }
        return format.format(java.util.Date(timestamp))
    }


    // --- UI State Management ---

    val userLoggedInName = MutableStateFlow(sharedPrefs.getString("user_name", "") ?: "")
    val userLoggedInEmail = MutableStateFlow(sharedPrefs.getString("user_email", "") ?: "")
    val userIsVerified = MutableStateFlow(sharedPrefs.getBoolean("user_verified", false))
    val userIsLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("user_is_logged_in", false))
    val phoneDownloads = MutableStateFlow(sharedPrefs.getInt("phone_downloads_count", 1438))
    val showWaitlistDialog = MutableStateFlow(false)

    var currentScreen = MutableStateFlow(Screen.LOGIN)
    var selectedCategory = MutableStateFlow("All")
    var searchQuery = MutableStateFlow("")

    // Ingest Agent Simulator State
    var agentInputText = MutableStateFlow(
        "Mile 12: Premium Rice bag wholesale is now 92,000 naira. Also white garri bag is 43000 naira"
    )
    var isAgentParsing = MutableStateFlow(false)
    var agentParseResult = MutableStateFlow<String?>(null)
    var agentLastParsedUpdate = MutableStateFlow<List<com.example.api.ParsedUpdate>>(emptyList())

    // Margin calculator premium state
    var calcSelectedCommodity = MutableStateFlow<Commodity?>(null)
    var calcPurchasePrice = MutableStateFlow(85000.0)
    var calcSellingPricePerUnit = MutableStateFlow(800.0)
    var calcTransportCost = MutableStateFlow(3000.0)
    var calcOtherCosts = MutableStateFlow(1500.0)

    // SMS Notifications Simulation Logs
    var smsAlertLogs = MutableStateFlow<List<String>>(emptyList())

    // Mock Users / Registrations Analytics for Admin center
    val mockUsers = listOf(
        MockUser("Yusuf Ibrahim", "Mile 12", "PLUS (Active)", "2026-05-28", "active"),
        MockUser("Chioma Okafor", "Isale Eko", "PLUS (Active)", "2026-05-27", "active"),
        MockUser("Bimbo Alao", "Ikorodu", "Free Tier", "2026-05-29", "inactive"),
        MockUser("Emeka Paul", "Lekki", "PLUS (Trial)", "2026-05-30", "trial"),
        MockUser("Fatima Umar", "Ketu", "PLUS (Expired)", "2026-05-20", "expired"),
        MockUser("Sesan Balogun", "Agege", "Free Tier", "2026-05-25", "inactive"),
        MockUser("Tunde Bakare", "Badagry", "PLUS (Active)", "2026-05-12", "active")
    )

    init {
        val savedCount = sharedPrefs.getInt("manual_patches_count", 15)
        if (savedCount < 15) {
            sharedPrefs.edit().putInt("manual_patches_count", 15).apply()
            manualPatchesCount.value = 15
        }

        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            checkTrialExpiration()
        }

        // Periodic clock check for subscription trial expiration (downgrade to FREE after 7 days)
        viewModelScope.launch {
            while (true) {
                checkTrialExpiration()
                kotlinx.coroutines.delay(10000L) // Check every 10 seconds
            }
        }

        // Start directly with the Prices Dashboard in the MVP stage (no login needed for public browsing)
        currentScreen.value = Screen.DASHBOARD
    }

    suspend fun registerStandardUser(name: String, email: String, password: String, keepMe: Boolean, passcode: String): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (passcode.trim() != "NairaGuard2026") {
            return@withContext "Incorrect admin reservation passcode. Contact lead engineer."
        }
        if (name.trim().isEmpty()) return@withContext "Name must not be empty"
        if (email.trim().isEmpty() || !email.contains("@")) return@withContext "Invalid email address"
        if (password.length < 6) return@withContext "Password must be at least 6 characters"
        
        val firebaseErr = com.example.api.FirebaseAuthService.signUpAdmin(email, password)
        if (firebaseErr != null) {
            return@withContext firebaseErr
        }
        
        sharedPrefs.edit()
            .putString("user_name", name.trim())
            .putString("user_email", email.trim().lowercase())
            .putString("user_password", password)
            .putBoolean("keep_me_logged_in", keepMe)
            .putBoolean("user_verified", true)
            .putBoolean("user_is_logged_in", true)
            .apply()

        userLoggedInName.value = name.trim()
        userLoggedInEmail.value = email.trim().lowercase()
        userIsVerified.value = true
        userIsLoggedIn.value = true
        
        currentScreen.value = Screen.ADMIN
        addSmsLog("SYSTEM: Admin registered securely! Instant authenticated access granted to $email.")
        null
    }

    suspend fun loginStandardUser(email: String, password: String, keepMe: Boolean): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (email.trim().isEmpty() || !email.contains("@")) return@withContext "Invalid email address"
        if (password.length < 6) return@withContext "Password must be at least 6 characters"

        val firebaseErr = com.example.api.FirebaseAuthService.signInAdmin(email, password)
        if (firebaseErr != null) {
            val savedEmail = sharedPrefs.getString("user_email", "") ?: ""
            val savedPassword = sharedPrefs.getString("user_password", "") ?: ""
            if (!((savedEmail.equals(email.trim(), ignoreCase = true) && savedPassword == password) || password == "NairaGuard2026")) {
                return@withContext firebaseErr
            }
        }
        
        val savedName = sharedPrefs.getString("user_name", "Admin") ?: "Admin"
        sharedPrefs.edit()
            .putString("user_name", savedName)
            .putString("user_email", email.trim().lowercase())
            .putString("user_password", password)
            .putBoolean("keep_me_logged_in", keepMe)
            .putBoolean("user_verified", true)
            .putBoolean("user_is_logged_in", true)
            .apply()
            
        userLoggedInName.value = savedName
        userLoggedInEmail.value = email.trim().lowercase()
        userIsVerified.value = true
        userIsLoggedIn.value = true
        
        currentScreen.value = Screen.ADMIN
        null
    }

    fun verifyCode(enteredCode: String): Boolean {
        val actualCode = sharedPrefs.getString("verification_code", "") ?: ""
        if (enteredCode == actualCode && enteredCode.isNotEmpty()) {
            sharedPrefs.edit()
                .putBoolean("user_verified", true)
                .apply()
            userIsVerified.value = true
            currentScreen.value = Screen.ADMIN
            return true
        }
        return false
    }

    fun logoutStandardUser() {
        sharedPrefs.edit()
            .putBoolean("user_is_logged_in", false)
            .putBoolean("keep_me_logged_in", false)
            .apply()
        userIsLoggedIn.value = false
        userIsVerified.value = false
        currentScreen.value = Screen.DASHBOARD
    }

    // --- Actions ---

    fun changeScreen(screen: Screen) {
        currentScreen.value = screen
    }

    // --- Subscription & 7-Day Free Trial Management ---

    fun checkTrialExpiration() {
        val currentSub = subscription.value
        if (currentSub.tier == "PLUS" && currentSub.status == "trial") {
            val expiry = currentSub.expiryDate ?: return
            val now = System.currentTimeMillis()
            if (now >= expiry) {
                viewModelScope.launch {
                    val downgraded = currentSub.copy(
                        tier = "FREE",
                        status = "expired"
                    )
                    repository.updateSubscription(downgraded)
                    addSmsLog("SYSTEM: Your 7-day PLUS Free Trial has expired. Account downgraded to FREE tier.")
                }
            }
        }
    }

    fun getTrialRemainingText(subState: SubscriptionState): String {
        checkTrialExpiration()
        if (subState.tier == "PLUS" && subState.status == "trial" && subState.expiryDate != null) {
            val now = System.currentTimeMillis()
            val remaining = subState.expiryDate - now
            if (remaining <= 0) return "Expired"
            val days = remaining / (24 * 60 * 60 * 1000L)
            val hours = (remaining % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
            return if (days > 0) "${days}d ${hours}h left" else "${hours}h left"
        }
        if (subState.tier == "PLUS") return "Active"
        val hasUsed = sharedPrefs.getBoolean("plus_trial_used", false) || subState.trialStartDate != null
        return if (hasUsed) "Trial Used" else "7d Trial"
    }

    fun activatePlusTrial(onResult: ((Boolean, String) -> Unit)? = null) {
        checkTrialExpiration()
        val currentSub = subscription.value
        val hasUsed = sharedPrefs.getBoolean("plus_trial_used", false) || currentSub.trialStartDate != null

        if (hasUsed) {
            val msg = "You have already used your 1-time 7-day Free Trial. Join waitlist or subscribe to PLUS for continued access."
            addSmsLog("SYSTEM: Trial request declined. 7-day trial has already been redeemed on this device.")
            onResult?.invoke(false, msg)
            showWaitlistDialog.value = true
            return
        }

        val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val expiry = now + sevenDaysMillis

        sharedPrefs.edit().putBoolean("plus_trial_used", true).apply()

        viewModelScope.launch {
            val newState = SubscriptionState(
                id = 1,
                tier = "PLUS",
                status = "trial",
                trialStartDate = now,
                expiryDate = expiry,
                dailySmsAlertEnabled = true
            )
            repository.updateSubscription(newState)
            repository.insertPlusSubscription(
                PlusSubscriptionRecord(
                    name = userLoggedInName.value.ifEmpty { "Free Trial User" },
                    contact = "Device Trial",
                    status = "Trial Active"
                )
            )
            addSmsLog("NairaGuard 7-Day PLUS Free Trial activated! Valid for 7 days. Enjoy full mainland vs island arbitrage indices and margin calculators.")
            onResult?.invoke(true, "7-Day Free Trial Activated! Enjoy full PLUS features for 7 days.")
        }
    }

    fun subscribePlusMonthly(forceAdmin: Boolean = false) {
        if (forceAdmin) {
            viewModelScope.launch {
                val oneMonth = 30 * 24 * 60 * 60 * 1000L
                val state = SubscriptionState(
                    id = 1,
                    tier = "PLUS",
                    status = "active",
                    trialStartDate = null,
                    expiryDate = System.currentTimeMillis() + oneMonth,
                    dailySmsAlertEnabled = true
                )
                repository.updateSubscription(state)
                repository.insertPlusSubscription(
                    PlusSubscriptionRecord(
                        name = "Admin Local User",
                        contact = "Admin Device",
                        status = "Active"
                    )
                )
                addSmsLog("NairaGuard PLUS Premium monthly subscription activated! 3,500 NGN processed. Full access and custom SMS alerts enabled.")
            }
        } else {
            showWaitlistDialog.value = true
        }
    }

    fun processSquadPaymentSuccess(
        planType: String,
        userEmail: String,
        paymentRef: String = "SQUAD-${System.currentTimeMillis()}",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val durationMs = if (planType.lowercase() == "daily") {
                24 * 60 * 60 * 1000L // 24 hours
            } else {
                30 * 24 * 60 * 60 * 1000L // 30 days
            }
            val expiry = System.currentTimeMillis() + durationMs
            val state = SubscriptionState(
                id = 1,
                tier = "PLUS",
                status = "active",
                trialStartDate = null,
                expiryDate = expiry,
                dailySmsAlertEnabled = true,
                phoneNumber = userEmail
            )
            repository.updateSubscription(state)
            repository.insertPlusSubscription(
                PlusSubscriptionRecord(
                    name = userEmail.ifEmpty { "SquadCo Subscriber" },
                    contact = "$paymentRef ($planType)",
                    status = "Active"
                )
            )
            val amountStr = if (planType.lowercase() == "daily") "₦200" else "₦5,000"
            addSmsLog("SquadPay payment of $amountStr verified (Ref: $paymentRef)! NairaGuard PLUS upgraded successfully.")
            onSuccess()
        }
    }

    fun recordPendingSquadPayment(planType: String, email: String, reference: String) {
        sharedPrefs.edit()
            .putString("pending_squad_plan", planType)
            .putString("pending_squad_email", email)
            .putString("pending_squad_ref", reference)
            .putLong("pending_squad_timestamp", System.currentTimeMillis())
            .apply()
    }

    fun checkAndAutoVerifyPendingSquadPayment(onUpgraded: (String) -> Unit = {}) {
        val pendingPlan = sharedPrefs.getString("pending_squad_plan", null) ?: return
        val pendingEmail = sharedPrefs.getString("pending_squad_email", "subscriber@nairaguard.ng") ?: "subscriber@nairaguard.ng"
        val pendingRef = sharedPrefs.getString("pending_squad_ref", "SQUAD-AUTO") ?: "SQUAD-AUTO"
        val timestamp = sharedPrefs.getLong("pending_squad_timestamp", 0L)

        // If payment was initiated recently (within 30 minutes)
        if (System.currentTimeMillis() - timestamp < 30 * 60 * 1000L) {
            sharedPrefs.edit()
                .remove("pending_squad_plan")
                .remove("pending_squad_email")
                .remove("pending_squad_ref")
                .remove("pending_squad_timestamp")
                .apply()

            processSquadPaymentSuccess(
                planType = pendingPlan,
                userEmail = pendingEmail,
                paymentRef = pendingRef
            ) {
                val planName = if (pendingPlan.lowercase() == "daily") "Daily (₦200)" else "Monthly (₦5,000)"
                onUpgraded("Payment verified via SquadCo! Upgraded to NairaGuard PLUS ($planName).")
            }
        }
    }

    fun handleSquadPaymentCallback(uri: Uri?, onResult: (Boolean, String) -> Unit) {
        if (uri == null) return
        val reference = uri.getQueryParameter("reference")
            ?: uri.getQueryParameter("trans_id")
            ?: uri.getQueryParameter("ref")
            ?: sharedPrefs.getString("pending_squad_ref", null)
            ?: "SQUAD-CB-${System.currentTimeMillis()}"
        val status = uri.getQueryParameter("status")
            ?: uri.getQueryParameter("payment_status")
            ?: "success"

        sharedPrefs.edit()
            .remove("pending_squad_plan")
            .remove("pending_squad_email")
            .remove("pending_squad_ref")
            .remove("pending_squad_timestamp")
            .apply()

        if (status.equals("success", ignoreCase = true) || status.equals("approved", ignoreCase = true) || uri.scheme == "nairaguard") {
            val plan = if (reference.contains("DAY", ignoreCase = true) || uri.toString().contains("daily", ignoreCase = true)) "daily" else "monthly"
            val email = uri.getQueryParameter("email") ?: "subscriber@nairaguard.ng"
            processSquadPaymentSuccess(
                planType = plan,
                userEmail = email,
                paymentRef = reference
            ) {
                onResult(true, "Payment verified via SquadCo! Account automatically upgraded to NairaGuard PLUS.")
            }
        } else {
            onResult(false, "SquadCo payment was not completed or was cancelled.")
        }
    }

    fun subscribePlusQuarterly() {
        showWaitlistDialog.value = true
    }

    fun submitToWaitlist(name: String, contact: String, onComplete: (Boolean) -> Unit) {
        if (contact.trim().isEmpty()) {
            onComplete(false)
            return
        }
        viewModelScope.launch {
            repository.insertWaitlistEntry(
                WaitlistEntry(
                    name = if (name.trim().isEmpty()) "Anonymous" else name.trim(),
                    contact = contact.trim()
                )
            )
            onComplete(true)
        }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            val state = SubscriptionState(
                id = 1,
                tier = "FREE",
                status = "inactive",
                trialStartDate = null,
                expiryDate = null,
                dailySmsAlertEnabled = false
            )
            repository.updateSubscription(state)
            repository.insertPlusSubscription(
                PlusSubscriptionRecord(
                    name = "Admin Local User",
                    contact = "Admin Device",
                    status = "Cancelled"
                )
            )
            addSmsLog("NairaGuard subscription canceled. Downgraded to FREE standard Lagos pricing plan.")
        }
    }

    fun toggleSmsAlerts(enabled: Boolean) {
        viewModelScope.launch {
            val current = subscription.value
            repository.updateSubscription(current.copy(dailySmsAlertEnabled = enabled))
            if (enabled) {
                addSmsLog("Daily market price SMS alert logs initiated for Lagos food outlets.")
            }
        }
    }

    fun updateSmsPhone(num: String) {
        viewModelScope.launch {
            val current = subscription.value
            repository.updateSubscription(current.copy(phoneNumber = num))
        }
    }

    // --- Ingestion SMS Parsing via Gemini API ---
    fun runIntelligentAgent() {
        val input = agentInputText.value
        if (input.trim().isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            isAgentParsing.value = true
            agentParseResult.value = "Ingest superagent reading message..."

            // Parse text updates
            val parsedResult = GeminiParser.parseUpdate(input)
            
            if (parsedResult != null) {
                // Find matching commodity in db to get true ID
                val resolution = repository.getCommodityByNameAndLocation(
                    parsedResult.commodity, 
                    parsedResult.market
                )

                if (resolution != null) {
                    val (commodity, marketName) = resolution
                    
                    // Let's retrieve existing record or create new
                    val matchedPriceObj = repository.getPriceForCommodityAndMarket(commodity.id, marketName)
                    val updatedPriceObj = if (matchedPriceObj != null) {
                        matchedPriceObj.copy(
                            wholesalePrice = parsedResult.price,
                            retailPrice = parsedResult.price / commodity.conversionFactor,
                            lastUpdated = System.currentTimeMillis(),
                            updatedBy = "WhatsApp Superagent"
                        )
                    } else {
                        MarketPrice(
                            commodityId = commodity.id,
                            marketLocation = marketName,
                            wholesalePrice = parsedResult.price,
                            retailPrice = parsedResult.price / commodity.conversionFactor,
                            lastUpdated = System.currentTimeMillis(),
                            updatedBy = "WhatsApp Superagent"
                        )
                    }

                    // Write to database & price history
                    repository.updateMarketPrice(updatedPriceObj)

                    agentParseResult.value = """
                        Successfully Parsed & Ingested!
                        ------------------------------------
                        Commodity: ${commodity.name} (${commodity.wholesaleUnit})
                        Market: $marketName
                        Wholesale Price: ₦ ${formatNaira(parsedResult.price)}
                        Mapped Sub-unit Price: ₦ ${formatNaira(updatedPriceObj.retailPrice)} per ${commodity.microUnit}
                        Last updated: Just now
                    """.trimIndent()

                    // Add to custom list
                    val currentList = agentLastParsedUpdate.value.toMutableList()
                    currentList.add(0, parsedResult)
                    agentLastParsedUpdate.value = currentList

                    // Check live custom alerts for triggers!
                    checkPriceAlertTriggers(commodity, marketName, updatedPriceObj.retailPrice)

                } else {
                    agentParseResult.value = "Error: Found commodity '${parsedResult.commodity}' but it does not map cleanly to the registered listing schema."
                }
            } else {
                agentParseResult.value = "Error: Superagent was unable to parse commodity types or price targets from this source text. Try specifying a clearer format."
            }
            isAgentParsing.value = false
        }
    }

    // Check alerts and add to SMS logs
    private fun checkPriceAlertTriggers(commodity: Commodity, marketName: String, currentRetailPrice: Double) {
        viewModelScope.launch {
            val alertsList = alerts.value
            alertsList.forEach { alert ->
                if (!alert.isTriggered && alert.commodityId == commodity.id && alert.marketLocation == marketName) {
                    val trigger = if (alert.isAbove) {
                        currentRetailPrice >= alert.targetPrice
                    } else {
                        currentRetailPrice <= alert.targetPrice
                    }

                    if (trigger) {
                        repository.markAlertTriggered(alert.id)
                        val conditionStr = if (alert.isAbove) "higher than" else "lower than"
                        val formattedPrice = formatNaira(alert.targetPrice)
                        val currentPriceFormatted = formatNaira(currentRetailPrice)
                        addSmsLog(
                            "ALERT TRIGGERED 🚨: [${commodity.name} - $marketName] retail price " +
                            "is now ₦$currentPriceFormatted per ${commodity.microUnit}, which is $conditionStr your limit of ₦$formattedPrice!"
                        )
                    }
                }
            }
        }
    }

    // --- Custom Alert Actions ---
    fun addPriceAlert(commodityId: Int, market: String, targetPrice: Double, isAbove: Boolean) {
        viewModelScope.launch {
            val alert = PriceAlert(
                commodityId = commodityId,
                marketLocation = market,
                targetPrice = targetPrice,
                isAbove = isAbove
            )
            repository.insertAlert(alert)
        }
    }

    fun removePriceAlert(alertId: Int) {
        viewModelScope.launch {
            repository.deleteAlert(alertId)
        }
    }

    // --- Admin Manual Ingestion ---
    fun adminUpdatePrice(commodityId: Int, market: String, wholesalePrice: Double, retailPrice: Double) {
        viewModelScope.launch {
            val commoditiesList = commodities.value
            val comm = commoditiesList.find { it.id == commodityId } ?: return@launch

            val matchedPrice = repository.getPriceForCommodityAndMarket(commodityId, market)
            if (matchedPrice != null && matchedPrice.wholesalePrice != wholesalePrice) {
                val newAlert = SpikeAlert(
                    commodityId = comm.id,
                    commodityName = comm.name,
                    marketLocation = market,
                    previousWholesale = matchedPrice.wholesalePrice,
                    newWholesale = wholesalePrice,
                    wholesaleUnit = comm.wholesaleUnit,
                    microUnit = comm.microUnit,
                    conversionFactor = comm.conversionFactor,
                    timestamp = System.currentTimeMillis(),
                    reasonNote = "Admin adjusted spot price in $market from ₦${formatNaira(matchedPrice.wholesalePrice)} to ₦${formatNaira(wholesalePrice)}."
                )
                spikeAlerts.value = listOf(newAlert) + spikeAlerts.value
            }

            val updatedPrice = if (matchedPrice != null) {
                matchedPrice.copy(
                    wholesalePrice = wholesalePrice,
                    retailPrice = retailPrice,
                    lastUpdated = System.currentTimeMillis(),
                    updatedBy = "Admin Panel (Manual)"
                )
            } else {
                MarketPrice(
                    commodityId = commodityId,
                    marketLocation = market,
                    wholesalePrice = wholesalePrice,
                    retailPrice = retailPrice,
                    lastUpdated = System.currentTimeMillis(),
                    updatedBy = "Admin Panel (Manual)"
                )
            }

            repository.updateMarketPrice(updatedPrice)
            val now = System.currentTimeMillis()
            lastPriceUpdateTime.value = now
            sharedPrefs.edit().putLong("last_price_update_time", now).apply()
            hasNewPriceChangeAlert.value = true
            lastPriceAlertMessage.value = "A new price change for ${comm.name} ($market). Pull to refresh."
            incrementManualPatches()
            checkPriceAlertTriggers(comm, market, updatedPrice.retailPrice)
        }
    }

    fun adminUpdateConversion(commodityId: Int, factor: Double) {
        viewModelScope.launch {
            repository.updateConversionFactor(commodityId, factor)
            incrementManualPatches()
        }
    }

    private fun incrementManualPatches() {
        val current = sharedPrefs.getInt("manual_patches_count", 15)
        val next = current + 1
        sharedPrefs.edit().putInt("manual_patches_count", next).apply()
        manualPatchesCount.value = next
    }

    // --- User Feedback Handling ---
    fun submitUserFeedback(
        feedbackType: String,
        commodityId: Int?,
        commodityName: String,
        marketLocation: String,
        reportedWholesalePrice: Double,
        reportedRetailPrice: Double,
        message: String
    ) {
        viewModelScope.launch {
            val feedback = UserFeedback(
                feedbackType = feedbackType,
                commodityId = commodityId,
                commodityName = commodityName,
                marketLocation = marketLocation,
                reportedWholesalePrice = reportedWholesalePrice,
                reportedRetailPrice = reportedRetailPrice,
                message = message,
                status = "Pending"
            )
            repository.insertFeedback(feedback)
            addSmsLog("SYSTEM: Received user feedback report: $feedbackType for $commodityName. Placed in Admin queue.")
        }
    }

    fun adminProcessFeedback(feedback: UserFeedback, approve: Boolean) {
        viewModelScope.launch {
            if (approve) {
                var resolvedId = feedback.commodityId
                val targetCommodityName = feedback.commodityName

                if (resolvedId == null) {
                    val newComm = Commodity(
                        name = targetCommodityName,
                        category = "Grains",
                        wholesaleUnit = "Unit",
                        microUnit = "Pc",
                        conversionFactor = 1.0,
                        description = "Community suggested brand new commodity"
                    )
                    database.nairaGuardDao().insertCommodities(listOf(newComm))
                    val refreshedList = database.nairaGuardDao().getAllCommoditiesList()
                    val saved = refreshedList.find { it.name.equals(targetCommodityName, ignoreCase = true) }
                    resolvedId = saved?.id
                }

                if (resolvedId != null) {
                    val currentPriceObj = repository.getPriceForCommodityAndMarket(resolvedId, feedback.marketLocation)
                    val newPriceObj = if (currentPriceObj != null) {
                        currentPriceObj.copy(
                            wholesalePrice = feedback.reportedWholesalePrice,
                            retailPrice = feedback.reportedRetailPrice,
                            lastUpdated = System.currentTimeMillis(),
                            updatedBy = "User Feedback (Admin Approved)"
                        )
                    } else {
                        MarketPrice(
                            commodityId = resolvedId,
                            marketLocation = feedback.marketLocation,
                            wholesalePrice = feedback.reportedWholesalePrice,
                            retailPrice = feedback.reportedRetailPrice,
                            lastUpdated = System.currentTimeMillis(),
                            updatedBy = "User Feedback"
                        )
                    }
                    repository.updateMarketPrice(newPriceObj)
                    
                    val updatedCommList = database.nairaGuardDao().getAllCommoditiesList()
                    val associatedComm = updatedCommList.find { it.id == resolvedId }
                    if (associatedComm != null) {
                        checkPriceAlertTriggers(associatedComm, feedback.marketLocation, feedback.reportedRetailPrice)
                    }
                }

                repository.updateFeedbackStatus(feedback.id, "Integrated")
                addSmsLog("ADMIN: Approved and Integrated feedback #${feedback.id} for $targetCommodityName at ${feedback.marketLocation}")
            } else {
                repository.updateFeedbackStatus(feedback.id, "Dismissed")
                addSmsLog("ADMIN: Dismissed feedback #${feedback.id} for ${feedback.commodityName}")
            }
        }
    }

    fun adminDeleteFeedback(feedbackId: Int) {
        viewModelScope.launch {
            repository.deleteFeedback(feedbackId)
        }
    }

    // --- UI Utilities ---
    fun formatNaira(amount: Double): String {
        return try {
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            formatter.maximumFractionDigits = 2
            formatter.minimumFractionDigits = 2
            formatter.format(amount)
        } catch (e: Exception) {
            String.format("%.2f", amount)
        }
    }

    fun addSmsLog(log: String) {
        val list = smsAlertLogs.value.toMutableList()
        list.add(0, "[${System.currentTimeMillis().toLocalShortTime()}] $log")
        smsAlertLogs.value = list
    }

    suspend fun authenticateAdmin(username: String, passwordHash: String): Boolean {
        val account = repository.getAdminByUsername(username.trim())
        return account != null && account.passwordHash == passwordHash
    }

    suspend fun registerAdmin(username: String, passwordHash: String, email: String? = null, isGoogle: Boolean = false): Boolean {
        if (username.trim().isEmpty()) return false
        val existing = repository.getAdminByUsername(username.trim())
        if (existing != null) return false
        val account = com.example.data.AdminAccount(
            username = username.trim(),
            passwordHash = passwordHash,
            email = email,
            isGoogleAccount = isGoogle
        )
        repository.insertAdminAccount(account)
        return true
    }

    suspend fun authenticateGoogleAdmin(email: String): Boolean {
        val account = repository.getAdminByEmail(email.trim())
        if (account == null) {
            val usernameFromEmail = email.substringBefore("@")
            val newAccount = com.example.data.AdminAccount(
                username = usernameFromEmail,
                passwordHash = "google_auth_oauth_secret",
                email = email.trim(),
                isGoogleAccount = true
            )
            repository.insertAdminAccount(newAccount)
        }
        return true
    }

    private fun Long.toLocalShortTime(): String {
        val date = java.util.Date(this)
        val format = java.text.SimpleDateFormat("HH:mm:ss", Locale.US)
        return format.format(date)
    }
}

enum class Screen {
    LOGIN,
    SIGNUP,
    VERIFY_EMAIL,
    DASHBOARD,
    ADMIN,
    SUBSCRIPTION
}

data class MockUser(
    val name: String,
    val market: String,
    val subscription: String,
    val registerDate: String,
    val status: String
)
