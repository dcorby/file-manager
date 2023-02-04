package com.example.filesystem

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.filesystem.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity(), MainReceiver {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mimeTypes: Map<String, *>
    private var actionStates: HashMap<String, HashMap<String, String?>> = hashMapOf(
        "copy" to HashMap(),
        "createFile" to HashMap(),
        "createFolder" to HashMap(),
        "delete" to HashMap(),
        "move" to HashMap(),
        "open" to HashMap(),
        "rename" to HashMap()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load mime types
        val jsonObject = JSONObject(Utils.readAssetsFile(this, "mime_types.json"))
        mimeTypes = MimeTypes.toMap(jsonObject)

        val settings = getSharedPreferences("UserInfo", 0)
        val root = settings.getString("root", null)
        val startDestinationId = if (root == null) {
            R.id.InitFragment
        } else {
            R.id.FolderFragment
        }
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(startDestinationId)
        navController.setGraph(navGraph, null)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Make sure the keyboard is always closed on activity recycle, otherwise popup opens over top
        // https://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard-programmatically
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var view: View? = getCurrentFocus()
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    + THIS INTERFACE NEEDS TO MANAGE ALL DATA INCL. CURRENTACTION, ETC.    +
    + OR USE VIEW MODEL??                                                  +
    + https://developer.android.com/reference/androidx/lifecycle/ViewModel +
    ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // Interface methods
    override fun getActionState(action: String): HashMap<String, String> {
        return actionStates[action]!! as HashMap<String, String>
        // ^ fix for kotlin.collections/java.util issue
    }
    override fun getActionState(action: String, key: String): String? {
        return actionStates[action]!![key]
    }
    override fun setActionState(action: String, key: String, value: String?) {
        actionStates[action]!![key] = value
    }
    override fun getMimeType(key: String): String {
        if (mimeTypes.containsKey(key)) {
            return mimeTypes[key] as String
        }
        return "application/octet-stream"
    }
}


