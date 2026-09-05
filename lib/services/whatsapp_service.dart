import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

/// Service for handling direct WhatsApp integrations for NairaGuard traders.
class WhatsappService {
  /// Configurable Admin WhatsApp Number for NairaGuard (Nigeria country code: +234)
  static const String adminWhatsAppNumber = "2348020556342";

  /// Launches WhatsApp with dynamic pre-filled text for reporting price updates or staples.
  static Future<void> launchPriceReportWhatsApp({
    required String commodity,
    required String market,
    String? currentPrice,
    String? reportType,
    BuildContext? context,
  }) async {
    final typeText = reportType ?? "Inaccuracy / Suggestion";
    final priceText = currentPrice != null && currentPrice.isNotEmpty
        ? (currentPrice.startsWith('₦') ? currentPrice : '₦$currentPrice')
        : 'N/A';

    final messageBuffer = StringBuffer()
      ..writeln("Hi NairaGuard Team! 🇳🇬")
      ..writeln("I want to report a price update:")
      ..writeln("• Type: $typeText")
      ..writeln("• Commodity: $commodity")
      ..writeln("• Market: $market")
      ..writeln("• Current Listed Price: $priceText")
      ..write("• Notes / New Price: ");

    final encodedMessage = Uri.encodeComponent(messageBuffer.toString());
    final urlString = "https://wa.me/$adminWhatsAppNumber?text=$encodedMessage";
    final uri = Uri.parse(urlString);

    try {
      final launched = await launchUrl(
        uri,
        mode: LaunchMode.externalApplication,
      );

      if (!launched && context != null && context.mounted) {
        _showErrorSnackBar(
          context,
          "Could not open WhatsApp. Please verify WhatsApp is installed on your device.",
        );
      }
    } catch (e) {
      if (context != null && context.mounted) {
        _showErrorSnackBar(
          context,
          "Failed to launch WhatsApp link. Please check your connectivity or WhatsApp app.",
        );
      }
    }
  }

  static void _showErrorSnackBar(BuildContext context, String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(
          message,
          style: const TextStyle(color: Colors.white, fontSize: 13.0),
        ),
        backgroundColor: const Color(0xFFDC2626), // Red alert
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10.0),
        ),
        duration: const Duration(seconds: 4),
      ),
    );
  }
}
