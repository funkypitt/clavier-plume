package org.futo.inputmethod.latin.plume

import android.content.Context
import android.content.SharedPreferences
import org.futo.inputmethod.latin.uix.PreferenceUtils

/**
 * Mesure (version privée) : interrupteur du hit-testing probabiliste, lu à chaque frappe,
 * journalisé dans typo_log.jsonl (champ « boost ») pour comparer les périodes avec et sans.
 */
object PlumeExperiment {
    const val PREF_PROBABILISTIC_KEYS = "plume_probabilistic_keys"

    @Volatile @JvmStatic var lastBoostState: Boolean = true
        private set
    @Volatile private var prefs: SharedPreferences? = null

    @JvmStatic
    fun probabilisticKeys(context: Context): Boolean {
        val p = prefs ?: PreferenceUtils.getDefaultSharedPreferences(context).also { prefs = it }
        val v = p.getBoolean(PREF_PROBABILISTIC_KEYS, true)
        lastBoostState = v
        return v
    }
}
