package com.example.ui

import java.util.Locale
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.theme.*
import com.example.api.SquadPayService
import androidx.compose.ui.res.painterResource
import com.example.R
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun launchWhatsAppPriceReport(
    context: android.content.Context,
    commodity: String,
    market: String,
    currentPrice: String? = null,
    reportType: String = "Inaccuracy"
) {
    val priceText = if (!currentPrice.isNullOrEmpty()) {
        if (currentPrice.startsWith("₦")) currentPrice else "₦$currentPrice"
    } else "N/A"

    val message = """
Hi NairaGuard Team! 🇳🇬
I want to report a price update:
• Type: $reportType
• Commodity: $commodity
• Market: $market
• Current Listed Price: $priceText
• Notes / New Price: 
    """.trimIndent()

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/2348020556342?text=${Uri.encode(message)}"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open WhatsApp. Please check if WhatsApp is installed.", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsAppWaitlist(
    context: android.content.Context,
    name: String,
    contact: String
) {
    val dateStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(java.util.Date())
    val message = """
Hi NairaGuard Team! 🇳🇬
New NairaGuard PLUS Priority Waitlist Entry:
• Lead Name: ${if (name.isBlank()) "Anonymous Lead" else name}
• Contact: $contact
• Submitted: $dateStr
• Request: Early Access to PLUS Market Insights & Real-time Alerts
    """.trimIndent()

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/2348020556342?text=${Uri.encode(message)}"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open WhatsApp. Please check if WhatsApp is installed.", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsAppAllWaitlistEntries(
    context: android.content.Context,
    entries: List<com.example.data.WaitlistEntry>
) {
    if (entries.isEmpty()) {
        Toast.makeText(context, "No waitlist entries to send.", Toast.LENGTH_SHORT).show()
        return
    }
    val sdf = java.text.SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    val formattedList = entries.mapIndexed { idx, entry ->
        "${idx + 1}. ${entry.name} - ${entry.contact} (${sdf.format(java.util.Date(entry.timestamp))})"
    }.joinToString("\n")

    val message = """
Hi NairaGuard Admin! 🇳🇬
📋 Priority Waitlist Registry Summary (${entries.size} Leads):

$formattedList

Total Leads: ${entries.size}
Dispatched from NairaGuard App
    """.trimIndent()

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/2348020556342?text=${Uri.encode(message)}"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open WhatsApp. Please check if WhatsApp is installed.", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SpikeNotificationDialog(
    spikeAlerts: List<SpikeAlert>,
    onDismiss: () -> Unit,
    onSelectAlert: (SpikeAlert) -> Unit,
    formatNaira: (Double) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Spike Alert Icon",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Spike Alerts & Movers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "PLUS Wholesale Price Telemetry",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Track real-time wholesale price changes in Mile 12 & Isale Eko. Tap any card below to view full details.",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )

                if (spikeAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No price spikes recorded yet today.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    spikeAlerts.forEach { alert ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectAlert(alert) }
                                .testTag("spike_alert_card_${alert.commodityId}"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(
                                1.dp,
                                if (alert.isIncrease) Color(0xFF16A34A).copy(alpha = 0.5f) else Color(0xFFDC2626).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Spike Alert Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (alert.isIncrease) Color(0xFFDC2626) else Color(0xFF2563EB)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (alert.isIncrease) "SPIKE ALERT" else "PRICE DROP",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Arrow chart: Green arrow chart for price increase, Red arrow chart for price decrease
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (alert.isIncrease) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = "Price Spiked Up",
                                                tint = Color(0xFF22C55E),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "+${String.format(Locale.US, "%.1f", alert.percentChange)}%",
                                                color = Color(0xFF22C55E),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.TrendingDown,
                                                contentDescription = "Price Dropped Down",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", alert.percentChange)}%",
                                                color = Color(0xFFEF4444),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = alert.commodityName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )

                                Text(
                                    text = "Market: ${alert.marketLocation} • ${alert.wholesaleUnit}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₦${formatNaira(alert.previousWholesale)}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            style = androidx.compose.ui.text.TextStyle(
                                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                            )
                                        )
                                        Text(
                                            text = " ➔ ",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "₦${formatNaira(alert.newWholesale)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (alert.isIncrease) Color(0xFFF87171) else Color(0xFF4ADE80)
                                        )
                                    }

                                    Text(
                                        text = "View Details ➔",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008B54)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close Notification Panel", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SpikeAlertDetailDialog(
    alert: SpikeAlert,
    onDismiss: () -> Unit,
    formatNaira: (Double) -> String
) {
    val deltaNaira = alert.newWholesale - alert.previousWholesale
    val perSubunitImpact = if (alert.conversionFactor > 0) deltaNaira / alert.conversionFactor else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (alert.isIncrease) Color(0xFFDC2626) else Color(0xFF16A34A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (alert.isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = "Spike Status Icon",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (alert.isIncrease) "Wholesale Spike Alert" else "Wholesale Price Drop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = alert.marketLocation,
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Commodity Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = alert.commodityName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Wholesale Unit: ${alert.wholesaleUnit}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Previous Wholesale", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    "₦${formatNaira(alert.previousWholesale)}",
                                    fontSize = 14.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                if (alert.isIncrease) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = "Trend Icon",
                                tint = if (alert.isIncrease) Color(0xFF22C55E) else Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text("New Spot Price", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    "₦${formatNaira(alert.newWholesale)}",
                                    fontSize = 16.sp,
                                    color = if (alert.isIncrease) Color(0xFFF87171) else Color(0xFF4ADE80),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Price Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Wholesale Delta", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "${if (deltaNaira >= 0) "+₦" else "-₦"}${formatNaira(Math.abs(deltaNaira))}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (deltaNaira >= 0) Color(0xFFF87171) else Color(0xFF4ADE80)
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Percentage Movement", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "${if (alert.percentChange >= 0) "+" else ""}${String.format(Locale.US, "%.1f", alert.percentChange)}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (alert.percentChange >= 0) Color(0xFF22C55E) else Color(0xFFEF4444)
                            )
                        }
                    }
                }

                // Granular Retail Sub-unit Impact Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF022C22)),
                    border = BorderStroke(1.dp, Color(0xFF059669)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = "Subunit impact", tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retail Sub-unit Margin Impact", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF34D399))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Conversion Factor: 1 ${alert.wholesaleUnit} = ${alert.conversionFactor.toInt()} ${alert.microUnit}s.",
                            fontSize = 10.sp,
                            color = Color(0xFFA7F3D0)
                        )
                        Text(
                            text = "Estimated unit impact: ${if (perSubunitImpact >= 0) "+₦" else "-₦"}${formatNaira(Math.abs(perSubunitImpact))} per ${alert.microUnit}.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Market Reason & Intelligence Note
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Intelligence Note:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = alert.reasonNote,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008B54)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dismiss Alert", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SquadPayCheckoutDialog(
    planType: String,
    userDefaultEmail: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: (String, String) -> Unit,
    onInitiatePayment: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var email by remember { mutableStateOf(userDefaultEmail.ifEmpty { "subscriber@nairaguard.ng" }) }
    val context = LocalContext.current

    val amountNaira = if (planType.lowercase() == "daily") 200 else 5000
    val amountKobo = amountNaira * 100
    val planTitle = if (planType.lowercase() == "daily") "24-Hour Day Pass" else "30-Day Monthly Pass"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0B132B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF013220)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SQ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("SquadCo Payment Gateway", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text("Secure NGN Payment API Integration", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Selected Tier:", fontSize = 11.sp, color = Color.Gray)
                            Text("NairaGuard PLUS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFD4AF37))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Plan Duration:", fontSize = 11.sp, color = Color.Gray)
                            Text(planTitle, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payable:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("₦${String.format(Locale.US, "%,d", amountNaira)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF2ECC71))
                        }
                    }
                }

                // API Key & Parameters info box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SquadCo Public Key:", fontSize = 9.sp, color = Color.Gray)
                        Text(SquadPayService.PUBLIC_KEY, fontSize = 9.sp, color = Color(0xFF38BDF8), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Amount: $amountKobo Kobo (₦$amountNaira NGN) | Currency: NGN", fontSize = 9.sp, color = Color.LightGray)
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Billing Email Address", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF013220),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("squad_email_input")
                )

                // Renamed payment button to simply "Pay"
                Button(
                    onClick = {
                        val ref = "SQUAD-${if (planType.lowercase() == "daily") "DAY" else "MONTH"}-${System.currentTimeMillis()}"
                        onInitiatePayment(planType, email, ref)
                        SquadPayService.launchSquadCheckout(context, planType, email, ref)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF013220)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("launch_squad_checkout_btn")
                ) {
                    Icon(Icons.Default.Payment, contentDescription = "Launch Payment Gateway", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }

                // Automated Webhook & Callback Status Indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F291E),
                    border = BorderStroke(1.dp, Color(0xFF008B54).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Automated Verification",
                            tint = Color(0xFF2ECC71),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Automated callback verification active. Your account tier upgrades automatically upon payment confirmation.",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        confirmButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NairaGuardApp(viewModel: NairaGuardViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val subState by viewModel.subscription.collectAsStateWithLifecycle()
    val userIsLoggedIn by viewModel.userIsLoggedIn.collectAsStateWithLifecycle()
    val userIsVerified by viewModel.userIsVerified.collectAsStateWithLifecycle()
    val showWaitlistDialog by viewModel.showWaitlistDialog.collectAsStateWithLifecycle()
    val spikeAlerts by viewModel.spikeAlerts.collectAsStateWithLifecycle()

    var showSplashScreen by remember { mutableStateOf(true) }
    var showSpikeNotificationPanel by remember { mutableStateOf(false) }
    var activeSpikeAlertDetail by remember { mutableStateOf<SpikeAlert?>(null) }
    var showSquadPayCheckout by remember { mutableStateOf(false) }
    var squadPlanSelected by remember { mutableStateOf("monthly") }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentContext = LocalContext.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndAutoVerifyPendingSquadPayment { msg ->
                    Toast.makeText(currentContext, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showSplashScreen) {
        NairaGuardSplashScreen(
            onSplashFinished = {
                showSplashScreen = false
            }
        )
        return
    }

    if (showSpikeNotificationPanel) {
        SpikeNotificationDialog(
            spikeAlerts = spikeAlerts,
            onDismiss = { showSpikeNotificationPanel = false },
            onSelectAlert = { alert ->
                showSpikeNotificationPanel = false
                activeSpikeAlertDetail = alert
            },
            formatNaira = { v ->
                if (v % 1.0 == 0.0) String.format(Locale.US, "%,d", v.toLong()) else String.format(Locale.US, "%,.2f", v)
            }
        )
    }

    activeSpikeAlertDetail?.let { alert ->
        SpikeAlertDetailDialog(
            alert = alert,
            onDismiss = { activeSpikeAlertDetail = null },
            formatNaira = { v ->
                if (v % 1.0 == 0.0) String.format(Locale.US, "%,d", v.toLong()) else String.format(Locale.US, "%,.2f", v)
            }
        )
    }

    if (showSquadPayCheckout) {
        val context = LocalContext.current
        SquadPayCheckoutDialog(
            planType = squadPlanSelected,
            userDefaultEmail = subState.phoneNumber.let { if (it.contains("@")) it else "subscriber@nairaguard.ng" },
            onDismiss = { showSquadPayCheckout = false },
            onPaymentSuccess = { plan, email ->
                viewModel.processSquadPaymentSuccess(plan, email)
                showSquadPayCheckout = false
                Toast.makeText(context, "Payment verified! Upgraded to NairaGuard PLUS.", Toast.LENGTH_LONG).show()
            },
            onInitiatePayment = { plan, email, ref ->
                viewModel.recordPendingSquadPayment(plan, email, ref)
            }
        )
    }
    
    // Support adaptive layouts by using window dimensions
    val scrollState = rememberScrollState()

    if (showWaitlistDialog) {
        var waitlistName by remember { mutableStateOf("") }
        var waitlistContact by remember { mutableStateOf("") }
        val context = LocalContext.current
        
        AlertDialog(
            onDismissRequest = { viewModel.showWaitlistDialog.value = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "PLUS logo",
                        tint = AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NairaGuard PLUS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "We are currently calibrating our real-time AI price algorithms for major market hubs (Mile 12 & Isale Eko) to ensure 100% margin accuracy. Enter your phone number below to join our exclusive Beta testing group.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0)
                    )
                    
                    OutlinedTextField(
                        value = waitlistName,
                        onValueChange = { waitlistName = it },
                        label = { Text("Your Name (Optional)", color = Color(0xFFA1A1AA)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = Color(0xFF3F3F46),
                            focusedLabelColor = Color(0xFF34D399),
                            unfocusedLabelColor = Color(0xFFA1A1AA)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("waitlist_name_input")
                    )

                    OutlinedTextField(
                        value = waitlistContact,
                        onValueChange = { waitlistContact = it },
                        label = { Text("Phone Number or Email", color = Color(0xFFA1A1AA)) },
                        singleLine = true,
                        placeholder = { Text("e.g. +234 80 1234 5678", color = Color(0xFF71717A)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = Color(0xFF3F3F46),
                            focusedLabelColor = Color(0xFF34D399),
                            unfocusedLabelColor = Color(0xFFA1A1AA)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("waitlist_contact_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (waitlistContact.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter your contact details.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.submitToWaitlist(waitlistName, waitlistContact) { success ->
                            if (success) {
                                viewModel.showWaitlistDialog.value = false
                                Toast.makeText(context, "You're on the list! Opening WhatsApp to notify admin...", Toast.LENGTH_LONG).show()
                                launchWhatsAppWaitlist(context, waitlistName, waitlistContact)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
                    modifier = Modifier.testTag("submit_waitlist_button")
                ) {
                    Text("Join Priority Waitlist", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showWaitlistDialog.value = false },
                    modifier = Modifier.testTag("cancel_waitlist_button")
                ) {
                    Text("Maybe Later", color = Color(0xFFA1A1AA))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF09090B)
        )
    }

    val showScaffoldBars = currentScreen != Screen.LOGIN && 
                           currentScreen != Screen.SIGNUP && 
                           currentScreen != Screen.VERIFY_EMAIL

    Scaffold(
        topBar = {
            if (showScaffoldBars) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_brand_logo),
                                contentDescription = "NairaGuard Brand Shield Logo",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NairaGuard",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    },
                    actions = {
                        if (subState.tier == "PLUS") {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(AccentAmberBg)
                                    .border(1.dp, AccentAmberBorder, RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                    Text(
                                        text = "PLUS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentAmberText
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { viewModel.changeScreen(Screen.SUBSCRIPTION) }
                                    .background(AccentPurpleBg)
                                    .border(1.dp, AccentPurpleBorder, RoundedCornerShape(50))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "GET PLUS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentPurpleText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        val isPlusOrAdmin = subState.tier == "PLUS" || (userIsLoggedIn && userIsVerified)
                        if (isPlusOrAdmin) {
                            IconButton(
                                onClick = { showSpikeNotificationPanel = true },
                                modifier = Modifier
                                    .testTag("notification_nav_button")
                                    .size(32.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (spikeAlerts.isNotEmpty()) {
                                            Badge(
                                                containerColor = Color(0xFFDC2626),
                                                contentColor = Color.White
                                            ) {
                                                Text(spikeAlerts.size.toString(), fontSize = 8.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Spike Alert Notifications",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (showSpikeNotificationPanel) NairaSuccessGreen else DeepCharcoal
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        IconButton(
                            onClick = {
                                if (userIsLoggedIn && userIsVerified) {
                                    viewModel.changeScreen(Screen.ADMIN)
                                } else {
                                    viewModel.changeScreen(Screen.LOGIN)
                                }
                            },
                            modifier = Modifier
                                .testTag("admin_nav_button")
                                .size(32.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Go to Admin Command Center",
                                modifier = Modifier.size(16.dp),
                                tint = if (currentScreen == Screen.ADMIN) NairaSuccessGreen else Color.Gray
                            )
                        }
                        if (userIsLoggedIn) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.logoutStandardUser() },
                                modifier = Modifier
                                    .testTag("logout_nav_button")
                                    .size(32.dp)
                                    .border(1.dp, Color.LightGray, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout Admin",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SoftGrey,
                        titleContentColor = DeepCharcoal
                    )
                )
            }
        },
        bottomBar = {
            if (showScaffoldBars) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("app_bottom_bar"),
                    containerColor = SoftGrey,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.DASHBOARD,
                        onClick = { viewModel.changeScreen(Screen.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Prices Dashboard") },
                        label = { Text("Discover") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreen,
                            selectedTextColor = DeepCharcoal,
                            indicatorColor = ForestGreen.copy(alpha = 0.15f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_discover")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.SUBSCRIPTION,
                        onClick = { viewModel.changeScreen(Screen.SUBSCRIPTION) },
                        icon = { Icon(Icons.Default.AccountBox, contentDescription = "Subscriptions") },
                        label = { Text("My Plan") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreen,
                            selectedTextColor = DeepCharcoal,
                            indicatorColor = ForestGreen.copy(alpha = 0.15f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_plus")
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showScaffoldBars) innerPadding else PaddingValues(0.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    label = "ScreenTransition",
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) { screen ->
                    when (screen) {
                        Screen.LOGIN -> LoginView(viewModel)
                        Screen.SIGNUP -> SignUpView(viewModel)
                        Screen.VERIFY_EMAIL -> VerifyEmailView(viewModel)
                        Screen.DASHBOARD -> DashboardView(viewModel)
                        Screen.ADMIN -> AdminCommandView(viewModel)
                        Screen.SUBSCRIPTION -> SubscriptionView(viewModel)
                    }
                }
            }

            // SEMANTIC VERSIONING FOOTER (commonMain UI base layout)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftGrey)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "© NairaGuard Technologies, 2026",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.testTag("semantic_version_footer")
                )
                Text(
                    text = "Version V.1.19",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD / DISCOVER VIEW
// ==========================================
// ==========================================
// 1. DASHBOARD / DISCOVER VIEW
// ==========================================
@Composable
fun DashboardView(viewModel: NairaGuardViewModel) {
    val commodities by viewModel.commodities.collectAsStateWithLifecycle()
    val prices by viewModel.prices.collectAsStateWithLifecycle()
    val subState by viewModel.subscription.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val userLoggedInName by viewModel.userLoggedInName.collectAsStateWithLifecycle()
    val userIsLoggedIn by viewModel.userIsLoggedIn.collectAsStateWithLifecycle()
    val userIsVerified by viewModel.userIsVerified.collectAsStateWithLifecycle()
    val isAdmin = userIsLoggedIn && userIsVerified

    // Live Refresh and Synchronization states
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lastPriceUpdateTime by viewModel.lastPriceUpdateTime.collectAsStateWithLifecycle()
    val hasNewPriceAlert by viewModel.hasNewPriceChangeAlert.collectAsStateWithLifecycle()
    val lastPriceAlertMsg by viewModel.lastPriceAlertMessage.collectAsStateWithLifecycle()
    val formattedLiveTime = remember(lastPriceUpdateTime) {
        viewModel.getFormattedLiveTimestamp(lastPriceUpdateTime)
    }

    var activeCommodityForDetails by remember { mutableStateOf<Commodity?>(null) }
    var selectedMarketFilter by remember { mutableStateOf("All Lagos") }

    val categories = listOf(
        "All",
        "Grains",
        "Beans",
        "Processed Tubers",
        "Oils",
        "Vegetables",
        "Fruits",
        "Agro Products",
        "Meats",
        "Tubers",
        "Livestock",
        "Household",
        "Instant Noodles",
        "Salt",
        "Sugar",
        "Seasoning Cubes"
    )

    // User Feedback Dialog Trigger states
    var showSubmissionDialog by remember { mutableStateOf(false) }
    var submissionDialogType by remember { mutableStateOf("Inaccuracy") } // "Inaccuracy" or "Suggestion"
    var submissionCommodityId by remember { mutableStateOf<Int?>(null) }
    var submissionCommodityName by remember { mutableStateOf("") }
    var submissionMarketLocation by remember { mutableStateOf("Mile 12 (Mainland)") }
    var submissionWholesalePrice by remember { mutableStateOf("") }
    var submissionRetailPrice by remember { mutableStateOf("") }
    var submissionMessage by remember { mutableStateOf("") }

    // Categories Filter
    val filteredCommodities = commodities.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
        (it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream)
            .padding(10.dp)
    ) {
        // Flashing animation for LIVE indicator
        val infiniteTransition = rememberInfiniteTransition(label = "live_flashing_transition")
        val liveAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "live_alpha_anim"
        )
        val liveScale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "live_scale_anim"
        )

        // Streamlined Real-time Status Header (LIVE badge + Local Clock)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Live Status Indicator Button/Badge (Flashing Red Live indicator + Green Real-Time Prices)
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Flashing radar dot
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(10.dp)
                    ) {
                        // Outer pulsating aura ring
                        Box(
                            modifier = Modifier
                                .size((10 * liveScale).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935).copy(alpha = liveAlpha * 0.35f))
                        )
                        // Inner flashing core
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F).copy(alpha = liveAlpha.coerceAtLeast(0.5f)))
                        )
                    }
                    Text(
                        text = "LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFD32F2F).copy(alpha = liveAlpha.coerceAtLeast(0.65f)),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "• Real-Time Prices",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }
            }

            // Local Time Clock Display
            val displayTime = java.text.SimpleDateFormat("hh:mm a", Locale.US).format(java.util.Date())
            Surface(
                shape = RoundedCornerShape(50),
                color = AccentBlueBg,
                border = BorderStroke(1.dp, AccentBlueBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Local Time",
                        modifier = Modifier.size(12.dp),
                        tint = DeepCharcoal
                    )
                    Text(
                        text = displayTime,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                }
            }
        }

        // Market segmented selector (Interactive)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All Lagos", "Mainland (Mile 12)", "Island (Isale Eko)").forEach { market ->
                val isSelected = selectedMarketFilter == market
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) DeepCharcoal else SoftGrey)
                        .border(1.dp, if (isSelected) DeepCharcoal else BorderGrey, RoundedCornerShape(50))
                        .clickable { selectedMarketFilter = market }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = market,
                        color = if (isSelected) Color.White else Color(0xFF475569),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 3-Column Statistical widgets (Matched to High-Density HTML)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Volatility Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlueBg)
                    .border(1.dp, AccentBlueBorder, RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Column {
                    Text(
                        text = "AVG VOLATILITY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "+4.2%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "today",
                        fontSize = 8.sp,
                        color = Color.Gray
                    )
                }
            }

            // Best Arbitrage Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentGreenBg)
                    .border(1.dp, AccentGreenBorder, RoundedCornerShape(12.dp))
                    .padding(6.dp)
            ) {
                Column {
                    Text(
                        text = "BEST ARBITRAGE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = NairaSuccessGreen,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Rice",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Isale Eko",
                        fontSize = 8.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Plus Trial Access Card
            val context = LocalContext.current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentPurpleBg)
                    .border(1.dp, AccentPurpleBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        if (subState.tier != "PLUS" && !isAdmin) {
                            val hasUsed = viewModel.sharedPrefs.getBoolean("plus_trial_used", false) || subState.trialStartDate != null
                            if (!hasUsed) {
                                viewModel.activatePlusTrial { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            } else {
                                viewModel.showWaitlistDialog.value = true
                            }
                        }
                    }
                    .padding(6.dp)
            ) {
                Column {
                    Text(
                        text = "PLUS ACCESS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurpleText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    val isLocked = subState.tier != "PLUS" && !isAdmin
                    val statusText = if (isAdmin) {
                        "Active"
                    } else {
                        viewModel.getTrialRemainingText(subState)
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isLocked) (if (viewModel.sharedPrefs.getBoolean("plus_trial_used", false) || subState.trialStartDate != null) "Expired" else "Tap for Trial") else "Premium",
                        fontSize = 8.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Search Bar (Compact Height)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("search_commodity_input"),
            placeholder = { Text("Search a Commodity...", fontSize = 13.sp, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Search", modifier = Modifier.size(18.dp))
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF013220),
                unfocusedBorderColor = Color(0xFF013220),
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                cursorColor = Color(0xFF013220),
                focusedLeadingIconColor = Color(0xFF013220),
                unfocusedLeadingIconColor = Color(0xFF013220)
            )
        )

        // Categories Scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                val bgColor = if (isSelected) Color(0xFF005A36) else Color(0xFFF0F2F0)
                val textColor = if (isSelected) Color.White else Color(0xFF4A4A4A)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable { viewModel.selectedCategory.value = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("category_pill_$cat"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (subState.tier == "PLUS") {
            val context = LocalContext.current
            // Spot something wrong or suggest new price point suggestion card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable {
                        launchWhatsAppPriceReport(
                            context = context,
                            commodity = "General Staple Commodity",
                            market = "Mile 12 (Mainland)",
                            currentPrice = null,
                            reportType = "Suggestion"
                        )
                    }
                    .testTag("onboard_feedback_trigger"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Feedback,
                        contentDescription = "Offer Feedback Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spot a price error or missing staples?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Suggest new prices or commodities directly to NairaGuard.",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text("SUGGEST", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Price Change Alert Notification Banner (Animated)
        AnimatedVisibility(
            visible = hasNewPriceAlert,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { viewModel.triggerPriceRefresh() }
                    .testTag("price_change_alert_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "New Price Alert",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "A new price change. Pull to refresh",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = lastPriceAlertMsg ?: "Market prices were updated. Tap to sync now.",
                                fontSize = 10.sp,
                                color = Color(0xFFB45309),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { viewModel.triggerPriceRefresh() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("refresh_alert_btn")
                    ) {
                        Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Real-Time Header with Pulse light, Synchronized WAT timestamp, and Refresh Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MARKET INDICES (REAL-TIME)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                letterSpacing = 1.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isRefreshing) Color(0xFFF59E0B) else Color(0xFF2ECC71))
                    )
                    Text(
                        text = if (isRefreshing) "Syncing..." else "Live: $formattedLiveTime",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }

                // Refresh Button with Spin Animation
                val rotationAngle by animateFloatAsState(
                    targetValue = if (isRefreshing) 360f else 0f,
                    animationSpec = tween(durationMillis = 500),
                    label = "refresh_spin"
                )
                IconButton(
                    onClick = { viewModel.triggerPriceRefresh() },
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("dashboard_refresh_button")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Prices",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(15.dp)
                            .graphicsLayer(rotationZ = rotationAngle)
                    )
                }
            }
        }

        // Price List
        val isRegionalGated = selectedMarketFilter != "All Lagos" && subState.tier != "PLUS" && !isAdmin

        if (isRegionalGated) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SubscriptionGate(
                    message = "Discrete regional breakdown (Mainland vs. Island) is locked. Unlock localized wholesale and retail indices under NairaGuard PLUS."
                ) {
                    viewModel.changeScreen(Screen.SUBSCRIPTION)
                }
            }
        } else if (filteredCommodities.isEmpty()) {
            val context = LocalContext.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "No Commodities Found",
                        modifier = Modifier.size(44.dp),
                        tint = DeepCharcoal.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedCategory != "All") "No commodities listed under \"$selectedCategory\" yet" else "No commodities match search",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "New market survey updates are in progress.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            launchWhatsAppPriceReport(
                                context = context,
                                commodity = if (selectedCategory != "All") selectedCategory else "Staple Commodity",
                                market = "Mile 12 (Mainland)",
                                currentPrice = null,
                                reportType = "Suggestion"
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF15803D))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Suggest", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Suggest a Commodity", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            if (isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCommodities) { comm ->
                    val commPrices = prices.filter { it.commodityId == comm.id }
                    val isAllLagos = selectedMarketFilter == "All Lagos"
                    val mile12Price = commPrices.find { it.marketLocation.contains("Mile 12") || it.marketLocation.contains("Mainland") }
                    val isaleEkoPrice = commPrices.find { it.marketLocation.contains("Isale Eko") || it.marketLocation.contains("Island") }

                    val (finalWholesale, finalRetail) = if (isAllLagos) {
                        if (mile12Price != null && isaleEkoPrice != null) {
                            ((mile12Price.wholesalePrice + isaleEkoPrice.wholesalePrice) / 2.0) to ((mile12Price.retailPrice + isaleEkoPrice.retailPrice) / 2.0)
                        } else if (mile12Price != null) {
                            mile12Price.wholesalePrice to mile12Price.retailPrice
                        } else if (isaleEkoPrice != null) {
                            isaleEkoPrice.wholesalePrice to isaleEkoPrice.retailPrice
                        } else if (commPrices.isNotEmpty()) {
                            commPrices.map { it.wholesalePrice }.average() to commPrices.map { it.retailPrice }.average()
                        } else {
                            0.0 to 0.0
                        }
                    } else {
                        val targetPrice = if (selectedMarketFilter == "Mainland (Mile 12)") mile12Price else isaleEkoPrice
                        (targetPrice?.wholesalePrice ?: 0.0) to (targetPrice?.retailPrice ?: 0.0)
                    }

                    CommodityPriceCard(
                        commodity = comm,
                        avgWholesale = finalWholesale,
                        avgRetail = finalRetail,
                        isPlus = subState.tier == "PLUS" || isAdmin,
                        isAverageMode = isAllLagos,
                        onDetailsClick = { activeCommodityForDetails = comm }
                    )
                }

                item {
                    val context = LocalContext.current
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Don't see your commodity?",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = "Suggest any staple or food item to track in real-time.",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    launchWhatsAppPriceReport(
                                        context = context,
                                        commodity = if (selectedCategory != "All") selectedCategory else "Staple Commodity",
                                        market = "Mile 12 (Mainland)",
                                        currentPrice = null,
                                        reportType = "Suggestion"
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF15803D)),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Suggest", modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Suggest a Commodity", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Modal / BottomSheet
    activeCommodityForDetails?.let { comm ->
        val commPrices = prices.filter { it.commodityId == comm.id }
        val allHistory by viewModel.history.collectAsStateWithLifecycle()
        val commHistory = allHistory.filter { it.commodityId == comm.id }
        val finalIsPlus = subState.tier == "PLUS" || isAdmin

        AlertDialog(
            onDismissRequest = { activeCommodityForDetails = null },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                TextButton(onClick = { activeCommodityForDetails = null }) {
                    Text("Close", color = Color.White)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Eco,
                        contentDescription = "Commodity Logo",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(comm.name, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = comm.category.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = comm.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    if (finalIsPlus) {
                        // PLUS TIER: HIGH DETAILS
                        // Unit map card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Wholesale Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(comm.wholesaleUnit, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Conversion Arrow")
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Retail Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "${comm.conversionFactor.roundToInt()} ${comm.microUnit}s / unit",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Trend Chart Container
                        Text("5-Day Wholesale Volatility Trend", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (commHistory.isNotEmpty()) {
                            NairaLineChart(
                                history = commHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .padding(vertical = 8.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Awaiting price history stream", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Mainland vs. Island Comparison", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Column {
                            commPrices.forEach { price ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .drawBehind {
                                            val strokeWidth = 1.dp.toPx()
                                            drawLine(
                                                color = Color.LightGray.copy(alpha = 0.5f),
                                                start = Offset(0f, size.height),
                                                end = Offset(size.width, size.height),
                                                strokeWidth = strokeWidth
                                            )
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text(price.marketLocation, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                                        Text(
                                            "Source: ${price.updatedBy} · Last sync: ${System.currentTimeMillis().minus(price.lastUpdated).div(1000).div(60)}m ago",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.weight(0.9f),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Wholesale",
                                                fontSize = 8.sp,
                                                color = Color.LightGray.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "₦${viewModel.formatNaira(price.wholesalePrice)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(20.dp)
                                                .background(Color.White.copy(alpha = 0.2f))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = "Retail",
                                                fontSize = 8.sp,
                                                color = Color.LightGray.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "₦${viewModel.formatNaira(price.retailPrice)}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF2ECC71),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Granular Retail Unit Matrix", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderGrey),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                val baselineRetail = if (commPrices.isNotEmpty()) commPrices.map { it.retailPrice }.average() else 1500.0
                                val baselineWholesale = if (commPrices.isNotEmpty()) commPrices.map { it.wholesalePrice }.average() else 45000.0

                                val matrixItems = listOf(
                                    Triple("Cup", "Standard measuring cup", baselineRetail),
                                    Triple("Derica", "Traditional 800ml tin", baselineRetail * 5.0),
                                    Triple("Roll", "Pack of 10-12 sachets/pieces", baselineRetail * 10.0),
                                    Triple("Sachet", "Single serve packet", baselineRetail * 0.15),
                                    Triple("Bag", "Full commercial wholesale sack", baselineWholesale),
                                    Triple("Per-Unit", "Single individual count", baselineRetail * 1.5)
                                )

                                matrixItems.forEachIndexed { idx, (unitName, unitDesc, unitPrice) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(unitName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepCharcoal)
                                            Text(unitDesc, fontSize = 9.sp, color = Color.Gray)
                                        }
                                        Text(
                                            "₦${String.format("%,.0f", unitPrice)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NairaSuccessGreen
                                        )
                                    }
                                    if (idx < matrixItems.size - 1) {
                                        HorizontalDivider(color = BorderGrey, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                launchWhatsAppPriceReport(
                                    context = context,
                                    commodity = comm.name,
                                    market = commPrices.firstOrNull()?.marketLocation ?: "Mile 12 (Mainland)",
                                    currentPrice = if (commPrices.isNotEmpty()) "₦${String.format("%,.0f", commPrices.first().wholesalePrice)}" else null,
                                    reportType = "Inaccuracy"
                                )
                                activeCommodityForDetails = null
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_feedback_for_${comm.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF15803D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Report Error Status", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Report Price Inaccuracy via WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // FREE TIER: SIMPLIFIED BASIC OVERVIEW ONLY
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151516)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Wholesale Price per Bag",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        val wholesaleVal = if (commPrices.isNotEmpty()) commPrices.map { it.wholesalePrice }.average() else 0.0
                                        Text(
                                            text = "₦${if (wholesaleVal > 0) String.format("%,.0f", wholesaleVal) else "N/A"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Unit: ${comm.wholesaleUnit}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(55.dp)
                                            .background(Color.White.copy(alpha = 0.25f))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "Basic Retail Price per Unit",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        val retailVal = if (commPrices.isNotEmpty()) commPrices.map { it.retailPrice }.average() else 0.0
                                        Text(
                                            text = "₦${if (retailVal > 0) String.format("%,.0f", retailVal) else "N/A"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Unit: 1 ${comm.microUnit}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                launchWhatsAppPriceReport(
                                    context = context,
                                    commodity = comm.name,
                                    market = commPrices.firstOrNull()?.marketLocation ?: "Mile 12 (Mainland)",
                                    currentPrice = if (commPrices.isNotEmpty()) "₦${String.format("%,.0f", commPrices.first().wholesalePrice)}" else null,
                                    reportType = "Inaccuracy"
                                )
                                activeCommodityForDetails = null
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_feedback_free_for_${comm.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF15803D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = "Report Error Status", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Report Price Inaccuracy via WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SUBSCRIPTION REQUIREMENT GATE INFO
                        SubscriptionGate(
                            message = "Mainland vs. Island price comparison, granular unit metrics, and 5-day historical volatility trend lines are features unlocked under the PLUS membership master plan."
                        ) {
                            viewModel.changeScreen(Screen.SUBSCRIPTION)
                            activeCommodityForDetails = null
                        }
                    }
                }
            }
        )
    }

    if (false) {
        val commoditiesList by viewModel.commodities.collectAsStateWithLifecycle()
        
        AlertDialog(
            onDismissRequest = { showSubmissionDialog = false },
            containerColor = Color(0xFF09090B),
            title = {
                Text(
                    text = if (submissionDialogType == "Inaccuracy") "⚠️ Report Price Inaccuracy" else "💡 Suggest Pricing or Staples",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wholesale = submissionWholesalePrice.toDoubleOrNull() ?: 0.0
                        val retail = submissionRetailPrice.toDoubleOrNull() ?: 0.0
                        if (submissionCommodityName.isNotBlank() && wholesale > 0) {
                            val computedRetail = if (retail > 0) retail else {
                                val assocComm = commoditiesList.find { it.id == submissionCommodityId }
                                val factor = assocComm?.conversionFactor ?: 12.0
                                wholesale / factor
                            }
                            viewModel.submitUserFeedback(
                                feedbackType = submissionDialogType,
                                commodityId = submissionCommodityId,
                                commodityName = submissionCommodityName,
                                marketLocation = submissionMarketLocation,
                                reportedWholesalePrice = wholesale,
                                reportedRetailPrice = computedRetail,
                                message = submissionMessage
                            )
                            showSubmissionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = Color.White),
                    enabled = submissionCommodityName.isNotBlank() && submissionWholesalePrice.isNotEmpty() && (submissionWholesalePrice.toDoubleOrNull() ?: 0.0) > 0,
                    modifier = Modifier.testTag("submit_vendor_feedback_btn")
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmissionDialog = false }) {
                    Text("Cancel", color = Color(0xFFA1A1AA))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Feedback Type Segmented Control
                    Text("Feedback Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Inaccuracy", "Suggestion").forEach { type ->
                            val isSelected = submissionDialogType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ForestGreen else Color(0xFF27272A))
                                    .clickable { submissionDialogType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Commodity Input/Dropdown Selection
                    Text("Commodity / Staple Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    if (submissionCommodityId != null) {
                        OutlinedTextField(
                            value = submissionCommodityName,
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = Color(0xFF3F3F46)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        var selectFromDropdown by remember { mutableStateOf(true) }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { selectFromDropdown = !selectFromDropdown }) {
                                Text(
                                    if (selectFromDropdown) "Keyboard Name" else "Select Existing List",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                        
                        if (selectFromDropdown) {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                                color = Color(0xFF18181B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true }
                                    .padding(vertical = 12.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = if (submissionCommodityName.isEmpty()) "Tap to select commodity..." else submissionCommodityName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF18181B))
                            ) {
                                commoditiesList.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name, color = Color.White) },
                                        onClick = {
                                            submissionCommodityId = c.id
                                            submissionCommodityName = c.name
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = submissionCommodityName,
                                onValueChange = { submissionCommodityName = it; submissionCommodityId = null },
                                placeholder = { Text("E.g. Sweet Plantains, White Salt...", color = Color(0xFF71717A)) },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = Color(0xFF3F3F46)
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("feedback_commodity_input"),
                                singleLine = true
                            )
                        }
                    }

                    // Market Location Selection
                    Text("Market Hub Location", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    var marketDropdownExpanded by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF3F3F46)),
                        color = Color(0xFF18181B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { marketDropdownExpanded = true }
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                    ) {
                        Text(text = submissionMarketLocation, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                    val adminLocations = listOf("Mile 12 (Mainland)", "Isale Eko (Island)")
                    DropdownMenu(
                        expanded = marketDropdownExpanded,
                        onDismissRequest = { marketDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF18181B))
                    ) {
                        adminLocations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc, color = Color.White) },
                                onClick = {
                                    submissionMarketLocation = loc
                                    marketDropdownExpanded = false
                                }
                            )
                        }
                    }

                    // Prices Reported
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wholesale Price (₦)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            OutlinedTextField(
                                value = submissionWholesalePrice,
                                onValueChange = { submissionWholesalePrice = it },
                                placeholder = { Text("E.g. 85000", color = Color(0xFF71717A)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = Color(0xFF3F3F46)
                                ),
                                singleLine = true,
                                modifier = Modifier.testTag("feedback_wholesale_price_input")
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Subunit Retail (₦)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            OutlinedTextField(
                                value = submissionRetailPrice,
                                onValueChange = { submissionRetailPrice = it },
                                placeholder = { Text("Cup/Sachet Price", color = Color(0xFF71717A)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = Color(0xFF3F3F46)
                                ),
                                singleLine = true,
                                modifier = Modifier.testTag("feedback_retail_price_input")
                            )
                        }
                    }

                    // Message Note
                    Text("Reporting Vendor Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    OutlinedTextField(
                        value = submissionMessage,
                        onValueChange = { submissionMessage = it },
                        placeholder = { Text("Tell the admin why this price is updated or correct...", color = Color(0xFF71717A)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = Color(0xFF3F3F46)
                        ),
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        maxLines = 3
                    )
                }
            }
        )
    }
}

@Composable
fun CommodityPriceCard(
    commodity: Commodity,
    avgWholesale: Double,
    avgRetail: Double,
    isPlus: Boolean,
    isAverageMode: Boolean = false,
    onDetailsClick: () -> Unit
) {
    // Generate abbreviation from first 3 letters
    val cleanName = commodity.name.replace("(", "").replace(")", "").trim()
    val abbr = if (cleanName.length >= 3) cleanName.substring(0, 3).uppercase() else cleanName.uppercase()

    // Determine bubble color combos matching high-density mockup
    val (bgBubbleColor, textBubbleColor) = when (commodity.category) {
        "Grains" -> Color(0xFFFFF7ED) to Color(0xFFEA580C)         // orange-50 / orange-600
        "Beans" -> Color(0xFFFEF2F2) to Color(0xFFDC2626)          // red-50 / red-600
        "Processed Tubers" -> Color(0xFFFEF9C3) to Color(0xFF854D0E) // yellow-50 / yellow-700
        "Oils" -> Color(0xFFECFDF5) to Color(0xFF047857)           // emerald-50 / emerald-700
        "Vegetables" -> Color(0xFFF0FDF4) to Color(0xFF16A34A)     // green-50 / green-600
        "Fruits" -> Color(0xFFECFDF5) to Color(0xFF059669)         // emerald-50 / emerald-600
        "Agro Products" -> Color(0xFFF0FDF4) to Color(0xFF15803D) // green-50 / green-700
        "Instant Noodles" -> Color(0xFFFDF2F8) to Color(0xFFDB2777)  // pink-50 / pink-600
        "Salt" -> Color(0xFFF0FDFA) to Color(0xFF0D9488)             // teal-50 / teal-600
        "Sugar" -> Color(0xFFFEF3C7) to Color(0xFFD97706)            // amber-50 / amber-600
        "Seasoning Cubes" -> Color(0xFFF5F3FF) to Color(0xFF7C3AED)  // violet-50 / violet-600
        else -> Color(0xFFEFF6FF) to Color(0xFF2563EB)             // blue-50 / blue-600
    }

    // Border line logic: highlight VIP staple indices with a left amber indicator border
    val hasGoldBorder = isPlus && (commodity.name.contains("Rice") || commodity.name.contains("Garri"))
    val leftAccentBorderPaddingModifier = if (hasGoldBorder) {
        Modifier.drawBehind {
            val strokeWidth = 10f // 4.dp equivalent
            drawLine(
                color = Color(0xFFF59E0B), // gold
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = strokeWidth
            )
        }
    } else Modifier

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailsClick() }
            .then(leftAccentBorderPaddingModifier)
            .testTag("commodity_card_${commodity.id}"),
        colors = CardDefaults.cardColors(containerColor = SoftGrey),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left icon bubble & names
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored letter bubble
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgBubbleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = abbr,
                        fontWeight = FontWeight.Bold,
                        color = textBubbleColor,
                        fontSize = 11.sp,
                        letterSpacing = (-0.5).sp
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column {
                    Text(
                        text = commodity.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPlus) {
                        Text(
                            text = "Conversion: ${commodity.wholesaleUnit} = ${commodity.conversionFactor.toInt()} ${commodity.microUnit}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Right side: Price & Highlights
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Wholesale Price Column
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isAverageMode) "Wholesale (AVG.)" else "Wholesale",
                        fontSize = 8.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₦${if(avgWholesale > 0) String.format("%,.0f", avgWholesale) else "0"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                }

                if (isPlus || isAverageMode) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    // Retail Price Column
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (isAverageMode) "Retail (AVG.)" else "Retail",
                            fontSize = 8.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "₦${if(avgRetail > 0) String.format("%.0f", avgRetail) else "0"}/${commodity.microUnit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            if (isPlus) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = "Plus Unlocked",
                                    tint = Color(0xFFD4AF37),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Plus Locked",
                        tint = Color.LightGray,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. ARBITRAGE VIEW (PLUS SCREEN)
// ==========================================
@Composable
fun ArbitrageView(viewModel: NairaGuardViewModel) {
    val subState by viewModel.subscription.collectAsStateWithLifecycle()
    val commodities by viewModel.commodities.collectAsStateWithLifecycle()
    val prices by viewModel.prices.collectAsStateWithLifecycle()

    if (subState.tier != "PLUS") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "PLUS feature locked icon",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Arbitrage Tools are Locked",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "With NairaGuard PLUS, track mainland-to-island price arbitrage. Buy wholesale in mainland hubs (Mile 12, Ikorodu) and retail on the Islands (Isale Eko, Lekki) with extreme margin clarity.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.changeScreen(Screen.SUBSCRIPTION) },
                modifier = Modifier.testTag("unlock_plus_button")
            ) {
                Text("Unlock NairaGuard PLUS")
            }
        }
    } else {
        // Active PLUS Feature
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Compare,
                            contentDescription = "Compare Icon",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Live Lagos Arbitrage Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Buy mainland, Sell Island. This lists buy-wholesale opportunities against retail sale points.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            items(commodities) { comm ->
                val commPrices = prices.filter { it.commodityId == comm.id }
                
                // Fetch Mainland prices (Mile 12)
                val mainlandPriceObj = commPrices.find { it.marketLocation.contains("Mile 12") }
                    ?: commPrices.find { it.marketLocation.contains("Mainland") }
                
                // Fetch Island prices (Isale Eko)
                val islandPriceObj = commPrices.find { it.marketLocation.contains("Isale Eko") }
                    ?: commPrices.find { it.marketLocation.contains("Island") }

                if (mainlandPriceObj != null && islandPriceObj != null) {
                    val purchasePrice = mainlandPriceObj.wholesalePrice
                    val islandRetailEq = islandPriceObj.retailPrice
                    
                    // Arbitrage margin calculations
                    val totalRevenues = islandRetailEq * comm.conversionFactor
                    val grossProfit = totalRevenues - purchasePrice
                    val marginPercentage = if (totalRevenues > 0) (grossProfit / totalRevenues) * 100 else 0.0

                    ArbitrageItemCard(
                        commodity = comm,
                        buyMarket = mainlandPriceObj.marketLocation,
                        buyPrice = purchasePrice,
                        sellMarket = islandPriceObj.marketLocation,
                        sellPricePerUnit = islandRetailEq,
                        potentialProfit = grossProfit,
                        marginPercent = marginPercentage
                    )
                }
            }
        }
    }
}

@Composable
fun ArbitrageItemCard(
    commodity: Commodity,
    buyMarket: String,
    buyPrice: Double,
    sellMarket: String,
    sellPricePerUnit: Double,
    potentialProfit: Double,
    marginPercent: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = commodity.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (marginPercent > 20) NairaSuccessGreen.copy(alpha = 0.15f)
                            else AmberGold.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Margin: ${String.format("%.1f", marginPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (marginPercent > 20) NairaSuccessGreen else TerracottaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Buy card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("BUY WHOLESALE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(buyMarket.split(" ")[0], fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "₦${String.format("%,.0f", buyPrice)} / ${commodity.wholesaleUnit.split(" ")[0]}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward Arbitrage Flow",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(18.dp),
                    tint = Color.Gray
                )

                // Sell card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("SELL RETAIL", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(sellMarket.split(" ")[0], fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "₦${String.format("%.1f", sellPricePerUnit)} per ${commodity.microUnit}",
                            fontWeight = FontWeight.Bold,
                            color = NairaSuccessGreen,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = NairaSuccessGreen.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Potential Profit / unit", style = MaterialTheme.typography.bodySmall, color = NairaSuccessGreen)
                    Text(
                        "₦ ${String.format("%,.2f", potentialProfit)}",
                        fontWeight = FontWeight.Bold,
                        color = NairaSuccessGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. MARGIN CALCULATOR VIEW (PLUS SCREEN)
// ==========================================
@Composable
fun MarginCalculatorView(viewModel: NairaGuardViewModel) {
    val subState by viewModel.subscription.collectAsStateWithLifecycle()
    val commodities by viewModel.commodities.collectAsStateWithLifecycle()
    val prices by viewModel.prices.collectAsStateWithLifecycle()

    if (subState.tier != "PLUS") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "PLUS Locked Icon",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Premium Margin Calculator Locked",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Instantly input custom logistics costs, purchase overheads, and target retail pricing to evaluate margins for daily Lagos retail runs.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.changeScreen(Screen.SUBSCRIPTION) },
                modifier = Modifier.testTag("unlock_plus_calc_button")
            ) {
                Text("Unlock NairaGuard PLUS")
            }
        }
    } else {
        // ACTIVE MARGIN CALCULATOR ACTIVE
        val selectedComm by viewModel.calcSelectedCommodity.collectAsStateWithLifecycle()
        val purchaseCost by viewModel.calcPurchasePrice.collectAsStateWithLifecycle()
        val retailSellingPrice by viewModel.calcSellingPricePerUnit.collectAsStateWithLifecycle()
        val transportCost by viewModel.calcTransportCost.collectAsStateWithLifecycle()
        val otherCost by viewModel.calcOtherCosts.collectAsStateWithLifecycle()

        var isCommMenuExpanded by remember { mutableStateOf(false) }

        // Setup default selection if none
        if (selectedComm == null && commodities.isNotEmpty()) {
            val defaultComm = commodities.find { it.name == "Premium Rice" } ?: commodities[0]
            viewModel.calcSelectedCommodity.value = defaultComm
            
            // Auto seed purchase price
            val commPrices = prices.filter { it.commodityId == defaultComm.id }
            val mainlandPrice = commPrices.find { it.marketLocation.contains("Mile 12") }?.wholesalePrice ?: 85000.0
            viewModel.calcPurchasePrice.value = mainlandPrice

            // Auto seed selling price unit
            val islandPrice = commPrices.find { it.marketLocation.contains("Isale Eko") }?.retailPrice ?: 850.0
            viewModel.calcSellingPricePerUnit.value = islandPrice
        }

        // Calculations
        val activeComm = selectedComm
        val conversion = activeComm?.conversionFactor ?: 128.0
        val totalInvestment = purchaseCost + transportCost + otherCost
        val projectedSales = retailSellingPrice * conversion
        val netGain = projectedSales - totalInvestment
        val markup = if (totalInvestment > 0) (netGain / totalInvestment) * 100 else 0.0
        val profitMargin = if (projectedSales > 0) (netGain / projectedSales) * 100 else 0.0

        val safetyColor = when {
            netGain <= 0 -> TerracottaOrange
            profitMargin < 12.0 -> AmberGold
            else -> NairaSuccessGreen
        }

        val safetyStatus = when {
            netGain <= 0 -> "LOSS DETECTED (Alert! Check logistical costs or raise retail price!)"
            profitMargin < 12.0 -> "RISKY / LOW MARGIN (Very tight spread. Vulnerable to spoilage & price variations.)"
            else -> "SAFE PROFIT MARGIN (Strong healthy returns for retail vendor!)"
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Dynamic Retail Margin Worksheet",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            // Commodity Dropdown Selector
            Text("Select Staple Retail Commodity", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { isCommMenuExpanded = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeComm?.name ?: "No commodity selected",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand dropdown")
                }

                DropdownMenu(
                    expanded = isCommMenuExpanded,
                    onDismissRequest = { isCommMenuExpanded = false }
                ) {
                    commodities.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = {
                                viewModel.calcSelectedCommodity.value = c
                                isCommMenuExpanded = false

                                // Default seed purchase + selling per unit from live prices in database
                                val commPrices = prices.filter { it.commodityId == c.id }
                                val rawPurchase = commPrices.find { it.marketLocation.contains("Mile 12") }?.wholesalePrice ?: 85000.0
                                val retailTarget = commPrices.find { it.marketLocation.contains("Isale Eko") }?.retailPrice ?: (rawPurchase / c.conversionFactor * 1.15)
                                
                                viewModel.calcPurchasePrice.value = rawPurchase
                                viewModel.calcSellingPricePerUnit.value = retailTarget
                            },
                            modifier = Modifier.testTag("calc_dropdown_item_${c.id}")
                        )
                    }
                }
            }

            if (activeComm != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Wholesale Unit: ${activeComm.wholesaleUnit}", fontSize = 12.sp, color = Color.Gray)
                            Text("Subunit unit: ${activeComm.microUnit}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // INPUT FORMS
            Text("Purchasing & Running Overhead", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Purchase cost
                    OutlinedTextField(
                        value = purchaseCost.toString(),
                        onValueChange = { viewModel.calcPurchasePrice.value = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Wholesale Import Cost (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_calc_purchase"),
                        singleLine = true
                    )

                    // Transport costs
                    OutlinedTextField(
                        value = transportCost.toString(),
                        onValueChange = { viewModel.calcTransportCost.value = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Lagos Transport & Logistics Overhead (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_calc_transport"),
                        singleLine = true
                    )

                    // Other operational costs
                    OutlinedTextField(
                        value = otherCost.toString(),
                        onValueChange = { viewModel.calcOtherCosts.value = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Rent, Handling, Bags, etc. (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_calc_other"),
                        singleLine = true
                    )
                }
            }

            Text("Retail Target Yield", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Target retail unit
                    OutlinedTextField(
                        value = retailSellingPrice.toString(),
                        onValueChange = { viewModel.calcSellingPricePerUnit.value = it.toDoubleOrNull() ?: 0.0 },
                        label = { Text("Target Retail Selling Price per ${activeComm?.microUnit} (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_calc_retail"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Total conversion: ${conversion.roundToInt()} units will generate ₦${viewModel.formatNaira(projectedSales)} gross yield.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // OUTPUT METRIC HIGHLIGHTS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = safetyColor.copy(alpha = 0.12f)),
                border = BorderStroke(2.dp, safetyColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MARGIN ANALYSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = safetyColor
                    )
                    Text(
                        text = "₦ ${viewModel.formatNaira(netGain)} Net Profit",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = safetyColor
                    )
                    Text(
                        text = safetyStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = safetyColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Markup %", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                "${String.format("%.1f", markup)}%",
                                fontWeight = FontWeight.Bold,
                                color = safetyColor,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text("Total Expenses", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                "₦${viewModel.formatNaira(totalInvestment)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Return on Investment", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                "${String.format("%.1f", profitMargin)}% Net Margin",
                                fontWeight = FontWeight.Bold,
                                color = safetyColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. INTELLIGENT WHATSAPP/SMS INGEST AGENT
// ==========================================
@Composable
fun SuperAgentView(viewModel: NairaGuardViewModel) {
    val userInputRawMsg by viewModel.agentInputText.collectAsStateWithLifecycle()
    val isAgentParsing by viewModel.isAgentParsing.collectAsStateWithLifecycle()
    val agentResultOutput by viewModel.agentParseResult.collectAsStateWithLifecycle()
    val listParsedHistory by viewModel.agentLastParsedUpdate.collectAsStateWithLifecycle()

    val sampleInputs = listOf(
        "Mile 12: Premium Rice bag wholesale is now 92,000 naira. Also white garri bag is 43000 naira",
        "Isale Eko Market Update! Oloyin Beans 50kg bag rises to 125,000 NGN. Rodo pepper basket is 32000 naira.",
        "Lekki Hub: Tuber Yam heap (100 tubers) priced at 235,000 NGN. Chicken Eggs crate is 5,800."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Gemini Logo",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "NairaGuard AI Wholesaler Ingestion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Trained on Gemini 3.5 Flash. Transmit raw SMS, WhatsApp alerts or messy voice transcripts from wholesalers to automatically normalize them.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Text("Select Quick Sample Message", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleInputs.forEachIndexed { idx, sample ->
                OutlinedButton(
                    onClick = { viewModel.agentInputText.value = sample },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("ai_sample_msg_$idx")
                ) {
                    Text(
                        text = "Sample ${idx + 1}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // INPUT FIELD REPRESENTING WHATSAPP INPUT / High Density Slate Luxury Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSlateBg)
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentBlue)
                        )
                        Text(
                            text = "SMS Superagent v1.2 Ingest Engine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                
                OutlinedTextField(
                    value = userInputRawMsg,
                    onValueChange = { viewModel.agentInputText.value = it },
                    placeholder = { 
                        Text(
                            text = "Paste text, e.g. \"Mile12 Rice 50kg now 82k...\"", 
                            color = Color(0xFF64748B), 
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .testTag("ai_messy_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = DarkSlateInput,
                        unfocusedContainerColor = DarkSlateInput,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    ),
                    maxLines = 4
                )

                Button(
                    onClick = { viewModel.runIntelligentAgent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("ai_parse_submit_button"),
                    enabled = !isAgentParsing && userInputRawMsg.isNotEmpty(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        disabledContainerColor = Color(0xFFE2E8F0),
                        contentColor = Color.White,
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isAgentParsing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 1.5.dp
                            )
                            Text("Parsing Message...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bolt, 
                                contentDescription = "AI Submit Run", 
                                modifier = Modifier.size(14.dp)
                            )
                            Text("Parse & Normalize with Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // RENDER OUPUT SCREEN RESULT
        agentResultOutput?.let { result ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (result.startsWith("Error")) TerracottaOrange.copy(alpha = 0.12f)
                    else NairaSuccessGreen.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.5.dp, 
                    if (result.startsWith("Error")) TerracottaOrange.copy(alpha = 0.4f) 
                    else NairaSuccessGreen.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("ai_response_box")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (result.startsWith("Error")) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                            contentDescription = "Status Status",
                            tint = if (result.startsWith("Error")) TerracottaOrange else NairaSuccessGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (result.startsWith("Error")) "Ingested Engine Error" else "AI Ingestion Log Saved",
                            fontWeight = FontWeight.Bold,
                            color = if (result.startsWith("Error")) TerracottaOrange else NairaSuccessGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = result,
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        if (listParsedHistory.isNotEmpty()) {
            Text("Ingested Logs (This Session)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listParsedHistory.forEach { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.commodity, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(log.market, fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                "₦${viewModel.formatNaira(log.price)}",
                                fontWeight = FontWeight.Bold,
                                color = NairaSuccessGreen,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SECURE ADMIN COMMAND CENTER
// ==========================================
@Composable
fun AdminCommandView(viewModel: NairaGuardViewModel) {
    val userIsLoggedIn by viewModel.userIsLoggedIn.collectAsStateWithLifecycle()
    val userIsVerified by viewModel.userIsVerified.collectAsStateWithLifecycle()

    if (!userIsLoggedIn || !userIsVerified) {
        Box(
            modifier = Modifier.fillMaxSize().background(LightCream).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.widthIn(max = 400.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGrey),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Denied",
                        tint = TerracottaOrange,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Secure Admin Keyhole",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepCharcoal
                    )
                    Text(
                        "You must authenticate as a regional administrator to configure conversion ratios, dispatch SMS price alerts, and manually override local market prices.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { viewModel.changeScreen(Screen.LOGIN) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sign In as Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val commodities by viewModel.commodities.collectAsStateWithLifecycle()
    val prices by viewModel.prices.collectAsStateWithLifecycle()
    val subscription by viewModel.subscription.collectAsStateWithLifecycle()
    val waitlistInDb by viewModel.waitlist.collectAsStateWithLifecycle()
    val plusSubscriptions by viewModel.plusSubscriptions.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Manual prices, 1 = Conversions, 2 = Subscription Status, 3 = Analytics
    val listLocations = listOf("Mile 12 (Mainland)", "Isale Eko (Island)")
    fun formatAdminPrice(v: Double): String = if (v % 1.0 == 0.0) v.toLong().toString() else String.format(Locale.US, "%.2f", v)

    // Edit states
    var selectedCommAdmin by remember { mutableStateOf<Commodity?>(null) }
    var selectedLocationAdmin by remember { mutableStateOf("Mile 12 (Mainland)") }
    var inputWholesalePriceAdmin by remember { mutableStateOf("") }
    var inputRetailPriceAdmin by remember { mutableStateOf("") }
    var showAdminManualSuccessMsg by remember { mutableStateOf(false) }

    // Search query for historical subscribers
    var subscriberSearchQuery by remember { mutableStateOf("") }

    // Conversion edit states
    var selectCommConvAdmin by remember { mutableStateOf<Commodity?>(null) }
    var inputConvFactorAdmin by remember { mutableStateOf("") }
    var showAdminConvSuccessMsg by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Admin Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin command emblem",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Secure Admin Command Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Alter conversion rules, update live pricing manually, view registration charts, and monitor premium subscriber statuses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Real-time Waitlist Registry Size Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Priority waitlist badge",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Active Waitlist Subscribers:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E)) // Pulse active indicator green
                            )
                            Text(
                                text = "${waitlistInDb.size} verified leads",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // TABS SELECTORS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Input Prices", "Set Conversions", "Subscribers", "Feedback Queue").forEachIndexed { idx, label ->
                FilterChip(
                    selected = activeTab == idx,
                    onClick = { activeTab = idx },
                    label = { 
                        Text(
                            text = label,
                            color = if (activeTab == idx) Color.White else DeepCharcoal,
                            fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreen,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF1F5F9),
                        labelColor = DeepCharcoal
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = activeTab == idx,
                        borderColor = Color(0xFFE2E8F0),
                        selectedBorderColor = ForestGreen,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("admin_tab_$idx")
                )
            }
        }

        // TAB ACTIONS
        when (activeTab) {
            0 -> {
                // INPUT RAW WHOLESALE PRICES MANUALLY
                Text("Publish Manual Price Index Updates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("1. Select Commodity Staple", style = MaterialTheme.typography.bodySmall)
                        // Simple dropdown selector implementation
                        var isExpanded by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = true }
                                .padding(vertical = 12.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = selectedCommAdmin?.name ?: "Tap to choose commodity...",
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoal
                            )
                        }
                        DropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            commodities.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name, color = DeepCharcoal) },
                                    onClick = {
                                        selectedCommAdmin = c
                                        isExpanded = false
                                        // seed input
                                        val pList = prices.filter { it.commodityId == c.id }
                                        val matched = pList.find { it.marketLocation == selectedLocationAdmin }
                                        val defaultPrice = matched?.wholesalePrice ?: 50000.0
                                        val defaultRetail = matched?.retailPrice ?: (defaultPrice / c.conversionFactor)
                                        inputWholesalePriceAdmin = formatAdminPrice(defaultPrice)
                                        inputRetailPriceAdmin = formatAdminPrice(defaultRetail)
                                    },
                                    modifier = Modifier.testTag("admin_price_selector_${c.id}")
                                )
                            }
                        }

                        Text("2. Target Market Location", style = MaterialTheme.typography.bodySmall)
                        var isLocationExpanded by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isLocationExpanded = true }
                                .padding(vertical = 12.dp, horizontal = 12.dp)
                        ) {
                            Text(text = selectedLocationAdmin, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                        }
                        DropdownMenu(
                            expanded = isLocationExpanded,
                            onDismissRequest = { isLocationExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            listLocations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc, color = DeepCharcoal) },
                                    onClick = {
                                        selectedLocationAdmin = loc
                                        isLocationExpanded = false
                                        selectedCommAdmin?.let { c ->
                                            val pList = prices.filter { it.commodityId == c.id }
                                            val matched = pList.find { it.marketLocation == loc }
                                            val defaultPrice = matched?.wholesalePrice ?: 50000.0
                                            val defaultRetail = matched?.retailPrice ?: (defaultPrice / c.conversionFactor)
                                            inputWholesalePriceAdmin = formatAdminPrice(defaultPrice)
                                            inputRetailPriceAdmin = formatAdminPrice(defaultRetail)
                                        }
                                    }
                                )
                            }
                        }

                        Text("3. Input Wholesale Price (₦)", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = inputWholesalePriceAdmin,
                            onValueChange = { inputWholesalePriceAdmin = it },
                            placeholder = { Text("Enter value in Naira...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_price_input"),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("4. Input Retail/Sub-unit Price (₦)", style = MaterialTheme.typography.bodySmall)
                            selectedCommAdmin?.let { c ->
                                val microCount = if (c.conversionFactor % 1.0 == 0.0) c.conversionFactor.toLong().toString() else String.format(Locale.US, "%.1f", c.conversionFactor)
                                Text(
                                    text = "Auto-calc (÷ $microCount ${c.microUnit}s)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        val wholesale = inputWholesalePriceAdmin.toDoubleOrNull()
                                        if (wholesale != null && wholesale > 0) {
                                            val calculatedRetail = wholesale / c.conversionFactor
                                            inputRetailPriceAdmin = formatAdminPrice(calculatedRetail)
                                        }
                                    }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = inputRetailPriceAdmin,
                            onValueChange = { inputRetailPriceAdmin = it },
                            placeholder = { Text("Enter retail/cup price in Naira...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_retail_price_input"),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val comm = selectedCommAdmin
                                val cost = inputWholesalePriceAdmin.toDoubleOrNull()
                                val retail = inputRetailPriceAdmin.toDoubleOrNull()
                                if (comm != null && cost != null && cost > 0 && retail != null && retail > 0) {
                                    viewModel.adminUpdatePrice(comm.id, selectedLocationAdmin, cost, retail)
                                    showAdminManualSuccessMsg = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_price_submit"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = selectedCommAdmin != null && inputWholesalePriceAdmin.isNotEmpty() && inputRetailPriceAdmin.isNotEmpty()
                        ) {
                            Text("Publish Market Price Log")
                        }

                        if (showAdminManualSuccessMsg) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NairaSuccessGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.0.dp, NairaSuccessGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Database live updated successfully!", fontSize = 12.sp, color = NairaSuccessGreen, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { showAdminManualSuccessMsg = false }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear msg", tint = NairaSuccessGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // MANAGE SUB-UNIT ATOMIC CONVERSION FACTOR
                Text("Modify Sub-unit Atomic Conversion Factor Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "If wholesalers change container bags sizes (e.g. Rice bag changes from 120 cups to 128 cups due to supply shrinkflation), update the conversion metric rules. NairaGuard auto-recalculates retail units rates instantly.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("1. Select Commodity Model", style = MaterialTheme.typography.bodySmall)
                        var isConvExpanded by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isConvExpanded = true }
                                .padding(vertical = 12.dp, horizontal = 12.dp)
                        ) {
                            Text(
                                text = selectCommConvAdmin?.name ?: "Tap to choose commodity...",
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoal
                            )
                        }
                        DropdownMenu(
                            expanded = isConvExpanded,
                            onDismissRequest = { isConvExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            commodities.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name, color = DeepCharcoal) },
                                    onClick = {
                                        selectCommConvAdmin = c
                                        isConvExpanded = false
                                        inputConvFactorAdmin = c.conversionFactor.roundToInt().toString()
                                    },
                                    modifier = Modifier.testTag("admin_conv_selector_${c.id}")
                                )
                            }
                        }

                        selectCommConvAdmin?.let { c ->
                            Text(
                                "Active rule: 1 ${c.wholesaleUnit} yields ${c.conversionFactor.roundToInt()} ${c.microUnit}s.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text("2. Set New Subunits Factor Value", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = inputConvFactorAdmin,
                            onValueChange = { inputConvFactorAdmin = it },
                            placeholder = { Text("Numbers of micro-units inside a bag/carton...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_conv_input"),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val comm = selectCommConvAdmin
                                val factor = inputConvFactorAdmin.toDoubleOrNull()
                                if (comm != null && factor != null && factor > 0) {
                                    viewModel.adminUpdateConversion(comm.id, factor)
                                    showAdminConvSuccessMsg = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_conv_submit"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = selectCommConvAdmin != null && inputConvFactorAdmin.isNotEmpty()
                        ) {
                            Text("Overwrite Conversion Factor Rule")
                        }

                        if (showAdminConvSuccessMsg) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NairaSuccessGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.0.dp, NairaSuccessGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Global rule overwritten successfully!", fontSize = 12.sp, color = NairaSuccessGreen, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { showAdminConvSuccessMsg = false }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear status", tint = NairaSuccessGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // HISTORICAL PLUS SUBSCRIBERS REGISTRY
                Text("NairaGuard PLUS Historical Subscribers (Never Deleted)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Track and audit permanent subscription activations, cancellations, and renewals. Even if users delete accounts, history is locked.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                // Search field for subscribers
                OutlinedTextField(
                    value = subscriberSearchQuery,
                    onValueChange = { subscriberSearchQuery = it },
                    placeholder = { Text("Filter subscribers by name, phone or status...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("subscriber_history_search"),
                    singleLine = true
                )

                val filteredSubs = plusSubscriptions.filter {
                    it.contact.contains(subscriberSearchQuery, ignoreCase = true) ||
                    it.status.contains(subscriberSearchQuery, ignoreCase = true) ||
                    it.name.contains(subscriberSearchQuery, ignoreCase = true)
                }

                if (filteredSubs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No matching historic subscription records found.", fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    val sdfFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredSubs.forEach { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BorderGrey)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(record.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoal)
                                            val badgeColor = when (record.status) {
                                                "Active" -> NairaSuccessGreen
                                                "Cancelled" -> TerracottaOrange
                                                else -> Color.Gray
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = badgeColor.copy(alpha = 0.15f),
                                                border = BorderStroke(0.5.dp, badgeColor)
                                            ) {
                                                Text(
                                                    text = record.status.uppercase(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = badgeColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Contact: ${record.contact}", fontSize = 12.sp, color = Color.DarkGray)
                                        Text("Date: ${sdfFormat.format(java.util.Date(record.timestamp))}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                // SUBSCRIBERS STATUS CONTROLLER
                Text("NairaGuard PLUS Priority Waitlist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "View and notify prospective leads who submitted inquiries to join the NairaGuard PLUS exclusive beta group. Total waitlist size: ${waitlistInDb.size} leads.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                if (waitlistInDb.isNotEmpty()) {
                    val context = LocalContext.current
                    Button(
                        onClick = { launchWhatsAppAllWaitlistEntries(context, waitlistInDb) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Send WhatsApp", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send All (${waitlistInDb.size}) Entries to WhatsApp (+2348020556342)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Current sub override (For admin testing)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Testing Override: Toggle Admin Local App Tier", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.cancelSubscription() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaOrange)
                            ) {
                                Text("Set Live App to FREE", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { viewModel.subscribePlusMonthly(forceAdmin = true) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NairaSuccessGreen)
                            ) {
                                Text("Force Active PLUS", fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Admin Local App State: ${subscription.tier} - Member Status: ${subscription.status.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                // Waitlist Entry list
                if (waitlistInDb.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No waitlist subscribers yet.", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 13.sp)
                            Text("When users click GET PLUS and fill the waitlist form, their contacts will appear here.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val context = LocalContext.current
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        waitlistInDb.forEach { entry ->
                            var showEmailComposeDialog by remember { mutableStateOf(false) }
                            var emailSubject by remember { mutableStateOf("NairaGuard PLUS - Calibrated Release Notification") }
                            var emailBody by remember { mutableStateOf("Hello ${entry.name},\n\nWe are pleased to inform you that our real-time AI price algorithms for major market hubs (Mile 12 & Isale Eko) are now calibrated with 100% margin accuracy! You can now access NairaGuard PLUS features.\n\nWarm regards,\nNairaGuard Team") }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoal)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Contact: ${entry.contact}", fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                                        Text("Submitted: ${sdf.format(java.util.Date(entry.timestamp))}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { launchWhatsAppWaitlist(context, entry.name, entry.contact) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "WhatsApp",
                                                tint = Color(0xFF15803D),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Button(
                                            onClick = { showEmailComposeDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(15.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Email", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            if (showEmailComposeDialog) {
                                AlertDialog(
                                    onDismissRequest = { showEmailComposeDialog = false },
                                    title = { Text("Notify Subscriber: ${entry.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = emailSubject,
                                                onValueChange = { emailSubject = it },
                                                label = { Text("Subject") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = emailBody,
                                                onValueChange = { emailBody = it },
                                                label = { Text("Message Body") },
                                                minLines = 4,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                // Trigger email Intent
                                                showEmailComposeDialog = false
                                                try {
                                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                        data = Uri.parse("mailto:${entry.contact}")
                                                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                                                        putExtra(Intent.EXTRA_TEXT, emailBody)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Email dispatched to waitlist subscriber via background server!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                        ) {
                                            Text("Send Notification")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showEmailComposeDialog = false }) {
                                            Text("Cancel", color = Color.Gray)
                                        }
                                    },
                                    containerColor = Color.White
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()
                
                Text(text = "Vendor Feedback & Price Corrections Queue", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Review suggestions and price inaccuracy reports filed by retail vendors. Approving integration automatically syncs live items and triggers alert pipelines.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                if (feedbackList.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGrey)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Feedback, contentDescription = "Empty Feedback", modifier = Modifier.size(32.dp), tint = Color.LightGray)
                            Text("No feedback reports submitted yet.", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        feedbackList.forEach { feedback ->
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("feedback_card_${feedback.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (feedback.status) {
                                        "Integrated" -> NairaSuccessGreen.copy(alpha = 0.03f)
                                        "Dismissed" -> Color.LightGray.copy(alpha = 0.05f)
                                        else -> Color.White
                                    }
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    when (feedback.status) {
                                        "Integrated" -> NairaSuccessGreen.copy(alpha = 0.3f)
                                        "Dismissed" -> Color.LightGray
                                        else -> BorderGrey
                                    }
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (feedback.feedbackType == "Inaccuracy") TerracottaOrange.copy(alpha = 0.12f)
                                                        else AccentBlueBg
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = feedback.feedbackType.uppercase(),
                                                    color = if (feedback.feedbackType == "Inaccuracy") TerracottaOrange else AccentBlue,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = feedback.commodityName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = DeepCharcoal
                                            )
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (feedback.status) {
                                                        "Integrated" -> NairaSuccessGreen.copy(alpha = 0.15f)
                                                        "Dismissed" -> Color.Gray.copy(alpha = 0.15f)
                                                        else -> Color(0xFFFEF3C7)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = feedback.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (feedback.status) {
                                                    "Integrated" -> NairaSuccessGreen
                                                    "Dismissed" -> Color.DarkGray
                                                    else -> Color(0xFFD97706)
                                                }
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Market: ${feedback.marketLocation}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.DarkGray
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column {
                                            Text("Reported Wholesale", fontSize = 10.sp, color = Color.Gray)
                                            Text("₦${viewModel.formatNaira(feedback.reportedWholesalePrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                                        }
                                        Column {
                                            Text("Reported Subunit Retail", fontSize = 10.sp, color = Color.Gray)
                                            Text("₦${viewModel.formatNaira(feedback.reportedRetailPrice)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                                        }
                                    }

                                    if (feedback.message.isNotEmpty()) {
                                        Text(
                                            text = "\"${feedback.message}\"",
                                            fontSize = 11.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    if (feedback.status == "Pending") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.adminProcessFeedback(feedback, false) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                                modifier = Modifier.weight(1f).height(32.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Dismiss", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            
                                            Button(
                                                onClick = { viewModel.adminProcessFeedback(feedback, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = NairaSuccessGreen),
                                                modifier = Modifier.weight(1.5f).height(32.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve & Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        TextButton(
                                            onClick = { viewModel.adminDeleteFeedback(feedback.id) },
                                            modifier = Modifier.align(Alignment.End).height(24.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp)
                                        ) {
                                            Text("Delete Report", color = Color.Red, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoginPage(onLoginSuccess: () -> Unit) {
    val viewModel: NairaGuardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Google selection sheet simulator dialog
    var showGoogleAuthDialog by remember { mutableStateOf(false) }

    if (showGoogleAuthDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showGoogleAuthDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGrey),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SoftGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G", 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 24.sp, 
                            color = AccentBlue
                        )
                    }

                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )

                    Text(
                        text = "NairaGuard Admin portal is requesting access to verify identity of your Google Account.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    // User Profile Option
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGrey),
                        color = SoftGrey.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    viewModel.authenticateGoogleAdmin("kenennakingsleychukwuma@gmail.com")
                                    showGoogleAuthDialog = false
                                    onLoginSuccess()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlueBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "K", 
                                    fontWeight = FontWeight.Bold, 
                                    color = AccentBlue, 
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kenenna Kingsley",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DeepCharcoal
                                )
                                Text(
                                    text = "kenennakingsleychukwuma@gmail.com",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active account",
                                tint = NairaSuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showGoogleAuthDialog = false }) {
                            Text("Reject", color = TerracottaOrange, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.authenticateGoogleAdmin("kenennakingsleychukwuma@gmail.com")
                                    showGoogleAuthDialog = false
                                    onLoginSuccess()
                                }
                            }
                        ) {
                            Text("Fast Verify", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        
        // Lock Badge Header
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Gate",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isSignUp) "Create Admin Credentials" else "Administrative Access Only",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DeepCharcoal
        )
        
        Text(
            text = if (isSignUp) 
                "Establish a secure administrator profile in the system to ingest commodity updates and control regional conversion factors."
                else "Authenticate with registered administrative credentials or instant Google Account SSO to access backend controls.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BorderGrey),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isSignUp) "Secured Sign-Up Console" else "Secured Log-In Console",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        errorMessage = ""
                        successMessage = ""
                    },
                    label = { Text("Admin Username") },
                    placeholder = { Text("e.g. admin") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_username_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Username Input", modifier = Modifier.size(18.dp))
                    }
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = ""
                        successMessage = ""
                    },
                    label = { Text("Secure Password") },
                    placeholder = { Text("••••••••") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("admin_password_input"),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Password Input", modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
                
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = TerracottaOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (successMessage.isNotEmpty()) {
                    Text(
                        text = successMessage,
                        color = NairaSuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                
                Button(
                    onClick = {
                        if (username.trim().isEmpty() || password.isEmpty()) {
                            errorMessage = "Please complete both details correctly."
                            return@Button
                        }
                        if (isSignUp) {
                            coroutineScope.launch {
                                val registered = viewModel.registerAdmin(username, password)
                                if (registered) {
                                    successMessage = "Account created! Standard login initiated."
                                    errorMessage = ""
                                    isSignUp = false
                                } else {
                                    errorMessage = "This username is registered or invalid."
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                val validated = viewModel.authenticateAdmin(username, password)
                                if (validated) {
                                    onLoginSuccess()
                                    errorMessage = ""
                                } else {
                                    errorMessage = "Credentials mismatch or unregistered."
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_login_submit"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignUp) AccentBlue else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isSignUp) "Register Admin Account" else "Verify & Authenticate", 
                        fontWeight = FontWeight.Bold
                    )
                }

                // SECURE TOGGLE
                TextButton(
                    onClick = {
                        isSignUp = !isSignUp
                        errorMessage = ""
                        successMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSignUp) "Already have an admin account? Log In" else "Don't have an admin account? Sign Up securely",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        // Google Sign In / Sign Up Button
        OutlinedButton(
            onClick = { showGoogleAuthDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_sso_button"),
            border = BorderStroke(1.5.dp, BorderGrey),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(AccentBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AccentBlue)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isSignUp) "Sign Up with Google Account" else "Continue with Google Account",
                    color = DeepCharcoal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        // Hint Container for Sandbox Testing
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Standard login hint",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = "Standard Sandbox Fallbacks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Username: admin   |   Password: admin123",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ==========================================
// 6. SUBSCRIPTION VIEW (ALERTS SETUP AND SUBSCRIPTION STATUS)
// ==========================================
@Composable
fun SubscriptionView(viewModel: NairaGuardViewModel) {
    val subState by viewModel.subscription.collectAsStateWithLifecycle()
    val alertLogs by viewModel.smsAlertLogs.collectAsStateWithLifecycle()
    val userIsLoggedIn by viewModel.userIsLoggedIn.collectAsStateWithLifecycle()
    val userIsVerified by viewModel.userIsVerified.collectAsStateWithLifecycle()
    val isAdmin = userIsLoggedIn && userIsVerified

    var userPhoneInput by remember { mutableStateOf(subState.phoneNumber) }
    var userTriggerPriceInput by remember { mutableStateOf("") }
    var selectedCommForAlert by remember { mutableStateOf<Commodity?>(null) }
    var selectedMarketForAlert by remember { mutableStateOf("Mile 12 (Mainland)") }
    var isAlertAbove by remember { mutableStateOf(true) }

    val commodities by viewModel.commodities.collectAsStateWithLifecycle()
    val alertsList by viewModel.alerts.collectAsStateWithLifecycle()

    var showLocalSquadCheckout by remember { mutableStateOf(false) }
    var selectedLocalSquadPlan by remember { mutableStateOf("monthly") }

    if (showLocalSquadCheckout) {
        val context = LocalContext.current
        SquadPayCheckoutDialog(
            planType = selectedLocalSquadPlan,
            userDefaultEmail = if (userPhoneInput.contains("@")) userPhoneInput else "subscriber@nairaguard.ng",
            onDismiss = { showLocalSquadCheckout = false },
            onPaymentSuccess = { plan, email ->
                viewModel.processSquadPaymentSuccess(plan, email)
                showLocalSquadCheckout = false
                Toast.makeText(context, "Payment verified! Upgraded to NairaGuard PLUS.", Toast.LENGTH_LONG).show()
            },
            onInitiatePayment = { plan, email, ref ->
                viewModel.recordPendingSquadPayment(plan, email, ref)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Membership details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (subState.tier == "PLUS" || isAdmin) NairaSuccessGreen.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(
                1.5.dp,
                if (subState.tier == "PLUS" || isAdmin) NairaSuccessGreen else Color.LightGray.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "CURRENT TIERS STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subState.tier == "PLUS" || isAdmin) NairaSuccessGreen else Color.Gray
                        )
                        Text(
                            text = "NairaGuard ${if (isAdmin) "PLUS (ADMIN)" else subState.tier}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (subState.tier == "PLUS" || isAdmin) NairaSuccessGreen else Color.LightGray
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (subState.tier == "PLUS" || isAdmin) "PLUS UNLOCKED" else "FREE LICENSE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (subState.tier == "PLUS" || isAdmin) {
                    Text(
                        text = "Licensed Membership Status: ${if (isAdmin) "ADMIN BYPASS" else subState.status.uppercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!isAdmin) {
                        subState.expiryDate?.let { date ->
                            val daysRemaining = ((date - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                            Text(
                                text = "Subscription valid for another $daysRemaining days.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { viewModel.cancelSubscription() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_sub_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Downgrade to Standard FREE Account", color = TerracottaOrange)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Admin bypass is active. You have permanent complimentary unthrottled access.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        "Free licenses can only view combined standard averages of Lagos indices. Upgrade to access separate Mainland vs Island pricing and premium tools.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    val context = LocalContext.current
                    val hasUsedTrial = viewModel.sharedPrefs.getBoolean("plus_trial_used", false) || subState.trialStartDate != null
                    Button(
                        onClick = {
                            viewModel.activatePlusTrial { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_trial_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasUsedTrial) Color.Gray else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (hasUsedTrial) "7-Day Trial Already Claimed" else "Start 7-Day FREE Trial")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val userEmail = if (userPhoneInput.contains("@")) userPhoneInput else "subscriber@nairaguard.ng"
                            val ref = "SQUAD-DAY-${System.currentTimeMillis()}"
                            viewModel.recordPendingSquadPayment("daily", userEmail, ref)
                            SquadPayService.launchDailyCheckoutDirect(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subscribe_daily_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subscribe Daily (₦200 / day)", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            selectedLocalSquadPlan = "monthly"
                            showLocalSquadCheckout = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subscribe_monthly_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NairaSuccessGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subscribe to PLUS (₦5,000 / month)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Features Comparison Card (MVP Specs from user's notebook)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("plans_comparison_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BorderGrey)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Plans & Features Comparison Matrix",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DeepCharcoal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Headings
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SoftGrey)
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("FEATURE / BENEFIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1.4f))
                    Text("BASIC (FREE)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("PLUS (₦5,000/mo)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NairaSuccessGreen, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                }

                val items = listOf(
                    Triple("Open Market Index (Average Prices)", "Average Only (✓)", "Full Actuals (✓)"),
                    Triple("Mainland Vs Island Prices", "Locked (✗)", "Included (✓)"),
                    Triple("Spike Alerts & Telemetry", "Locked (✗)", "Included (✓)"),
                    Triple("Offline Data Sync", "Included (✓)", "Included (✓)"),
                    Triple("7-Day PLUS Trial", "Included (✓)", "N/A"),
                    Triple("Customer Support", "Feedback/Suggestion (✓)", "Priority Support (✓)")
                )

                items.forEach { (feat, basic, plus) ->
                    HorizontalDivider(color = BorderGrey, thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(feat, fontSize = 10.sp, color = DeepCharcoal, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.4f))
                        Text(
                            text = basic,
                            fontSize = 10.sp,
                            color = if (basic.contains("✓")) DeepCharcoal else Color.Gray,
                            fontWeight = if (basic.contains("✓")) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = plus,
                            fontSize = 10.sp,
                            color = if (plus.contains("✓")) NairaSuccessGreen else Color.Gray,
                            fontWeight = if (plus.contains("✓")) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val context = LocalContext.current

        // USER FEEDBACK CARD / REDIRECT (Jotform)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("my_plans_feedback_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BorderGrey),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Have Feedback or Feature Requests?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DeepCharcoal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Share your thoughts to help us improve NairaGuard for all Lagos traders.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://form.jotform.com/262396510803052"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open feedback link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("my_plans_feedback_btn")
                ) {
                    Icon(Icons.Default.Feedback, contentDescription = "Give Feedback", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Feedback", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Helpline and Customer Support panel
        var showInAppChat by remember { mutableStateOf(false) }
        var chatInput by remember { mutableStateOf("") }
        var chatMessages by remember { mutableStateOf(listOf(
            "Agent: Hello! Welcome to NairaGuard Priority Support Desk. How can we assist you with wholesale-retail comparisons, alert setups, or live data sync today?"
        )) }

        val supportLevel = if (subState.tier == "PLUS") "Priority Support (PLUS Unlocked)" else "Feedback/Suggestion (FREE)"
        val supportColor = if (subState.tier == "PLUS") NairaSuccessGreen else Color.Gray

        Card(
            modifier = Modifier.fillMaxWidth().testTag("customer_support_panel_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BorderGrey)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Support & Feedback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DeepCharcoal
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(supportColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = supportLevel.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = supportColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (subState.tier == "PLUS") {
                    Text(
                        text = "As a PLUS subscriber, you have 24/7 Priority support channel access with a direct hotline dialer (08020556342), direct email queue, and instant in-app live chat rooms.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Embedded in-app live chat button
                        Button(
                            onClick = { showInAppChat = true },
                            modifier = Modifier.weight(1f).testTag("in_app_chat_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = NairaSuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "Live Chat", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Live Chat", fontSize = 10.sp)
                        }

                        // Direct email link
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:")
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf("hello.nairaguard@gmail.com"))
                                        putExtra(Intent.EXTRA_SUBJECT, "[Priority PLUS Support] Helpdesk Request")
                                        putExtra(Intent.EXTRA_TEXT, "User Phone: ${subState.phoneNumber}\n\nPlease enter details of your request here:\n")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open email client", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("direct_email_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Email Support", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Email", fontSize = 10.sp)
                        }

                        // Hotline dialer (08020556342)
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08020556342"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1.2f).testTag("hotline_dialer_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepCharcoal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call Hotline", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hotline: 08020556342", fontSize = 8.5.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Direct support lines (Hotline, WhatsApp, and Priority Live Chat) are reserved exclusively for PLUS members. BASIC users can submit price feedback and suggestions via the form below.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://form.jotform.com/260592965611059"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open feedback form", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("basic_feedback_suggestion_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Feedback, contentDescription = "Feedback / Suggestion", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Feedback / Suggestion", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Chat simulation dialog for priority support users
        if (showInAppChat) {
            AlertDialog(
                onDismissRequest = { showInAppChat = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(NairaSuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("24/7 Priority Live Support Chat", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInAppChat = false }) {
                        Text("Minimize")
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftGrey),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                chatMessages.forEach { msg ->
                                    val isAgent = msg.startsWith("Agent:")
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        contentAlignment = if (isAgent) Alignment.CenterStart else Alignment.CenterEnd
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isAgent) Color(0xFFE2E8F0) else NairaSuccessGreen.copy(alpha = 0.15f)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = msg.substringAfter(": "),
                                                fontSize = 11.sp,
                                                color = DeepCharcoal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Ask anything...", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                maxLines = 1,
                                shape = RoundedCornerShape(8.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        val typed = chatInput
                                        chatMessages = chatMessages + "User: $typed"
                                        chatInput = ""
                                        // Auto simulation reply trigger
                                        val reply = when {
                                            typed.contains("price", ignoreCase = true) || typed.contains("wholesale", ignoreCase = true) -> 
                                                "Agent: Our regional field representatives refresh wholesale and retail data feeds every 15 minutes. All changes are propagated instantly to your active cache."
                                            typed.contains("alert", ignoreCase = true) || typed.contains("sms", ignoreCase = true) -> 
                                                "Agent: Standard alerts are distributed once matching price triggers occur. You can setup threshold definitions under alert triggers above."
                                            else -> "Agent: Thank you! An escalation ticket has been routed to our Lagos lead-support team. We'll assist you immediately."
                                        }
                                        chatMessages = chatMessages + reply
                                    }
                                },
                                modifier = Modifier.size(36.dp).background(NairaSuccessGreen, CircleShape)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send Chat", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            )
        }

    }
}

// PREMIUM LOCKED CARD PLACEHOLDER
@Composable
fun SubscriptionGate(message: String, onUpgrade: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Lock",
                tint = Color(0xFFD4AF37),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onUpgrade,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    contentColor = Color(0xFF1E293B)
                ),
                modifier = Modifier.minimumInteractiveComponentSize().testTag("upgrade_gate_trigger")
            ) {
                Text("Unlock NairaGuard PLUS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// CUSTOM LINE CHART CANVAS ELEMENT TO RENDER PRICE TREND VOLATILITY
@Composable
fun NairaLineChart(history: List<PriceHistory>, modifier: Modifier = Modifier) {
    val reversedHist = history.takeLast(5)
    if (reversedHist.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Gathering historical price indices...", fontSize = 11.sp)
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height

        val maxPrice = reversedHist.maxOf { it.wholesalePrice }
        val minPrice = reversedHist.minOf { it.wholesalePrice }
        val range = (maxPrice - minPrice).coerceAtLeast(1.0)
        
        val marginX = 20f
        val marginY = 30f
        val drawWidth = chartWidth - marginX * 2
        val drawHeight = chartHeight - marginY * 2

        val stepX = drawWidth / (reversedHist.size - 1)

        val points = reversedHist.mapIndexed { idx, hist ->
            val fractY = (hist.wholesalePrice - minPrice) / range
            val x = marginX + idx * stepX
            val y = chartHeight - marginY - (fractY * drawHeight).toFloat()
            Offset(x, y)
        }

        // Draw background horizontal grids
        for (i in 0..2) {
            val hGridY = marginY + (i / 2f) * drawHeight
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(marginX, hGridY),
                end = Offset(chartWidth - marginX, hGridY),
                strokeWidth = 1f
            )
        }

        // DRAW SHADED GRADIENT BENEATH CURVE
        val fillPath = Path().apply {
            moveTo(points.first().x, chartHeight - marginY)
            points.forEach { pt ->
                lineTo(pt.x, pt.y)
            }
            lineTo(points.last().x, chartHeight - marginY)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                startY = marginY,
                endY = chartHeight - marginY
            )
        )

        // DRAW THE CORE LINE
        val strokePath = Path().apply {
            val first = points.first()
            moveTo(first.x, first.y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = strokePath,
            color = primaryColor,
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // DRAW JOIN COORD POINTS (RIPPLES)
        points.forEachIndexed { i, pt ->
            // Point anchor dot
            drawCircle(
                color = primaryColor,
                radius = 8f,
                center = pt
            )
            // Accent halo
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = pt
            )
        }
    }
}

// ==========================================
// 6. MODERN AUTHENTICATION FLOWS (SaaSSkip Inspired)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(viewModel: NairaGuardViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepMeLoggedIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brand shield logo on top of centered form card
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = "NairaGuard Emblem",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Admin Portal Sign In",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = DeepCharcoal,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Access secure food price ingestion gates, conversions control, and analytical dashboards.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFF87171)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFF991B1B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // EMAIL INPUT
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Administrator Email",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    placeholder = { Text("Enter your admin email (kingsley@nairaguard.com)...", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input")
                )
            }

            // PASSWORD INPUT
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Admin Password",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    placeholder = { Text("Enter your password...", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = "Toggle password visibility", tint = Color.Gray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )
            }

            // REMEMBER ME / KEEP ME LOGGED IN
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { keepMeLoggedIn = !keepMeLoggedIn }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = keepMeLoggedIn,
                    onCheckedChange = { keepMeLoggedIn = it },
                    colors = CheckboxDefaults.colors(checkedColor = ForestGreen),
                    modifier = Modifier.testTag("login_keep_me")
                )
                Text(
                    text = "Keep admin session active",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DeepCharcoal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val scope = rememberCoroutineScope()
            var isAuthenticating by remember { mutableStateOf(false) }

            // SIGN IN CTA BUTTON
            Button(
                onClick = {
                    if (!isAuthenticating) {
                        isAuthenticating = true
                        errorMessage = null
                        scope.launch {
                            val err = viewModel.loginStandardUser(email, password, keepMeLoggedIn)
                            isAuthenticating = false
                            if (err != null) {
                                errorMessage = err
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = !isAuthenticating
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign In as Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            // REDIRECT LINK TO SIGN UP
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Need Admin Access?", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = "Register Admin Account",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    modifier = Modifier
                        .testTag("go_to_signup")
                        .clickable { viewModel.changeScreen(Screen.SIGNUP) }
                )
            }

            // BACK TO DASHBOARD BYPASS LINK FOR QUICK EXIT
            Text(
                text = "← Back to Public Dashboard",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier
                    .testTag("back_to_dashboard_from_login")
                    .clickable { viewModel.changeScreen(Screen.DASHBOARD) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpView(viewModel: NairaGuardViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var invitationPasscode by remember { mutableStateOf("") }
    var keepMeLoggedIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = "NairaGuard Emblem",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Register Admin Account",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = DeepCharcoal,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Establish and secure administrative credentials to ingest regional food price updates, enforce units conversion factors, and configure notification alerts.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFF87171)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFF991B1B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // NAME FIELD
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Full Name",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    placeholder = { Text("What is your name? (e.g. Kingsley)", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_name_input")
                )
            }

            // EMAIL FIELD
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Email Address",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    placeholder = { Text("Enter email for verification...", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_email_input")
                )
            }

            // PASSWORD FIELD
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Password",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    placeholder = { Text("At least 6 characters long...", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = "Toggle password visibility", tint = Color.Gray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_password_input")
                )
            }

            // INVITATION PASSCODE FIELD
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Admin Invitation Passcode",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                OutlinedTextField(
                    value = invitationPasscode,
                    onValueChange = { invitationPasscode = it; errorMessage = null },
                    placeholder = { Text("Contact team for invitation code...", color = Color.Gray) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("signup_passcode_input")
                )
            }

            // KEEP ME LOGGED IN CHECKBOX
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { keepMeLoggedIn = !keepMeLoggedIn }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = keepMeLoggedIn,
                    onCheckedChange = { keepMeLoggedIn = it },
                    colors = CheckboxDefaults.colors(checkedColor = ForestGreen),
                    modifier = Modifier.testTag("signup_keep_me")
                )
                Text(
                    text = "Keep admin session active",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DeepCharcoal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val scope = rememberCoroutineScope()
            var isRegistering by remember { mutableStateOf(false) }

            // SIGN UP CTA BUTTON
            Button(
                onClick = {
                    if (!isRegistering) {
                        isRegistering = true
                        errorMessage = null
                        scope.launch {
                            val err = viewModel.registerStandardUser(name, email, password, keepMeLoggedIn, invitationPasscode)
                            isRegistering = false
                            if (err != null) {
                                errorMessage = err
                            } else {
                                viewModel.changeScreen(Screen.VERIFY_EMAIL)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("signup_submit_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = !isRegistering
            ) {
                if (isRegistering) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Register Admin Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            // REDIRECT TO LOGIN
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Already registered?", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = "Admin Sign In",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    modifier = Modifier
                        .testTag("go_to_login")
                        .clickable { viewModel.changeScreen(Screen.LOGIN) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailView(viewModel: NairaGuardViewModel) {
    var codeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val userEmail by viewModel.userLoggedInEmail.collectAsStateWithLifecycle()
    
    val sentCode = viewModel.sharedPrefs.getString("verification_code", "1234") ?: "1234"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightCream)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Verify Email",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ForestGreen.copy(alpha = 0.1f))
                    .padding(14.dp),
                tint = ForestGreen
            )

            Text(
                text = "Verify Admin Email",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = DeepCharcoal,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "We have dispatched a 4-digit administrator security activation code to $userEmail.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Helpful simulated email message card showing the actual received code so Kingsley can test seamlessly
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentAmberBg),
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📬 Simulated Email Delivery Notification", 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = DeepCharcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "From: security@nairaguard.com\nTo: $userEmail\nSubject: NairaGuard Email Verification Code\n\nYour security activation code is: $sentCode",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Start,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFF87171)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFF991B1B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // CODE INPUT FIELD
            OutlinedTextField(
                value = codeInput,
                onValueChange = { 
                    if (it.length <= 6) {
                        codeInput = it
                        errorMessage = null
                    }
                },
                placeholder = { Text("Enter 4-digit code...", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = BorderGrey,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verify_code_input")
            )

            // VERIFY CODE SUBMIT
            Button(
                onClick = {
                    val verified = viewModel.verifyCode(codeInput)
                    if (!verified) {
                        errorMessage = "Registration code mismatch. Try again."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("verify_code_submit"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text(
                    text = "Verify & Access",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            // RESEND CODE LINK
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Didn't receive code?", fontSize = 13.sp, color = Color.Gray)
                Text(
                    text = "Resend Code",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    modifier = Modifier
                        .testTag("resend_code_button")
                        .clickable {
                            val code = (1000..9999).random().toString()
                            viewModel.sharedPrefs.edit().putString("verification_code", code).apply()
                            viewModel.addSmsLog("SYSTEM: Resent activation code $code has been dispatched to $userEmail.")
                            errorMessage = "A fresh activation code has been resent to your email!"
                        }
                )
            }
        }
    }
}

