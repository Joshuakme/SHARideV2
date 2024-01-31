package com.example.sharidev2.screen.ride

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.R
import com.example.sharidev2.adapter.CountryCodeAdapter
import com.example.sharidev2.adapter.GenderAdapter
import com.example.sharidev2.data.model.Country
import com.example.sharidev2.viewmodel.SearchRideViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class GenderBottomDialogFragment :
    BottomSheetDialogFragment(),
    GenderAdapter.OnGenderClickListener {

    private var initGenderList: List<String> = getInitGenderList()
    private lateinit var adapter: GenderAdapter
    private var recyclerView: RecyclerView?= null
    private val searchRideViewModel: SearchRideViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gender_bottom_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the recyclerView here
        recyclerView = view.findViewById(R.id.recycler_bottom_dialog)

        // ELEMENT VARIABLES

        // Initialize adapter
        setupRecyclerView(initGenderList)

        // LAYOUT


        // EVENT LISTENERS

    }

    private fun setupRecyclerView(country: List<String>) {
        adapter = GenderAdapter(initGenderList, this)
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = adapter
    }

    override fun onGenderClick(gender: String) {
        // Update the text in the spinner when a recycler item is pressed

        searchRideViewModel.setDriverGender(gender)

        // Hide the bottom dialog after click
        dismiss()
    }

    private fun getInitGenderList(): List<String> {
        return listOf("Male","Female")
    }
}