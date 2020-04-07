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
        val arrow: ImageView = itemView.flecheButton
        val serviceUuid: TextView = itemView.textUUID
        val nameService: TextView = itemView.textNom
      //  val serviceName: TextView = detailsView.textUUID

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
        val characteristicName: TextView = itemView.characteristicName
        val properties: TextView = itemView.properties
        val valueBle: TextView = itemView.characteristicValue
        val buttonRead: TextView = itemView.readButton
        val buttonWrite: TextView = itemView.writeButton
        val buttonNotify: TextView = itemView.notifyButton
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
        //val uuid = characteristic.uuid
        // holder.characteristicUUID.text = uuid.toString()
        holder.buttonRead.visibility = View.GONE
        holder.buttonWrite.visibility = View.GONE
        holder.buttonNotify.visibility = View.GONE

        if (proprieties(characteristic.properties).contains("Lire")) {
            holder.buttonRead.visibility = View.VISIBLE
        }
        if (proprieties(characteristic.properties).contains("Ecrire")) {
            holder.buttonWrite.visibility = View.VISIBLE
         }
        if (proprieties(characteristic.properties).contains("Notifier")) {
            holder.buttonNotify.visibility = View.VISIBLE
        }


        val uuid = characteristic.uuid
        val name = propertyName(uuid)

        holder.characteristicUUID.text = uuid.toString()
        holder.characteristicName.text = name
        holder.properties.text = "Proprietés : ${proprieties(characteristic.properties)}"

        ble?.readCharacteristic(characteristic)
        holder.valueBle.text =  "Valeur : "


        holder.buttonRead.setOnClickListener {
            ble?.readCharacteristic(characteristic)
            if(characteristic.value != null){
               holder.valueBle.text =  "Valeur : ${String (characteristic.value)}"
            } else {
               holder.valueBle.text =  "Valeur : null"
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
                holder.buttonNotify.setBackgroundColor(0x40FF0000)
                if (ble != null) {
                    setCharacteristicNotificationInternal(ble, characteristic, true)
                    if(characteristic.value != null){
                        holder.valueBle.text =  "Valeur : ${byteArrayToHexString(characteristic.value)}"
                    } else {
                        holder.valueBle.text =  "Valeur : null"
                    }
                }
            } else {
                notifier = false
                holder.buttonNotify.setBackgroundColor(0x00FFFFFF)
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
            val toAppend = String.format("%X", byte) // hexadecimal
            result.append(toAppend).append("-")
        }
        result.setLength(result.length - 1) // remove last '-'
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
            sb.append("Ecrire")
        }
        if (property and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            sb.append(" Lire")
        }
        if (property and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            sb.append(" Notifier")
        }
        if (sb.isEmpty()) sb.append("Aucune")

        return sb
    }

    fun propertyName(uuid: UUID): String {

        var nom: String

        when (uuid.toString().substring(4, 8)) {
            "2a7e" -> nom = "Limite inférieure de la fréquence cardiaque aérobie"
            "2a84" -> nom = "Limite supérieure de la fréquence cardiaque aérobie"
            "2a7f" -> nom = "Seuil aérobie"
            "2a80" -> nom = "Age"
            "2a5a" -> nom = "Agrégat"
            "2a43" -> nom = "Alert Category ID"
            "2a42" -> nom = "Alert Category ID Bit Mask"
            "2a06" -> nom = "Niveau d'alerte"
            "2a44" -> nom = "Point de contrôle de la notification d'alerte"
            "2a3f" -> nom = "état d'alerte"
            "2ab3" -> nom = "Altitude"
            "2a81" -> nom = "Limite inférieure de la fréquence cardiaque anaérobie"
            "2a82" -> nom = "Limite supérieure de la fréquence cardiaque anaérobie"
            "2a83" -> nom = "Seuil anaérobie"
            "2a58" -> nom = "Analogue"
            "2a59" -> nom = "Sortie analogique"
            "2a73" -> nom = "Direction apparente du vent"
            "2a72" -> nom = "Vitesse apparente du vent"
            "2a01" -> nom = "Apparence"
            "2aa3" -> nom = "Tendance de la pression barométrique"
            "2a19" -> nom = "Niveau de la batterie"
            "2a1b" -> nom = "État du niveau de la batterie"
            "2a1a" -> nom = "État de la batterie"
            "2a49" -> nom = "Article sur la pression artérielle"
            "2a35" -> nom = "Mesure de la pression artérielle"
            "2a9b" -> nom = "Élément de composition corporelle"
            "2a9c" -> nom = "Mesure de la composition corporelle"
            "2a38" -> nom = "Emplacement du capteur corporel"
            "2aa4" -> nom = "Point de contrôle de la gestion des obligations"
            "2aa5" -> nom = "Caractéristiques de la gestion des obligations"
            "2a22" -> nom = "Rapport de saisie au clavier d'amorçage"
            "2a32" -> nom = "Boot Keyboard Output Report"
            "2a33" -> nom = "Rapport de saisie de la souris d'amorçage"
            "2b2b" -> nom = "Point de contrôle BSS"
            "2b2c" -> nom = "Réponse du BSS"
            "2aa8" -> nom = "Élément du CGM"
            "2aa7" -> nom = "Mesure CGM"
            "2aab" -> nom = "Durée de la session de la CGM"
            "2aaa" -> nom = "Heure de début de la session de la CGM"
            "2aac" -> nom = "Point de contrôle des opérations spécifiques du CGM"
            "2aa9" -> nom = "Statut CGM"
            "2b29" -> nom = "Fonctionnalités supportées par le client"
            "2ace" -> nom = "Données relatives aux formateurs"
            "2a5c" -> nom = "Dossier CSC"
            "2a5b" -> nom = "Mesure CSC"
            "2a2b" -> nom = "Heure actuelle"
            "2a66" -> nom = "Point de contrôle de la puissance des cyclistes"
            "2a65" -> nom = "Fonctionnalité de puissance cycliste"
            "2a63" -> nom = "Mesure de la puissance des vélos"
            "2a64" -> nom = "Vecteur de puissance cycliste"
            "2a99" -> nom = "Changement de l'incrément de la base de données"
            "2b2a" -> nom = "Hachage de la base de données"
            "2a85" -> nom = "Date de naissance"
            "2a86" -> nom = "Date de l'évaluation du seuil"
            "2a08" -> nom = "Date Heure"
            "2aed" -> nom = "Date UTC"
            "2a0a" -> nom = "Jour Date Heure"
            "2a09" -> nom = "Jour de la semaine"
            "2a7d" -> nom = "Valeur du descripteur modifiée"
            "2a7b" -> nom = "Point de rosée"
            "2a56" -> nom = "Numérique"
            "2a57" -> nom = "Sortie numérique"
            "2a0d" -> nom = "DST Offset"
            "2a6c" -> nom = "Élévation"
            "2a87" -> nom = "Adresse électronique"
            "2b2d" -> nom = "Emergency ID"
            "2b2e" -> nom = "Texte d'urgence"
            "2a0b" -> nom = "Exact Time 100"
            "2a0c" -> nom = "Exact Time 256"
            "2a88" -> nom = "Limite inférieure de la fréquence cardiaque des brûlures d'estomac"
            "2a89" -> nom = "Limite supérieure de la fréquence cardiaque des brûlures de graisse"
            "2a26" -> nom = "Chaîne de révision du microprogramme"
            "2a8a" -> nom = "Prénom"
            "2ad9" -> nom = "Point de contrôle des appareils de fitness"
            "2acc" -> nom = "Appareil de fitness"
            "2ada" -> nom = "État de l'appareil de fitness"
            "2a8b" -> nom = "Limites de fréquence cardiaque à cinq zones"
            "2ab2" -> nom = "Numéro d'étage"
            "2aa6" -> nom = "Résolution d'adresse centrale"
            "2a00" -> nom = "Nom de l'appareil"
            "2a04" -> nom = "Paramètres de connexion préférés des périphériques"
            "2a02" -> nom = "drapeau de confidentialité périphérique"
            "2a03" -> nom = "Adresse de reconnexion"
            "2a05" -> nom = "Service modifié"
            "2a8c" -> nom = "Genre"
            "2a51" -> nom = "Glucose Feature"
            "2a18" -> nom = "Mesure du glucose"
            "2a34" -> nom = "Contexte de la mesure du glucose"
            "2a74" -> nom = "facteur de rafale"
            "2a27" -> nom = "Chaîne de révision matérielle"
            "2a39" -> nom = "Point de contrôle de la fréquence cardiaque"
            "2a8d" -> nom = "Fréquence cardiaque maximale"
            "2a37" -> nom = "Mesure de la fréquence cardiaque"
            "2a7a" -> nom = "Indice de chaleur"
            "2a8e" -> nom = "Hauteur"
            "2a4c" -> nom = "Point de contrôle HID"
            "2a4a" -> nom = "HID Information"
            "2a8f" -> nom = "Tour de hanche"
            "2aba" -> nom = "Point de contrôle HTTP"
            "2ab9" -> nom = "Entité HTTP"
            "2ab7" -> nom = "En-têtes HTTP"
            "2ab8" -> nom = "Code d'état HTTP"
            "2abb" -> nom = "HTTPS Sécurité"
            "2a6f" -> nom = "Humidité"
            "2b22" -> nom = "IDD Annunciation Status"
            "2b25" -> nom = "Point de contrôle de commande IDD"
            "2b26" -> nom = "Données de commande IDD"
            "2b23" -> nom = "Caractéristiques de l'IDD"
            "2b28" -> nom = "Données historiques de l'IDD"
            "2b27" -> nom = "Point de contrôle d'accès aux enregistrements IDD"
            "2b21" -> nom = "Statut de l'IDD"
            "2b20" -> nom = "IDD Statut modifié"
            "2b24" -> nom = "Point de contrôle du lecteur de statut IDD"
            "2a2a" -> nom = "IEEE 11073-20601 liste des données de certification réglementaire"
            "2ad2" -> nom = "Données sur les vélos d'intérieur"
            "2aad" -> nom = "Configuration du positionnement intérieur"
            "2a36" -> nom = "Pression intermédiaire des brassards"
            "2a1e" -> nom = "Température intermédiaire"
            "2a77" -> nom = "Irradiance"
            "2aa2" -> nom = "Langue"
            "2a90" -> nom = "Nom de famille"
            "2aae" -> nom = "Latitude"
            "2a6b" -> nom = "Point de contrôle LN"
            "2a6a" -> nom = "LN Feature"
            "2ab1" -> nom = "Coordonnées locales de l'Est"
            "2ab0" -> nom = "Coordonnée locale du Nord"
            "2a0f" -> nom = "Information sur l'heure locale"
            "2a67" -> nom = "Caractéristique de localisation et de vitesse"
            "2ab5" -> nom = "Nom du lieu"
            "2aaf" -> nom = "Longitude"
            "2a2c" -> nom = "Déclinaison magnétique"
            "2aa0" -> nom = "Densité du flux magnétique - 2D"
            "2aa1" -> nom = "Densité du flux magnétique - 3D"
            "2a29" -> nom = "Chaîne de nom du fabricant"
            "2a91" -> nom = "Fréquence cardiaque maximale recommandée"
            "2a21" -> nom = "Intervalle de mesure"
            "2a24" -> nom = "Chaîne de numéros de modèle"
            "2a68" -> nom = "Navigation"
            "2a3e" -> nom = "Disponibilité du réseau"
            "2a46" -> nom = "Nouvelle alerte"
            "2ac5" -> nom = "Point de contrôle de l'action sur l'objet"
            "2ac8" -> nom = "Objet modifié"
            "2ac1" -> nom = "Objet créé en premier"
            "2ac3" -> nom = "Object ID"
            "2ac2" -> nom = "Objet modifié en dernier lieu"
            "2ac6" -> nom = "Point de contrôle de la liste d'objets"
            "2ac7" -> nom = "Filtre de la liste d'objets"
            "2abe" -> nom = "Nom de l'objet"
            "2ac4" -> nom = "Propriétés de l'objet"
            "2ac0" -> nom = "Taille de l'objet"
            "2abf" -> nom = "Type d'objet"
            "2abd" -> nom = "OTS Feature"
            "2a5f" -> nom = "Caractéristique de mesure continue PLX"
            "2a60" -> nom = "PLX Features"
            "2a5e" -> nom = "Mesure de contrôle ponctuelle PLX"
            "2a50" -> nom = "PnP ID"
            "2a75" -> nom = "Concentration de pollen"
            "2a2f" -> nom = "Position 2D"
            "2a30" -> nom = "Position 3D"
            "2a69" -> nom = "Qualité du poste"
            "2a6d" -> nom = "Pression"
            "2a4e" -> nom = "Mode Protocole"
            "2a62" -> nom = "Point de contrôle de l'oxymétrie de pouls"
            "2a78" -> nom = "Pluie"
            "2b1d" -> nom = "Fonctionnalité RC"
            "2b1e" -> nom = "Réglages RC"
            "2b1f" -> nom = "Point de contrôle de la configuration de la reconnexion"
            "2a52" -> nom = "Point de contrôle d'accès aux enregistrements"
            "2a14" -> nom = "Information sur l'heure de référence"
            "2b37" -> nom = "Caractéristique de l'utilisateur enregistré"
            "2a3a" -> nom = "Amovible"
            "2a4d" -> nom = "Rapport"
            "2a4b" -> nom = "Carte de rapport"
            "2ac9" -> nom = "Adresse privée résoluble uniquement"
            "2a92" -> nom = "Fréquence cardiaque au repos"
            "2a40" -> nom = "Point de contrôle des sonneries"
            "2a41" -> nom = "Réglage de la sonnerie"
            "2ad1" -> nom = "Données sur les rameurs"
            "2a54" -> nom = "RSC Feature"
            "2a53" -> nom = "Mesure RSC"
            "2a55" -> nom = "Point de contrôle SC"
            "2a4f" -> nom = "Fenêtre d'intervalle de balayage"
            "2a31" -> nom = "Rafraîchissement du scan"
            "2a3c" -> nom = "Température en Celsius"
            "2a10" -> nom = "Fuseau horaire secondaire"
            "2a5d" -> nom = "Emplacement du capteur"
            "2a25" -> nom = "Numéros de série"
            "2b3a" -> nom = "Caractéristiques supportées par le serveur"
            "2a3b" -> nom = "Service requis"
            "2a28" -> nom = "Chaîne de révision du logiciel"
            "2a93" -> nom = "Type de sport pour les seuils aérobies et anaérobies"
            "2ad0" -> nom = "Stair Climber Data"
            "2acf" -> nom = "String"
            "2a3d" -> nom = "Fréquence cardiaque supportée"
            "2ad7" -> nom = "Plage d'inclinaison supportée"
            "2a47" -> nom = "Catégorie d'alerte soutenue"
            "2ad8" -> nom = "Gamme de puissance supportée"
            "2ad6" -> nom = "Fourchette de niveaux de résistance pris en charge"
            "2ad4" -> nom = "Gamme de vitesse supportée"
            "2a48" -> nom = "Catégorie d'alerte non lue soutenue"
            "2a23" -> nom = "System ID"
            "2abc" -> nom = "Point de contrôle TDS"
            "2a6e" -> nom = "Température"
            "2a1f" -> nom = "Température Celsius"
            "2a20" -> nom = "Température Fahrenheit"
            "2a1c" -> nom = "Mesure de la température"
            "2a1d" -> nom = "Type de température"
            "2a94" -> nom = "Limites de fréquence cardiaque à trois zones"
            "2a12" -> nom = "Précision du temps"
            "2a15" -> nom = "Diffusion du temps"
            "2a13" -> nom = "Time Source"
            "2a16" -> nom = "Point de contrôle de la mise à jour de l'heure"
            "2a17" -> nom = "État de mise à jour du temps"
            "2a11" -> nom = "Temps avec DST"
            "2a0e" -> nom = "Fuseau horaire"
            "2ad3" -> nom = "Statut de formation"
            "2acd" -> nom = "Données du tapis roulant"
            "2a71" -> nom = "Direction du vent réel"
            "2a70" -> nom = "Vitesse du vent réel"
            "2a95" -> nom = "Limite de fréquence cardiaque à deux zones"
            "2a07" -> nom = "Niveau de puissance Tx"
            "2ab4" -> nom = "Incertitude"
            "2a45" -> nom = "Statut d'alerte non lu"
            "2ab6" -> nom = "URI"
            "2a9f" -> nom = "Point de contrôle des utilisateurs"
            "2a9a" -> nom = "Index des utilisateurs"
            "2a76" -> nom = "indice UV"
            "2a96" -> nom = "VO2 Max"
            "2a97" -> nom = "Tour de taille"
            "2a98" -> nom = "Poids"
            "2a9d" -> nom = "Mesure du poids"
            "2a9e" -> nom = "Elément de la balance de poids"
            "2a79" -> nom = "Refroidissement éolien"
            else -> nom = "Caractéristique spécifique"
        }
        return nom
    }
}