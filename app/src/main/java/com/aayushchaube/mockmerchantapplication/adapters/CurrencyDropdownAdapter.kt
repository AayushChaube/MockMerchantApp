package com.aayushchaube.mockmerchantapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.aayushchaube.mockmerchantapplication.databinding.ItemCurrencyDropdownBinding
import com.aayushchaube.mockmerchantapplication.models.CurrencyInfo

class CurrencyDropdownAdapter(
    context: Context,
    private val currencies: List<CurrencyInfo>
) : ArrayAdapter<CurrencyInfo>(context, 0, currencies) {
    private var filteredCurrencies = currencies.toMutableList()

    override fun getCount(): Int = filteredCurrencies.size

    override fun getItem(position: Int): CurrencyInfo? {
        return filteredCurrencies.getOrNull(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = convertView?.tag as? ItemCurrencyDropdownBinding
            ?: ItemCurrencyDropdownBinding.inflate(
                LayoutInflater.from(context), parent, false
            ).also { it.root.tag = it }

        val currency = getItem(position)
        currency?.let {
            binding.tvCurrencyCode.text = it.code
            binding.tvCurrencyName.text = it.name
            binding.tvCurrencySymbol.text = it.symbol
        }

        return binding.root
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""

                val filtered = if (query.isEmpty()) {
                    currencies
                } else {
                    currencies.filter { currency ->
                        currency.code.lowercase().contains(query) ||
                                currency.name.lowercase().contains(query) ||
                                currency.symbol.lowercase().contains(query)
                    }
                }

                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCurrencies.clear()
                results?.values?.let { values ->
                    filteredCurrencies.addAll(values as List<CurrencyInfo>)
                }
                notifyDataSetChanged()
            }
        }
    }
}