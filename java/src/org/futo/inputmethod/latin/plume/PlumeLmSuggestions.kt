package org.futo.inputmethod.latin.plume

import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo

/**
 * Suggestions du modèle de langue, accompagnées des log-probabilités exactes des membres de la paire de
 * confusion du mot tapé (LanguageModel.scoreCorrectionsNative). Clé : membre en minuscules, apostrophe droite.
 */
class PlumeLmSuggestions(
    items: Collection<SuggestedWordInfo>,
    val confusionLogProbs: Map<String, Float>
) : ArrayList<SuggestedWordInfo>(items)
