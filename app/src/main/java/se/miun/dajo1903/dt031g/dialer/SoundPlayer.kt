package se.miun.dajo1903.dt031g.dialer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.preference.PreferenceManager

class SoundPlayer(context: Context) {

    private lateinit var soundPool: SoundPool

    private val soundIds: MutableMap<String, Int> = mutableMapOf()
    init {
        loadSounds(context)
    }

    /**
     * Loads sound resources into the SoundPool based on the files found in the default voice directory.
     * @param context The context used to obtain the default voice directory.
     */
    private fun loadSounds(context: Context) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        soundPool = SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentVoice = sharedPref.getString(context.getString(R.string.current_voice), null)
        val voiceFolder = Util.getDirForVoice(context, currentVoice)
        voiceFolder.listFiles()?.forEach { file ->
            val num = getNumberTitle(file.name.substringAfterLast("/"))
            val soundId = soundPool.load(file.path, 1)
            soundIds[num] = soundId
        }
    }

    private fun getNumberTitle(file: String) : String {
        return when(file) {
            "zero.mp3" -> "0"
            "one.mp3" -> "1"
            "two.mp3" -> "2"
            "three.mp3" -> "3"
            "four.mp3" -> "4"
            "five.mp3" -> "5"
            "six.mp3" -> "6"
            "seven.mp3" -> "7"
            "eight.mp3" -> "8"
            "nine.mp3" -> "9"
            "star.mp3" -> "*"
            "pound.mp3" -> "#"
            else -> throw IllegalArgumentException("Invalid file name $file")
        }
    }

    /**
     * Plays the button-specific sound based on the dialpadbutton title
     * @param dialpadButton The dialpadbutton clicked
     */
    fun playSound(dialpadButton: DialpadButton) {
        val soundId = soundIds[dialpadButton.getTitle()]
        soundId?.let {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun reloadSounds(context: Context) {
        this.loadSounds(context)
    }
    fun destroy() {
        soundPool.release()
    }
}