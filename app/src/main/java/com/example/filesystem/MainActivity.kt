package com.example.filesystem

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.filesystem.data.MyFile
import com.example.filesystem.databinding.ActivityMainBinding


const val MYFILE_ID = "myfile id"

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val newMyFileActivityRequestCode = 1
    private val myFilesListViewModel by viewModels<MyFilesListViewModel> {
        MyFilesListViewModelFactory(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val settings = getSharedPreferences("UserInfo", 0)
        val root = settings.getString("root", null).toString()
        var startDestinationId : Int? = null
        if (root == null) {
            startDestinationId = R.layout.fragment_init
        } else {
            startDestinationId = R.layout.fragment_root
        }
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(startDestinationId!!)
        navController.graph = navGraph

//        /* Instantiates headerAdapter and filesAdapter. Both adapters are added to concatAdapter.
//        which displays the contents sequentially */
//        val headerAdapter = HeaderAdapter()
//        val myFilesAdapter = MyFilesAdapter { file -> adapterOnClick(file) }
//        val concatAdapter = ConcatAdapter(headerAdapter, myFilesAdapter)
//
//        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
//        recyclerView.adapter = concatAdapter
//
//        myFilesListViewModel.myFilesLiveData.observe(this) {
//            it?.let {
//                myFilesAdapter.submitList(it as MutableList<MyFile>)
//                headerAdapter.updateMyFileCount(it.size)
//            }
//        }

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            fabOnClick()
        }







        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val uri: Uri? = data?.data

            //Log.v("XXX", uri.toString())
            // https://stackoverflow.com/questions/10209814/saving-user-information-in-app-settings

            /*
            if (result.resultCode == Activity.RESULT_OK) {
                contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                val files = DocumentFile.fromTreeUri(this, uri)
                for (file in files!!.listFiles()) {
                    Log.v("ww2", file.name.toString())
                }
            }

             */
        }.launch(intent)
    }

    /* Opens MyFileDetailActivity when RecyclerView item is clicked. */
    private fun adapterOnClick(flower: MyFile) {
        Toast.makeText(applicationContext,"clicked!",Toast.LENGTH_SHORT).show()
        //val intent = Intent(this, MyFileDetailActivity()::class.java)
        //intent.putExtra(MYFILE_ID, flower.id)
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