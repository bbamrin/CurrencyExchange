package com.example.currencyv1.api

import com.squareup.moshi.Json

data class ExchangePairs(
    @field:Json(name = "EUR")val EUR:Double,
    @field:Json(name = "USD")val USD: Double,
    @field:Json(name = "RUB")val RUB: Double
)
data class BaseCurrency(
    @field:Json(name = "base")val name: String,
    @field:Json(name = "rates")val pairs: ExchangePairs,
    @field:Json(name = "date")val date: String
)