package com.paypal.checkoutsamples.token

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.createorder.UserAction
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.CaptureOrderResult
import com.paypal.checkout.shipping.OnShippingChange
import com.paypal.checkoutsamples.R
import com.paypal.checkoutsamples.token.repository.CheckoutApi
import com.paypal.checkoutsamples.token.repository.CreatedOrder
import com.paypal.checkoutsamples.token.repository.OrderRepository
import com.paypal.checkoutsamples.token.repository.request.AmountRequest
import com.paypal.checkoutsamples.token.repository.request.ApplicationContextRequest
import com.paypal.checkoutsamples.token.repository.request.OrderRequest
import com.paypal.checkoutsamples.token.repository.request.PurchaseUnitRequest
import kotlinx.android.synthetic.main.activity_token_quick_start.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.io.IOException

class TokenQuickStartActivity : AppCompatActivity() {

    private val checkoutApi = CheckoutApi()

    private val orderRepository = OrderRepository(checkoutApi)

    private val uiScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token_quick_start)

        totalAmountInput.editText?.addTextChangedListener { totalAmountInput.error = null }

        submitTokenButton.setOnClickListener {
            /*if (totalAmountInput.editText!!.text.isEmpty()) {
                totalAmountInput.error = getString(R.string.token_quick_start_activity_total_amount_required)
                return@setOnClickListener
            }*/

            uiScope.launch {
                orderRepository.create(OrderRequest(OrderIntent.CAPTURE.name, ApplicationContextRequest(UserAction.PAY_NOW.name),
                    listOf(PurchaseUnitRequest(amount = AmountRequest(value = "1", currencyCode = CurrencyCode.USD.name)))))?.let { startCheckout(it.id) }
            }
        }

        submitTokenButton2.setOnClickListener {
            startCheckout("01J50853WD141342W")
        }
    }

    private fun startCheckout(id: String) {
        PayPalCheckout.start(
            createOrder = CreateOrder { createOrderActions ->
                uiScope.launch {
                    /*orderRepository.create(OrderRequest(OrderIntent.CAPTURE.name, ApplicationContextRequest(UserAction.PAY_NOW.name),
                        listOf(PurchaseUnitRequest(amount = AmountRequest(value = "1", currencyCode = CurrencyCode.USD.name)))))?.let { createOrderActions.set(it.id) }*/

                    createOrderActions.set(id)
                }
                //createOrderActions.set("01J50853WD141342W")
            }, null, null, OnCancel {
                        println("======Stephen===========>")
            },
            OnError { errorInfo -> {
                println("======Stephen===========>")
            }}
        )
    }

    override fun onStop() {
        super.onStop()
        uiScope.coroutineContext.cancelChildren()
    }

    companion object {
        fun startIntent(context: Context): Intent {
            return Intent(context, TokenQuickStartActivity::class.java)
        }
    }
}
