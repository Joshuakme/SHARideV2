package com.example.sharidev2.screen.ride

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharidev2.MainActivity
import com.example.sharidev2.R
import com.example.sharidev2.adapter.RideAdapter
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.databinding.FragmentMatchedRideBinding
import com.example.sharidev2.utility.FirebaseClient
import com.example.sharidev2.utility.CommonUtils
import com.example.sharidev2.utility.Constants
import com.example.sharidev2.viewmodel.RideViewModel
import com.example.sharidev2.viewmodel.SharedCreateRideViewModel
import com.example.sharidev2.viewmodel.SharedSearchRideViewModel

class MatchedRideFragment :
    Fragment(),
    RideAdapter.OnRideClickListener {
    private lateinit var binding: FragmentMatchedRideBinding
    private val searchRideViewModel: SharedSearchRideViewModel by activityViewModels()
    private val createRideViewModel: SharedCreateRideViewModel by activityViewModels()
    private val currentUser = FirebaseClient.firebaseAuth.currentUser

    private lateinit var context: Context


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_matched_ride, container, false)


        context = if(getContext() != null) {
            requireContext()
        } else {
            requireActivity().applicationContext
        }

        // ELEMENT VARIABLES
        val resultNumText = binding.textMatchedRideResultCount
        val recyclerView = binding.recyclerMatchedRide
        var adapter: RideAdapter


        // LAYOUT SETTINGS
        val activity = activity as MainActivity
        activity.setStatusBarColor(CommonUtils().getThemeColor(context, android.R.attr.colorBackground))
        activity.setBottomNavVisible(false)


        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        searchRideViewModel.searchRide()
            .observe(viewLifecycleOwner) {rideList ->
                loading(Constants.UI_DATA_LOADING)

                if(currentUser != null && rideList != null && rideList.isNotEmpty()) {
                    resultNumText.text = getString(R.string.matched_ride_fragment_ride_found_result_number, rideList.size)

                    adapter = RideAdapter(requireContext(), rideList, this)
                    recyclerView.adapter = adapter
                    loading(Constants.UI_DATA_SUCCESS)
                } else if (rideList == null || rideList.isEmpty()) {
                    loading(Constants.UI_DATA_FAILED)
                } else if(currentUser == null) {
                    Toast.makeText(requireContext(), "Please login to proceed", Toast.LENGTH_SHORT).show()

                    loading(Constants.UI_DATA_FAILED)
                } else {
                    Toast.makeText(requireContext(), "Error loading result", Toast.LENGTH_SHORT).show()
                    loading(Constants.UI_DATA_FAILED)
                }
            }


        // On Click Listeners
        setupOnClickListeners()

        return binding.root
    }


    private fun setupOnClickListeners() {
        val backBtn = binding.imgBtnMatchedRideNavBack
        val createRideBtnText = binding.textMatchedRideErrorCta

        // NAVIGATION
        // Matched Ride Fragment -> Ride Detail Configuration Fragment
        backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_matchedRideFragment_to_rideDetailConfigurationFragment)
        }

        // Matched Ride Fragment -> Driver Create Ride Fragment
        createRideBtnText.setOnClickListener {
            if(searchRideViewModel.origin.value != null && searchRideViewModel.destination.value != null) {
                createRideViewModel.setOrigin(searchRideViewModel.origin.value!!)
                createRideViewModel.setDestination(searchRideViewModel.destination.value!!)
            }


            findNavController().navigate(R.id.action_matchedRideFragment_to_driverCreateRideFragment)
        }
    }


    private fun loading(loadingState: Int) {
        val loadingMatchedRideCard = binding.cardMatchedRideLoading
        val resultLinearLayout = binding.llMatchedRideResult
        val errorLinearLayout = binding.llMatchedRideErrorResult

        when(loadingState) {
            Constants.UI_DATA_LOADING -> {
                loadingMatchedRideCard.visibility = View.VISIBLE
                resultLinearLayout.visibility = View.GONE
                errorLinearLayout.visibility = View.GONE
            }

            Constants.UI_DATA_SUCCESS -> {
                loadingMatchedRideCard.visibility = View.GONE
                resultLinearLayout.visibility = View.VISIBLE
                errorLinearLayout.visibility = View.GONE
            }

            Constants.UI_DATA_FAILED -> {
                loadingMatchedRideCard.visibility = View.GONE
                resultLinearLayout.visibility = View.GONE
                errorLinearLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onRideClick(ride: Ride) {
        if(ride.id != null) {
            val action = MatchedRideFragmentDirections.actionMatchedRideFragmentToRideDetailFragment(ride.id)
            findNavController().navigate(action)
        }
    }
}