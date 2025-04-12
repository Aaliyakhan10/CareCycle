package com.example.carecycle

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.carecycle.viewmodel.ViewModelAuth

class LoginActivity : AppCompatActivity() {
    private val viewModel: ViewModelAuth by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        if(viewModel.currentuser.value){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}