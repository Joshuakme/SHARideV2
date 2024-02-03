package com.example.sharidev2.screen.emergency

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.sharidev2.R
import com.example.sharidev2.databinding.FragmentAddContactBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddContactFragment : Fragment() {
    private lateinit var binding:FragmentAddContactBinding
    private lateinit var database: DatabaseReference
    var nodeId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nodeId = it.getString("contact_id").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =FragmentAddContactBinding.inflate(inflater,container,false)
        val root:View = binding.root

        binding.btnSaveContactDetail.setOnClickListener(){
            addData()

            val fragment =AddContactFragment()
            val fragmentManager =activity?.supportFragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.frameLayout,fragment)
                .addToBackStack(AddContactFragment().toString())
                .commit()
        }

        if(nodeId != ""){
            displayContact()
        }

        //Update Changed Data
        binding.btnUpdateContactDetail.setOnClickListener(){
            updateContact()
        }
        binding.btnDeleteContactDetail.setOnClickListener(){
            deleteContact()
        }

        return root
    }

    private fun deleteContact() {
        database = FirebaseDatabase.getInstance().getReference("contacts")
        database.child(nodeId).removeValue().addOnSuccessListener {
            binding.inputEmergencyName.text?.clear()
            binding.inputEmergencyPhoneNo.text?.clear()
            binding.btnSaveContactDetail.visibility = View.VISIBLE
            binding.btnUpdateContactDetail.visibility = View.INVISIBLE
            binding.btnDeleteContactDetail.visibility = View.INVISIBLE

            Toast.makeText(context,"Contact Deleted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(context,"Contact Fail To Delete", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateContact() {
        val contactName = binding.inputEmergencyName.text.toString()
        val contactPhone = binding.inputEmergencyPhoneNo.text.toString()
        database = FirebaseDatabase.getInstance().getReference("contacts")
        val contact = contactClass(contactName,contactPhone)

        database.child(nodeId).setValue(contact).addOnSuccessListener {
            binding.inputEmergencyName.text?.clear()
            binding.inputEmergencyPhoneNo.text?.clear()
            binding.btnSaveContactDetail.visibility = View.INVISIBLE
            binding.btnUpdateContactDetail.visibility = View.VISIBLE
            binding.btnDeleteContactDetail.visibility = View.VISIBLE

            Toast.makeText(context,"Contact Updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(context,"Contact Fail To Update", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayContact() {
        database = FirebaseDatabase.getInstance().getReference("contacts")
        database.child(nodeId).get().addOnSuccessListener {
            if(it.exists()){
                val ctcClass = contactClass()
                binding.inputEmergencyName.setText(it.child("contactName").value.toString())
                binding.inputEmergencyPhoneNo.setText(it.child("contactPhone").value.toString())
                binding.btnBackContactDetail.visibility = View.VISIBLE
                binding.btnSaveContactDetail.visibility = View.VISIBLE

            }
        }
    }

    //Function to Add Emergency Contact
    private fun addData() {
        val contactName = binding.inputEmergencyName.text.toString()
        val contactPhone = binding.inputEmergencyPhoneNo.text.toString()
        database = FirebaseDatabase.getInstance().getReference("contacts")
        val contact = contactClass(contactName,contactPhone)
        val databaseReference = FirebaseDatabase.getInstance().reference
        val id = databaseReference.push().key

        //Data Added Successfully Listener
        database.child(id.toString()).setValue(contact).addOnSuccessListener {
            binding.inputEmergencyName.text?.clear()
            binding.inputEmergencyPhoneNo.text?.clear()
            Toast.makeText(context, "Contact Added Successfully", Toast.LENGTH_SHORT).show()

            //Data Added Failed Listener
        }.addOnFailureListener{
            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}