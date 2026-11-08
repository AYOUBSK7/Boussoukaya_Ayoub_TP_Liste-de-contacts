package com.example.contactsv2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

// Adaptateur personnalisé : chaque ligne utilise le layout item_contact.xml
class ContactAdapter(context: Context, private val contacts: List<Contact>) :
    ArrayAdapter<Contact>(context, 0, contacts) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // On réutilise la vue si elle existe déjà, sinon on la crée
        val vue = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false)

        val contact = contacts[position]

        val ivPhoto = vue.findViewById<ImageView>(R.id.ivPhoto)
        val tvNom = vue.findViewById<TextView>(R.id.tvNom)
        val tvTelephone = vue.findViewById<TextView>(R.id.tvTelephone)

        tvNom.text = context.getString(R.string.nom_complet, contact.nom, contact.prenom)
        tvTelephone.text = contact.telephone
        afficherPhoto(ivPhoto, contact.photo)

        return vue
    }
}
