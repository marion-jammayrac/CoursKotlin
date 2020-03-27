package fr.isen.jammayrac.androidtoolbox

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_ble2.view.*
import kotlin.reflect.KFunction1

class BleActivity2(
    private val list: List<BleActivity.Device>,
    kFunction1: KFunction1<@ParameterName(name = "device") BluetoothDevice, Unit>
) : //private val deviceClickListener: (BluetoothDevice) -> Unit) :
    RecyclerView.Adapter<BleActivity2.BluetoothViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder =
        BluetoothViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_ble2, parent, false)
        )

    override fun getItemCount(): Int = list.size

    class BluetoothViewHolder(bluetoothView: View) : RecyclerView.ViewHolder(bluetoothView) {
        val layout = bluetoothView.cellBle
        val deviceName: TextView = bluetoothView.NameBleText
        val deviceAddress: TextView =  bluetoothView.MacBleText
        val deviceRssi: TextView = bluetoothView.RissBleText
    }

    override fun onBindViewHolder(holder: BleActivity2.BluetoothViewHolder, position: Int) {
        holder.deviceName.text = list[position].name?: "Nom inconnu"
        holder.deviceAddress.text = list[position].address
        holder.deviceRssi.text = list[position].rssi.toString()
    }

}