package com.paypal.checkoutsamples.order

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.ItemCategory
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.ShippingPreference
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.Address
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.AppContext
import com.paypal.checkout.order.AuthorizeOrderResult
import com.paypal.checkout.order.BreakDown
import com.paypal.checkout.order.CaptureOrderResult
import com.paypal.checkout.order.Items
import com.paypal.checkout.order.Order
import com.paypal.checkout.order.PurchaseUnit
import com.paypal.checkout.order.Shipping
import com.paypal.checkout.order.UnitAmount
import com.paypal.checkoutsamples.R
import com.paypal.checkoutsamples.order.usecase.CreateAmountRequest
import com.paypal.checkoutsamples.order.usecase.CreateAmountUseCase
import com.paypal.checkoutsamples.order.usecase.CreateItemsRequest
import com.paypal.checkoutsamples.order.usecase.CreateItemsUseCase
import com.paypal.checkoutsamples.order.usecase.CreateOrderRequest
import com.paypal.checkoutsamples.order.usecase.CreateOrderUseCase
import com.paypal.checkoutsamples.order.usecase.CreatePurchaseUnitRequest
import com.paypal.checkoutsamples.order.usecase.CreatePurchaseUnitUseCase
import com.paypal.checkoutsamples.order.usecase.CreateShippingRequest
import com.paypal.checkoutsamples.order.usecase.CreateShippingUseCase
import kotlinx.android.synthetic.main.activity_orders_quick_start.*
import kotlinx.android.synthetic.main.item_preview_item.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.UUID

class OrdersQuickStartActivity : AppCompatActivity() {

    private val tag = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) = with(applicationContext) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders_quick_start)

        submitOrderButton.setOnClickListener {
            startCheckoutWithSampleOrders()
        }
    }

    private fun startCheckoutWithSampleOrders() {
        fun showSnackbar(text: String) {
            Snackbar.make(rootOrdersQuickStart, text, Snackbar.LENGTH_LONG).show()
        }

        PayPalCheckout.start(
            createOrder = CreateOrder { actions ->
                val createdItems = listOf(CreatedItem("TestGoods", "1", "1", "0.1", ItemCategory.DIGITAL_GOODS))
                val shippingPreference = ShippingPreference.NO_SHIPPING
                val currencyCode = CurrencyCode.USD

                val itemTotal = createdItems.map { it.amount.toDouble() * it.quantity.toInt() }
                    .sum().toBigDecimal().scaledForMoney
                val taxTotal = createdItems.map { it.taxAmount.toDouble() * it.quantity.toInt() }
                    .sum().toBigDecimal().scaledForMoney
                val shippingTotal = BigDecimal(0.00).scaledForMoney
                val handlingTotal = BigDecimal(0.00).scaledForMoney
                val shippingDiscountTotal = BigDecimal(0.00).scaledForMoney
                val itemDiscountTotal = BigDecimal(0.00).scaledForMoney
                val totalValue = itemTotal.add(taxTotal).add(shippingTotal).add(handlingTotal).subtract(shippingDiscountTotal).subtract(itemDiscountTotal)
                actions.create(
                    Order.Builder()
                    .intent(OrderIntent.CAPTURE)
                    .purchaseUnitList(listOf(PurchaseUnit.Builder()
                            .referenceId(UUID.randomUUID().toString())
                            .amount(
                                Amount.Builder()
                                    .currencyCode(currencyCode)
                                    .value(totalValue.asMoneyString)
                                    .breakdown(
                                        BreakDown.Builder()
                                            .itemTotal(itemTotal.unitAmountFor(currencyCode))
                                            .shipping(shippingTotal.unitAmountFor(currencyCode))
                                            .handling(handlingTotal.unitAmountFor(currencyCode))
                                            .taxTotal(taxTotal.unitAmountFor(currencyCode))
                                            .shippingDiscount(shippingDiscountTotal.unitAmountFor(currencyCode))
                                            .discount(itemDiscountTotal.unitAmountFor(currencyCode))
                                            .build())
                                    .build())
                            .items(
                                createdItems.map { createdItem ->
                                    Items.Builder().name(createdItem.name)
                                        .quantity(createdItem.quantity)
                                        .category(createdItem.itemCategory)
                                        .unitAmount(UnitAmount.Builder().value(createdItem.amount).currencyCode(currencyCode).build())
                                        .tax(UnitAmount.Builder().value(createdItem.taxAmount).currencyCode(currencyCode).build()).build()
                                })
                            .shipping(Shipping.Builder().address(Address.Builder()
                                        .addressLine1("123 Townsend St")
                                        .addressLine2("Floor 6")
                                        .adminArea2("San Francisco")
                                        .adminArea1("CA")
                                        .postalCode("94107")
                                        .countryCode("US")
                                        .build()).options(null).build())//Omitting shipping will default to the customer's default shipping address.
                            .customId("CUSTOM-123")//The API caller-provided external ID. Used to reconcile API caller-initiated transactions with PayPal transactions. Appears in transaction and settlement reports.
                            .description("Purchase from Orders Quick Start")
                            .softDescriptor("800-123-1234")//The soft descriptor is the dynamic text used to construct the statement descriptor that appears on a payer's card statement.
                            .build() )
                    ).appContext(
                            AppContext.Builder()
                                .brandName("Acme Inc")
                                .userAction(UserAction.PAY_NOW)
                                .shippingPreference(shippingPreference)
                                .build()
                        ).build()){ id ->
                            Log.d(tag, "Order ID: $id")
                        }
            },
            onApprove = OnApprove { approval ->
                Log.i(tag, "OnApprove: $approval")
                approval.orderActions.capture { result ->
                    val message = when (result) {
                        is CaptureOrderResult.Success -> {
                            Log.i(tag, "Success: $result")
                            "💰 Order Capture Succeeded 💰"
                        }
                        is CaptureOrderResult.Error -> {
                            Log.i(tag, "Error: $result")
                            "🔥 Order Capture Failed 🔥"
                        }
                    }
                    showSnackbar(message)
                }
            },
            onCancel = OnCancel {
                Log.d(tag, "OnCancel")
                showSnackbar("😭 Buyer Cancelled Checkout 😭")
            },
            onError = OnError { errorInfo ->
                Log.d(tag, "ErrorInfo: $errorInfo")
                showSnackbar("🚨 An Error Occurred 🚨")
            }
        )
    }

    private fun BigDecimal.unitAmountFor(currencyCode: CurrencyCode): UnitAmount {
        return UnitAmount.Builder().value(asMoneyString).currencyCode(currencyCode).build()
    }

    private val BigDecimal.asMoneyString: String
        get() = DecimalFormat("#0.00").format(this)

    private val BigDecimal.scaledForMoney: BigDecimal
        get() = setScale(2, RoundingMode.HALF_UP)

    companion object {
        fun startIntent(context: Context): Intent {
            return Intent(context, OrdersQuickStartActivity::class.java)
        }
    }
}
