# Clavier Plume

Clavier Android français / anglais fait d'abord pour bien écrire le français : élisions, accents,
traits d'union et typographie justes. La correction vient du dictionnaire et de la grammaire, sans
modèle de langue. Hors ligne, sans publicité ni télémétrie : l'app n'a pas la permission Internet.
C'est une **version modifiée de [FUTO Keyboard](https://github.com/futo-org/android-keyboard)**.

## Points-clés

- Élisions générées par la grammaire et validées contre Dicollecte, pas devinées : « l'hôtel » oui, « l'haricot » non ; « j'aime » oui, « j'aimes » non.
- Accents et traits d'union retrouvés : « voila » → « voilà », « coeur » → « cœur », « rappelle-moi », « a-t-il ».
- Typographie : apostrophe courbe, espaces insécables avant `; : ! ?`, guillemets « ». Pas de majuscule après « etc. », « cf. », « M. » ; une URL ou une adresse e-mail n'est ni corrigée, ni coupée, ni capitalisée.
- Deux langues : la touche EN/FR bascule, la barre d'espace aussi par glissement ; après trois mots dans l'autre langue, la barre propose de changer. Au choix (prise en main, puis Langues) : une langue à la fois — le défaut — ou les deux dictionnaires ensemble.
- Un mot inconnu n'est retenu qu'à sa troisième validation ; effacé au retour arrière, il est désappris ; ★ le garde d'un toucher ; l'écran « Mots appris » permet de l'oublier.
- Vos mots : import d'une liste (texte, CSV, export Gboard, Hunspell, LibreOffice) dans le dictionnaire personnel.
- Dictionnaire : Lexique 3.83, noms propres et bigrammes du corpus de Leipzig, vocabulaire récent de LanguageTool, vocabulaire passif du Littré (reconnu s'il est tapé, jamais proposé).
- Dictée hors ligne (Whisper, modèle multilingue embarqué), glissement, presse-papiers. Le micro et, facultativement, les contacts (prénoms reconnus) sont les seules permissions sensibles.
- Réglages : 7 écrans, une trentaine d'options. Treize thèmes, dont deux « encre » à fort contraste ; typographie Selawik.

## Installation


[<img src="docs/badge_obtainium.png" alt="Disponible sur Obtainium" height="48">](https://gallaz.ch/eink/fr.html#clavier-plume)

- **Obtainium** (recommandé, les mises à jour arrivent seules) : touchez le badge depuis le téléphone, ou ajoutez `https://github.com/funkypitt/clavier-plume` dans [Obtainium](https://github.com/ImranR98/Obtainium).
- **APK** : joint à la [dernière version](../../releases/latest). Pas de mises à jour automatiques.

Puis Réglages Android → Langues et saisie → activer « Clavier Plume ».

## Compiler

```bash
git clone --recurse-submodules https://github.com/funkypitt/clavier-plume.git
cd clavier-plume
VERSION_CODE=1 VERSION_NAME=1.0.0 ./gradlew assemblePubliqueStableRelease
```

Les sous-modules (bibliothèques, traductions, thèmes, ressources, glissement) sont ceux de
FUTO Keyboard.

Le dictionnaire français `plume/res-large-overlay/raw/main_fr.dict` est généré par les outils du
projet (Lexique + règles d'élision + contrôle Dicollecte) ; il est fourni compilé.

## Licence

Le code est sous [FUTO Source First License 1.1](LICENSE.md) : usage, modification et
redistribution libres à des fins **non commerciales**. Clavier Plume est une version modifiée
de FUTO Keyboard ; les modifications sont décrites dans [NOTICE-PLUME.md](NOTICE-PLUME.md),
avec les sources de données et leurs licences (Lexique CC BY-SA, Leipzig CC BY, Littré
CC BY-SA, LanguageTool LGPL, LibreOffice MPL, Selawik OFL).

---

*Clavier Plume is a French/English Android keyboard derived from FUTO Keyboard, focused on
correct French elision, accents and typography, with a bilingual toggle key and cautious word
learning. Non-commercial licence (FUTO Source First 1.1). Nothing leaves the device.*

## Crédits / Credits

Basé sur / Based on [FUTO Keyboard](https://github.com/futo-org/android-keyboard) by FUTO Holdings Inc.,
FUTO Source First License 1.1. Voir / see `NOTICE.md` et `NOTICE-PLUME.md`.

© 2026 Pierre Gallaz. Développé avec [Claude Code](https://claude.com/claude-code) (Anthropic).
Licence FUTO Source First 1.1, voir `LICENSE.md`.

© 2026 Pierre Gallaz. Developed with [Claude Code](https://claude.com/claude-code) (Anthropic).
FUTO Source First 1.1 licence, see `LICENSE.md`.

## Thèmes

| Plume Coquette | Plume Torride |
|---|---|
| ![Plume Coquette](screenshots/coquette.png) | ![Plume Torride](screenshots/torride.png) |

| Plume Militante | Plume Nature |
|---|---|
| ![Plume Militante](screenshots/militante.png) | ![Plume Nature](screenshots/nature.png) |

| Plume Lavande | Plume Terracotta |
|---|---|
| ![Plume Lavande](screenshots/lavande.png) | ![Plume Terracotta](screenshots/terracotta.png) |
