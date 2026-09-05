package com.example.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedUpdate(
    val commodity: String,
    val market: String,
    val price: Double,
    val rawPhrase: String
)

object GeminiParser {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val TAG = "GeminiParser"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Supported commodities matching our seeded database
    private val VALID_COMMODITIES = listOf(
        "Local Rice", "Imported Foreign Rice", "Abuja Golden Beans (Oloyin)", "White Beans (Iron Beans)",
        "Garri (White)", "Garri (Yellow)", "Agege Bread", "Semovita", "Wheat Flour", "Hard Chicken (Frozen)",
        "Beef", "Local Eggs", "Vegetable Oil", "Palm Oil", "Yam", "Tomatoes",
        "Rodo", "Onions", "Dano Milk", "Spaghetti (Golden Penny)", "Sachet Tomato Paste (Gino and Tasty Tom)",
        
        // Category: "Instant Noodles"
        "Indomie Indomitables", "Indomie Super Pack", "Indomie Onion Flavour", "Indomie Hungry Man Size", "Indomie Belle Full", "Golden Penny Noodles",
        
        // Category: "Salt"
        "Dangote Salt", "Mr. Chef Salt", "Royal Salt",
        
        // Category: "Sugar"
        "Dangote Sugar", "St. Louis Sugar", "Golden Penny Sugar",
        
        // Category: "Seasoning Cubes"
        "Maggi Star", "Maggi Chicken", "Maggi Crayfish", "Knorr Beef", "Knorr Chicken", "Royco", "Terra", "Kitchen Glory", "Chicken Glory"
    )

    private val VALID_MARKETS = listOf(
        "Mile 12 (Mainland)",
        "Isale Eko (Island)"
    )

    suspend fun parseUpdate(rawMessage: String): ParsedUpdate? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY" || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local keyword parsing.")
            return fallbackLocalParser(rawMessage)
        }

        return try {
            val systemInstruction = """
                Your task is to parse a raw text message containing wholesale Lagos market food prices and convert them to structured, normalized JSON.
                
                *** DEFINITIVE CATEGORY TAXONOMY AND BRAND LISTS ***
                Do NOT classify any item under parent labels like "Processed Goods" or "Packaged Goods" (these are DECOMMISSIONED). Instead, map any match to one of these precisely:
                
                1. Category: "Instant Noodles"
                   Valid products: "Indomie Indomitables", "Indomie Super Pack", "Indomie Onion Flavour", "Indomie Hungry Man Size", "Indomie Belle Full", "Golden Penny Noodles"
                   
                2. Category: "Salt"
                   Valid products: "Dangote Salt", "Mr. Chef Salt", "Royal Salt"
                   
                3. Category: "Sugar"
                   Valid products: "Dangote Sugar", "St. Louis Sugar", "Golden Penny Sugar" (Be sure to check if they specify cubes or granulated, e.g. "St. Louis Sugar" or "Dangote Sugar")
                   
                4. Category: "Seasoning Cubes"
                   Valid products: "Maggi Star", "Maggi Chicken", "Maggi Crayfish", "Knorr Beef", "Knorr Chicken", "Royco", "Terra", "Kitchen Glory", "Chicken Glory"
                   
                5. Traditional Commodities:
                   "Local Rice", "Imported Foreign Rice", "Abuja Golden Beans (Oloyin)", "White Beans (Iron Beans)", "Garri (White)", "Garri (Yellow)", "Agege Bread", "Semovita", "Wheat Flour", "Hard Chicken (Frozen)", "Beef", "Local Eggs", "Vegetable Oil", "Palm Oil", "Yam", "Tomatoes", "Rodo", "Onions", "Dano Milk", "Spaghetti (Golden Penny)", "Sachet Tomato Paste (Gino and Tasty Tom)"

                *** FUZZY / OMISSION RESOLUTION MAP ***
                If a brand variation is omitted in the raw text, apply these default context clues:
                - If the message mentions "noodles" or "Indomie" without details, map it to: "Indomie Onion Flavour"
                - If the message mentions "salt" without details, map it to: "Dangote Salt"
                - If the message mentions "sugar" without details, map it to: "Dangote Sugar"
                - If the message mentions "maggi" or "seasoning" or "cubes" without details, map it to: "Maggi Star"
                - If the message mentions "st louis", map it to: "St. Louis Sugar"
                - If the message mentions "knorr", map it to "Knorr Beef"
                
                *** TARGET JSON FIELDS ***
                Extract and output EXACTLY:
                - commodity: Must be one of the precise matching commodity names listed above.
                - market: One of these two: "Mile 12 (Mainland)" or "Isale Eko (Island)". Default to "Mile 12 (Mainland)" if Mainland or general; "Isale Eko (Island)" if Island.
                - price: Numeric double price. E.g. "85,000 NGN" or "85k" or "85" (if representing thousands) is 85000.0.
                - rawPhrase: A short quote from the raw text matching this record.

                Respond ONLY with raw JSON matching this schema:
                {
                  "commodity": "Indomie Onion Flavour",
                  "market": "Mile 12 (Mainland)",
                  "price": 7200.0,
                  "rawPhrase": "Indomie onion carton 7200"
                }
            """.trimIndent()

            val requestBodyJson = JSONObject()
            
            // Contents
            val contentsArray = org.json.JSONArray()
            val contentObj = JSONObject()
            val partsArray = org.json.JSONArray()
            val partObj = JSONObject()
            partObj.put("text", "Parse this raw update message: \"$rawMessage\"")
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            // System Instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = org.json.JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstructionObj.put("parts", sysPartsArray)
            requestBodyJson.put("systemInstruction", sysInstructionObj)

            // Config
            val generationConfig = JSONObject()
            generationConfig.put("responseMimeType", "application/json")
            generationConfig.put("temperature", 0.1)
            requestBodyJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBodyJson.toString().toRequestBody(mediaType)
            
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "API Call failed with status code ${response.code}. Response: ${response.body?.string()}")
                return fallbackLocalParser(rawMessage)
            }

            val responseString = response.body?.string() ?: ""
            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val firstPart = parts?.optJSONObject(0)
            val textResult = firstPart?.optString("text") ?: ""

            if (textResult.isEmpty()) {
                return fallbackLocalParser(rawMessage)
            }

            val resultJson = JSONObject(textResult.trim())
            val commodity = resultJson.optString("commodity", "")
            val market = resultJson.optString("market", "Mile 12 (Mainland)")
            val price = resultJson.optDouble("price", 0.0)
            val rawPhrase = resultJson.optString("rawPhrase", rawMessage)

            if (commodity.isNotEmpty() && price > 0) {
                ParsedUpdate(commodity, market, price, rawPhrase)
            } else {
                fallbackLocalParser(rawMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API: ${e.message}", e)
            fallbackLocalParser(rawMessage)
        }
    }

    private fun fallbackLocalParser(message: String): ParsedUpdate? {
        Log.i(TAG, "Parsing update locally via Regex match rules.")
        val text = message.lowercase()

        // 1. Find commodity using contextual match and exact sub-brand lookup
        val matchedComm = when {
            // Salt fuzzy mapping
            text.contains("dangote salt") -> "Dangote Salt"
            text.contains("mr chef salt") || text.contains("mr. chef salt") -> "Mr. Chef Salt"
            text.contains("royal salt") -> "Royal Salt"
            text.contains("salt") -> "Dangote Salt" // Context clue fallback
            
            // Sugar fuzzy mapping
            text.contains("dangote sugar") -> "Dangote Sugar"
            text.contains("st louis sugar") || text.contains("st. louis sugar") || text.contains("st louis") || text.contains("st. louis") -> "St. Louis Sugar"
            text.contains("golden penny sugar") -> "Golden Penny Sugar"
            text.contains("sugar") -> "Dangote Sugar" // Context clue fallback
            
            // Seasoning fuzzy mapping
            text.contains("maggi star") || text.contains("maggi cube") -> "Maggi Star"
            text.contains("maggi chicken") -> "Maggi Chicken"
            text.contains("maggi crayfish") -> "Maggi Crayfish"
            text.contains("knorr beef") -> "Knorr Beef"
            text.contains("knorr chicken") -> "Knorr Chicken"
            text.contains("royco") -> "Royco"
            text.contains("terra") -> "Terra"
            text.contains("kitchen glory") -> "Kitchen Glory"
            text.contains("chicken glory") -> "Chicken Glory"
            text.contains("maggi") || text.contains("seasoning") || text.contains("cube") -> "Maggi Star" // Context clue fallback
            
            // Noodles fuzzy mapping
            text.contains("indomie indomitables") -> "Indomie Indomitables"
            text.contains("super pack") || text.contains("superpack") -> "Indomie Super Pack"
            text.contains("onion flavour") || text.contains("onion flavor") -> "Indomie Onion Flavour"
            text.contains("hungry man") || text.contains("hungryman") -> "Indomie Hungry Man Size"
            text.contains("belle full") || text.contains("bellefull") -> "Indomie Belle Full"
            text.contains("golden penny noodles") -> "Golden Penny Noodles"
            text.contains("indomie") || text.contains("noodle") -> "Indomie Onion Flavour" // Context clue fallback

            // Otherwise check standard match list
            else -> VALID_COMMODITIES.find { comm ->
                text.contains(comm.lowercase()) ||
                comm.split(" ").any { part -> part.length > 3 && text.contains(part.lowercase()) }
            } ?: "Local Rice" // Fallback default
        }

        // 2. Find market location
        val market = when {
            text.contains("isale") || text.contains("eko") || text.contains("isale-eko") || text.contains("island") || text.contains("lekki") -> "Isale Eko (Island)"
            else -> "Mile 12 (Mainland)" // Standard default
        }

        // 3. Find price via regex
        val numbers = mutableListOf<Double>()
        val matches = Regex("[0-9,]+").findAll(text)
        for (m in matches) {
            val numStr = m.value.replace(",", "")
            if (numStr.isNotEmpty()) {
                val num = numStr.toDoubleOrNull() ?: 0.0
                if (num > 100) { // Filter out random tiny numbers like units, dates, codes
                    numbers.add(num)
                }
            }
        }

        val finalPrice = numbers.firstOrNull() ?: 65000.0 // Default dynamic seed range

        return ParsedUpdate(
            commodity = matchedComm,
            market = market,
            price = finalPrice,
            rawPhrase = "Extracted locally from: \"$message\""
        )
    }
}
