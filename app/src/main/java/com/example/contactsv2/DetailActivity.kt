package com.example.contactsv2

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import java.io.File
import java.io.FileOutputStream

class DetailActivity : AppCompatActivity() {

    private lateinit var ivPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText

    private lateinit var contact: Contact   // le contact reçu de MainActivity
    private var position = -1               // sa position dans la liste
    private var photo: String? = null       // photo affichée (pas encore enregistrée)
    private var fichierCamera: File? = null // fichier où l'appareil photo écrit la photo

    // 1) Galerie : choisir une image
    private val choisirPhoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) {
                // l'utilisateur a annulé
                message(R.string.msg_photo_annulee)
            } else {
                changerPhoto(uri)
            }
        }

    // 2) Appareil photo : prendre une photo (enregistrée dans fichierCamera)
    private val prendrePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { reussi ->
            val fichier = fichierCamera
            if (reussi && fichier != null && fichier.length() > 0) {
                changerPhoto(Uri.fromFile(fichier))
            } else {
                message(R.string.msg_photo_annulee)
            }
            fichier?.delete() // le fichier temporaire ne sert plus
            fichierCamera = null
        }

    // 3) Permission caméra
    private val demanderCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accordee ->
            if (accordee) {
                ouvrirCamera()
            } else {
                message(R.string.msg_camera_refusee)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // flèche retour

        ivPhoto = findViewById(R.id.ivPhoto)
        etNom = findViewById(R.id.etNom)
        etPrenom = findViewById(R.id.etPrenom)
        etTelephone = findViewById(R.id.etTelephone)
        etEmail = findViewById(R.id.etEmail)

        // Récupération des données envoyées par MainActivity
        val recu = IntentCompat.getSerializableExtra(intent, EXTRA_CONTACT, Contact::class.java)
        if (recu == null) {
            finish()
            return
        }
        contact = recu
        position = intent.getIntExtra(EXTRA_POSITION, -1)

        if (savedInstanceState == null) {
            // Premier affichage : on remplit les champs
            etNom.setText(contact.nom)
            etPrenom.setText(contact.prenom)
            etTelephone.setText(contact.telephone)
            etEmail.setText(contact.email)
            photo = contact.photo
        } else {
            // Activité recréée : les EditText se restaurent seuls, on récupère la photo
            photo = savedInstanceState.getString("photo")
            savedInstanceState.getString("fichierCamera")?.let { fichierCamera = File(it) }
        }
        afficherPhoto(ivPhoto, photo)

        findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }
        findViewById<Button>(R.id.btnGalerie).setOnClickListener { choisirPhoto.launch("image/*") }
        findViewById<Button>(R.id.btnCamera).setOnClickListener { clicCamera() }
        findViewById<Button>(R.id.btnEnregistrer).setOnClickListener { enregistrer() }
        findViewById<Button>(R.id.btnAnnuler).setOnClickListener { annuler() }
    }

    // ---------- Intents implicites : appel et e-mail ----------

    private fun appeler() {
        val telephone = etTelephone.text.toString().trim()
        if (telephone.isEmpty()) {
            etTelephone.error = getString(R.string.msg_champ_obligatoire)
            return
        }
        // ACTION_DIAL ouvre le composeur, sans appeler directement (pas de permission)
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", telephone, null))
        lancer(intent)
    }

    private fun envoyerEmail() {
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.msg_champ_obligatoire)
            return
        }
        // mailto: -> seules les applications de messagerie répondent
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        lancer(intent)
    }

    // Lance un Intent implicite, avec un message si aucune application ne peut le traiter
    private fun lancer(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            message(R.string.msg_aucune_application)
        }
    }

    // ---------- Photo ----------

    private fun clicCamera() {
        // Le téléphone n'a pas de caméra
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            message(R.string.msg_camera_indisponible)
            return
        }
        // Permission déjà accordée ? sinon on la demande
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            ouvrirCamera()
        } else {
            demanderCamera.launch(Manifest.permission.CAMERA)
        }
    }

    private fun ouvrirCamera() {
        // Fichier vide où l'appareil photo va écrire, partagé grâce au FileProvider
        val fichier = File(dossierPhotos(), "camera_${System.currentTimeMillis()}.jpg")
        fichierCamera = fichier
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", fichier)

        try {
            prendrePhoto.launch(uri)
        } catch (e: ActivityNotFoundException) {
            message(R.string.msg_aucune_application)
        }
    }

    // Copie l'image choisie (galerie ou caméra) dans l'appli puis l'affiche
    private fun changerPhoto(uri: Uri) {
        val chemin = copierPhoto(uri)
        if (chemin == null) {
            message(R.string.msg_photo_erreur)
        } else {
            photo = chemin
            afficherPhoto(ivPhoto, photo)
        }
    }

    // Enregistre une copie réduite de l'image dans files/photos/ et renvoie son chemin.
    // (L'image de la galerie n'est accessible que temporairement, d'où la copie.)
    private fun copierPhoto(uri: Uri): String? {
        return try {
            // a) lire seulement la taille de l'image
            val taille = BitmapFactory.Options()
            taille.inJustDecodeBounds = true
            contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, taille) }

            // b) réduire l'image (environ 800 px max) pour ne pas saturer la mémoire
            var echelle = 1
            while (taille.outWidth / echelle > 800 || taille.outHeight / echelle > 800) {
                echelle *= 2
            }
            val options = BitmapFactory.Options()
            options.inSampleSize = echelle
            val image = contentResolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            // c) enregistrer la copie en JPEG
            val fichier = File(dossierPhotos(), "contact_${System.currentTimeMillis()}.jpg")
            FileOutputStream(fichier).use { image.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            fichier.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun dossierPhotos(): File {
        val dossier = File(filesDir, "photos")
        dossier.mkdirs()
        return dossier
    }

    // ---------- Enregistrer / Annuler ----------

    private fun enregistrer() {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        // Champs obligatoires
        var valide = true
        if (nom.isEmpty()) {
            etNom.error = getString(R.string.msg_champ_obligatoire)
            valide = false
        }
        if (prenom.isEmpty()) {
            etPrenom.error = getString(R.string.msg_champ_obligatoire)
            valide = false
        }
        if (telephone.isEmpty()) {
            etTelephone.error = getString(R.string.msg_champ_obligatoire)
            valide = false
        }
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.msg_champ_obligatoire)
            valide = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.msg_email_invalide)
            valide = false
        }
        if (!valide) return

        // On renvoie le contact modifié à MainActivity
        val contactModifie = Contact(nom, prenom, telephone, email, photo)
        val resultat = Intent()
        resultat.putExtra(EXTRA_CONTACT, contactModifie)
        resultat.putExtra(EXTRA_POSITION, position)
        setResult(RESULT_OK, resultat)
        finish()
    }

    private fun annuler() {
        // Une nouvelle photo a été prise mais pas enregistrée : on la supprime
        val nouvellePhoto = photo
        if (nouvellePhoto != null && nouvellePhoto != contact.photo) {
            File(nouvellePhoto).delete()
        }
        setResult(RESULT_CANCELED)
        finish()
    }

    // Flèche retour de la barre du haut = Annuler
    override fun onSupportNavigateUp(): Boolean {
        annuler()
        return true
    }

    // Sauvegarde de la photo si l'activité est recréée (rotation, retour de la caméra...)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("photo", photo)
        outState.putString("fichierCamera", fichierCamera?.absolutePath)
    }

    private fun message(texte: Int) {
        Toast.makeText(this, texte, Toast.LENGTH_SHORT).show()
    }
}
