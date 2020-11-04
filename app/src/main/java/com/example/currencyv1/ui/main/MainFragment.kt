package com.example.currencyv1.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.currencyv1.R
import com.example.currencyv1.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

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
            binding.exchangeRate.text = getString(R.string.rate_value, it.toString())
        })

        viewModel.resultValue.observe(this, {
            binding.resultCurrencyValue.text = it.toString()
        })

        viewModel.isLoaded.observe(this, {
            binding.reloadRate.isEnabled = it
        })

        viewModel.loadedDate.observe(this, {
            binding.reloadTime.text = it
        })

        viewModel.isErrorOcurred.observe(this, {
            if (it)
                Toast.makeText(context, viewModel.errorMessage.value, Toast.LENGTH_LONG).show()
        })

        binding.reloadRate.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                viewModel.onRateRefreshRequested()
            }
        })

        binding.baseCurrencyValue.doOnTextChanged { text, _, _, _ ->
            text?.let {
                viewModel.onBaseCurrencyValueChanged(if (it.isNotEmpty()) text.toString() else "")
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
        viewModel.initialRefreshRate()
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