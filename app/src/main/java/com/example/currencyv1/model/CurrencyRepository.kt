package com.example.currencyv1.model

import com.example.currencyv1.api.BaseCurrency
import com.example.currencyv1.api.createForeignExchangeService
import retrofit2.await

object CurrencyRepository {
    private var currency: Currency = Currency(Currencies.USD, Currencies.USD, 0.0,0.0, 1.0, 1.0)
    private val foreignExchangeService = createForeignExchangeService()
    private val baseMap = mutableMapOf<Currencies, BaseCurrency?>(Currencies.RUB to null, Currencies.EUR to null, Currencies.USD to null)

    private fun getExactPairRate(
            baseCurrency: BaseCurrency?,
            pair: Currencies
    ):Double {
        baseCurrency?.let {
            if (Currencies.valueOf(baseCurrency.name) == pair)
                return 1.0
            return when (pair) {
                Currencies.RUB -> {
                    it.pairs.RUB
                }
                Currencies.EUR -> {
                    it.pairs.EUR
                }
                Currencies.USD -> {
                    it.pairs.USD
                }
            }
        }
        throw RateIsUndefinedException("Cannot get current rate")
    }

    private fun getPairRate(base: Currencies, pair: Currencies):Double {
        return when (base) {
            Currencies.RUB -> {
                getExactPairRate(baseMap[Currencies.RUB], pair)
            }
            Currencies.EUR -> {
                getExactPairRate(baseMap[Currencies.EUR], pair)
            }
            Currencies.USD -> {
                getExactPairRate(baseMap[Currencies.USD], pair)
            }
        }
    }

    private fun updateCurrency(baseCurrency: Currencies, resultCurrency: Currencies, baseValue: Double): Currency {
        currency.baseName = baseCurrency
        currency.pairName = resultCurrency
        currency.baseValue = baseValue
        currency.pairRate = getPairRate(currency.baseName, currency.pairName)
        currency.baseRate = 1 / currency.pairRate
        currency.resultValue = currency.baseValue * currency.pairRate
        return currency
    }

    private fun updateRate(): Currency {
        currency.pairRate = getPairRate(currency.baseName, currency.pairName)
        currency.baseRate = 1/currency.pairRate
        currency.resultValue = currency.baseValue * currency.pairRate
        return currency
    }

    suspend fun refreshRate() : Resource<Currency> {
        return try {
            baseMap[Currencies.RUB] = foreignExchangeService.latestBaseCurrency("RUB", "EUR,USD").await()
            baseMap[Currencies.EUR] = foreignExchangeService.latestBaseCurrency("EUR", "RUB,USD").await()
            baseMap[Currencies.USD] = foreignExchangeService.latestBaseCurrency("USD", "EUR,RUB").await()
            ResponseHandler.handleSuccess(updateRate())
        } catch (e : Exception) {
            ResponseHandler.handleException(e)
        }
    }

    suspend fun initialRefreshRate() : Resource<Currency> {
        if (baseMap[Currencies.RUB] == null &&
                baseMap[Currencies.EUR] == null &&
                baseMap[Currencies.USD] == null)
        {
            return try {
                baseMap[Currencies.RUB] = foreignExchangeService.latestBaseCurrency("RUB", "EUR,USD").await()
                baseMap[Currencies.EUR] = foreignExchangeService.latestBaseCurrency("EUR", "RUB,USD").await()
                baseMap[Currencies.USD] = foreignExchangeService.latestBaseCurrency("USD", "EUR,RUB").await()
                ResponseHandler.handleSuccess(updateRate())
            } catch (e : Exception) {
                ResponseHandler.handleException(e)
            }
        }
        return ResponseHandler.handleSuccess(currency)
    }

    suspend fun loadBaseCurrency (baseCurrency: Currencies?, resultCurrency: Currencies?, baseValue: Double?) : Resource<Currency> {
        if (baseCurrency == null || resultCurrency == null || baseCurrency == null)
            return ResponseHandler.handleNullArgumentError()
        return try {
            ResponseHandler.handleSuccess(updateCurrency(baseCurrency!!, resultCurrency!!, baseValue!!))
        } catch (e: Exception) {
            ResponseHandler.handleException(e)
        }
    }
}