package com.example.sharidev2.screen.user

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.adapter.CountryCodeAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.sharidev2.data.model.Country
import com.example.sharidev2.viewmodel.LoginViewModel

class CountryCodeBottomDialogFragment :
    BottomSheetDialogFragment(),
    CountryCodeAdapter.OnCountryCodeClickListener {

    private var initCountryList: List<Country> = getInitCountryList()
    private lateinit var adapter: CountryCodeAdapter
    private var recyclerView: RecyclerView ?= null
    private val loginViewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_country_code_bottom_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the recyclerView here
        recyclerView = view.findViewById(R.id.recycler_bottom_dialog_country_code)

        // ELEMENT VARIABLES
        val searchBar = view.findViewById<SearchView>(R.id.search_bottom_dialog_country_code)

        // Initialize adapter
        setupRecyclerView(initCountryList)

        // LAYOUT


        // EVENT LISTENERS
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCountries(newText)
                return true
            }
        })
    }

    private fun getInitCountryList(): List<Country> {
        return listOf(
            Country("Malaysia", 60),
            Country("Singapore", 65),
            Country("Indonesia", 62)
        )
    }


    override fun onCountryCodeClick(country: Country) {
        // Update the text in the spinner when a recycler item is pressed
        country.countryCode?.let { loginViewModel.setCountryCode(it) }

        // Hide the bottom dialog after click
        dismiss()
    }

    private fun setupRecyclerView(country: List<Country>) {
        adapter = CountryCodeAdapter(country, this)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = adapter
    }

    private fun filterCountries(query: String?) {
        val filteredList = initCountryList.filter { country ->
                country.name!!.contains(query.orEmpty(), true) ||
                        country.countryCode.toString()!!.contains(query.orEmpty(), true)

        }
        adapter.updateData(filteredList)
    }
}