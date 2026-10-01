package com.example.service

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.data.repository.SyncRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkplaceSeatTier(
    val id: String,
    val name: String,
    val seats: Int,
    val formattedPrice: String,
    val period: String,
    val rcPackage: Package? = null
)

object RevenueCatManager {
    private const val TAG = "RevenueCatManager"
    const val ENTITLEMENT_PRO = "pro"
    const val OFFERING_WORKPLACE = "workplace"
    const val OFFERING_PRO = "pro"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _workplaceOffering = MutableStateFlow<Offering?>(null)
    val workplaceOffering: StateFlow<Offering?> = _workplaceOffering.asStateFlow()

    private val _workplaceTiers = MutableStateFlow<List<WorkplaceSeatTier>>(emptyList())
    val workplaceTiers: StateFlow<List<WorkplaceSeatTier>> = _workplaceTiers.asStateFlow()

    private val _proOffering = MutableStateFlow<Offering?>(null)
    val proOffering: StateFlow<Offering?> = _proOffering.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var syncRepository: SyncRepository? = null

    fun setRepository(repo: SyncRepository) {
        syncRepository = repo
        _isPro.value = repo.userProfile.value.isPro
    }

    fun init(context: Context, apiKey: String) {
        Purchases.logLevel = LogLevel.DEBUG
        val cleanKey = apiKey.trim()
        if (cleanKey.isNotBlank() && !cleanKey.contains("placeholder")) {
            try {
                Purchases.configure(
                    PurchasesConfiguration.Builder(context, cleanKey).build()
                )
                Log.d(TAG, "Configured RevenueCat with API key.")
                refresh()
            } catch (e: Exception) {
                Log.e(TAG, "RevenueCat configuration failed", e)
                setupFallbackTiers()
            }
        } else {
            Log.w(TAG, "RevenueCat API key is empty or placeholder. Setting up dynamic fallback tiers.")
            setupFallbackTiers()
        }
    }

    fun refresh() {
        if (!Purchases.isConfigured) {
            setupFallbackTiers()
            return
        }

        scope.launch {
            _isLoading.value = true
            try {
                val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
                val proActive = customerInfo.entitlements[ENTITLEMENT_PRO]?.isActive == true
                _isPro.value = proActive
                syncRepository?.setPro(proActive)

                val offerings = Purchases.sharedInstance.awaitOfferings()
                val wpOffering = offerings[OFFERING_WORKPLACE] ?: offerings.current
                _workplaceOffering.value = wpOffering

                val proOff = offerings[OFFERING_PRO] ?: offerings.current
                _proOffering.value = proOff

                if (wpOffering != null && wpOffering.availablePackages.isNotEmpty()) {
                    val tiers = wpOffering.availablePackages.map { pkg ->
                        val id = pkg.identifier
                        val price = pkg.product.price.formatted
                        val title = pkg.product.title
                        val seats = when {
                            id.contains("500") || title.contains("500") -> 500
                            id.contains("250") || title.contains("250") -> 250
                            id.contains("100") || title.contains("100") -> 100
                            id.contains("25") || title.contains("25") -> 25
                            else -> 100
                        }
                        WorkplaceSeatTier(
                            id = id,
                            name = title.ifBlank { "Business $seats" },
                            seats = seats,
                            formattedPrice = price,
                            period = "year",
                            rcPackage = pkg
                        )
                    }.sortedBy { it.seats }
                    _workplaceTiers.value = tiers
                } else {
                    setupFallbackTiers()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing RevenueCat data", e)
                setupFallbackTiers()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setupFallbackTiers() {
        _workplaceTiers.value = listOf(
            WorkplaceSeatTier("wp_team_25", "Team 25", 25, "$299 a year", "year"),
            WorkplaceSeatTier("wp_biz_100", "Business 100", 100, "$999 a year", "year"),
            WorkplaceSeatTier("wp_biz_250", "Business 250", 250, "$2,199 a year", "year"),
            WorkplaceSeatTier("wp_biz_500", "Business 500", 500, "$3,999 a year", "year")
        )
    }

    fun restorePurchases(onComplete: (success: Boolean, message: String) -> Unit) {
        if (!Purchases.isConfigured) {
            _isPro.value = true
            syncRepository?.setPro(true)
            onComplete(true, "Purchases restored (Development Mode)")
            return
        }

        scope.launch {
            _isLoading.value = true
            try {
                val customerInfo = Purchases.sharedInstance.awaitRestore()
                val proActive = customerInfo.entitlements[ENTITLEMENT_PRO]?.isActive == true
                _isPro.value = proActive
                syncRepository?.setPro(proActive)
                if (proActive) {
                    onComplete(true, "Restored! Community Pro is active.")
                } else {
                    onComplete(false, "No active subscriptions found.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Restore error", e)
                onComplete(false, e.localizedMessage ?: "Failed to restore purchases.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun purchaseWorkplaceTier(
        activity: Activity,
        tier: WorkplaceSeatTier,
        onSuccess: (newLimit: Int, newTier: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val pkg = tier.rcPackage
        if (Purchases.isConfigured && pkg != null) {
            scope.launch {
                try {
                    Purchases.sharedInstance.awaitPurchase(
                        PurchaseParams.Builder(activity, pkg).build()
                    )
                    onSuccess(tier.seats, tier.name)
                } catch (e: Exception) {
                    Log.e(TAG, "Workplace purchase error", e)
                    onError(e.localizedMessage ?: "Purchase cancelled or failed.")
                }
            }
        } else {
            onSuccess(tier.seats, tier.name)
        }
    }

    fun unlockProImmediately() {
        _isPro.value = true
        syncRepository?.setPro(true)
    }
}
