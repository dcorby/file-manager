package com.example.filesystem

import android.os.Build
import android.os.Bundle
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

/*
TODO
[ ] Print current directory in header adapter
[ ] Make file selectable in listview (highlighted)
[ ] Add a menu of operations at the bottom (delete, copy, move, rename, open (w/ select application), properties)
[ ] Long-press will mimic doubleclick
[ ] Wire up events to listview items
[ ] Alow select multiple with holding-down-already tap
[ ] Implement mp3 handler first, and implement services
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
        var startDestinationId : Int? = null
        startDestinationId = if (root == null) {
            R.id.InitFragment
        } else {
            R.id.FolderFragment
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(startDestinationId)
        navController.setGraph(navGraph, null)

        // Hack for black device nav bar occluding layout
        // https://stackoverflow.com/questions/20264268/how-do-i-get-the-height-and-width-of-the-android-navigation-bar-programmatically
//        if (Build.VERSION.SDK_INT >= 30) {
//            // https://stackoverflow.com/questions/36514167/how-to-really-get-the-navigation-bar-height-in-android/73984106#73984106
//            val insets: WindowInsets = windowManager.currentWindowMetrics.windowInsets
//            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
//            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
//        } else {
//
//        }





    }

    /* Opens SanFileDetailActivity when RecyclerView item is clicked. */
    private fun adapterOnClick(flower: SanFile) {
        Toast.makeText(applicationContext,"clicked!",Toast.LENGTH_SHORT).show()
        //val intent = Intent(this, SanFileDetailActivity()::class.java)
        //intent.putExtra(SANFILE_ID, flower.id)
        //startActivity(intent)
    }

    /* Adds flower to flowerList when FAB is clicked. */
    private fun fabOnClick() {
        Toast.makeText(applicationContext,"clicked!",Toast.LENGTH_SHORT).show()
        //val intent = Intent(this, AddFlowerActivity::class.java)
        //startActivityForResult(intent, newFlowerActivityRequestCode)
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