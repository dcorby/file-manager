package com.example.filesystem

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.filesystem.databinding.ActivityMainBinding
import org.json.JSONObject

// https://stackoverflow.com/questions/51173002/how-to-change-start-destination-of-a-navigation-graph-programmatically
// https://stackoverflow.com/questions/64414301/why-onviewcreated-is-called-twice-in-android-app-using-navigation-components

class MainActivity : AppCompatActivity(), MainReceiver {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mimeTypes: Map<String, *>
    private var stateManager: HashMap<String, String?> = HashMap()

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

    // Interface methods
    override fun getState(key: String): String? {
        return stateManager.get(key)
    }
    override fun setState(key: String, value: String?) {
        stateManager[key] = value
    }
    override fun getMimeType(key: String): Any? {
        return mimeTypes.getOrDefault(key, null)
    }


}


