package com.example.currencyv1.ui.main

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RestrictTo
import androidx.core.widget.doOnTextChanged
import com.example.currencyv1.R
import com.example.currencyv1.api.BaseCurrency
import com.example.currencyv1.databinding.MainFragmentBinding
import com.example.currencyv1.model.Currency
import com.example.currencyv1.model.CurrencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var repository: CurrencyRepository
    private lateinit var button: Button;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainViewModel::class.java)
        _binding = MainFragmentBinding.inflate(inflater, container, false)

        binding.baseCurrencySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.currencies)
        )

        binding.resultCurrencySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.currencies)
        )

        return binding.root;
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resultRate.observe(this, {
            binding.exchangeRate.text = String.format("%.1f", it)
        })

        viewModel.resultValue.observe(this, {
            binding.resultCurrencyValue.text = String.format("%.1f", it)
        })

        viewModel.isLoaded.observe(this, {
            binding.reloadRate.isEnabled = it
        })

        viewModel.loadedDate.observe(this, {
            binding.reloadTime.text = it
        })


        binding.reloadRate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.e("e", "e")
                viewModel.onRateRefreshRequested()
            }
        })

        binding.baseCurrencyValue.doOnTextChanged { text, _, _, _ ->
            if (viewModel.isErrorOcurred.value == true) {
                Toast.makeText(context, viewModel.errorMessage.value, Toast.LENGTH_LONG).show()

            }
            text?.let {
                viewModel.onBaseCurrencyValueChanged(if (it.isNotEmpty()) text.toString() else "0")
            }
        }

        binding.resultCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.let {
                    if (viewModel.isErrorOcurred.value == true) {
                        Log.e("test", it.getItemAtPosition(position).toString())
                    }
                    viewModel.onResultCurrencyChanged(it.getItemAtPosition(position).toString())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {}
        }

        binding.baseCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.let {
                    if (viewModel.isErrorOcurred.value == true) {
                        Log.e("test", it.getItemAtPosition(position).toString())
                    }
                    viewModel.onBaseCurrencyChanged(it.getItemAtPosition(position).toString())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}