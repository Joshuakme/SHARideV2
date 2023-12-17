package com.example.sharidev2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NavigationBarView.OnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_title_home -> {
                    // Respond to navigation item 1 click
                    true
                }
                R.id.item_title_booking -> {
                    // Respond to navigation item 2 click
                    true
                }
                R.id.item_title_messages -> {
                    // Respond to navigation item 3 click
                    true
                }
                R.id.item_title_profile -> {
                    // Respond to navigation item 4 click
                    true
                }
                else -> false
            }
        }
    }
}