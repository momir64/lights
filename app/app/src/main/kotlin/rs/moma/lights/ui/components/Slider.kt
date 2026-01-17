@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package rs.moma.lights.ui.components

import com.google.android.material.slider.Slider as XmlSlider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import android.content.res.ColorStateList
import android.view.LayoutInflater
import androidx.compose.ui.Modifier
import rs.moma.lights.ui.theme.*
import kotlin.math.roundToInt
import java.math.RoundingMode
import java.math.BigDecimal
import rs.moma.lights.R

@Composable
fun Slider(value: Float?, modifier: Modifier = Modifier, vertical: Boolean = false, onRelease: (value: Float) -> Unit) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val sliderLayout = if (vertical) R.layout.slider_vertical else R.layout.slider_horizontal
            LayoutInflater.from(context).inflate(sliderLayout, null).apply {
                val slider = findViewById<XmlSlider>(R.id.slider)
                slider.trackInactiveTintList = ColorStateList.valueOf(ButtonColor.toArgb())
                slider.trackActiveTintList = ColorStateList.valueOf(AccentColor.toArgb())
                slider.thumbTintList = ColorStateList.valueOf(AccentColor.toArgb())
                slider.tickTintList = ColorStateList.valueOf(AccentColor.toArgb())
                slider.setLabelFormatter { value -> "${value.roundToInt()}%" }
                slider.addOnSliderTouchListener(object : XmlSlider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: XmlSlider) {}
                    override fun onStopTrackingTouch(slider: XmlSlider) = onRelease(slider.value)
                })
                slider.addOnChangeListener { _, value, _ ->
                    val rounded = BigDecimal(value.toDouble()).setScale(2, RoundingMode.HALF_UP).toFloat()
                    slider.value = rounded.coerceAtLeast(1f).coerceAtMost(100f)
                }
            }
        },
        update = {
            val slider = it.findViewById<XmlSlider>(R.id.slider)
            if (slider.value != value && value != null)
                slider.value = value
        }
    )
}