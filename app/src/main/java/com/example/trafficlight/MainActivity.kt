package com.example.trafficlight

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.trafficlight.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var currentState: TrafficState = TrafficState.CAR_RED

    private val carGroups by lazy {
        listOf(
            CarGroup(binding.redCarTop, binding.yellowCarTop, binding.greenCarTop),
            CarGroup(binding.redCarBottom, binding.yellowCarBottom, binding.greenCarBottom)
        )
    }

    private val zebraGroups by lazy {
        listOf(
            ZebraGroup(binding.redZebraLeft, binding.greenZebraLeft),
            ZebraGroup(binding.redZebraRight, binding.greenZebraRight)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            currentState = TrafficState.values()[savedInstanceState.getInt("state")]
        }

        startCurrentState()
    }

    private fun startCurrentState() {
        when (currentState) {
            TrafficState.CAR_RED -> startCarRed()
            TrafficState.CAR_RED_YELLOW -> startCarRedYellow()
            TrafficState.CAR_GREEN -> startCarGreen()
            TrafficState.CAR_YELLOW -> startCarYellow()
            TrafficState.CAR_RED_WAIT -> startCarRedWait()
        }
    }

    private fun startCarRed() {
        setCarState(CarState.RED)
        setZebraState(ZebraState.GREEN)
        handler.postDelayed({ blinkZebraGreen { startZebraRedPause { transitionTo(TrafficState.CAR_RED_YELLOW) } } }, 5000)
    }

    private fun startCarRedYellow() {
        setCarState(CarState.RED_YELLOW)
        setZebraState(ZebraState.RED)
        handler.postDelayed({ transitionTo(TrafficState.CAR_GREEN) }, 2000)
    }

    private fun startCarGreen() {
        setCarState(CarState.GREEN)
        setZebraState(ZebraState.RED)
        startZebraRedPause {
            handler.postDelayed({
                blinkCarGreen { transitionTo(TrafficState.CAR_YELLOW) }
            }, 5000)
        }
    }

    private fun startCarYellow() {
        setCarState(CarState.YELLOW)
        startZebraRedPause {
            handler.postDelayed({ transitionTo(TrafficState.CAR_RED_WAIT) }, 2000)
        }
    }

    private fun startCarRedWait() {
        setCarState(CarState.RED)
        setZebraState(ZebraState.RED)
        handler.postDelayed({ transitionTo(TrafficState.CAR_RED) }, 2000)
    }

    private fun transitionTo(nextState: TrafficState) {
        currentState = nextState
        startCurrentState()
    }

    private fun blinkCarGreen(onFinish: () -> Unit) {
        blinkLights(carGroups.flatMap { listOf(it.green) }, R.color.green, 6, onFinish)
    }

    private fun blinkZebraGreen(onFinish: () -> Unit) {
        blinkLights(zebraGroups.flatMap { listOf(it.green) }, R.color.green, 6, onFinish)
    }

    private fun blinkLights(views: List<View>, colorRes: Int, count: Int, onFinish: () -> Unit) {
        var blinkCount = 0
        val blinkRunnable = object : Runnable {
            override fun run() {
                if (blinkCount < count) {
                    val isVisible = blinkCount % 2 == 0
                    views.forEach { v ->
                        setLightColor(v, isVisible, colorRes)
                    }
                    handler.postDelayed(this, 500)
                    blinkCount++
                } else {
                    onFinish()
                }
            }
        }
        blinkRunnable.run()
    }

    private fun setCarState(state: CarState) {
        carGroups.forEach { group ->
            setLightColor(group.red, state.isRed(), R.color.red)
            setLightColor(group.yellow, state.isYellow(), R.color.yellow)
            setLightColor(group.green, state.isGreen(), R.color.green)
        }
    }

    private fun setZebraState(state: ZebraState) {
        zebraGroups.forEach { group ->
            setLightColor(group.red, state == ZebraState.RED || state == ZebraState.RED, R.color.red)
            setLightColor(group.green, state == ZebraState.GREEN, R.color.green)
        }
    }

    private fun startZebraRedPause(nextAction: () -> Unit) {
        setZebraState(ZebraState.RED)
        handler.postDelayed({
            nextAction()
        }, 2000)
    }

    private fun setLightColor(view: View, isActive: Boolean, colorRes: Int) {
        val color = if (isActive) ContextCompat.getColor(this, colorRes) else ContextCompat.getColor(this, R.color.gray)
        view.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("state", currentState.ordinal)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}