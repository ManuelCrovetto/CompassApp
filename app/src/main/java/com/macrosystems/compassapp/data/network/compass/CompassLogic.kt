package com.macrosystems.compassapp.data.network.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.MutableLiveData
import com.macrosystems.compassapp.data.model.CompassData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class CompassLogic @Inject constructor(@ApplicationContext private val context: Context) :
    SensorEventListener {

    private var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var haveSensorAccelerometer = false
    private var haveSensorMagnetometer = false
    private var rotationVector: Sensor? = null
    private var haveSensorRotationVector = false

    private val compassData: MutableLiveData<CompassData> = MutableLiveData()

    private fun startCompass() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null || sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
                compassData.postValue(CompassData(null, null, true))
            } else {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

                haveSensorAccelerometer =
                    sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_UI
                    )
                haveSensorMagnetometer =
                    sensorManager.registerListener(
                        this,
                        magnetometer,
                        SensorManager.SENSOR_DELAY_UI
                    )
            }
        } else {
            rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensorRotationVector =
                sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_UI)
        }

    }

    fun stopCompass(){
        if (haveSensorRotationVector) sensorManager.unregisterListener(this, rotationVector)
        if (haveSensorAccelerometer) sensorManager.unregisterListener(this, accelerometer)
        if (haveSensorMagnetometer) sensorManager.unregisterListener(this, magnetometer)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var azimuth = 0
        val lastAccelerometer = FloatArray(3)
        var lastAccelerometerSet = false

        val lastMagnetoMeter = FloatArray(3)
        var lastMagnetoMeterSet = false

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            azimuth = (Math.toDegrees(
                SensorManager.getOrientation(
                    rotationMatrix,
                    orientation
                )[0].toDouble()
            ) + 360).toInt() % 360
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true

        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetoMeter, 0, event.values.size)
            lastMagnetoMeterSet = true
        }

        if (lastAccelerometerSet && lastMagnetoMeterSet) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetoMeter
            )
            SensorManager.getOrientation(rotationMatrix, orientation)
            azimuth = (Math.toDegrees(
                SensorManager.getOrientation(
                    rotationMatrix,
                    orientation
                )[0].toDouble()
            ) + 360).toInt() % 360
        }

        azimuth = azimuth.toFloat().roundToInt()


        val where = when (azimuth) {
            in 281..349 -> "NW"
            in 261..280 -> "W"
            in 191..260 -> "SW"
            in 171..190 -> "S"
            in 101..170 -> "SE"
            in 81..100 -> "E"
            in 11..80 -> "NE"
            else -> "N"
        }
        compassData.postValue(CompassData(azimuth = azimuth, cardinalPoint = where, false))
    }

    fun updateCompass(): MutableLiveData<CompassData> {
        startCompass()
        return compassData
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}