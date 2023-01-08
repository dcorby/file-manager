package com.example.filesystem

import android.Manifest
import android.app.Activity
import android.content.ContentProvider
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
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

        /* Instantiates headerAdapter and filesAdapter. Both adapters are added to concatAdapter.
        which displays the contents sequentially */
        val headerAdapter = HeaderAdapter()
        val myFilesAdapter = MyFilesAdapter { file -> adapterOnClick(file) }
        val concatAdapter = ConcatAdapter(headerAdapter, myFilesAdapter)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.adapter = concatAdapter

        myFilesListViewModel.myFilesLiveData.observe(this) {
            it?.let {
                myFilesAdapter.submitList(it as MutableList<MyFile>)
                headerAdapter.updateMyFileCount(it.size)
            }
        }

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            fabOnClick()
        }

        //val extraUri = "Documents/MyFiles"
        //val extraUri: Uri = "content://com.android.externalstorage.documents/tree/primary:Documents/MyFiles".toUri()
        val extraUri: Uri = "content://com.android.externalstorage.documents/tree/home:MyFiles/".toUri()
        //val extraUri = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/primary:Documents/MyFiles").toUri()

        Log.v("AAA", "FOO")
        Log.v("ZZZ", extraUri.toString())
        Log.v("ZZZ", extraUri.toString())



        val list = contentResolver.persistedUriPermissions
        for (p in list) {
            Log.v("w",p.toString())
            Log.v("w",p.uri.toString())
            Log.v("w",p.isReadPermission.toString())
            Log.v("w",p.isWritePermission.toString())

        }

        val pkg = "com.android.externalstorage"
        grantUriPermission(pkg, extraUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        grantUriPermission(pkg, extraUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)


                    Log.v("ZZZ", "attempt2")
        try {
            var fields = arrayOf<String>(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )
            var cursor = contentResolver.query(extraUri, fields, null, null, null)
            while (cursor!!.moveToNext()) {
                var docId = cursor.getString(0)
                var name = cursor.getString(1)
                var mime = cursor.getString(2)
                Log.v("ZZ3", "docId: " + docId + ", name: " + name + ", mime: " + mime)
            }
        } catch(e: Exception) {
            //launcher.launch(intent)
        }



        var ok: Boolean = false
        try {
            Log.v("ZZZ", "attempt1")
            val files = DocumentFile.fromTreeUri(this, extraUri)
            for (file in files!!.listFiles()) {
                Log.v("OOO", file.name.toString())
                ok = true
            }
        } catch(e: Exception) {

        }

        if (!ok) {
            val intent2 = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker when it loads.
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, extraUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent2, 1)

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, extraUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }

            launcher.launch(intent)
        }

//        try {
//            Log.v("ZZZ", "attempt2")
//            var fields = arrayOf<String>(
//                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
//                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
//                DocumentsContract.Document.COLUMN_MIME_TYPE
//            )
//            var cursor = contentResolver.query(extraUri, fields, null, null, null)
//            while (cursor!!.moveToNext()) {
//                var docId = cursor.getString(0)
//                var name = cursor.getString(1)
//                var mime = cursor.getString(2)
//                Log.v("ZZ3", "docId: " + docId + ", name: " + name + ", mime: " + mime)
//            }
//        } catch (e: Exception) {
//
//            // Choose a directory using the system's file picker.
//            val intent2 = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//                // Optionally, specify a URI for the directory that should be opened in
//                // the system file picker when it loads.
//                putExtra(DocumentsContract.EXTRA_INITIAL_URI, extraUri)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//
//
//            }
//
//            startActivityForResult(intent2, 1)
//        }











    }



    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        Log.v("ZZZ", "attempting")
        if (requestCode == 1
            && resultCode == Activity.RESULT_OK) {

            Log.v("ZZZ", "onactivityresult")
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri ->
                // Perform operations on the document using its URI.

                val contentResolver = applicationContext.contentResolver

                Log.v("ZZZ", "taken")
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri, takeFlags)

//                var fields = arrayOf<String>(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
//                var cursor = contentResolver.query(uri, fields, null, null, null)
//                while (cursor!!.moveToNext()) {
//                    var docId = cursor.getString(0)
//                    var name = cursor.getString(1)
//                    var mime = cursor.getString(2)
//                    Log.v("ZZ5", "docId: " + docId + ", name: " + name + ", mime: " + mime)
//                }
            }


        }
    }





    var launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val uri: Uri? = data?.data

        Log.v("ZZZ","attempting2")

        if (result.resultCode == Activity.RESULT_OK) {

            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            val files = DocumentFile.fromTreeUri(this, uri)
            for (file in files!!.listFiles()) {
                Log.v("ZZ1", file.name.toString())
            }
//            var fields = arrayOf<String>(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
//            var cursor = contentResolver.query(uri, fields, null, null, null)
//            while (cursor!!.moveToNext()) {
//                var docId = cursor.getString(0)
//                var name = cursor.getString(1)
//                var mime = cursor.getString(2)
//                Log.v("ZZZ", "docId: " + docId + ", name: " + name + ", mime: " + mime)
//            }
        }
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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
//        super.onActivityResult(requestCode, resultCode, intentData)
//
//        /* Inserts flower into viewModel. */
//        if (requestCode == newFlowerActivityRequestCode && resultCode == Activity.RESULT_OK) {
//            intentData?.let { data ->
//                val flowerName = data.getStringExtra(FLOWER_NAME)
//                val flowerDescription = data.getStringExtra(FLOWER_DESCRIPTION)
//
//                flowersListViewModel.insertFlower(flowerName, flowerDescription)
//            }
//        }
//    }

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