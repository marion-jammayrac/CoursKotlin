package fr.isen.jammayrac.androidtoolbox

import android.bluetooth.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox.BleService
import fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox.BleServiceAdapter
import kotlinx.android.synthetic.main.activity_ble3.*


class BleActivity3 : AppCompatActivity() {

    private var bluetoothGatt: BluetoothGatt? = null
    private lateinit var adapter: BleServiceAdapter
    private var TAG:String = "services"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble3)
        val device : BluetoothDevice = intent.getParcelableExtra("ble_device")
        deviceName.text = device.name
        bluetoothGatt = device.connectGatt(this, true, gattCallback)

    }

    private val gattCallback = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt : BluetoothGatt, status: Int, newState: Int)
        {
            when (newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    runOnUiThread {
                        connectionState.text = STATE_CONNECTED
                    }
                    bluetoothGatt?.discoverServices()
                    Log.i(TAG, "Attempting to start discovery :" +bluetoothGatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    runOnUiThread {
                        connectionState.text = STATE_DISCONNECTED
                    }
                }
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            runOnUiThread {
                itemView.adapter = BleServiceAdapter(gatt?.services?.map {
                        BleService(it.uuid.toString(), it.characteristics)
                    }?.toMutableList() ?: arrayListOf(),this@BleActivity3, gatt)
                itemView.layoutManager = LinearLayoutManager(this@BleActivity3)
            }
        }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic, status: Int)
    {
        val value = characteristic.getStringValue(0)
        Log.e("TAG", "onCharacteristicRead: " + value + " UUID " + characteristic.uuid.toString())
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic, status: Int)
    {
        val value = characteristic.value
        Log.e("TAG", "onCharacteristicWrite: " + value + " UUID " + characteristic.uuid.toString())
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic
    ) {
        val value = byteArrayToHexString(characteristic.value)
        Log.e("TAG", "onCharacteristicChanged: " + value + " UUID " + characteristic.uuid.toString())
        adapter.notifyDataSetChanged()
    }
    }

    private fun byteArrayToHexString(array: ByteArray): String {
        val result = StringBuilder(array.size * 2)
        for ( byte in array ) {
            val toAppend = String.format("%X", byte)
            result.append(toAppend).append("-")
        }
        result.setLength(result.length - 1)
        return result.toString()
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

    override fun onStop() {
        super.onStop()
        bluetoothGatt?.close()
    }

}
