package se.miun.dajo1903.dt031g.dialer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.DownloadListener
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.miun.dajo1903.dt031g.dialer.databinding.ActivityDownloadBinding
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext

class DownloadActivity() : AppCompatActivity(), DownloadListener, CoroutineScope {
    private val TAG = "DownloadActivity"
    private lateinit var binding: ActivityDownloadBinding
    private lateinit var downloadView : ConstraintLayout
    private lateinit var webURL: String
    private var downloadProgressVal: Int = 0
    override val coroutineContext: CoroutineContext
        get() = Job()

    private val scope = CoroutineScope(coroutineContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        downloadView = binding.downloadingView
        setContentView(binding.root)
        webURL = intent.getStringExtra(getString(R.string.download_source)).toString()
        initDownloadView()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private enum class STATE {
        ERROR,
        INFO,
    }

    private enum class STATUS {
        VISIBLE,
        INVISIBLE
    }
     private fun CoroutineScope(context: CoroutineContext) : CoroutineScope = ContextScope(
        if (context[Job] != null) context else context + Job()
    )

    internal class ContextScope(context: CoroutineContext) : CoroutineScope {
        override val coroutineContext: CoroutineContext = context
        override fun toString(): String = "CoroutineScope(coroutineContext=$coroutineContext)"
    }
    private fun initDownloadView() {
        val downloadView = binding.downloadView
        downloadView.loadUrl(webURL)
        downloadView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
        downloadView.setDownloadListener(this)
    }

    override fun onDownloadStart(
        url: String?,
        userAgent: String?,
        contentDisposition: String?,
        mimetype: String?,
        contentLength: Long
    ) {
        url?.run {
            val voiceFileWithExtension = url.substringAfterLast('/')
            val voiceFileWithoutExtension = voiceFileWithExtension.substringBeforeLast(".")
            val parentPath = File(this@DownloadActivity.filesDir, "voices")
                val storePath = File(parentPath, voiceFileWithExtension)

            if (File(parentPath, voiceFileWithoutExtension).exists()) {
                makeSnackBar(STATE.INFO, getString(R.string.already_downloaded))
                return@run
            }
            scope.launch(Dispatchers.IO) {
                downloadVoice(voiceFileWithExtension, this@run, storePath)
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private suspend fun downloadVoice(voiceFile: String, url: String, storePath: File) {
        changeDownloadViewState(STATUS.VISIBLE, voiceFile)
        try {
            incrementProgress(33)
            downloadFile(url, storePath)
        } catch (e: Exception) {
            Log.e(TAG, "${getString(R.string.error_downloading)} $storePath: $e")
            makeSnackBar(STATE.ERROR, e.message!!)
        } finally {
            storePath.delete()
        }
    }

    private suspend fun downloadFile(urlString: String, downloadPath: File) {
        withContext(Dispatchers.IO){
            val url = URL(urlString)
            val httpUrlConnection = url.openConnection() as HttpURLConnection
            try {
                httpUrlConnection.connect()
                if (httpUrlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    downloadVoiceToPath(httpUrlConnection, downloadPath)
                }
            } catch (e: IOException) {
                Log.e(TAG, "${getString(R.string.error_downloading)}: $e")
                makeSnackBar(STATE.ERROR, e.message!!)
            }
            finally {
                httpUrlConnection.disconnect()
            }
        }
    }

    private suspend fun downloadVoiceToPath(connection: HttpURLConnection, downloadPath: File) {
        withContext(Dispatchers.IO) {
            val inputStream = BufferedInputStream(connection.inputStream)
            val outputStream = FileOutputStream(downloadPath)
            try {
                incrementProgress(33)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            } finally {
                outputStream.close()
                unzipFile(downloadPath)
            }
        }
    }

    private suspend fun unzipFile(storePath: File) {
        try {
            incrementProgress(34)
            val parentDir = File(storePath.parent!!)
            Util.unzip(storePath, parentDir)
        } finally {
            changeDownloadViewState(STATUS.INVISIBLE)
        }
    }
    private suspend fun changeDownloadViewState(status: STATUS, voiceFile: String = "") {
        val downloadText = downloadView.getViewById(R.id.downloadingText) as TextView
        val downloadProgress = downloadView.getViewById(R.id.downloadText) as TextView
        val progressBar = downloadView.getViewById(R.id.downloadProgressbar) as ProgressBar
        when (status) {
            STATUS.VISIBLE -> {
                withContext(Dispatchers.Main) {
                    downloadView.visibility = View.VISIBLE
                    downloadText.text = getString(R.string.downloading_file, voiceFile)
                }
            }
            STATUS.INVISIBLE -> {
                withContext(Dispatchers.Main) {
                    downloadText.text = ""
                    downloadProgress.text = ""
                    downloadProgressVal = 0
                    progressBar.progress = 0
                    downloadView.visibility = View.INVISIBLE
                }
            }
        }
    }
    private suspend fun incrementProgress(amount: Int) {
        withContext(Dispatchers.Main) {
            downloadProgressVal += amount
            val downloadProgress = downloadView.getViewById(R.id.downloadText) as TextView
            val progressBar = downloadView.getViewById(R.id.downloadProgressbar) as ProgressBar
            downloadProgress.text = getString(R.string.current_progress, downloadProgressVal, "%")
            progressBar.progress += amount
            delay(2000)
        }
    }

    private fun makeSnackBar(state: STATE = STATE.INFO, message: String) {
        when (state) {
            STATE.INFO -> {
                val infoSnack = Snackbar.make(binding.downloadView, message, Snackbar.LENGTH_LONG)
                infoSnack.setBackgroundTint(Color.GRAY)
                infoSnack.show()
            }
            STATE.ERROR -> {
                val errorSnack = Snackbar.make(binding.downloadView, message, Snackbar.LENGTH_LONG)
                errorSnack.setBackgroundTint(Color.RED)
                errorSnack.show()
            }
        }
    }
}