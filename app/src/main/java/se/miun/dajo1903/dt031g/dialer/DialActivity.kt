package se.miun.dajo1903.dt031g.dialer

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import se.miun.dajo1903.dt031g.dialer.databinding.ActivityDialBinding

class DialActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onRestart() {
        super.onRestart()
        binding.dialpad.reloadSound()
    }
    override fun onDestroy() {
        super.onDestroy()
        binding.dialpad.destroySoundPlayer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dial_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.download_voices -> {
                val intent = Intent(this, DownloadActivity::class.java)
                val downloadUrl = getString(R.string.download_url)
                intent.putExtra(getString(R.string.download_source), downloadUrl)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.dialpad.getDialArea().startDialIntent(true)
            } else {
                binding.dialpad.getDialArea().startDialIntent(false)
            }
        }
    }

    companion object {
        fun requestCallPhonePermission(activity: DialActivity) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CALL_PHONE)) {
                showPermissionDialog(activity)
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    100
                )
            }
        }

        private fun showPermissionDialog(activity: DialActivity) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Permission required")
            builder.setMessage(R.string.permission_dialog_info)
            // Lambda expression to assign onClickListener to button and take action accordingly.
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    100
                )
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}