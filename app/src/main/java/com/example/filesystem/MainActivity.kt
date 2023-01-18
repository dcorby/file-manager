package com.example.filesystem

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsets
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.filesystem.databinding.ActivityMainBinding


const val SANFILE_ID = "sanfile id"

// https://stackoverflow.com/questions/65726850/how-to-work-with-livedata-on-recyclerview
// https://www.digitalocean.com/community/tutorials/android-livedata
// https://github.com/android/views-widgets-samples/blob/main/RecyclerViewKotlin/app/src/main/java/com/example/recyclersample/flowerList/FlowersListViewModel.kt
// https://www.kodeco.com/29024188-recyclerview-selection-library-tutorial-for-android-adding-new-actions
// https://medium.com/coding-blocks/implementing-selection-in-recyclerview-36a9739844e0
// https://proandroiddev.com/a-guide-to-recyclerview-selection-3ed9f2381504

/*
TODO
[ ] Print current directory in header adapter
[ ] Make file selectable in listview (highlighted)
[ ] Add a menu of operations at the bottom (delete, copy, move, rename, open (w/ select application), properties)
[ ] Wire up events to listview items
[x] Implement multiselect
[ ] Implement mp3 handler first, and implement services
[ ] Implement icons: https://github.com/dmhendricks/file-icon-vectors
[ ] Fix bug for tap same file that's already activate (multi implemented=false)
 */

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Select startDestination
        // https://stackoverflow.com/questions/51173002/how-to-change-start-destination-of-a-navigation-graph-programmatically
        val settings = getSharedPreferences("UserInfo", 0)
        val root = settings.getString("root", null)
        Log.v("File-san", "root=$root")
        var startDestinationId : Int? = null
        startDestinationId = if (root == null) {
            R.id.InitFragment
        } else {
            R.id.FolderFragment
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(startDestinationId)
        // https://stackoverflow.com/questions/64414301/why-onviewcreated-is-called-twice-in-android-app-using-navigation-components
        // ^!!!!
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
}