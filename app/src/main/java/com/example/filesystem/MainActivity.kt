package com.example.filesystem

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Process.myPid
import android.os.Process.myUid
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

        /* allow the user to pick a directory subtree for storage */
        // https://commonsware.com/community/t/is-simple-case-of-creating-multiple-files-and-giving-the-user-an-easy-access-to-them-is-impossible-with-stored-access-framework/630
        // http://android-er.blogspot.com/2015/09/example-of-using-intentactionopendocume.html
        val extraInitialUri: Uri = "content://com.android.externalstorage.documents/tree/primary:Documents/FileSystem".toUri()

        // https://developer.android.com/topic/security/risks/content-resolver
        // https://stackoverflow.com/questions/6307793/how-do-i-check-the-permission-of-an-uri-that-has-been-send-with-an-intent


        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, extraInitialUri)
            // https://stackoverflow.com/questions/55669688/storage-access-framework-keep-file-permissions-after-revoke-tree-permissions
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            //type = "*/*"
        }

        Log.v("ZZZ", "1")
        //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, extraInitialUri)
        Log.v("ZZZ", "2")

        //val pkg = "com.example.filesystem"
        //grantUriPermission(pkg, extraInitialUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        //grantUriPermission(pkg, extraInitialUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //grantUriPermission(pkg, extraInitialUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        resultLauncher.launch(intent)


        val list = contentResolver.persistedUriPermissions
        for (p in list) {
            Log.v("w",p.toString())
            Log.v("w",p.uri.toString())
            Log.v("w",p.isReadPermission.toString())
            Log.v("w",p.isWritePermission.toString())
        }

        val fs: Uri = "content://com.android.externalstorage.documents/tree/primary:Documents/FileSystem".toUri()
        val pkg = "com.android.externalstorage.documents/tree/primary:Documents/FileSystem"
        grantUriPermission(pkg, fs, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        grantUriPermission(pkg, fs, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //grantUriPermission(pkg, fs, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)


        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            fs,
            DocumentsContract.getTreeDocumentId(fs)
        )

        // https://stackoverflow.com/questions/64271446/securityexception-on-android-q-for-acessing-externalstorage-with-action-open-doc
        // CommonsWare suggests "Call DocumentFile.fromTreeUri()"

        val docfile = DocumentFile.fromTreeUri(this, childrenUri)
        for (file in docfile!!.listFiles()) {
            Log.v("ZZZ", file.name!!)
        }


        var fields = arrayOf<String>(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
        var cursor = contentResolver.query(childrenUri, fields, null, null, null)
//
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

        //2023-01-08 03:02:09.005 6624-6624/com.example.filesystem V/w: UriPermission {uri=content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FFileSystem, modeFlags=3, persistedTime=1673157389384}
        //    2023-01-08 03:02:09.006 6624-6624/com.example.filesystem V/w: content://com.android.externalstorage.documents/tree/primary%3ADocuments%2FFileSystem
        //    2023-01-08 03:02:09.006 6624-6624/com.example.filesystem V/w: true
        //    2023-01-08 03:02:09.006 6624-6624/com.example.filesystem V/w: true

//        val perm = checkUriPermission(
//            extraInitialUri,
//            myPid(),
//            myUid(),
//            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        )
//        if (perm == PackageManager.PERMISSION_DENIED) {
//            Toast.makeText(
//                applicationContext,
//                "No permissions", Toast.LENGTH_LONG
//            ).show()
//            finish()
//        }
//        if (perm == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(
//                applicationContext,
//                "Have permissions", Toast.LENGTH_LONG
//            ).show()
//            finish()
//        }

        Log.v("ZZZ", "zzz")
    }

    /* ActivityResult class for pick directory subtree */
    // https://stackoverflow.com/questions/72246437/how-to-use-action-open-document-tree-without-startactivityforresult
    // Suggests to use ActivityResultContracts.OpenDocumentTree
    // https://stackoverflow.com/questions/70869063/android-open-document-tree-with-activity-result-contract
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.v("ZZZ", "3")
        // this is the activity callback
        // https://developer.android.com/training/basics/intents/result#kotlin

        val data: Intent? = result.data
        val uri: Uri? = data?.data
        Log.v("ZZZ", "permissions taken")



        if (result.resultCode == Activity.RESULT_OK) {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )

            //val pkg = "com.example.filesystem"
            //grantUriPermission(pkg, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            //grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //grantUriPermission(pkg, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

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