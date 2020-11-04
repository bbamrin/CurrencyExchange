package com.example.currencyv1.ui.main

import android.util.Log
import androidx.core.content.contentValuesOf
import androidx.lifecycle.*
import com.example.currencyv1.api.BaseCurrency
import com.example.currencyv1.model.Currencies
import com.example.currencyv1.model.Currency
import com.example.currencyv1.model.CurrencyRepository
import com.example.currencyv1.model.Status
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainViewModel : ViewModel() {

    private var refreshJob: Job? = null
    private var convertJob: Job? = null

    private val _baseValue: MutableLiveData<Double> = MutableLiveData()
    val baseValue: LiveData<Double> = _baseValue

    private val _baseCurrency: MutableLiveData<Currencies> = MutableLiveData()
    val  baseCurrency: LiveData<Currencies> = _baseCurrency

    private val _resultCurrency: MutableLiveData<Currencies> = MutableLiveData()
    val resultCurrency: LiveData<Currencies> = _resultCurrency

    private val _isLoaded: MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoaded: LiveData<Boolean> = _isLoaded

    private val _loadedDate: MutableLiveData<String> = MutableLiveData()
    val loadedDate: LiveData<String> = _loadedDate

    private val _currency: MediatorLiveData<Currency> = MediatorLiveData()
    val currency: LiveData<Currency> = _currency

    private val _isErrorOcurred : MutableLiveData<Boolean> = MutableLiveData(false)
    val isErrorOcurred : LiveData<Boolean> = _isErrorOcurred

    private val _errorMessage : MutableLiveData<String> = MutableLiveData()
    val errorMessage : LiveData<String> = _errorMessage

    val resultValue: LiveData<Double> = Transformations.switchMap(_currency) {
        MutableLiveData(it.resultValue)
    }

    val resultRate: LiveData<Double> = Transformations.switchMap(_currency) {
        MutableLiveData(it.baseRate)
    }

    init {
        _currency.addSource(_baseValue) {
            convertBaseCurrency()
        }

        _currency.addSource(_baseCurrency) {
            convertBaseCurrency()
        }

        _currency.addSource(_resultCurrency) {
            convertBaseCurrency()
        }
    }

    fun onBaseCurrencyValueChanged(value: String) {
        _baseValue.value = value.toDouble()
    }

    fun onResultCurrencyChanged (value: String) {
        _resultCurrency.value = Currencies.valueOf(value)
    }

    fun onBaseCurrencyChanged (value: String) {
        _baseCurrency.value = Currencies.valueOf(value)
    }

    fun convertBaseCurrency() {
        if (_isErrorOcurred.value != true) {
            cancelJob(convertJob)
            convertJob = viewModelScope.launch {
                if (_baseCurrency.value != null && _resultCurrency.value != null && _baseValue.value != null) {
                    val res = CurrencyRepository.loadBaseCurrency(
                            _baseCurrency.value,
                            _resultCurrency.value,
                            _baseValue.value
                    )
                    if (res.status == Status.ERROR) {
                        _isErrorOcurred.value = true
                        _errorMessage.value = res.message
                    }
                    else {
                        _isErrorOcurred.value = false
                        _currency.value = res.data
                    }
                }
            }
        }
    }

    fun refreshRate() {
        cancelJob(refreshJob)
        refreshJob = viewModelScope.launch {
            _isLoaded.value = false
            val res = CurrencyRepository.refreshRate()
            if (res.status == Status.ERROR) {
                _isErrorOcurred.value = true
                _errorMessage.value = res.message
            }
            else {
                _isErrorOcurred.value = false
                _currency.value = res.data
                convertBaseCurrency()
                val formatter = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                _loadedDate.value = formatter.format(Calendar.getInstance().time)
                _isLoaded.value = true
            }
        }
    }

    fun onRateRefreshRequested() {
        refreshRate()
    }

    private fun cancelJob (job: Job?) {
        job?.let {it.cancel()}
    }
}