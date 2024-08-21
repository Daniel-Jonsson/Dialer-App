package se.miun.dajo1903.dt031g.dialer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class CallListActivity : AppCompatActivity() {
    private lateinit var callList: ArrayList<String>
    private lateinit var callRVAdapter: Adapter
    private lateinit var callRV: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_list)

        callRV = findViewById(R.id.recycler_view)
        callRV.layoutManager = LinearLayoutManager(this)
        loadData(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.list_setting -> {
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPref.edit().remove(this.getString(R.string.saved_number_list_key)).apply()
                loadData(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadData(context: Context) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()

        val json = sharedPref.getString(context.getString(R.string.saved_number_list_key), null) ?: return loadDefault()
        val type: Type = object : TypeToken<ArrayList<String?>?>() {}.type
        callList = gson.fromJson(json, type)
        callRVAdapter = Adapter(callList)
        callRV.adapter = callRVAdapter
    }

    private fun loadDefault() {
        callList = arrayListOf("No numbers are stored.")
        callRVAdapter = Adapter(callList)
        callRV.adapter = callRVAdapter
    }
}