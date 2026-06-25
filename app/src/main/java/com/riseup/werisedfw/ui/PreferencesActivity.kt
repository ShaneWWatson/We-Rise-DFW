package com.riseup.werisedfw.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.MaterialToolbar
import com.riseup.werisedfw.R
import com.riseup.werisedfw.i18n.Languages

/**
 * Hosts the [SettingsFragment] and a back-arrow toolbar.
 *
 * Settings exposed:
 *  - Search radius in miles.
 *  - Whether to include faith-based providers.
 *  - Translation language (defaults to English).
 */
class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
	    enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

	    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.prefRoot)) { v, insets ->
		    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
		    v.updatePadding(
			    left = systemBars.left,
			    top = systemBars.top,
			    right = systemBars.right,
			    bottom = systemBars.bottom
		                   )
		    insets
	    }

        findViewById<MaterialToolbar>(R.id.prefToolbar).apply {
            setNavigationIcon(android.R.drawable.ic_menu_revert)
            setNavigationOnClickListener { finish() }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.prefContainer, SettingsFragment())
                .commit()
        }
    }

    /** Inflates `preferences.xml` and populates the language picker at runtime. */
    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            // The language list is populated programmatically so we don't have to
            // ship 60-ish strings as a static XML array.
            findPreference<ListPreference>(getString(R.string.pref_language_key))?.apply {
                entries = Languages.entries
                entryValues = Languages.entryValues
                if (value == null) value = "en"
            }
        }
    }
}

