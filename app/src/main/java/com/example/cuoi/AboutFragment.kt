package com.example.cuoi

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat

class AboutFragment : Fragment() {
    private lateinit var colorChangingText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val colors = arrayOf(
        R.color.redder, R.color.red, R.color.orange,
        R.color.yellow, R.color.greener, R.color.green,
        R.color.teal_700, R.color.teal_200, R.color.blue,
        R.color.purple_200, R.color.purple_500, R.color.purple_700,
        R.color.black)
    private var currentColorIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        colorChangingText = view.findViewById(R.id.text)

        // Runnable that changes color every 0.15 seconds
        val colorChangeRunnable = object : Runnable {
            override fun run() {
                // Change color
                colorChangingText.setTextColor(ContextCompat.getColor(requireContext(), colors[currentColorIndex]))

                // Update to the next color
                currentColorIndex = (currentColorIndex + 1) % colors.size

                // Re-run the Runnable after 150ms
                handler.postDelayed(this, 200)
            }
        }

        // Start changing color
        handler.postDelayed(colorChangeRunnable, 200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // Stop the color change when the view is destroyed
    }
}