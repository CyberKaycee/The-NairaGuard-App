import 'package:flutter/material.dart';
import 'package:lottie/lottie.dart';
import 'package:url_launcher/url_launcher.dart';
import '../services/waitlist_service.dart';

/// Modal dialog capturing feature demand signals and adding users to the PLUS Priority Waitlist.
class PlusWaitlistModal extends StatefulWidget {
  final String entryPoint;
  final String defaultPhoneNumber;
  final String userId;
  final WaitlistApiService? apiService;

  const PlusWaitlistModal({
    super.key,
    required this.entryPoint,
    this.defaultPhoneNumber = '',
    this.userId = 'NG-USER-8821',
    this.apiService,
  });

  /// Helper launcher to open the modal bottom sheet smoothly across screens.
  static Future<void> show(
    BuildContext context, {
    required String entryPoint,
    String defaultPhoneNumber = '',
    String userId = 'NG-USER-8821',
  }) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => Padding(
        padding: EdgeInsets.only(
          bottom: MediaQuery.of(context).viewInsets.bottom,
        ),
        child: PlusWaitlistModal(
          entryPoint: entryPoint,
          defaultPhoneNumber: defaultPhoneNumber,
          userId: userId,
        ),
      ),
    );
  }

  @override
  State<PlusWaitlistModal> createState() => _PlusWaitlistModalState();
}

class _PlusWaitlistModalState extends State<PlusWaitlistModal> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _phoneController;
  late WaitlistApiService _apiService;

  String _selectedLocation = 'Mile 12 (Mainland)';
  bool _isSubmitting = false;
  bool _isSubmittedSuccess = false;
  String? _errorMessage;

  final List<String> _marketLocations = [
    'Mile 12 (Mainland)',
    'Isale Eko (Island)',
    'Ikorodu Central',
    'Lekki Ultra-Modern',
    'Bodija Market (Ibadan)',
    'Onitsha Main Market',
    'Other / Mobile Outlet',
  ];

  @override
  void initState() {
    super.initState();
    _phoneController = TextEditingController(text: widget.defaultPhoneNumber);
    _apiService = widget.apiService ?? WaitlistApiService();
  }

  @override
  void dispose() {
    _phoneController.dispose();
    super.dispose();
  }

  /// Validates Nigerian phone numbers format (080, 081, 070, 090, +234...)
  String? _validateNigerianPhone(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Please enter your Phone / WhatsApp number';
    }
    final clean = value.replaceAll(RegExp(r'[\s\-]'), '');
    final ngPhoneRegex = RegExp(r'^(?:\+234|234|0)[789][01]\d{8}$');
    if (!ngPhoneRegex.hasMatch(clean)) {
      return 'Enter a valid Nigerian phone (e.g. 08012345678 or +23480...)';
    }
    return null;
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isSubmitting = true;
      _errorMessage = null;
    });

    final entry = WaitlistEntry(
      userId: widget.userId,
      phoneNumber: _phoneController.text.trim(),
      marketLocation: _selectedLocation,
      entryPoint: widget.entryPoint,
      timestamp: DateTime.now().millisecondsSinceEpoch,
      status: 'pending_priority',
    );

    final success = await _apiService.submitWaitlistEntry(entry);

    if (mounted) {
      setState(() {
        _isSubmitting = false;
        if (success) {
          _isSubmittedSuccess = true;
          _launchWhatsAppChat();
        } else {
          _errorMessage = 'Could not process submission. Please check network connection.';
        }
      });
    }
  }

  Future<void> _launchWhatsAppChat() async {
    final phone = _phoneController.text.replaceAll(RegExp(r'[^\d+]'), '');
    final message = Uri.encodeComponent(
      "Hello NairaGuard Team! I just joined the PLUS waitlist for my market at $_selectedLocation (Feature requested: ${widget.entryPoint}). "
      "My phone is $phone.",
    );
    final whatsappUrl = Uri.parse("https://wa.me/2348020556342?text=$message");

    if (await canLaunchUrl(whatsappUrl)) {
      await launchUrl(whatsappUrl, mode: LaunchMode.externalApplication);
    } else {
      // Fallback fallback URL launch
      await launchUrl(whatsappUrl, mode: LaunchMode.platformDefault);
    }
  }

  @override
  Widget build(BuildContext context) {
    const colorNairaSuccessGreen = Color(0xFF059669);

    return Container(
      decoration: const BoxDecoration(
        color: Color(0xFF09090B), // Black / ultra-dark background
        borderRadius: BorderRadius.vertical(top: Radius.circular(24.0)),
        border: Border(
          top: BorderSide(color: Color(0xFF27272A), width: 1.0),
        ),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 24.0),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Handle bar
            Center(
              child: Container(
                width: 40.0,
                height: 4.0,
                margin: const EdgeInsets.only(bottom: 16.0),
                decoration: BoxDecoration(
                  color: const Color(0xFF3F3F46),
                  borderRadius: BorderRadius.circular(2.0),
                ),
              ),
            ),

            if (_isSubmittedSuccess)
              _buildSuccessView(context)
            else
              _buildFormView(context, colorNairaSuccessGreen),
          ],
        ),
      ),
    );
  }

  Widget _buildFormView(
    BuildContext context,
    Color colorNairaSuccessGreen,
  ) {
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header Badge
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 4.0),
                decoration: BoxDecoration(
                  color: const Color(0xFF2E1065),
                  border: Border.all(color: const Color(0xFF7C3AED)),
                  borderRadius: BorderRadius.circular(50.0),
                ),
                child: const Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.star_rounded, size: 14.0, color: Color(0xFFA7F3D0)),
                    SizedBox(width: 4.0),
                    Text(
                      "PLUS EXCLUSIVE",
                      style: TextStyle(
                        fontSize: 10.0,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                  ],
                ),
              ),
              const Spacer(),
              IconButton(
                icon: const Icon(Icons.close, size: 20.0, color: Colors.white70),
                onPressed: () => Navigator.pop(context),
              ),
            ],
          ),
          const SizedBox(height: 12.0),

          // Headline
          const Text(
            "NairaGuard PLUS is Launching Soon!",
            style: TextStyle(
              fontSize: 20.0,
              fontWeight: FontWeight.extrabold,
              color: Colors.white,
              letterSpacing: -0.5,
            ),
          ),
          const SizedBox(height: 6.0),

          // Body Description
          const Text(
            "Upgrade billing is temporarily paused while we optimize local server capacity. "
            "Join our priority waitlist to unlock early access to regional price gaps, smart margin calculators, and automated restock alerts.",
            style: TextStyle(
              fontSize: 12.5,
              color: Color(0xFFCBD5E1),
              height: 1.4,
            ),
          ),
          const SizedBox(height: 16.0),

          // Feature Bullets Preview
          Container(
            padding: const EdgeInsets.all(12.0),
            decoration: BoxDecoration(
              color: const Color(0xFF18181B),
              borderRadius: BorderRadius.circular(12.0),
              border: Border.all(color: const Color(0xFF27272A)),
            ),
            child: const Column(
              children: [
                _FeatureRow(
                  icon: Icons.map_outlined,
                  text: "Mainland vs. Island discrete wholesale price breakdowns",
                ),
                SizedBox(height: 8.0),
                _FeatureRow(
                  icon: Icons.calculate_outlined,
                  text: "Smart Margin & Bag-to-Cup profit margin calculators",
                ),
                SizedBox(height: 8.0),
                _FeatureRow(
                  icon: Icons.notifications_active_outlined,
                  text: "Instant WhatsApp SMS restock volatility alert triggers",
                ),
              ],
            ),
          ),
          const SizedBox(height: 16.0),

          // Phone Number Field
          const Text(
            "WhatsApp / Phone Number",
            style: TextStyle(
              fontSize: 12.0,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 6.0),
          TextFormField(
            controller: _phoneController,
            style: const TextStyle(color: Colors.white, fontSize: 13.5),
            keyboardType: TextInputType.phone,
            validator: _validateNigerianPhone,
            decoration: InputDecoration(
              hintText: "e.g. 08012345678 or +234...",
              hintStyle: const TextStyle(color: Colors.white38, fontSize: 13.0),
              prefixIcon: const Icon(Icons.phone_android, size: 18.0, color: Colors.white70),
              filled: true,
              fillColor: const Color(0xFF18181B),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 12.0),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(10.0),
                borderSide: const BorderSide(color: Color(0xFF3F3F46)),
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(10.0),
                borderSide: const BorderSide(color: Color(0xFF3F3F46)),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(10.0),
                borderSide: const BorderSide(color: Color(0xFF10B981), width: 1.5),
              ),
            ),
          ),
          const SizedBox(height: 12.0),

          // Market Location Dropdown
          const Text(
            "Primary Operating Market",
            style: TextStyle(
              fontSize: 12.0,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 6.0),
          DropdownButtonFormField<String>(
            value: _selectedLocation,
            dropdownColor: const Color(0xFF18181B),
            style: const TextStyle(color: Colors.white, fontSize: 13.0),
            iconEnabledColor: Colors.white70,
            items: _marketLocations.map((loc) {
              return DropdownMenuItem(
                value: loc,
                child: Text(loc, style: const TextStyle(color: Colors.white, fontSize: 13.0)),
              );
            }).toList(),
            onChanged: (val) {
              if (val != null) {
                setState(() => _selectedLocation = val);
              }
            },
            decoration: InputDecoration(
              prefixIcon: const Icon(Icons.storefront_outlined, size: 18.0, color: Colors.white70),
              filled: true,
              fillColor: const Color(0xFF18181B),
              contentPadding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 12.0),
              enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(10.0),
                borderSide: const BorderSide(color: Color(0xFF3F3F46)),
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(10.0),
                borderSide: const BorderSide(color: Color(0xFF3F3F46)),
              ),
              focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(10.0),
                borderSide: const BorderSide(color: Color(0xFF10B981), width: 1.5),
              ),
            ),
          ),
          const SizedBox(height: 16.0),

          if (_errorMessage != null) ...[
            Text(
              _errorMessage!,
              style: const TextStyle(color: Color(0xFFF87171), fontSize: 11.5),
            ),
            const SizedBox(height: 10.0),
          ],

          // Primary CTA Button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: colorNairaSuccessGreen,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 14.0),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10.0),
                ),
                elevation: 0,
              ),
              onPressed: _isSubmitting ? null : _handleSubmit,
              child: _isSubmitting
                  ? const SizedBox(
                      height: 20.0,
                      width: 20.0,
                      child: CircularProgressIndicator(
                        strokeWidth: 2.0,
                        color: Colors.white,
                      ),
                    )
                  : const Text(
                      "Join Priority Waitlist",
                      style: TextStyle(
                        fontSize: 14.0,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
            ),
          ),
          const SizedBox(height: 8.0),

          // Secondary CTA Button
          Center(
            child: TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text(
                "Keep opening the app",
                style: TextStyle(
                  color: Color(0xFFA1A1AA),
                  fontSize: 13.0,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSuccessView(BuildContext context) {
    const colorNairaSuccessGreen = Color(0xFF059669);

    return Column(
      children: [
        SizedBox(
          height: 100.0,
          width: 100.0,
          child: Lottie.network(
            'https://assets9.lottiefiles.com/packages/lf20_pqn0lazp.json',
            repeat: false,
            fit: BoxFit.contain,
            errorBuilder: (context, error, stackTrace) {
              return TweenAnimationBuilder<double>(
                tween: Tween<double>(begin: 0.0, end: 1.0),
                duration: const Duration(milliseconds: 500),
                curve: Curves.elasticOut,
                builder: (context, value, child) {
                  return Transform.scale(
                    scale: value,
                    child: Container(
                      decoration: const BoxDecoration(
                        color: Color(0xFF064E3B),
                        shape: BoxShape.circle,
                      ),
                      child: const Icon(
                        Icons.check_circle_rounded,
                        color: Color(0xFF34D399),
                        size: 64.0,
                      ),
                    ),
                  );
                },
              );
            },
          ),
        ),
        const SizedBox(height: 12.0),
        const Text(
          "You're on the Priority List!",
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 20.0,
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        const SizedBox(height: 8.0),
        Text(
          "We recorded your interest for market point: '$_selectedLocation'. "
          "You will receive an instant SMS / WhatsApp invite as soon as PLUS features roll out.",
          textAlign: TextAlign.center,
          style: const TextStyle(
            fontSize: 13.0,
            color: Color(0xFFCBD5E1),
            height: 1.4,
          ),
        ),
        const SizedBox(height: 20.0),

        // WhatsApp direct chat card
        Container(
          padding: const EdgeInsets.all(14.0),
          decoration: BoxDecoration(
            color: const Color(0xFF18181B),
            border: Border.all(color: const Color(0xFF059669)),
            borderRadius: BorderRadius.circular(12.0),
          ),
          child: Column(
            children: [
              const Text(
                "Have urgent questions or specific commodity feedback?",
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 12.0,
                  fontWeight: FontWeight.w600,
                  color: Colors.white,
                ),
              ),
              const SizedBox(height: 10.0),
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF25D366), // WhatsApp Green
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 10.0),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8.0),
                  ),
                ),
                onPressed: _launchWhatsAppChat,
                icon: const Icon(Icons.chat_bubble_outline, size: 18.0),
                label: const Text(
                  "Chat on WhatsApp with Team",
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16.0),

        SizedBox(
          width: double.infinity,
          child: OutlinedButton(
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 12.0),
              side: const BorderSide(color: Color(0xFF3F3F46)),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10.0),
              ),
            ),
            onPressed: () => Navigator.pop(context),
            child: const Text(
              "Return to Dashboard",
              style: TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _FeatureRow extends StatelessWidget {
  final IconData icon;
  final String text;

  const _FeatureRow({required this.icon, required this.text});

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 16.0, color: const Color(0xFF34D399)),
        const SizedBox(width: 8.0),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              fontSize: 11.5,
              fontWeight: FontWeight.w500,
              color: Color(0xFFE2E8F0),
            ),
          ),
        ),
      ],
    );
  }
}
