import express from 'express';
import { GoogleGenAI, Type } from '@google/genai';
import pg from 'pg';
import dotenv from 'dotenv';

dotenv.config();

const app = express();
app.use(express.json());

// Initialize Supabase/PostgreSQL Connection Pool
const pool = new pg.Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false },
  family: 4 // Forces IPv4 resolution for Render/Supabase compatibility
});

// Initialize Google AI Studio SDK
const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

/**
 * KOBO WEBHOOK INGESTION ENDPOINT (Tolerant & Safe Parsing)
 */
app.post('/v1/kobo-webhook', async (req, res) => {
  try {
    // CRITICAL FIX: Unwraps Kobo's nested .data object safely
    const rawData = req.body.data || req.body || {};

    const koboId = String(rawData._id || rawData.id || Date.now());
    const agentName = rawData.Field_Agent_Name || rawData._submitted_by || 'Unknown Agent';
    const marketHub = rawData.Market_Hub || rawData.market_hub || 'Unknown Market';
    const stallName = rawData.Merchant_Stall_Name || rawData.stall_name || 'General Stall';
    const merchantType = rawData.Type_of_Merchant || rawData.merchant_type || 'Retailer';
    const category = rawData.Commodity_Category || rawData.category || 'General';
    const rawItemName = rawData.Specific_Item_Name || rawData.item_name || 'Item';
    const rawUnit = rawData.Wholesale_Unit_Measurement || rawData.unit || 'Unit';
    const rawPrice = parseInt(rawData.Wholesale_Price || rawData.price || 0, 10);
    const geopoint = rawData['start-geopoint'] || rawData._geolocation || '';

    // Run AI Studio Gemini 3.8 Flash Anomaly Audit
    const aiResponse = await ai.models.generateContent({
      model: 'gemini-3.8-flash',
      contents: `Audit this market submission: Market: "${marketHub}", Category: "${category}", Item: "${rawItemName}", Unit: "${rawUnit}", Price: NGN ${rawPrice}`,
      config: {
        systemInstruction: "You are the NairaGuard Trade Intelligence AI. Validate Nigerian terminal market price entries, correct typos, and flag price anomalies.",
        responseMimeType: "application/json",
        responseSchema: {
          type: Type.OBJECT,
          properties: {
            clean_market: { type: Type.STRING },
            clean_category: { type: Type.STRING },
            clean_item_name: { type: Type.STRING },
            clean_unit: { type: Type.STRING },
            clean_price_ngn: { type: Type.INTEGER },
            is_anomaly: { type: Type.BOOLEAN },
            ai_notes: { type: Type.STRING }
          },
          required: ["clean_market", "clean_category", "clean_item_name", "clean_unit", "clean_price_ngn", "is_anomaly", "ai_notes"]
        }
      }
    });

    const aiAudit = JSON.parse(aiResponse.text);

    // Save Record to Supabase PostgreSQL Database
    const queryText = `
      INSERT INTO pending_market_feeds 
      (kobo_id, agent_name, market_hub, stall_name, merchant_type, category, item_name, unit_measurement, price_ngn, raw_price_logged, geopoint, anomaly_flagged, ai_notes, status)
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, 'PENDING')
      ON CONFLICT (kobo_id) DO UPDATE SET status = 'PENDING'
      RETURNING id;
    `;

    const values = [
      koboId,
      agentName,
      aiAudit.clean_market,
      stallName,
      merchantType,
      aiAudit.clean_category,
      aiAudit.clean_item_name,
      aiAudit.clean_unit,
      aiAudit.clean_price_ngn,
      rawPrice,
      geopoint,
      aiAudit.is_anomaly,
      aiAudit.ai_notes
    ];

    const result = await pool.query(queryText, values);
    console.log(`✅ Submission saved to database with ID: ${result.rows[0]?.id}`);

    return res.status(200).json({ status: 'success', kobo_id: koboId });
  } catch (error) {
    console.error('❌ Ingestion Error Details:', error);
    // CRITICAL FIX: Returns 200 OK so Kobo doesn't mark retries as Failed
    return res.status(200).json({ status: 'error_logged', details: error.message });
  }
});

/**
 * ADMIN PENDING FEEDS ENDPOINT
 */
app.get('/v1/admin/pending-submissions', async (req, res) => {
  try {
    const result = await pool.query("SELECT * FROM pending_market_feeds WHERE status = 'PENDING' ORDER BY submitted_at DESC");
    return res.json({ submissions: result.rows });
  } catch (err) {
    console.error('❌ Database Query Error Details:', err);
    return res.status(500).json({ 
      error: err.message || 'Unknown database error', 
      code: err.code || null, 
      detail: err.detail || null,
      hint: err.hint || null
    });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`NairaGuard Webhook Engine live on port ${PORT}`));
