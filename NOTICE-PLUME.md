# Clavier Plume — avis de modification et attributions

Clavier Plume est une **version modifiée** de FUTO Keyboard (FUTO Holdings Inc.), distribuée
sous la FUTO Source First License 1.1 (voir `LICENSE.md`). Ce n'est pas un produit FUTO et FUTO
n'en assure pas le support.

## Principales modifications

- dictionnaire français propre (`plume/res-large-overlay/raw/main_fr.dict`) avec élisions,
  inversions, impératifs à pronom, vocabulaire passif et raccourcis de correction ;
- règles françaises dans `java/src/org/futo/inputmethod/latin/plume/` : espaces insécables,
  apostrophe typographique, guillemets, abréviations, seuil d'autocorrection par longueur,
  mots-outils protégés, vocabulaire personnel et écran « Mots appris » ;
- apprentissage à la deuxième validation, oubli sans liste noire ;
- ballon d'aperçu, trackpad par appui long sur la barre d'espace, chip d'annulation ;
- touche de bascule de langue affichant la langue cible ;
- thèmes Plume, police Selawik, géométrie des touches ;
- réglages réduits à l'essentiel ; modèle de dictée multilingue embarqué ; modèle de langue français/anglais
  embarqué (entraîné pour Plume, sans aucune donnée personnelle) ; nom et icône.

## Données et licences

| Source | Usage | Licence |
|---|---|---|
| Lexique 3.83 (New, Pallier, Brysbaert, Ferrand) — lexique.org | formes, catégories, fréquences | CC BY-SA 4.0 |
| Leipzig Corpora Collection, via Helium314/aosp-dictionaries | noms propres, bigrammes, h muet | CC BY 4.0 |
| XMLittré (François Gannaz) — littre.org | vocabulaire passif | CC BY-SA 3.0 |
| LanguageTool, ressources françaises | vocabulaire récent, mots composés | LGPL 2.1+ |
| Dicollecte / Grammalecte (Olivier R.) | validation des formes à la construction | MPL 2.0 |
| LibreOffice, autocorrection française | corrections et abréviations | MPL 2.0 |
| Selawik (Microsoft) | police du clavier | SIL OFL 1.1 |
| OpenAI Whisper, whisper.cpp / ggml, AOSP LatinIME | hérités de FUTO Keyboard | Apache 2.0 / MIT |
| FineWeb-2 (fra_Latn) et FineWeb (Hugging Face) | pré-entraînement du modèle de langue | ODC-By 1.0 |
| Wikipédia française et anglaise (wikimedia/wikipedia, 2023-11-01) | pré-entraînement du modèle de langue | CC BY-SA 4.0 |
| OPUS : OpenSubtitles (monolingue), OPUS-100 (Helsinki-NLP), parallel-sentences-opensubtitles | registre conversationnel du modèle de langue | selon OPUS (usage libre, attribution) |

Les textes des licences sont dans `licenses/`. Le dictionnaire compilé est une œuvre dérivée
de ces sources et est mis à disposition aux mêmes conditions (attribution, partage à l'identique
pour Lexique et Littré).

Le modèle de langue embarqué (`plume_fr_en_4b.gguf`, 36 M de paramètres, format GGUF de FUTO) a été entraîné
par le projet Plume sur les corpus ci-dessus (aucun texte de l'utilisateur) ; il est mis à disposition sous
CC BY-SA 4.0, avec attribution des sources. Il ne contient pas les textes eux-mêmes, seulement des paramètres
statistiques ; les sous-titres OpenSubtitles restent la propriété de leurs auteurs.
