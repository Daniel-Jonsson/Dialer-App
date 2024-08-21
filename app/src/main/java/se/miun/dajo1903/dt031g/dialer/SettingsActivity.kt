package se.miun.dajo1903.dt031g.dialer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import java.io.File

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit();
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val mySwitchPref = findPreference<SwitchPreferenceCompat>("store")
            val myButtonPref = findPreference<Preference>("delete")

            val myVoicePref = findPreference<ListPreference>("downloaded_voices")
            myVoicePref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            val voicesList = getVoices(File(this@SettingsFragment.activity?.filesDir, "voices"))
            myVoicePref?.entries = voicesList.toTypedArray()
            myVoicePref?.entryValues = voicesList.toTypedArray()

            val preferences = arrayListOf(myButtonPref, mySwitchPref, myVoicePref)
            preferences.forEach{ pref -> setOnChangeListener(pref) }
        }


        private fun getVoices(parentFolder: File) : List<String> {
            return if (parentFolder.isDirectory) {
                val subfolders = parentFolder.listFiles { file -> file.isDirectory
                }
                subfolders?.map { it.name } ?: emptyList()
            } else {
                emptyList()
            }
        }

        private fun setOnChangeListener(pref: Preference?) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            when(pref) {
                is ListPreference -> {
                    pref.onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener{ _, newValue ->
                            val editor = sharedPref.edit()
                            editor.putString(getString(R.string.current_voice), newValue as String)
                            editor.apply()
                            true
                        }
                }
                is SwitchPreferenceCompat -> {
                    pref.onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener{ _, newValue ->
                            val editor = sharedPref.edit()
                            editor.putBoolean(
                                getString(R.string.store_number_key),
                                newValue as Boolean
                            )
                            editor.apply()
                            true
                        }
                }
                is Preference -> {
                    pref.onPreferenceClickListener =
                        Preference.OnPreferenceClickListener {
                            sharedPref.edit().remove(getString(R.string.saved_number_list_key)).apply()
                            true
                        }
                }
            }
        }
    }

    companion object {
        fun shouldStoreNumbers(context: Context) : Boolean {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPref.getBoolean(context.getString(R.string.store_number_key), true)
        }
    }
}