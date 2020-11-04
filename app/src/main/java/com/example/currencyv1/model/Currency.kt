package com.example.currencyv1.model

data class Currency (
        var baseName: Currencies,
        var pairName: Currencies,
        var resultValue: Double,
        var baseValue: Double,
        var pairRate: Double,
        var baseRate: Double
)