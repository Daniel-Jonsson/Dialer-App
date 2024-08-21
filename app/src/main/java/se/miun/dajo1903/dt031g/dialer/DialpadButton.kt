package se.miun.dajo1903.dt031g.dialer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import se.miun.dajo1903.dt031g.dialer.databinding.DialpadButtonBinding

class DialpadButton : ConstraintLayout {
    private var binding: DialpadButtonBinding = DialpadButtonBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private lateinit var soundPlayer: SoundPlayer
    private var onClickedListener: OnClickedListener? = null

    constructor(context: Context) : super(context)
    constructor(
        context: Context,
        attributeSet: AttributeSet?
    ) : super(context, attributeSet) {
        attributeSet.let {
            val attributes = context.obtainStyledAttributes(attributeSet, R.styleable.DialpadButton)
            try {
                setMessage(attributes.getString(R.styleable.DialpadButton_message))
                setTitle(attributes.getString(R.styleable.DialpadButton_title))
            } finally {
                attributes.recycle()
            }
        }
    }
    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attributeSet, defStyleAttr) {
        attributeSet.let {
            val attributes = context.obtainStyledAttributes(attributeSet, R.styleable.DialpadButton)
            try {
                setMessage(attributes.getString(R.styleable.DialpadButton_title))
                setTitle(attributes.getString(R.styleable.DialpadButton_message))
            } finally {
                attributes.recycle()
            }
        }
    }

    init {
        setupTouchListener()
    }

    fun setSoundPlayer(soundPlayer: SoundPlayer) {
        this.soundPlayer = soundPlayer
    }

    fun getTitle() = binding.titleTV.text.toString()
    fun setTitle(title: String?) {
        title?.let {
            binding.titleTV.text = title.take(1).uppercase()
        }
    }

    fun setMessage(msg: String?) {
        msg?.let {
            binding.messageTV.text = msg.take(4).uppercase()
        }
    }


    private fun setupTouchListener() {
        setOnTouchListener { _, event ->
            performClick()
            when(event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateButton(true)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    onClickedListener?.onClick(this)
                    animateButton(false)
                    soundPlayer.playSound(this)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_CANCEL -> {
                    animateButton(false)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }
    private fun animateButton(isPressed: Boolean) {
        val alpha = if (isPressed) 0.5f else 1.0f
        val scale = if (isPressed) 0.9f else 1.0f

        val alphaAnimator = ObjectAnimator.ofFloat(binding.root, View.ALPHA, alpha)
        val scaleAnimatorX = ObjectAnimator.ofFloat(binding.root, View.SCALE_X, scale)
        val scaleAnimatorY = ObjectAnimator.ofFloat(binding.root, View.SCALE_Y, scale)
        alphaAnimator.duration = 90
        scaleAnimatorX.duration = 90
        scaleAnimatorY.duration = 90
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(alphaAnimator, scaleAnimatorX, scaleAnimatorY)
        animatorSet.start()
    }

    fun setOnClickedListener(listener: OnClickedListener) {
        this.onClickedListener = listener
    }
    interface OnClickedListener {
        fun onClick(button: DialpadButton)
    }
}