package de.sfunke.bottomsheetdialogfragmentcomposeinterop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import de.sfunke.bottomsheetdialogfragmentcomposeinterop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.mainButton.setOnClickListener {
            DemoBottomSheetDialogFragment.show(supportFragmentManager)
        }
    }
}