package com.fib.jarvis

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

class Billing(private val ctx: Context, private val onChange: (Boolean) -> Unit) : PurchasesUpdatedListener {
    private val client = BillingClient.newBuilder(ctx).setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build()

    fun connect() = client.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(r: BillingResult) { refresh() }
        override fun onBillingServiceDisconnected() {}
    })

    fun buy(a: Activity) {
        val q = QueryProductDetailsParams.newBuilder().setProductList(listOf(
            QueryProductDetailsParams.Product.newBuilder().setProductId("fib_premium_monthly")
                .setProductType(BillingClient.ProductType.SUBS).build())).build()
        client.queryProductDetailsAsync(q) { _, list ->
            val d = list.firstOrNull() ?: return@queryProductDetailsAsync
            val token = d.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@queryProductDetailsAsync
            val p = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d).setOfferToken(token).build()
            client.launchBillingFlow(a, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(p)).build())
        }
    }

    override fun onPurchasesUpdated(r: BillingResult, p: MutableList<Purchase>?) {
        p?.forEach { if (!it.isAcknowledged) client.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(it.purchaseToken).build()) {} }
        refresh()
    }

    fun refresh() = client.queryPurchasesAsync(
        QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { _, l ->
        val on = l.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        FibState.setPremium(ctx, on); onChange(on)
    }
}
