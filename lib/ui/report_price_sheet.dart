import 'package:flutter/material.dart';
import '../services/whatsapp_service.dart';

/// Lightweight Flutter bottom sheet component to launch 1-tap WhatsApp price report flow.
class ReportPriceSheet extends StatefulWidget {
  final String commodityName;
  final String marketLocation;
  final String? currentPrice;

  const ReportPriceSheet({
    super.key,
    required this.commodityName,
    required this.marketLocation,
    this.currentPrice,
  });

  /// Helper method to trigger the modal bottom sheet cleanly from anywhere in the app
  static Future<void> show(
    BuildContext context, {
    required String commodityName,
    required String marketLocation,
    String? currentPrice,
  }) {
    return showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => ReportPriceSheet(
        commodityName: commodityName,
        marketLocation: marketLocation,
        currentPrice: currentPrice,
      ),
    );
  }

  @override
  State<ReportPriceSheet> createState() => _ReportPriceSheetState();
}

class _ReportPriceSheetState extends State<ReportPriceSheet> {
  String _selectedReportType = "Inaccuracy"; // "Inaccuracy" or "Suggestion"

  @override
  Widget build(BuildContext context) {
    const colorNairaSuccessGreen = Color(0xFF059669);
    const colorWhatsAppGreen = Color(0xFF25D366);

    return Container(
      decoration: const BoxDecoration(
        color: Color(0xFF09090B), // Black theme matching waitlist modal
        borderRadius: BorderRadius.vertical(top: Radius.circular(24.0)),
        border: Border(
          top: BorderSide(color: Color(0xFF27272A), width: 1.0),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 24.0),
      child: SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Drag handle bar
            Center(
              child: Container(
                width: 36.0,
                height: 4.0,
                margin: const EdgeInsets.only(bottom: 16.0),
                decoration: BoxDecoration(
                  color: const Color(0xFF3F3F46),
                  borderRadius: BorderRadius.circular(2.0),
                ),
              ),
            ),

            // Header Row
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8.0),
                  decoration: BoxDecoration(
                    color: const Color(0xFF064E3B),
                    borderRadius: BorderRadius.circular(10.0),
                  ),
                  child: const Icon(
                    Icons.rate_review_rounded,
                    color: Color(0xFF34D399),
                    size: 20.0,
                  ),
                ),
                const SizedBox(width: 12.0),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        "Report Price Update",
                        style: TextStyle(
                          fontSize: 18.0,
                          fontWeight: FontWeight.extrabold,
                          color: Colors.white,
                          letterSpacing: -0.3,
                        ),
                      ),
                      Text(
                        "${widget.commodityName} • ${widget.marketLocation}",
                        style: const TextStyle(
                          fontSize: 12.0,
                          color: Color(0xFFA1A1AA),
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.white70, size: 20.0),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),

            const SizedBox(height: 20.0),

            // Toggle Report Type
            const Text(
              "Report Type",
              style: TextStyle(
                fontSize: 12.0,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 8.0),
            Row(
              children: [
                Expanded(
                  child: _buildTypeChip(
                    label: "Report Inaccuracy",
                    icon: Icons.warning_amber_rounded,
                    value: "Inaccuracy",
                    selectedColor: colorNairaSuccessGreen,
                  ),
                ),
                const SizedBox(width: 10.0),
                Expanded(
                  child: _buildTypeChip(
                    label: "Suggest Staple",
                    icon: Icons.lightbulb_outline_rounded,
                    value: "Suggestion",
                    selectedColor: colorNairaSuccessGreen,
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16.0),

            // Item Details Card
            Container(
              padding: const EdgeInsets.all(14.0),
              decoration: BoxDecoration(
                color: const Color(0xFF18181B),
                borderRadius: BorderRadius.circular(12.0),
                border: Border.all(color: const Color(0xFF27272A)),
              ),
              child: Column(
                children: [
                  _buildDetailRow("Commodity", widget.commodityName),
                  const Divider(color: Color(0xFF27272A), height: 16.0),
                  _buildDetailRow("Market Hub", widget.marketLocation),
                  if (widget.currentPrice != null && widget.currentPrice!.isNotEmpty) ...[
                    const Divider(color: Color(0xFF27272A), height: 16.0),
                    _buildDetailRow("Current Price", widget.currentPrice!),
                  ],
                ],
              ),
            ),

            const SizedBox(height: 20.0),

            // Primary WhatsApp CTA Button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: colorWhatsAppGreen,
                  foregroundColor: Colors.black,
                  padding: const EdgeInsets.symmetric(vertical: 14.0),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12.0),
                  ),
                  elevation: 0,
                ),
                onPressed: () {
                  Navigator.pop(context);
                  WhatsappService.launchPriceReportWhatsApp(
                    commodity: widget.commodityName,
                    market: widget.marketLocation,
                    currentPrice: widget.currentPrice,
                    reportType: _selectedReportType,
                    context: context,
                  );
                },
                icon: const Icon(
                  Icons.chat_bubble_rounded,
                  color: Colors.black,
                  size: 20.0,
                ),
                label: const Text(
                  "Open in WhatsApp",
                  style: TextStyle(
                    fontSize: 15.0,
                    fontWeight: FontWeight.bold,
                    color: Colors.black,
                  ),
                ),
              ),
            ),

            const SizedBox(height: 12.0),

            // Micro-copy Note
            Container(
              padding: const EdgeInsets.all(10.0),
              decoration: BoxDecoration(
                color: const Color(0xFF18181B),
                borderRadius: BorderRadius.circular(8.0),
                border: Border.all(color: const Color(0xFF27272A)),
              ),
              child: const Row(
                children: [
                  Icon(Icons.mic_none_rounded, color: Color(0xFF34D399), size: 16.0),
                  SizedBox(width: 8.0),
                  Expanded(
                    child: Text(
                      "Tip: You can send us a voice note or photo of your market receipt on WhatsApp!",
                      style: TextStyle(
                        fontSize: 11.5,
                        color: Color(0xFFCBD5E1),
                        height: 1.3,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTypeChip({
    required String label,
    required IconData icon,
    required String value,
    required Color selectedColor,
  }) {
    final isSelected = _selectedReportType == value;
    return GestureDetector(
      onTap: () {
        setState(() {
          _selectedReportType = value;
        });
      },
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(vertical: 10.0, horizontal: 12.0),
        decoration: BoxDecoration(
          color: isSelected ? selectedColor : const Color(0xFF18181B),
          borderRadius: BorderRadius.circular(10.0),
          border: Border.all(
            color: isSelected ? selectedColor : const Color(0xFF3F3F46),
            width: 1.5,
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 16.0,
              color: Colors.white,
            ),
            const SizedBox(width: 6.0),
            Flexible(
              child: Text(
                label,
                style: const TextStyle(
                  fontSize: 12.0,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          label,
          style: const TextStyle(
            fontSize: 12.0,
            color: Color(0xFFA1A1AA),
          ),
        ),
        Text(
          value,
          style: const TextStyle(
            fontSize: 12.5,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
      ],
    );
  }
}
