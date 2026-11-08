package com.example.contactsv2

import android.graphics.BitmapFactory
import android.widget.ImageView
import java.io.File
import java.io.Serializable

// Clés utilisées pour passer les données entre les deux activités
const val EXTRA_CONTACT = "contact"
const val EXTRA_POSITION = "position"

// Un contact. Serializable pour pouvoir le mettre dans un Intent.
data class Contact(
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photo: String? = null // chemin du fichier photo (null = photo par défaut)
) : Serializable

// Affiche la photo du contact, ou l'avatar par défaut s'il n'en a pas
fun afficherPhoto(imageView: ImageView, photo: String?) {
    imageView.clipToOutline = true // découpe la photo en rond (fond @drawable/cercle)

    if (photo != null && File(photo).exists()) {
        imageView.setImageBitmap(BitmapFactory.decodeFile(photo))
    } else {
        imageView.setImageResource(R.drawable.ic_avatar)
    }
}
