package se.miun.dajo1903.dt031g.dialer

import android.content.Context
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object Util {
    private const val TAG = "Util"

    /**
     * The directory in which all voices for the Dialer app are stored (in the app-specific
     * internal storage location).
     */
    const val VOICE_DIR = "voices"

    /**
     * The default voice to be used by the Dialer app.
     */
    const val DEFAULT_VOICE = "mamacita_us"

    /**
     * The file extension for the default voice files used by the Dialer app.
     */
    const val DEFAULT_VOICE_EXTENSION = "mp3"

    /**
     * The resource ids for the default voice (in res/raw).
     */
    val DEFAULT_VOICE_RESOURCE_IDS = intArrayOf(
        R.raw.zero, R.raw.one, R.raw.two, R.raw.three,
        R.raw.four, R.raw.five, R.raw.six, R.raw.seven,
        R.raw.eight, R.raw.nine, R.raw.star, R.raw.pound
    )

    /**
     * The file names for each sound in a voice, mapped to its corresponding button title.
     */
    val DEFAULT_VOICE_FILE_NAMES: MutableMap<String, String> = HashMap()

    init {
        DEFAULT_VOICE_FILE_NAMES["0"] = "zero.mp3"
        DEFAULT_VOICE_FILE_NAMES["1"] = "one.mp3"
        DEFAULT_VOICE_FILE_NAMES["2"] = "two.mp3"
        DEFAULT_VOICE_FILE_NAMES["3"] = "three.mp3"
        DEFAULT_VOICE_FILE_NAMES["4"] = "four.mp3"
        DEFAULT_VOICE_FILE_NAMES["5"] = "five.mp3"
        DEFAULT_VOICE_FILE_NAMES["6"] = "six.mp3"
        DEFAULT_VOICE_FILE_NAMES["7"] = "seven.mp3"
        DEFAULT_VOICE_FILE_NAMES["8"] = "eight.mp3"
        DEFAULT_VOICE_FILE_NAMES["9"] = "nine.mp3"
        DEFAULT_VOICE_FILE_NAMES["*"] = "star.mp3"
        DEFAULT_VOICE_FILE_NAMES["#"] = "pound.mp3"
    }

    /**
     * Returns the absolute path to the directory on the filesystem where app-specific files
     * are stored.
     * @param context an application context
     * @return The path of the directory holding application files.
     */
    fun getInternalStorageDir(context: Context): File {
        return context.filesDir
    }

    /**
     * Returns the absolute path to the directory containing the voice files for the
     * given voice name.
     * @param context an application context
     * @param voiceName the name of the voice to get the directory for
     * @return The path of the directory for holding the voice given by `voiceName`.
     */
    fun getDirForVoice(context: Context, voiceName: String?): File {
        var voiceName = voiceName
        if (voiceName == null || voiceName.length < 1) {
            voiceName = DEFAULT_VOICE
        }
        return File(getInternalStorageDir(context), VOICE_DIR + File.separator + voiceName)
    }

    /**
     * Returns the absolute path to the directory containing the default voice files.
     * @param context an application context
     * @return The path of the directory holding the default voice.
     */
    fun getDirForDefaultVoice(context: Context): File {
        return getDirForVoice(context, DEFAULT_VOICE)
    }

    /**
     * Copies each resource in `DEFAULT_VOICE_RESOURCE_IDS` to the directory returned
     * by `getDirForDefaultVoice`.
     * @param context an application context
     * @return true if all files are copied, or false if an error occurs.
     */
    fun copyDefaultVoiceToInternalStorage(context: Context): Boolean {
        // Complete path to the dir of the default voice
        val voiceDir = getDirForDefaultVoice(context)
        if (!voiceDir.exists()) {
            if (!voiceDir.mkdirs()) {
                Log.e(
                    TAG,
                    "Could not create dir: $voiceDir"
                )
                return false
            }
        }
        for (resourceId in DEFAULT_VOICE_RESOURCE_IDS) {
            val filename =
                context.resources.getResourceEntryName(resourceId) + "." + DEFAULT_VOICE_EXTENSION // ex. "one.mp3"
            try {
                context.resources.openRawResource(resourceId).use { `in` ->
                    FileOutputStream(File(voiceDir, filename)).use { out ->
                        // Copy from in to out using a byte buffer
                        val buffer = ByteArray(2048)
                        var bytesRead: Int
                        while (`in`.read(buffer).also { bytesRead = it } > 0) {
                            out.write(buffer, 0, bytesRead)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error copying file: " + e.message)
                return false
            }
        }
        return true // all files have been copied
    }

    /**
     * Unzips (decompresses) the contents of a source zip file to a destination directory.
     * @param sourceFile a zip file to be unzipped to the destination directory
     * @param destinationDir a directory in which the contents of the zip file are to be unzipped
     * @return true if the zip file is successfully unzipped
     */
    fun unzip(sourceFile: File, destinationDir: File?): Boolean {
        try {
            // Open the source ZIP file for reading
            val zipFile = ZipFile(sourceFile)

            // Get content of ZIP file (as an enumeration of ZipEntry)
            val e = zipFile.entries()
            println(e)
            // As long as there are more entries to handle
            while (e.hasMoreElements()) {
                // Get the next entry
                val entry = e.nextElement() as ZipEntry
                val destinationFile = File(destinationDir, entry.name)

                // Create destination folders (if any)
                val destinationParent = destinationFile.parentFile
                destinationParent?.mkdirs()

                // Do the unzipping, but only if the entry is a file
                if (!entry.isDirectory) {
                    // A BufferReader to read the entry from the zip file
                    val `in` = BufferedInputStream(
                        zipFile.getInputStream(entry)
                    )

                    // A BufferedOutputStream to save the entry to destination
                    val out = BufferedOutputStream(
                        FileOutputStream(destinationFile)
                    )

                    // Create a buffer to avoid writing byte for byte
                    val buffer = ByteArray(4096)
                    var bytesWrite: Int

                    // Writes an array of bytes from the zip to the destination
                    while (`in`.read(buffer).also { bytesWrite = it } != -1) {
                        out.write(buffer, 0, bytesWrite)
                    }

                    // Close streams used
                    out.flush()
                    out.close()
                    `in`.close()
                }
            }

            // Close the zip file
            zipFile.close()
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Error unzipping $sourceFile: $e"
            )
            return false
        }
        return true
    }

    /**
     * Check if the default voice files from res/raw already exists in the directory returned
     * by `getDirForDefaultVoice`.
     * @param context an application context
     * @return true if the default voice exists in internal app-specific storage, false if not.
     */
    fun defaultVoiceExist(context: Context?): Boolean {
        // TODO: Add check if default voice dir already exist in the internal app storage
        var doesExist = false
        context?.let {
            doesExist = getDirForDefaultVoice(context).exists()
        }
        return doesExist
    }
}