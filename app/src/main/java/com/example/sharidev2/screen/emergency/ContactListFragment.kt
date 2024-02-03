package com.example.sharidev2.screen.emergency

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sharidev2.R
import com.example.sharidev2.adapter.ContactAdapter
import com.example.sharidev2.databinding.FragmentContactListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ContactListFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var contactArrayList: ArrayList<contactClass>
    private lateinit var nodeList: ArrayList<tempData>
    private var c1:Long = 0
    private var c2:Long = 0

    private lateinit var binding:FragmentContactListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactListBinding.inflate(inflater,container,false)
        binding.contactList.layoutManager=LinearLayoutManager(context)
        binding.contactList.hasFixedSize()
        contactArrayList = arrayListOf<contactClass>()
        nodeList = arrayListOf<tempData>()
        getContactData()
        return binding.root
    }

    private fun getContactData() {
        database = FirebaseDatabase.getInstance().getReference("contacts")
        var query:Query
        query = database.orderByChild("contactName")
        query.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var ky:String = ""
                    var ctcName:String = ""

                    for(ctcsnapshot in snapshot.children){
                        val contact = ctcsnapshot.getValue(contactClass::class.java)
                        contactArrayList.add(contact!!)
                        ky = ctcsnapshot.key.toString()
                        ctcName=contact.contactName.toString()

                        val tempCtc = tempData(ky,ctcName)
                        nodeList.add(tempCtc)
                    }

                    var adapter = ContactAdapter(contactArrayList)
                    binding.contactList.adapter = adapter
                    adapter.setOnItemClickListener(object:ContactAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            val ctContact = nodeList[position]
                            val nodePath = ctContact.id.toString()
                            val fragment = AddContactFragment()
                            val bundle = Bundle()
                            bundle.putString("contact_id", nodePath.toString())
                            fragment.arguments = bundle
                            val fragmentManager = activity?.supportFragmentManager
                            val fragmentTransaction = fragmentManager!!.beginTransaction()
                            fragmentTransaction.replace(com.example.sharidev2.R.id.frameLayout, fragment)
                                .commit()
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }


}