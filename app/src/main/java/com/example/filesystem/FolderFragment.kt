package com.example.filesystem

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.FileObserver
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.example.filesystem.actions.*
import com.example.filesystem.databinding.FragmentFolderBinding


/**
 * If the user has already initialized the app, land on this fragment.
 * This could happen when they open the app, or after the
 * activity result callback if just getting started
 */

const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 1

class FolderFragment : Fragment() {

    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!
    private val sanFilesViewModel: SanFilesViewModel by viewModels()
    private var headerAdapter: HeaderAdapter? = null
    private var sanFilesAdapter: SanFilesAdapter? = null
    private var destination: String? = null
    private var tracker: SelectionTracker<String>? = null
    private var fileObserver : FileObserver? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        headerAdapter = HeaderAdapter()
        sanFilesAdapter = SanFilesAdapter { sanFile -> adapterOnClick(sanFile) }
        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = sanFilesAdapter


        val docId = arguments?.getString("docid")

        observeCurrent(docId)

        tracker = SelectionTracker.Builder<String>(
            "selectionItem",
            binding.recyclerView,
            ItemsKeyProvider(sanFilesAdapter!!),
            ItemsDetailsLookup(binding.recyclerView),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        sanFilesAdapter!!.initTracker()
        sanFilesAdapter!!.tracker = tracker

        if (savedInstanceState != null) {
            tracker?.onRestoreInstanceState(savedInstanceState)
        }

        // *Important*
        // https://stackoverflow.com/questions/72805727/what-is-the-authority-that-documentscontract-movedocument-needs

        // Actions
        // Register these actions outside of the onclicks:
        // https://stackoverflow.com/questions/64476827/how-to-resolve-the-error-lifecycleowners-must-call-register-before-they-are-sta
        var obj = Actions()
        Log.v("File-san", "this as FragmentActivity=${this as Fragment}")
        var actions = obj.get(this as Fragment)
        //val actions: HashMap<String, Any> = Actions.get(activity as FragmentActivity)
        // Copy
        binding.actionCopy.setOnClickListener {
        }
        // Create Folder
        binding.actionCreateFolder.setOnClickListener {
            val action : CreateFolder = actions["CreateFolder"] as CreateFolder
            val docId = DocumentsContract.getTreeDocumentId(destination!!.toUri())
            val docUri = DocumentsContract.buildDocumentUriUsingTree(destination!!.toUri(), docId)
            action.handle(docUri)
            observeCurrent(null)
        }
        // Create File
        binding.actionCreateFile.setOnClickListener {
            val action : CreateFile = actions["CreateFile"] as CreateFile
            // Use the treeUri of the directory:
            // https://developer.android.com/reference/android/provider/DocumentsContract
            val docId = DocumentsContract.getTreeDocumentId(destination!!.toUri())
            val docUri = DocumentsContract.buildDocumentUriUsingTree(destination!!.toUri(), docId)
            action.handle(docUri)
            observeCurrent(null)
        }
        // Move
        binding.actionMove.setOnClickListener {
        }
        // Delete
        binding.actionDelete.setOnClickListener {
        }
        // Open
        binding.actionOpen.setOnClickListener {
            val selections = tracker!!.selection
            val action : Open = actions["Open"] as Open
            action.handle(requireContext(), selections, destination!!)
        }
        // Rename
        binding.actionRename.setOnClickListener {
            val selections = tracker!!.selection
            val action : Rename = actions["Rename"] as Rename
            action.handle(requireContext(), selections, destination!!)
        }


        fileObserver = DirectoryObserver(destination!!.toUri().toFile(), FileObserver.ALL_EVENTS) {
        }

        val foo = object : FileObserver(destination!!.toUri().toFile(), FileObserver.ALL_EVENTS) {
            // set up a file observer to watch this directory on sd card
            override fun onEvent(event: Int, file: String?) {

            }
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings: SharedPreferences = requireActivity().getSharedPreferences("UserInfo", 0)
        destination = if (arguments?.getString("destination") != "" && arguments?.getString("destination") != null) {
            Log.v("File-san", "_destination=${arguments?.getString("destination")}")
            arguments?.getString("destination")
        } else {
            Log.v("File-san", "root=${settings.getString("root", null)}")
            settings.getString("root", null)
        }

        Log.v("File-san", "destination=$destination")

        Log.v("File-san", "fragmentActivity=${requireActivity()}")
        val uriPermissions = requireActivity().contentResolver.persistedUriPermissions
        var havePermissions = false
        // TODO - check that destination contains p.uri, not ==
        Log.v("File-san", "destination=$destination")
        for (p in uriPermissions) {
            Log.v("File-san", "uriPermission=${p.uri.toString()} (r=${p.isReadPermission}/w=${p.isWritePermission})")
            //if (p.uri.toString() == destination && p.isReadPermission && p.isWritePermission) {
            if (destination!!.contains(p.uri.toString()) && p.isReadPermission && p.isWritePermission) {
                havePermissions = true
            }
        }
        Log.v("File-san", "havePermissions=$havePermissions")

        // If user lost permissions somehow, regain them
        if (!havePermissions) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "")
            startActivityForResult(intent, OPEN_DOCUMENT_TREE_REQUEST_CODE)
        }
    }

    private fun observeCurrent(docId: String?) {
        Log.v("File-san", "Observing for destination=${destination!!}")
        val mutableList: MutableList<SanFile> = Utils.getChildren(requireActivity(), destination!!.toUri(), docId)
        Log.v("File-san", "MutableList length=${mutableList.size}")

        // Observe the current directory
        sanFilesViewModel.initSanFiles(mutableList).observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.v("File-san", "Observing")
                sanFilesAdapter!!.submitList(it as MutableList<SanFile>)
                headerAdapter!!.updateSanFileDestination(destination!!)
            }
        })
    }

    // Not currently used, but available from adapter
    private fun adapterOnClick(sanFile: SanFile) {
        // Toast.makeText(context,"clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_DOCUMENT_TREE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            destination = uri.toString()
            val contentResolver = requireActivity().contentResolver
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri!!, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            // save the root folder
            val settings: SharedPreferences = requireActivity().getSharedPreferences("UserInfo", 0)
            val editor = settings.edit()
            editor.putString("root", uri.toString())
            editor.commit()

            observeCurrent(null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }


}



