package com.example.sharidev2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.sharidev2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Variables
        val navHostFragment = supportFragmentManager.findFragmentById(binding.fragmentContainerMain.id) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = binding.bottomNavigation


        bottomNav.setupWithNavController(navController)
    }


}