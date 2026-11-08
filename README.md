# Contacts V2

TP Android Kotlin — Ayoub Boussoukaya, L3 DSI

Application de contacts avec deux écrans : la liste (MainActivity) et le détail d'un contact (DetailActivity).

## Fonctionnalités

- Liste personnalisée (ListView + adaptateur) : photo, nom, prénom, téléphone
- Clic sur un contact : Intent explicite vers DetailActivity
- Modification du nom, prénom, téléphone et e-mail
- Bouton Appeler (ACTION_DIAL) et bouton E-mail (ACTION_SENDTO)
- Photo depuis la galerie ou l'appareil photo
- Enregistrer : retour de résultat à MainActivity, la liste se met à jour
- Annuler : retour sans modification
- Gestion des erreurs : champs vides, photo annulée, aucune application, caméra refusée ou absente

## Captures

| Liste | Détail | Modification | Liste modifiée |
|:-:|:-:|:-:|:-:|
| <img src="captures/1_liste.png" width="160"> | <img src="captures/2_detail.png" width="160"> | <img src="captures/3_modification.png" width="160"> | <img src="captures/4_liste_modifiee.png" width="160"> |

| Galerie | Caméra | Appeler | Appel | E-mail |
|:-:|:-:|:-:|:-:|:-:|
| <img src="captures/5_galerie.png" width="160"> | <img src="captures/6_camera.png" width="160"> | <img src="captures/7_appel.png" width="160"> | <img src="captures/8_appel_en_cours.png" width="160"> | <img src="captures/9_email.png" width="160"> |
