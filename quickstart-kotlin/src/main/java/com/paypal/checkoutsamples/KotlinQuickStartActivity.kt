package com.paypal.checkoutsamples

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.createorder.CreateOrder
import com.paypal.checkout.error.OnError
import com.paypal.checkoutsamples.order.OrdersQuickStartActivity
import com.paypal.checkoutsamples.paymentbutton.PaymentButtonQuickStartActivity
import com.paypal.checkoutsamples.token.TokenQuickStartActivity
import kotlinx.android.synthetic.main.activity_kotlin_quick_start.*
import kotlinx.coroutines.launch

class KotlinQuickStartActivity : AppCompatActivity() {

    private val clientIdWasUpdated by lazy {
        PAYPAL_CLIENT_ID != "YOUR-CLIENT-ID-HERE"
    }

    private val secretWasUpdated by lazy {
        PAYPAL_SECRET != "ONLY-FOR-QUICKSTART-DO-NOT-INCLUDE-SECRET-IN-CLIENT-SIDE-APPLICATIONS"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_quick_start)

        buyWithOrder.setOnClickListener {
            if (clientIdWasUpdated) {
                startActivity(OrdersQuickStartActivity.startIntent(this))
            } else {
                displayErrorSnackbar("Please Update PAYPAL_CLIENT_ID In QuickStartConstants.")
            }
        }

        buyWithOrderToken.setOnClickListener {
            if (clientIdWasUpdated && secretWasUpdated) {
                startActivity(TokenQuickStartActivity.startIntent(this))
            } else {
                displayErrorSnackbar("Please Update PAYPAL_CLIENT_ID and PAYPAL_SECRET In QuickStartConstants.")
            }
        }

        buyWithPaymentButton.setOnClickListener {
            if (clientIdWasUpdated) {
                startActivity(PaymentButtonQuickStartActivity.startIntent(this))
            } else {
                displayErrorSnackbar("Please Update PAYPAL_CLIENT_ID In QuickStartConstants.")
            }
        }

        TestButton.setOnClickListener {
            PayPalCheckout.start(
                createOrder = CreateOrder { createOrderActions ->
                    createOrderActions.set("37B8065766115102E")
                }, null, null, OnCancel {
                    displayErrorSnackbar("======Stephen=========OnCancel==>")
                }, OnError { errorInfo -> {
                    displayErrorSnackbar("======Stephen========OnError====>$errorInfo")
                }}
            )
        }
    }

    private fun displayErrorSnackbar(errorMessage: String) {
        Snackbar.make(rootQuickStart, errorMessage, Snackbar.LENGTH_INDEFINITE).apply { setAction("Got It 👍") { dismiss() } }.show()
    }
}
