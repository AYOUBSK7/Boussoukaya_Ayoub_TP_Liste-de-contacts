package com.example.contactsv2

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat

class MainActivity : AppCompatActivity() {

    // Les contacts affichés dans la liste
    private var contacts = mutableListOf(
        Contact("Dupont", "Claire", "06 12 34 56 78", "claire.dupont@example.com"),
        Contact("Martin", "Thomas", "07 23 45 67 89", "thomas.martin@example.com"),
        Contact("Bernard", "Sophie", "06 98 76 54 32", "sophie.bernard@example.com"),
        Contact("Petit", "Lucas", "07 11 22 33 44", "lucas.petit@example.com"),
        Contact("Moreau", "Julien", "06 55 44 33 22", "julien.moreau@example.com")
    )

    private lateinit var adapter: ContactAdapter
    private lateinit var lvContacts: ListView

    // Retour de résultat : DetailActivity renvoie le contact modifié
    private val ouvrirDetail =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultat ->
            val data = resultat.data
            if (resultat.resultCode == RESULT_OK && data != null) {
                val contact = IntentCompat.getSerializableExtra(data, EXTRA_CONTACT, Contact::class.java)
                val position = data.getIntExtra(EXTRA_POSITION, -1)

                if (contact != null && position in contacts.indices) {
                    contacts[position] = contact      // on remplace l'ancien contact
                    adapter.notifyDataSetChanged()    // et on rafraîchit la liste
                    Toast.makeText(
                        this,
                        getString(R.string.msg_contact_modifie, contact.prenom),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // RESULT_CANCELED (bouton Annuler) : rien ne change
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Si l'activité est recréée (rotation...), on récupère la liste modifiée
        if (savedInstanceState != null) {
            @Suppress("DEPRECATION")
            val sauvegarde = savedInstanceState.getSerializable("contacts") as? ArrayList<*>
            if (sauvegarde != null) {
                contacts = sauvegarde.filterIsInstance<Contact>().toMutableList()
            }
        }

        lvContacts = findViewById(R.id.lvContacts)
        adapter = ContactAdapter(this, contacts)
        lvContacts.adapter = adapter

        // Clic sur un contact -> Intent explicite vers DetailActivity
        lvContacts.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(EXTRA_CONTACT, contacts[position])
            intent.putExtra(EXTRA_POSITION, position)
            ouvrirDetail.launch(intent)
        }
    }

    // On sauvegarde la liste pour ne pas perdre les modifications
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("contacts", ArrayList(contacts))
    }
}
