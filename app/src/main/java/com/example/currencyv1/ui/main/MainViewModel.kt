package com.example.currencyv1.ui.main

import androidx.lifecycle.*
import com.example.currencyv1.model.*
import com.example.currencyv1.model.Currency
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class MainViewModel : ViewModel() {

    private var refreshJob: Job? = null
    private var convertJob: Job? = null
    private var initialRefreshJob: Job? = null

    private val _baseValue: MutableLiveData<Double> = MutableLiveData()

    private val _baseCurrency: MutableLiveData<Currencies> = MutableLiveData()

    private val _resultCurrency: MutableLiveData<Currencies> = MutableLiveData()

    private val _isLoaded: MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoaded: LiveData<Boolean> = _isLoaded

    private val _loadedDate: MutableLiveData<String> = MutableLiveData()
    val loadedDate: LiveData<String> = _loadedDate

    private val _currency: MediatorLiveData<Currency> = MediatorLiveData()

    private val _isErrorOcurred : MutableLiveData<Boolean> = MutableLiveData(false)
    val isErrorOcurred : LiveData<Boolean> = _isErrorOcurred

    private val _errorMessage : MutableLiveData<String> = MutableLiveData(ResponseHandler.UNDEFINED_RATE_ERROR)
    val errorMessage : LiveData<String> = _errorMessage

    val resultValue: LiveData<Double> = Transformations.switchMap(_currency) {
        MutableLiveData(roundDouble(it.resultValue))
    }

    val resultRate: LiveData<Double> = Transformations.switchMap(_currency) {
        MutableLiveData(roundDouble(it.baseRate))
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
        if (value.isNotEmpty())
            _baseValue.value = value.toDouble()
        else
            _baseValue.value = 0.0
    }

    fun onResultCurrencyChanged (value: String) {
        _resultCurrency.value = Currencies.valueOf(value)
    }

    fun onBaseCurrencyChanged (value: String) {
        _baseCurrency.value = Currencies.valueOf(value)
    }

    private fun convertBaseCurrency() {
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
                        handleError(res)
                    }
                    else {
                        _currency.value = res.data
                    }
                }
            }
        }
    }

    private fun refreshRate() {
        cancelJob(refreshJob)
        refreshJob = viewModelScope.launch {
            _isLoaded.value = false
            val res = CurrencyRepository.refreshRate()
            if (res.status == Status.ERROR) {
                handleError(res)
            }
            else {
                convertBaseCurrency()
                _currency.value = res.data
                _loadedDate.value = getCurrentDate()
            }
            _isLoaded.value = true
        }
    }

    fun onRateRefreshRequested() {
        refreshRate()
    }

    fun initialRefreshRate() {
        cancelJob(initialRefreshJob)
        initialRefreshJob = viewModelScope.launch {
            _isLoaded.value = false
            val res = CurrencyRepository.initialRefreshRate()
            if (res.status == Status.ERROR) {
                handleError(res)
            }
            else {
                _isErrorOcurred.value = false
                convertBaseCurrency()
                _currency.value = res.data
                _loadedDate.value = getCurrentDate()
            }
            _isLoaded.value = true
        }
    }

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return formatter.format(Calendar.getInstance().time)
    }

    private fun handleError(error: Resource<Any>) {
        _errorMessage.value = error.message
        toggleError()
    }

    private fun toggleError() {
        _isErrorOcurred.value = true
        _isErrorOcurred.value = false
    }

    private fun roundDouble(x: Double) = round(x * 100) / 100

    private fun cancelJob (job: Job?) {
        job?.let {it.cancel()}
    }
}