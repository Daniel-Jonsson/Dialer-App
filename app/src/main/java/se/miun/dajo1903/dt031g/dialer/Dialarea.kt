package se.miun.dajo1903.dt031g.dialer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.PhoneNumberFormattingTextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import se.miun.dajo1903.dt031g.dialer.databinding.DialareaBinding
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Dialarea : ConstraintLayout {

    private val binding: DialareaBinding
    private var arrayList: ArrayList<String> = arrayListOf()
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(
        context: Context,
        attributeSet: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attributeSet, defStyleAttr)

    init {
        binding = DialareaBinding.inflate(LayoutInflater.from(context), this, true)
        setupButtonListener()
        binding.dialareaTV.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        loadData()
    }

    fun setDialText(dialText: String) {
        val newText = binding.dialareaTV.text.toString() + dialText
        binding.dialareaTV.text = context.getString(R.string.dial_area_text, newText)
    }

    private fun setupButtonListener() {
        binding.removeButton.setOnClickListener {
            removeLastDigit()
        }
        binding.removeButton.setOnLongClickListener {
            removeAllDigits()
            true
        }
        binding.dialButton.setOnClickListener {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startDialIntent(true)
            } else {
                DialActivity.requestCallPhonePermission(context as DialActivity)
            }
            if (SettingsActivity.shouldStoreNumbers(this.context)) {
                saveData(binding.dialareaTV.text.toString())
            }
        }
    }

    private fun saveData(number: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        if (number != "") {
            arrayList.add(number)
            val json = gson.toJson(arrayList.reversed())
            editor.putString(context.getString(R.string.saved_number_list_key), json)
            editor.apply()
        }
    }

    private fun loadData() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()

        val json = sharedPref.getString(context.getString(R.string.saved_number_list_key), null) ?: return
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        arrayList = gson.fromJson(json, type)
    }

    private fun removeLastDigit() {
        binding.dialareaTV.text = binding.dialareaTV.text.dropLast(1)
    }
    private fun removeAllDigits() {
        binding.dialareaTV.text = ""
    }

    fun startDialIntent(isGranted: Boolean) {
        if (isGranted) {
            val intent = Intent(Intent.ACTION_CALL)
            val encodedNumber = Uri.encode(binding.dialareaTV.text.toString())
            val uri = Uri.parse("tel:$encodedNumber")
            intent.data = uri
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_DIAL)
            val encodedNumber = Uri.encode(binding.dialareaTV.text.toString())
            val uri = Uri.parse("tel:$encodedNumber")
            intent.data = uri
            context.startActivity(intent)
        }
    }
}