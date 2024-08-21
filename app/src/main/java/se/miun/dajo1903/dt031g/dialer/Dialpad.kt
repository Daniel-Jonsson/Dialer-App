package se.miun.dajo1903.dt031g.dialer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import se.miun.dajo1903.dt031g.dialer.databinding.DialpadBinding

class Dialpad : ConstraintLayout {
    private var binding: DialpadBinding = DialpadBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )
    private val soundPlayer: SoundPlayer = SoundPlayer(context)

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(
        context: Context,
        attributeSet: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attributeSet, defStyleAttr)

    init {
        val buttonList = listOf(
            binding.dialZero,
            binding.dialOne,
            binding.dialTwo,
            binding.dialThree,
            binding.dialFour,
            binding.dialFive,
            binding.dialSix,
            binding.dialSeven,
            binding.dialEight,
            binding.dialNine,
            binding.dialPound,
            binding.dialStar
            )

        for (button in buttonList) {
            button.setSoundPlayer(soundPlayer)
            button.setOnClickedListener(object : DialpadButton.OnClickedListener {
                override fun onClick(button: DialpadButton) {
                    val number = button.getTitle()
                    updateNumberArea(number)
                }
            })
        }
    }

    private fun updateNumberArea(title: String) {
        binding.dialArea.setDialText(title)
    }

    fun destroySoundPlayer() {
        soundPlayer.destroy()
    }

    fun reloadSound() {
        soundPlayer.reloadSounds(context)
    }
    fun getDialArea() : Dialarea {
        return binding.dialArea
    }
}