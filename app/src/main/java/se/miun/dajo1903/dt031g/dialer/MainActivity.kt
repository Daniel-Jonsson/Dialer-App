package se.miun.dajo1903.dt031g.dialer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import se.miun.dajo1903.dt031g.dialer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var aboutChecked : Boolean = false
    companion object {
        const val ABOUT_STATE = "ABOUT_STATE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        copySounds()
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        setContentView(binding.root)
        savedInstanceState?.let {
            aboutChecked = savedInstanceState.getBoolean(ABOUT_STATE, false)
        }
        val aboutButton = binding.aboutButton
        val buttons = listOf(
            binding.dialButton,
            binding.downloadButton,
            binding.callListButton,
            binding.settingsButton,
            binding.mapButton
        )
        buttons.forEach { button -> button.setOnClickListener {
            when(button) {
                binding.dialButton -> changeActivity(DialActivity::class.java)
                binding.downloadButton -> changeActivity(DownloadActivity::class.java)
                binding.callListButton -> changeActivity(CallListActivity::class.java)
                binding.mapButton -> changeActivity(MapsActivity::class.java)
                binding.settingsButton -> changeActivity(SettingsActivity::class.java)
            }
        } }

        aboutButton.setOnClickListener {
            if (!aboutChecked){
                showAboutDialog()
                aboutChecked = true
            } else {
                showSnackBar()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ABOUT_STATE, aboutChecked)
    }

    private fun changeActivity(targetActivity: Class<*>){

        if (targetActivity == DownloadActivity::class.java) {
            val intent = Intent(this, targetActivity)
            val downloadUrl = getString(R.string.download_url)
            intent.putExtra(getString(R.string.download_source), downloadUrl)
            startActivity(intent)
        } else {
            startActivity(Intent(this, targetActivity))
        }
    }
    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("About")
        builder.setMessage(R.string.alert_dialog_info)
        // Lambda expression to assign onClickListener to button and take action accordingly.
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
    private fun showSnackBar() {
        val snackBar = Snackbar.make(binding.root, "Already clicked!", Snackbar.LENGTH_LONG)
        snackBar.show()
    }

    private fun copySounds() {
        if (!Util.defaultVoiceExist(this)) {
            Util.copyDefaultVoiceToInternalStorage(this)
        }
    }
}