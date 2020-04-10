package fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox

import android.app.AlertDialog
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import fr.isen.jammayrac.androidtoolbox.R
import kotlinx.android.synthetic.main.ble_details.view.*
import kotlinx.android.synthetic.main.ble_ecriture.view.*
import kotlinx.android.synthetic.main.ble_recycler_again.view.*
import java.util.*

class BleServiceAdapter(private val serviceList : MutableList<BleService>, var context: Context, gatt: BluetoothGatt?):
    ExpandableRecyclerViewAdapter<BleServiceAdapter.ServicesViewHolder, BleServiceAdapter.CharacteristicViewHolder>(serviceList){

    val ble: BluetoothGatt? = gatt
    var notifier = false

    class ServicesViewHolder(itemView: View) : GroupViewHolder(itemView) {
        val serviceUuid: TextView = itemView.textUUID
        val nameService: TextView = itemView.textNom
    }

    class CharacteristicViewHolder(itemView: View) : ChildViewHolder(itemView) {
        val characteristicUUID: TextView = itemView.charUUID
        val properties: TextView = itemView.proprietes
        val valueBle: TextView = itemView.charValue
        val buttonRead: TextView = itemView.readButton
        val buttonWrite: TextView = itemView.writeButton
        val buttonNotify: TextView = itemView.notifyButton
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): ServicesViewHolder =
        ServicesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.ble_recycler_again, parent, false)
        )

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): CharacteristicViewHolder =
        CharacteristicViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.ble_details, parent, false)
        )

    override fun onBindChildViewHolder(holder: CharacteristicViewHolder, flatPosition: Int, group: ExpandableGroup<*>, childIndex: Int)
    {
        val characteristic: BluetoothGattCharacteristic = (group as BleService).items[childIndex]
        holder.buttonRead.visibility = View.GONE
        holder.buttonWrite.visibility = View.GONE
        holder.buttonNotify.visibility = View.GONE

        if (proprieties(characteristic.properties).contains("Lecture")) {
            holder.buttonRead.visibility = View.VISIBLE
        }
        if (proprieties(characteristic.properties).contains("Ecriture")) {
            holder.buttonWrite.visibility = View.VISIBLE
         }
        if (proprieties(characteristic.properties).contains("Notification")) {
            holder.buttonNotify.visibility = View.VISIBLE
        }


        val uuid = characteristic.uuid
        holder.characteristicUUID.text = uuid.toString()
        holder.properties.text = "Proprietés : ${proprieties(characteristic.properties)}"

        ble?.readCharacteristic(characteristic)
        holder.valueBle.text =  "Valeur : "


        holder.buttonRead.setOnClickListener {
            ble?.readCharacteristic(characteristic)
            if(characteristic.value != null){
               holder.valueBle.text =  "${String (characteristic.value)}"
            } else {
               holder.valueBle.text =  "null"
            }
        }


        holder.buttonWrite.setOnClickListener {
            val dialog = AlertDialog.Builder(context)
            val editView = View.inflate(context, R.layout.ble_ecriture, null)
            dialog.setView(editView)
            dialog.setNegativeButton("Annuler", DialogInterface.OnClickListener { dialog, which ->  })
            dialog.setPositiveButton("Valider", DialogInterface.OnClickListener {
                    _, _ ->
                val text = editView.valueText.text.toString()
                characteristic.setValue(text)
                ble?.writeCharacteristic(characteristic)
            })
            dialog.show()
        }


        holder.buttonNotify.setOnClickListener {
            if (!notifier){
                notifier = true
                if (ble != null) {
                    setCharacteristicNotificationInternal(ble, characteristic, true)
                    if(characteristic.value != null){
                        holder.valueBle.text =  "${byteArrayToHexString(characteristic.value)}"
                    } else {
                        holder.valueBle.text =  "null"
                    }
                }
            } else {
                notifier = false
                if (ble != null) {
                    setCharacteristicNotificationInternal(ble, characteristic, false)
                }
            }
        }
    }

    override fun onBindGroupViewHolder(
        holder: ServicesViewHolder,
        flatPosition: Int,
        group: ExpandableGroup<*>
    ) {
        val title = group.title
        var uuidName: String = when (group.title) {
            "00001800-0000-1000-8000-00805f9b34fb" -> "Accès générique"
            "00001801-0000-1000-8000-00805f9b34fb" -> "Attirbut générique"
            else -> "Service spécifique"
        }
        holder.serviceUuid.text = title
        holder.nameService.text = uuidName
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

    private fun setCharacteristicNotificationInternal(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, enabled: Boolean){
        gatt.setCharacteristicNotification(characteristic, enabled)

        if (characteristic.descriptors.size > 0) {
            val descriptors = characteristic.descriptors
            for (descriptor in descriptors) {

                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                    descriptor.value = if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                } else if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
                    descriptor.value = if (enabled) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    private fun proprieties(property: Int): StringBuilder {

        val sb = StringBuilder()
        if (property and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
            sb.append("Ecriture")
        }
        if (property and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            sb.append("Lecture")
        }
        if (property and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            sb.append(" Notification")
        }
        if (sb.isEmpty()) sb.append("Vide")

        return sb
    }
}