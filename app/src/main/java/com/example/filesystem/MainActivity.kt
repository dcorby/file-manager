package com.example.filesystem

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
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

        /* get full permissions for external storage from user */
        // https://developer.android.com/reference/android/provider/Settings#ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
//        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
//        startActivity(
//            Intent(
//                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
//                uri
//            )
//        )

//        var canAccessExternalStorage: Boolean = false
//        if (android.os.Build.VERSION.SDK_INT >= 30) {
//            if (Environment.isExternalStorageManager()) {
//                canAccessExternalStorage = true
//            }
//        } else {
//            canAccessExternalStorage = true
//        }
//
//        if (canAccessExternalStorage) {
//
//        }

        /* allow the user to pick a directory subtree for storage */
        // https://commonsware.com/community/t/is-simple-case-of-creating-multiple-files-and-giving-the-user-an-easy-access-to-them-is-impossible-with-stored-access-framework/630
        // http://android-er.blogspot.com/2015/09/example-of-using-intentactionopendocume.html
        //val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        // content://com.android.externalstorage.documents/tree/primary%3ADocuments
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when it loads.
            // "You can only use an uri that you obtained before with ACTION_OPEN_DOCUMENT"
            val pickerInitialUri: Uri = "/Documents".toUri()
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, "content://com.android.externalstorage.documents/tree/primary:Documents")
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, "Documents")
        }
        val pickerInitialUri: Uri = "/Documents".toUri()
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "content://com.android.externalstorage.documents/tree/primary:Documents")
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "Documents")
        resultLauncher.launch(intent)
    }

    // "Persist Permissions" - so user doesn't have to select everytime
    // "To preserve access to files across device restarts and create a better user experience, your app can "take" the persistable URI permission grant that the system offers, as shown in the following code snippet:"
    // https://developer.android.com/training/data-storage/shared/documents-files#persist-permissions

    /* ActivityResult class for pick directory subtree */
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // this works and prompts user to select a folder
            val data: Intent? = result.data
            val uri: Uri? = data?.data
            Log.v("ZZZ", uri.toString())
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )

            var fields = arrayOf<String>(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
            var cursor = contentResolver.query(childrenUri, fields, null, null, null)

            // directions to get all files recursively
            // add folder, e.g. Documents/FileSystem, or allow user to
            // https://stackoverflow.com/questions/64408944/documentscontracts-buildchilddocumentsuriusingtreeuri-documentscontract-gettr
            while (cursor!!.moveToNext()) {
                var docId = cursor.getString(0)
                var name = cursor.getString(1)
                var mime = cursor.getString(2)
                Log.v("ZZZ", "docId: " + docId + ", name: " + name + ", mime: " + mime)
                // docId: primary:Documents/bar.txt, name: bar.txt, mime: text/plain
                //if (isDirectory(mime)) {
                //    final Uri newNode = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri, docId);
                //    dirNodes.add(newNode);
                //}
            }

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