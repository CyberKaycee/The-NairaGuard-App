package com.example.api

import android.content.Context
import android.content.Intent
import android.net.Uri

object SquadPayService {
    const val PUBLIC_KEY = "pk_46a30307bfe0041399d646ffefd9619dfc6e23fa"
    const val SECRET_KEY = "sk_46a30307bfe004139eb642f6e4d37ee7837544f8"

    // Official SquadCo Payment Links
    const val DAILY_PAYMENT_LINK = "https://pay.squadco.com/nairaguard_plus_daily"
    const val MONTHLY_PAYMENT_LINK = "https://pay.squadco.com/nairaguard_technologies"
    const val DIRECT_PAYMENT_LINK = DAILY_PAYMENT_LINK

    const val DAILY_PRICE_NAIRA = 200.0
    const val DAILY_PRICE_KOBO = 20000L // 200 * 100

    const val MONTHLY_PRICE_NAIRA = 5000.0
    const val MONTHLY_PRICE_KOBO = 500000L // 5000 * 100

    const val APP_CALLBACK_URL = "nairaguard://payment-callback"

    /**
     * Directly launches the ₦200/day tier payment gateway.
     */
    fun launchDailyCheckoutDirect(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DAILY_PAYMENT_LINK)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Builds payment URL or launches SquadCo gateway.
     * For daily plan, routes directly to https://pay.squadco.com/nairaguard_plus_daily.
     */
    fun launchSquadCheckout(
        context: Context, 
        planType: String, 
        userEmail: String,
        reference: String = "SQUAD-${if (planType.lowercase() == "daily") "DAY" else "MONTH"}-${System.currentTimeMillis()}"
    ): Boolean {
        val targetLink = if (planType.lowercase() == "daily") DAILY_PAYMENT_LINK else MONTHLY_PAYMENT_LINK
        return try {
            val amountKobo = if (planType.lowercase() == "daily") DAILY_PRICE_KOBO else MONTHLY_PRICE_KOBO
            val planLabel = if (planType.lowercase() == "daily") "NairaGuard PLUS (24h Daily Pass - ₦200)" else "NairaGuard PLUS (Monthly Pass - ₦5,000)"
            
            // Build checkout link with callback webhook/deep-link parameters
            val checkoutUri = Uri.parse(targetLink).buildUpon()
                .appendQueryParameter("email", userEmail.ifEmpty { "subscriber@nairaguard.ng" })
                .appendQueryParameter("amount", amountKobo.toString())
                .appendQueryParameter("currency_code", "NGN")
                .appendQueryParameter("desc", planLabel)
                .appendQueryParameter("reference", reference)
                .appendQueryParameter("callback_url", APP_CALLBACK_URL)
                .build()

            val intent = Intent(Intent.ACTION_VIEW, checkoutUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to direct link
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetLink)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }
}
