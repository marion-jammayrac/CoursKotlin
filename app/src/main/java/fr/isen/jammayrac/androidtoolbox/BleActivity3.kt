package fr.isen.jammayrac.androidtoolbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox.BleService
import fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox.BleServiceAdapter
import kotlinx.android.synthetic.main.activity_ble3.*
import kotlinx.android.synthetic.main.ble_recycler_again.*

class BleActivity3 : AppCompatActivity() {

    //private var connectionState = BleActivity3.STATE_DISCONNECTED
    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var adapter: BleServiceAdapter
    private var TAG:String = "services"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble3)

        val device : BluetoothDevice = intent.getParcelableExtra("ble_device")
        bluetoothGatt = device.connectGatt(this, true, gattCallback)

    }

    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(
            gatt : BluetoothGatt,
            status: Int,
            newState: Int
        ){
            val intentAction: String
            when (newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    runOnUiThread {
                        textUUID.text = STATE_CONNECTED
                    }
                    bluetoothGatt?.discoverServices()
                    Log.i(TAG, "Attempting to start service discovery :" +bluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    runOnUiThread {
                        textUUID.text = STATE_DISCONNECTED
                    }
                    bluetoothGatt?.discoverServices()
                    Log.i(TAG, "deconnecté")
                }
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            runOnUiThread {
                itemView.adapter = BleServiceAdapter(
                    gatt?.services?.map {
                        BleService(
                            it.uuid.toString(),
                            it.characteristics
                        )
                    }?.toMutableList() ?: arrayListOf()
                )
                itemView.layoutManager = LinearLayoutManager(this@BleActivity3)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        bluetoothGatt?.close()
    }

    companion object{
        private const val STATE_DISCONNECTED = "déconnecté"
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = "Connecté"
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    }

}