package org.futo.voiceinput.shared

import org.futo.voiceinput.shared.types.ModelBuiltInAsset
import org.futo.voiceinput.shared.types.ModelDownloadable
import org.futo.voiceinput.shared.types.ModelLoader

val BUILTIN_ENGLISH_MODEL: ModelLoader = ModelBuiltInAsset(
    name = R.string.tiny_en_name,
    ggmlFile = "tiny_en_acft_q8_0.bin.not.tflite"
)

val ENGLISH_MODELS: List<ModelLoader> = listOf(
    ModelBuiltInAsset(
        name = R.string.tiny_en_name,
        ggmlFile = "tiny_en_acft_q8_0.bin.not.tflite"
    ),

    ModelDownloadable(
        name = R.string.base_en_name,
        ggmlFile = "base_en_acft_q8_0.bin",
        checksum = "e9b4b7b81b8a28769e8aa9962aa39bb9f21b622cf6a63982e93f065ed5caf1c8"
    ),
    ModelDownloadable(
        name = R.string.small_en_name,
        ggmlFile = "small_en_acft_q8_0.bin",
        checksum = "58fbe949992dafed917590d58bc12ca577b08b9957f0b3e0d7ee71b64bed3aa8"
    ),
)

// Plume : le modèle multilingue « base » (74 M de paramètres, recommandé par FUTO pour le français) est
// embarqué (2026-09-11, remplace « tiny », jugé par FUTO « non recommandé pour la plupart des langues »).
// Fichier identique à voice-input-multilingual-74.bin de FUTO (sha256 e44f352c…). Le 244 (« small ») reste
// optionnel : téléchargement par le navigateur puis import (Plume n'a pas de permission Internet).
val BUILTIN_MULTILINGUAL_MODEL: ModelLoader = ModelBuiltInAsset(
    name = R.string.base_name,
    ggmlFile = "base_acft_q8_0.bin.not.tflite"
)

val MULTILINGUAL_MODELS: List<ModelLoader> = listOf(
    BUILTIN_MULTILINGUAL_MODEL,
    ModelDownloadable(
        name = R.string.small_name,
        ggmlFile = "small_acft_q8_0.bin",
        checksum = "15ef255465a6dc582ecf1ec651a4618c7ee2c18c05570bbe46493d248d465ac4"
    ),
)