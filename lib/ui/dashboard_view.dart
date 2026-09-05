import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'plus_waitlist_modal.dart';
import 'report_price_sheet.dart';
import '../services/whatsapp_service.dart';
// Note: Depending on your choice of state management, you can use:
// - Riverpod (Recommended for declarative state synchronization)
// - BLoC / Cubit (Enterprise-grade event-driven flow)
// - Provider / ChangeNotifier (Simpler StateFlow equivalents)

// =========================================================================
// 1. DATA MODELS & DOMAIN LAYER
// =========================================================================

class Commodity {
  final int id;
  final String name;
  final String category;
  final double conversionFactor;

  const Commodity({
    required this.id,
    required this.name,
    required this.category,
    required this.conversionFactor,
  });
}

class PriceLog {
  final int commodityId;
  final String marketLocation;
  final double wholesalePrice;
  final double retailPrice;

  const PriceLog({
    required this.commodityId,
    required this.marketLocation,
    required this.wholesalePrice,
    required this.retailPrice,
  });
}

class SubscriptionState {
  final String tier; // "FREE" or "PLUS"
  final String status; // "Active", "Trial", "Expired", "None"
  final int? trialStartDate; // Milliseconds timestamp
  final int? expiryDate; // Milliseconds timestamp
  final bool hasUsedTrial;
  final String phoneNumber;

  const SubscriptionState({
    required this.tier,
    required this.status,
    this.trialStartDate,
    this.expiryDate,
    this.hasUsedTrial = false,
    required this.phoneNumber,
  });

  SubscriptionState copyWith({
    String? tier,
    String? status,
    int? trialStartDate,
    int? expiryDate,
    bool? hasUsedTrial,
    String? phoneNumber,
  }) {
    return SubscriptionState(
      tier: tier ?? this.tier,
      status: status ?? this.status,
      trialStartDate: trialStartDate ?? this.trialStartDate,
      expiryDate: expiryDate ?? this.expiryDate,
      hasUsedTrial: hasUsedTrial ?? this.hasUsedTrial,
      phoneNumber: phoneNumber ?? this.phoneNumber,
    );
  }
}

// =========================================================================
// 2. STATE MANAGEMENT (RIVERPOD IMPLEMENTATION)
// Maps directly to NairaGuardViewModel in Jetpack Compose
// =========================================================================

class NairaGuardState {
  final List<Commodity> commodities;
  final List<PriceLog> prices;
  final SubscriptionState subscription;
  final String selectedCategory;
  final String searchQuery;
  final String userLoggedInName;
  final bool userIsLoggedIn;
  final bool userIsVerified;
  final String selectedMarketFilter;

  NairaGuardState({
    required this.commodities,
    required this.prices,
    required this.subscription,
    required this.selectedCategory,
    required this.searchQuery,
    required this.userLoggedInName,
    required this.userIsLoggedIn,
    required this.userIsVerified,
    required this.selectedMarketFilter,
  });

  NairaGuardState copyWith({
    List<Commodity>? commodities,
    List<PriceLog>? prices,
    SubscriptionState? subscription,
    String? selectedCategory,
    String? searchQuery,
    String? userLoggedInName,
    bool? userIsLoggedIn,
    bool? userIsVerified,
    String? selectedMarketFilter,
  }) {
    return NairaGuardState(
      commodities: commodities ?? this.commodities,
      prices: prices ?? this.prices,
      subscription: subscription ?? this.subscription,
      selectedCategory: selectedCategory ?? this.selectedCategory,
      searchQuery: searchQuery ?? this.searchQuery,
      userLoggedInName: userLoggedInName ?? this.userLoggedInName,
      userIsLoggedIn: userIsLoggedIn ?? this.userIsLoggedIn,
      userIsVerified: userIsVerified ?? this.userIsVerified,
      selectedMarketFilter: selectedMarketFilter ?? this.selectedMarketFilter,
    );
  }
}

class NairaGuardViewModel extends StateNotifier<NairaGuardState> {
  NairaGuardViewModel()
      : super(NairaGuardState(
          commodities: _dummyCommodities,
          prices: _dummyPrices,
          subscription: const SubscriptionState(
            tier: "FREE",
            status: "None",
            phoneNumber: "",
          ),
          selectedCategory: "All",
          searchQuery: "",
          userLoggedInName: "",
          userIsLoggedIn: true,
          userIsVerified: true,
          selectedMarketFilter: "All Lagos",
        ));

  bool checkTrialExpiration() {
    final sub = state.subscription;
    if (sub.tier == "PLUS" && sub.status == "Trial" && sub.expiryDate != null) {
      final now = DateTime.now().millisecondsSinceEpoch;
      if (now >= sub.expiryDate!) {
        state = state.copyWith(
          subscription: sub.copyWith(
            tier: "FREE",
            status: "Expired",
          ),
        );
        return true;
      }
    }
    return false;
  }

  String getTrialRemainingText() {
    checkTrialExpiration();
    final sub = state.subscription;
    if (sub.tier == "PLUS" && sub.status == "Trial" && sub.expiryDate != null) {
      final now = DateTime.now().millisecondsSinceEpoch;
      final remaining = sub.expiryDate! - now;
      if (remaining <= 0) return "Expired";
      final days = remaining ~/ (24 * 60 * 60 * 1000);
      final hours = (remaining % (24 * 60 * 60 * 1000)) ~/ (60 * 60 * 1000);
      return days > 0 ? "${days}d ${hours}h left" : "${hours}h left";
    } else if (sub.tier == "PLUS") {
      return "Active";
    } else if (sub.hasUsedTrial) {
      return "Trial Used";
    } else {
      return "7d Trial";
    }
  }

  String activatePlusTrial() {
    checkTrialExpiration();
    if (state.subscription.hasUsedTrial || state.subscription.trialStartDate != null) {
      return "You have already used your 1-time 7-day Free Trial.";
    }
    final now = DateTime.now().millisecondsSinceEpoch;
    final sevenDays = 7 * 24 * 60 * 60 * 1000;
    final expiry = now + sevenDays;

    state = state.copyWith(
      subscription: SubscriptionState(
        tier: "PLUS",
        status: "Trial",
        trialStartDate: now,
        expiryDate: expiry,
        hasUsedTrial: true,
        phoneNumber: state.subscription.phoneNumber,
      ),
    );
    return "7-Day PLUS Free Trial Activated! Enjoy full access for 7 days.";
  }

  void setSearchQuery(String query) {
    state = state.copyWith(searchQuery: query);
  }

  void setSelectedCategory(String category) {
    state = state.copyWith(selectedCategory: category);
  }

  void setMarketFilter(String filter) {
    state = state.copyWith(selectedMarketFilter: filter);
  }

  void updatePrice(int commodityId, String location, double wholesale, double retail) {
    final updatedPrices = state.prices.map((p) {
      if (p.commodityId == commodityId && p.marketLocation == location) {
        return PriceLog(
          commodityId: commodityId,
          marketLocation: location,
          wholesalePrice: wholesale,
          retailPrice: retail,
        );
      }
      return p;
    }).toList();
    state = state.copyWith(prices: updatedPrices);
  }

  void submitUserFeedback({
    required String type,
    int? commodityId,
    required String location,
    required double wholesale,
    required double retail,
    required String message,
  }) {
    // In actual implementation, send to remote database / API
    debugPrint("Feedback submitted: $type for ID $commodityId in $location");
  }

  void changeScreen(String screenName) {
    debugPrint("Navigate to screen: $screenName");
  }
}

final nairaGuardProvider = StateNotifierProvider<NairaGuardViewModel, NairaGuardState>((ref) {
  return NairaGuardViewModel();
});

// =========================================================================
// 3. COLOR THEME PALETTE
// =========================================================================

const Color colorLightCream = Color(0xFFFAF7F2);
const Color colorDeepCharcoal = Color(0xFF1E293B);
const Color colorBorderGrey = Color(0xFFE2E8F0);
const Color colorSoftGrey = Color(0xFFF1F5F9);

const Color colorAccentBlueBg = Color(0xFFEFF6FF);
const Color colorAccentBlueBorder = Color(0xFFBFDBFE);
const Color colorAccentBlueText = Color(0xFF2563EB);

const Color colorAccentGreenBg = Color(0xFFECFDF5);
const Color colorAccentGreenBorder = Color(0xFFA7F3D0);
const Color colorNairaSuccessGreen = Color(0xFF059669);

const Color colorAccentPurpleBg = Color(0xFFFAF5FF);
const Color colorAccentPurpleBorder = Color(0xFFE9D5FF);
const Color colorAccentPurpleText = Color(0xFF7C3AED);

const Color colorTerracottaOrange = Color(0xFFEF4444);

// =========================================================================
// 4. MAIN USER INTERFACE SCREEN
// Maps 1:1 to DashboardView composable
// =========================================================================

class DashboardView extends ConsumerStatefulWidget {
  const DashboardView({super.key});

  @override
  ConsumerState<DashboardView> createState() => _DashboardViewState();
}

class _DashboardViewState extends ConsumerState<DashboardView> {
  // Local dialog UI triggers
  Commodity? _activeCommodityForDetails;
  late Timer _clockTimer;
  String _currentTime = "";

  final List<String> _categories = [
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
  ];

  @override
  void initState() {
    super.initState();
    _updateClock();
    _clockTimer = Timer.periodic(const Duration(seconds: 30), (timer) {
      _updateClock();
    });
  }

  @override
  void dispose() {
    _clockTimer.cancel();
    super.dispose();
  }

  void _updateClock() {
    final now = DateTime.now();
    final hour = now.hour > 12 ? now.hour - 12 : (now.hour == 0 ? 12 : now.hour);
    final minute = now.minute.toString().padLeft(2, '0');
    final period = now.hour >= 12 ? "PM" : "AM";
    setState(() {
      _currentTime = "$hour:$minute $period";
    });
  }

  String _getGreetingText() {
    final hour = DateTime.now().hour;
    if (hour >= 0 && hour < 12) {
      return "Good Morning";
    } else if (hour >= 12 && hour < 17) {
      return "Good Afternoon";
    } else {
      return "Good Evening";
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(nairaGuardProvider);
    final viewModel = ref.read(nairaGuardProvider.notifier);

    final isAdmin = state.userIsLoggedIn && state.userIsVerified;

    // Filter commodities based on category and search query
    final filteredCommodities = state.commodities.where((c) {
      final matchesCategory = state.selectedCategory == "All" || c.category == state.selectedCategory;
      final matchesSearch = c.name.toLowerCase().contains(state.searchQuery.toLowerCase()) ||
          c.category.toLowerCase().contains(state.searchQuery.toLowerCase());
      return matchesCategory && matchesSearch;
    }).toList();

    return Scaffold(
      backgroundColor: colorLightCream,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // STREAMLINED REAL-TIME STATUS BAR (LIVE BADGE + LOCAL TIME)
              Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    // Live Price Update Indicator (Red Live + Green Real-Time Prices)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 5.0),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(50.0),
                        border: Border.all(color: const Color(0xFFE2E8F0)),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                            width: 8.0,
                            height: 8.0,
                            decoration: const BoxDecoration(
                              color: Color(0xFFD32F2F),
                              shape: BoxShape.circle,
                            ),
                          ),
                          const SizedBox(width: 6.0),
                          const Text(
                            "LIVE",
                            style: TextStyle(
                              fontSize: 11.0,
                              fontWeight: FontWeight.w900,
                              color: Color(0xFFD32F2F),
                              letterSpacing: 0.5,
                            ),
                          ),
                          const SizedBox(width: 4.0),
                          const Text(
                            "• Real-Time Prices",
                            style: TextStyle(
                              fontSize: 10.5,
                              fontWeight: FontWeight.w700,
                              color: Color(0xFF15803D),
                            ),
                          ),
                        ],
                      ),
                    ),

                    // Local Time Clock Display
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10.0, vertical: 5.0),
                      decoration: BoxDecoration(
                        color: colorAccentBlueBg,
                        borderRadius: BorderRadius.circular(50.0),
                        border: Border.all(color: colorAccentBlueBorder),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(
                            Icons.access_time_filled,
                            size: 13.0,
                            color: colorDeepCharcoal,
                          ),
                          const SizedBox(width: 5.0),
                          Text(
                            _currentTime,
                            style: const TextStyle(
                              fontSize: 11.0,
                              fontWeight: FontWeight.bold,
                              color: colorDeepCharcoal,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),

              // MARKET SEGMENTED SELECTOR row
              Row(
                children: ["All Lagos", "Mainland (Mile 12)", "Island (Isale Eko)"].map((market) {
                  final isSelected = state.selectedMarketFilter == market;
                  return Expanded(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 3.0),
                      child: InkWell(
                        onTap: () => viewModel.setMarketFilter(market),
                        borderRadius: BorderRadius.circular(50.0),
                        child: Container(
                          alignment: Alignment.center,
                          padding: const EdgeInsets.symmetric(vertical: 7.0),
                          decoration: BoxDecoration(
                            color: isSelected ? colorDeepCharcoal : colorSoftGrey,
                            border: BorderSide(
                              color: isSelected ? colorDeepCharcoal : colorBorderGrey,
                              width: 1.0,
                            ),
                            borderRadius: BorderRadius.circular(50.0),
                          ),
                          child: Text(
                            market,
                            style: TextStyle(
                              color: isSelected ? Colors.white : const Color(0xFF475569),
                              fontWeight: FontWeight.bold,
                              fontSize: 11.0,
                            ),
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
              const SizedBox(height: 10.0),

              // 3-COLUMN STATISTICAL WIDGETS
              Row(
                children: [
                  // Volatility widget
                  Expanded(
                    child: _buildStatCard(
                      title: "AVG VOLATILITY",
                      value: "+4.2%",
                      subtitle: "today",
                      bgColor: colorAccentBlueBg,
                      borderColor: colorAccentBlueBorder,
                      textColor: colorAccentBlueText,
                    ),
                  ),
                  const SizedBox(width: 6.0),
                  // Best Arbitrage widget
                  Expanded(
                    child: _buildStatCard(
                      title: "BEST ARBITRAGE",
                      value: "Rice",
                      subtitle: "Isale Eko",
                      bgColor: colorAccentGreenBg,
                      borderColor: colorAccentGreenBorder,
                      textColor: colorNairaSuccessGreen,
                    ),
                  ),
                  const SizedBox(width: 6.0),
                  // Plus Access Status widget
                  Expanded(
                    child: InkWell(
                      onTap: () {
                        PlusWaitlistModal.show(
                          context,
                          entryPoint: "plus_access_stat_click",
                        );
                      },
                      borderRadius: BorderRadius.circular(12.0),
                      child: _buildStatCard(
                        title: "PLUS ACCESS",
                        value: (state.subscription.tier == "PLUS" || isAdmin) ? "Active" : "7d Trial",
                        subtitle: (state.subscription.tier == "PLUS" || isAdmin) ? "Premium" : "Locked",
                        bgColor: colorAccentPurpleBg,
                        borderColor: colorAccentPurpleBorder,
                        textColor: colorAccentPurpleText,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 10.0),

              // COMPACT SEARCH BAR
              TextField(
                onChanged: (val) => viewModel.setSearchQuery(val),
                decoration: InputDecoration(
                  hintText: "Search staple commodities...",
                  hintStyle: const TextStyle(fontSize: 12.0),
                  prefixIcon: const Icon(Icons.search, size: 18.0),
                  suffixIcon: state.searchQuery.isNotEmpty
                      ? IconButton(
                          icon: const Icon(Icons.clear, size: 18.0),
                          onPressed: () => viewModel.setSearchQuery(""),
                        )
                      : null,
                  filled: true,
                  fillColor: colorSoftGrey,
                  contentPadding: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 12.0),
                  focusedBorder: OutlineInputBorder(
                    borderSide: const BorderSide(color: colorNairaSuccessGreen),
                    borderRadius: BorderRadius.circular(10.0),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderSide: const BorderSide(color: colorBorderGrey),
                    borderRadius: BorderRadius.circular(10.0),
                  ),
                ),
              ),
              const SizedBox(height: 10.0),

              // CATEGORIES HORIZONTAL PILL SCROLL
              SizedBox(
                height: 38.0,
                child: ListView.builder(
                  scrollDirection: Axis.horizontal,
                  itemCount: _categories.length,
                  itemBuilder: (context, index) {
                    final cat = _categories[index];
                    final isSelected = state.selectedCategory == cat;
                    return Padding(
                      padding: const EdgeInsets.only(right: 6.0),
                      child: InkWell(
                        onTap: () => viewModel.setSelectedCategory(cat),
                        borderRadius: BorderRadius.circular(50.0),
                        child: Container(
                          padding: const EdgeInsets.horizontal(12.0),
                          alignment: Alignment.center,
                          decoration: BoxDecoration(
                            color: isSelected ? const Color(0xFF005A36) : const Color(0xFFF0F2F0),
                            borderRadius: BorderRadius.circular(50.0),
                          ),
                          child: Text(
                            cat,
                            style: TextStyle(
                              color: isSelected ? Colors.white : const Color(0xFF4A4A4A),
                              fontWeight: FontWeight.bold,
                              fontSize: 11.0,
                            ),
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 10.0),

              // CONDITIONAL FEEDBACK SUGGESTION BANNER
              if (state.subscription.tier == "PLUS" || isAdmin)
                Card(
                  color: colorAccentBlueBg.withOpacity(0.4),
                  elevation: 0,
                  shape: RoundedRectangleBorder(
                    side: BorderSide(color: colorAccentBlueText.withOpacity(0.15)),
                    borderRadius: BorderRadius.circular(12.0),
                  ),
                  margin: const EdgeInsets.only(bottom: 10.0),
                  child: InkWell(
                    onTap: () => _showSubmissionDialog(context, viewModel),
                    borderRadius: BorderRadius.circular(12.0),
                    child: Padding(
                      padding: const EdgeInsets.all(10.0),
                      child: Row(
                        children: [
                          const Icon(
                            Icons.feedback_outlined,
                            color: colorAccentBlueText,
                            size: 24.0,
                          ),
                          const SizedBox(width: 10.0),
                          const Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  "Spot a price error or missing staples?",
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    fontSize: 11.0,
                                    color: colorDeepCharcoal,
                                  ),
                                ),
                                Text(
                                  "Suggest new prices or commodities directly to NairaGuard.",
                                  style: TextStyle(
                                    fontSize: 9.0,
                                    color: Colors.grey,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Container(
                            padding: const EdgeInsets.horizontal(6.0),
                            padding: const EdgeInsets.symmetric(vertical: 3.0),
                            decoration: BoxDecoration(
                              color: colorAccentBlueText,
                              borderRadius: BorderRadius.circular(4.0),
                            ),
                            child: const Text(
                              "SUGGEST",
                              style: TextStyle(
                                color: Colors.white,
                                fontSize: 8.0,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),

              // REAL-TIME DATA INDICES HEADER
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    "MARKET INDICES (REAL-TIME)",
                    style: TextStyle(
                      fontSize: 10.0,
                      fontWeight: FontWeight.bold,
                      color: Color(0xFF1A1A1A),
                      letterSpacing: 1.0,
                    ),
                  ),
                  Row(
                    children: [
                      Container(
                        width: 6.0,
                        height: 6.0,
                        decoration: const BoxDecoration(
                          color: Color(0xFF2ECC71),
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 4.0),
                      const Text(
                        "Live: 14:32 WAT",
                        style: TextStyle(
                          fontSize: 9.0,
                          color: Colors.grey,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 8.0),

              // DYNAMIC PRICE LIST / REGIONAL GATING VIEW
              Expanded(
                child: _buildMainContentArea(
                  context: context,
                  state: state,
                  viewModel: viewModel,
                  filteredCommodities: filteredCommodities,
                  isAdmin: isAdmin,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // STATISTICAL WIDGET CARD BUILDER
  Widget _buildStatCard({
    required String title,
    required String value,
    required String subtitle,
    required Color bgColor,
    required Color borderColor,
    required Color textColor,
  }) {
    return Container(
      padding: const EdgeInsets.all(6.0),
      decoration: BoxDecoration(
        color: bgColor,
        border: Border.all(color: borderColor, width: 1.0),
        borderRadius: BorderRadius.circular(12.0),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 8.0,
              fontWeight: FontWeight.bold,
              color: textColor,
              letterSpacing: 0.5,
            ),
          ),
          const SizedBox(height: 1.0),
          Text(
            value,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 12.0,
              fontWeight: FontWeight.bold,
              color: colorDeepCharcoal,
            ),
          ),
          Text(
            subtitle,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              fontSize: 8.0,
              color: Colors.grey,
            ),
          ),
        ],
      ),
    );
  }

  // DYNAMIC RENDERER: SUBSCRIPTION GATE vs EMPTY STATE vs LAZY LIST
  Widget _buildMainContentArea({
    required BuildContext context,
    required NairaGuardState state,
    required NairaGuardViewModel viewModel,
    required List<Commodity> filteredCommodities,
    required bool isAdmin,
  }) {
    final isRegionalGated = state.selectedMarketFilter != "All Lagos" &&
        state.subscription.tier != "PLUS" &&
        !isAdmin;

    if (isRegionalGated) {
      return Container(
        alignment: Alignment.center,
        child: SubscriptionGate(
          message: "Discrete regional breakdown (Mainland vs. Island) is locked. "
              "Unlock localized wholesale and retail indices under NairaGuard PLUS.",
          onUnlockPressed: () {
            PlusWaitlistModal.show(
              context,
              entryPoint: "mainland_island_price_click",
            );
          },
        ),
      );
    } else if (filteredCommodities.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.inventory_2_outlined,
              size: 48.0,
              color: colorDeepCharcoal.withOpacity(0.2),
            ),
            const SizedBox(height: 8.0),
            const Text(
              "No commodities match search",
              style: TextStyle(
                fontSize: 14.0,
                color: Colors.grey,
              ),
            ),
          ],
        ),
      );
    } else {
      return ListView.separated(
        itemCount: filteredCommodities.length,
        separatorBuilder: (context, index) => const SizedBox(height: 6.0),
        itemBuilder: (context, index) {
          final comm = filteredCommodities[index];
          final commPrices = state.prices.where((p) => p.commodityId == comm.id).toList();

          // Filter prices based on mainland / island segment UI selected
          final displayPrices = state.selectedMarketFilter == "Mainland (Mile 12)"
              ? commPrices.where((p) => p.marketLocation.contains("Mainland") || p.marketLocation.contains("Mile 12")).toList()
              : state.selectedMarketFilter == "Island (Isale Eko)"
                  ? commPrices.where((p) => p.marketLocation.contains("Island") || p.marketLocation.contains("Isale Eko")).toList()
                  : commPrices;

          final avgWholesale = displayPrices.isNotEmpty
              ? displayPrices.map((p) => p.wholesalePrice).reduce((a, b) => a + b) / displayPrices.length
              : (commPrices.isNotEmpty
                  ? commPrices.map((p) => p.wholesalePrice).reduce((a, b) => a + b) / commPrices.length
                  : 0.0);

          final avgRetail = displayPrices.isNotEmpty
              ? displayPrices.map((p) => p.retailPrice).reduce((a, b) => a + b) / displayPrices.length
              : (commPrices.isNotEmpty
                  ? commPrices.map((p) => p.retailPrice).reduce((a, b) => a + b) / commPrices.length
                  : 0.0);

          final isPlusActive = state.subscription.tier == "PLUS" || isAdmin;

          return CommodityPriceCard(
            commodity: comm,
            avgWholesale: avgWholesale,
            avgRetail: avgRetail,
            isPlus: isPlusActive,
            onDetailsClick: () {
              setState(() {
                _activeCommodityForDetails = comm;
              });
              _showDetailsModal(context, state, comm);
            },
          );
        },
      );
    }
  }

  // FEEDBACK SUGGESTION MODAL SHEET / DIALOG
  void _showSubmissionDialog(BuildContext context, NairaGuardViewModel viewModel) {
    ReportPriceSheet.show(
      context,
      commodityName: "General Staple Commodity",
      marketLocation: "Mile 12 (Mainland)",
    );
  }

  // DETAILED MODAL BOTTOM SHEET
  void _showDetailsModal(BuildContext context, NairaGuardState state, Commodity comm) {
    showModalBottomSheet(
      context: context,
      backgroundColor: const Color(0xFF1E293B),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20.0)),
      ),
      builder: (ctx) {
        return Padding(
          padding: const EdgeInsets.all(20.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                comm.name,
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 22.0,
                ),
              ),
              const SizedBox(height: 4.0),
              Text(
                "Category: ${comm.category}",
                style: const TextStyle(color: Colors.white70, fontSize: 14.0),
              ),
              const Divider(color: Colors.white24, height: 24.0),
              const Text(
                "Historic arbitrage and high-density trend graphs are fully premium and visible in NairaGuard App.",
                style: TextStyle(color: Colors.white60, fontSize: 13.0),
              ),
              const SizedBox(height: 16.0),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF15803D), // High contrast forest green for Lagos sun
                    padding: const EdgeInsets.symmetric(vertical: 12.0),
                  ),
                  onPressed: () {
                    Navigator.pop(ctx);
                    ReportPriceSheet.show(
                      context,
                      commodityName: comm.name,
                      marketLocation: state.selectedMarketFilter == "All Lagos"
                          ? "Mile 12 (Mainland)"
                          : state.selectedMarketFilter,
                    );
                  },
                  icon: const Icon(Icons.warning_amber_rounded, color: Colors.white, size: 18.0),
                  label: const Text(
                    "Report Price Inaccuracy via WhatsApp",
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                  ),
                ),
              ),
              const SizedBox(height: 8.0),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: colorNairaSuccessGreen,
                    padding: const EdgeInsets.symmetric(vertical: 12.0),
                  ),
                  onPressed: () {
                    Navigator.pop(ctx);
                    PlusWaitlistModal.show(
                      context,
                      entryPoint: "historic_trends_click",
                    );
                  },
                  icon: const Icon(Icons.star_rounded, color: Colors.white, size: 18.0),
                  label: const Text(
                    "Unlock Trends on Waitlist",
                    style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                  ),
                ),
              ),
              const SizedBox(height: 8.0),
              SizedBox(
                width: double.infinity,
                child: TextButton(
                  onPressed: () => Navigator.pop(ctx),
                  child: const Text("Close Window", style: TextStyle(color: Colors.white54)),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

// =========================================================================
// 5. HELPER COMPONENTS & CHIPS
// =========================================================================

class CommodityPriceCard extends StatelessWidget {
  final Commodity commodity;
  final double avgWholesale;
  final double avgRetail;
  final bool isPlus;
  final VoidCallback onDetailsClick;

  const CommodityPriceCard({
    super.key,
    required this.commodity,
    required this.avgWholesale,
    required this.avgRetail,
    required this.isPlus,
    required this.onDetailsClick,
  });

  @override
  Widget build(BuildContext context) {
    final formattedWholesale = avgWholesale > 0 ? "₦${avgWholesale.toStringAsFixed(0)}" : "₦0";
    final formattedRetail = avgRetail > 0 ? "₦${avgRetail.toStringAsFixed(1)}" : "₦0";

    return Card(
      color: Colors.white,
      elevation: 0,
      shape: RoundedRectangleBorder(
        side: const BorderSide(color: colorBorderGrey, width: 1.0),
        borderRadius: BorderRadius.circular(12.0),
      ),
      child: InkWell(
        onTap: onDetailsClick,
        borderRadius: BorderRadius.circular(12.0),
        child: Padding(
          padding: const EdgeInsets.all(12.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      commodity.name,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 14.0,
                        color: colorDeepCharcoal,
                      ),
                    ),
                    const SizedBox(height: 2.0),
                    Text(
                      commodity.category,
                      style: const TextStyle(
                        fontSize: 11.0,
                        color: Colors.grey,
                      ),
                    ),
                  ],
                ),
              ),
              Row(
                children: [
                  // Wholesale column
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      const Text(
                        "Wholesale Avg",
                        style: TextStyle(fontSize: 10.0, color: Colors.grey),
                      ),
                      Text(
                        formattedWholesale,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 13.0,
                          color: colorDeepCharcoal,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(width: 14.0),
                  // Retail column
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      const Text(
                        "Retail Cup",
                        style: TextStyle(fontSize: 10.0, color: Colors.grey),
                      ),
                      Text(
                        formattedRetail,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: 13.0,
                          color: colorNairaSuccessGreen,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class SubscriptionGate extends StatelessWidget {
  final String message;
  final VoidCallback onUnlockPressed;

  const SubscriptionGate({
    super.key,
    required this.message,
    required this.onUnlockPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16.0),
      margin: const EdgeInsets.all(10.0),
      decoration: BoxDecoration(
        color: colorAccentPurpleBg,
        border: Border.all(color: colorAccentPurpleBorder, width: 1.0),
        borderRadius: BorderRadius.circular(12.0),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(
            Icons.lock_person_rounded,
            color: colorAccentPurpleText,
            size: 32.0,
          ),
          const SizedBox(height: 8.0),
          Text(
            message,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 12.0,
              color: Colors.black87,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 12.0),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: colorAccentPurpleText,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
              padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 8.0),
            ),
            onPressed: onUnlockPressed,
            child: const Text(
              "Upgrade to NairaGuard PLUS",
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12.0),
            ),
          ),
        ],
      ),
    );
  }
}

// =========================================================================
// DUMMY SEED DATA FOR DEMONSTRATION
// =========================================================================

final List<Commodity> _dummyCommodities = [
  const Commodity(id: 1, name: "Mama Gold Rice (50kg Bag)", category: "Grains", conversionFactor: 110.0),
  const Commodity(id: 2, name: "Oloyin Beans (Oloyin Bag)", category: "Beans", conversionFactor: 80.0),
  const Commodity(id: 3, name: "Garri White (100kg Bag)", category: "Processed Tubers", conversionFactor: 120.0),
  const Commodity(id: 4, name: "Kings Vegetable Oil (25L)", category: "Oils", conversionFactor: 25.0),
];

final List<PriceLog> _dummyPrices = [
  const PriceLog(commodityId: 1, marketLocation: "Mile 12 (Mainland)", wholesalePrice: 65000.0, retailPrice: 1250.0),
  const PriceLog(commodityId: 1, marketLocation: "Isale Eko (Island)", wholesalePrice: 68000.0, retailPrice: 1300.0),
  const PriceLog(commodityId: 2, marketLocation: "Mile 12 (Mainland)", wholesalePrice: 52000.0, retailPrice: 950.0),
  const PriceLog(commodityId: 3, marketLocation: "Mile 12 (Mainland)", wholesalePrice: 38000.0, retailPrice: 450.0),
  const PriceLog(commodityId: 4, marketLocation: "Isale Eko (Island)", wholesalePrice: 42000.0, retailPrice: 2200.0),
];
