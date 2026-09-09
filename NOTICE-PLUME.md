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
- réglages réduits à l'essentiel ; modèle de dictée multilingue embarqué ; nom et icône.

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

Les textes des licences sont dans `licenses/`. Le dictionnaire compilé est une œuvre dérivée
de ces sources et est mis à disposition aux mêmes conditions (attribution, partage à l'identique
pour Lexique et Littré).
