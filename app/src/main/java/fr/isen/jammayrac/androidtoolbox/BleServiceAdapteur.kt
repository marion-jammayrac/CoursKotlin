package fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox

import android.bluetooth.BluetoothGattCharacteristic
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
import kotlinx.android.synthetic.main.activity_ble3.view.*
import kotlinx.android.synthetic.main.ble_details.view.*

class BleServiceAdapter(private val serviceList : MutableList<BleService>):
    ExpandableRecyclerViewAdapter<BleServiceAdapter.ServicesViewHolder,
            BleServiceAdapter.CharacteristicViewHolder>(serviceList){

    class ServicesViewHolder(detailsView: View) : GroupViewHolder(detailsView) {
        val arrow: ImageView = detailsView.flecheButton


        val serviceName: TextView = detailsView.textUUID

        override fun expand() {
            animateExpand()
        }

        override fun collapse() {
            animateCollapse()
        }

        private fun animateExpand() {
            val rotate = RotateAnimation(
                360F,
                180F,
                RELATIVE_TO_SELF,
                0.5f,
                RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 300
            rotate.fillAfter = true
            arrow.animation = rotate
        }

        private fun animateCollapse() {
            val rotate = RotateAnimation(
                180F,
                360F,
                RELATIVE_TO_SELF,
                0.5f,
                RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 300
            rotate.fillAfter = true
            arrow.animation = rotate
        }
    }

    class CharacteristicViewHolder(itemView: View) : ChildViewHolder(itemView) {
        val characteristicUUID: TextView = itemView.characteristicUUID
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): ServicesViewHolder =
        ServicesViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.ble_details, parent, false)
        )

    override fun onCreateChildViewHolder(
        parent: ViewGroup?,
        viewType: Int
    ): CharacteristicViewHolder =
        CharacteristicViewHolder(
            LayoutInflater.from(parent?.context)
                .inflate(R.layout.ble_details, parent, false)
        )

    override fun onBindChildViewHolder(
        holder: CharacteristicViewHolder,
        flatPosition: Int,
        group: ExpandableGroup<*>,
        childIndex: Int
    ) {
        val characteristic: BluetoothGattCharacteristic = (group as BleService).items[childIndex]
        val uuid = characteristic.uuid
        holder.characteristicUUID.text = uuid.toString()
    }

    override fun onBindGroupViewHolder(
        holder: ServicesViewHolder,
        flatPosition: Int,
        group: ExpandableGroup<*>
    ) {
        val title = group.title
        holder.serviceName.text = title
    }
}