package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NairaGuardDao {

    // --- Commodities ---
    @Query("SELECT * FROM commodities ORDER BY name ASC")
    fun getAllCommodities(): Flow<List<Commodity>>

    @Query("SELECT * FROM commodities ORDER BY name ASC")
    suspend fun getAllCommoditiesList(): List<Commodity>

    @Query("SELECT * FROM commodities WHERE id = :id")
    fun getCommodityById(id: Int): Flow<Commodity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommodities(commodities: List<Commodity>)

    @Update
    suspend fun updateCommodity(commodity: Commodity)

    // --- Market Prices ---
    @Query("SELECT * FROM market_prices ORDER BY lastUpdated DESC")
    fun getAllMarketPrices(): Flow<List<MarketPrice>>

    @Query("SELECT * FROM market_prices WHERE commodityId = :commodityId")
    fun getPricesForCommodity(commodityId: Int): Flow<List<MarketPrice>>

    @Query("SELECT * FROM market_prices WHERE commodityId = :commodityId")
    suspend fun getPricesForCommodityList(commodityId: Int): List<MarketPrice>

    @Query("SELECT * FROM market_prices WHERE marketLocation = :marketLocation")
    fun getPricesForMarket(marketLocation: String): Flow<List<MarketPrice>>

    @Query("SELECT * FROM market_prices WHERE commodityId = :commodityId AND marketLocation = :market LIMIT 1")
    suspend fun getPriceForCommodityAndMarket(commodityId: Int, market: String): MarketPrice?

    @Query("DELETE FROM market_prices WHERE commodityId = :commodityId AND marketLocation = :market")
    suspend fun deletePriceForCommodityAndMarket(commodityId: Int, market: String)

    @Query("DELETE FROM market_prices WHERE marketLocation NOT IN ('Mile 12 (Mainland)', 'Isale Eko (Island)')")
    suspend fun cleanLegacyMarkets()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrice(marketPrice: MarketPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrices(marketPrices: List<MarketPrice>)

    // --- Price History ---
    @Query("SELECT * FROM price_history ORDER BY timestamp DESC")
    fun getAllPriceHistory(): Flow<List<PriceHistory>>

    @Query("SELECT * FROM price_history WHERE commodityId = :commodityId ORDER BY timestamp ASC")
    fun getHistoryForCommodity(commodityId: Int): Flow<List<PriceHistory>>

    @Query("SELECT * FROM price_history WHERE commodityId = :commodityId ORDER BY timestamp ASC")
    suspend fun getHistoryForCommodityList(commodityId: Int): List<PriceHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PriceHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<PriceHistory>)

    // --- Price Alerts ---
    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<PriceAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlert)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Int)

    @Query("UPDATE price_alerts SET isTriggered = 1 WHERE id = :id")
    suspend fun markAlertTriggered(id: Int)

    // --- Subscription State ---
    @Query("SELECT * FROM subscription_state WHERE id = 1 LIMIT 1")
    fun getSubscriptionState(): Flow<SubscriptionState?>

    @Query("SELECT * FROM subscription_state WHERE id = 1 LIMIT 1")
    suspend fun getSubscriptionStateOnce(): SubscriptionState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubscription(subscriptionState: SubscriptionState)

    // --- User Feedback ---
    @Query("SELECT * FROM user_feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<UserFeedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: UserFeedback)

    @Query("UPDATE user_feedback SET status = :status WHERE id = :id")
    suspend fun updateFeedbackStatus(id: Int, status: String)

    @Query("DELETE FROM user_feedback WHERE id = :id")
    suspend fun deleteFeedback(id: Int)

    // --- Admin Accounts ---
    @Query("SELECT * FROM admin_accounts WHERE username = :username LIMIT 1")
    suspend fun getAdminByUsername(username: String): AdminAccount?

    @Query("SELECT * FROM admin_accounts WHERE email = :email AND isGoogleAccount = 1 LIMIT 1")
    suspend fun getAdminByEmail(email: String): AdminAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminAccount(account: AdminAccount)

    // --- Waitlist ---
    @Query("SELECT * FROM waitlist_entries ORDER BY timestamp DESC")
    fun getAllWaitlist(): Flow<List<WaitlistEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaitlistEntry(entry: WaitlistEntry)

    @Query("DELETE FROM waitlist_entries WHERE id = :id")
    suspend fun deleteWaitlistEntry(id: Int)

    // --- Plus Subscriptions History (Never Deleted) ---
    @Query("SELECT * FROM plus_subscriptions_history ORDER BY timestamp DESC")
    fun getAllPlusSubscriptionsHistory(): Flow<List<PlusSubscriptionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlusSubscriptionRecord(record: PlusSubscriptionRecord)

    // --- Processed Transaction References (Idempotent Webhook Verification) ---
    @Query("SELECT * FROM processed_transaction_refs WHERE transactionRef = :ref LIMIT 1")
    suspend fun getProcessedTransactionRef(ref: String): ProcessedTransactionRef?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProcessedTransactionRef(record: ProcessedTransactionRef): Long
}
