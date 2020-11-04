package com.example.currencyv1.model

import android.provider.Telephony
import java.util.concurrent.Exchanger

data class Currency (
        var baseName: Currencies,
        var pairName: Currencies,
        var resultValue: Double,
        var baseValue: Double,
        var pairRate: Double,
        var baseRate: Double
)