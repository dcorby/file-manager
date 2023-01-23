package com.example.filesystem

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
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
    lateinit var headerAdapter: HeaderAdapter
    lateinit var sanFilesAdapter: SanFilesAdapter
    lateinit var destinationUri: Uri
    lateinit var destinationDocId: String
    lateinit var tracker: SelectionTracker<String>
    lateinit var liveData: LiveData<MutableList<SanFile>>
    lateinit var receiver: MainReceiver

    private var AUTHORITY = "com.android.externalstorage.documents"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        headerAdapter = HeaderAdapter()
        sanFilesAdapter = SanFilesAdapter { sanFile -> adapterOnClick(sanFile) }
        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.adapter = sanFilesAdapter
        observeCurrent(destinationDocId)

        tracker = SelectionTracker.Builder<String>(
            "selectionItem",
            binding.recyclerView,
            ItemsKeyProvider(sanFilesAdapter),
            ItemsDetailsLookup(binding.recyclerView),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        sanFilesAdapter.tracker = tracker
        sanFilesAdapter.initTracker()

        if (savedInstanceState != null) {
            tracker.onRestoreInstanceState(savedInstanceState)
        }

        // Actions
        var obj = Actions()
        var actions = obj.get(this as Fragment)
        // Copy
        binding.actionCopy.setOnClickListener {
        }
        // Create Folder
        binding.actionCreateFolder.setOnClickListener {
            val action : CreateFolder = actions["CreateFolder"] as CreateFolder
            val docUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, destinationDocId)
            action.handle(docUri)
            observeCurrent(null)
        }
        // Create File
        binding.actionCreateFile.setOnClickListener {
            /* Get the docUri from the destination vars
               It might make sense to build docUri on the fly, e.g.
               var docUri = DocumentFile.fromTreeUri(requireContext(), destinationUri)!!.uri
               However, this *always* yields the root docUri. Is this a bug?
               This thread suggests so: https://stackoverflow.com/questions/62375696/unexpected-behavior-when-documentfile-fromtreeuri-is-called-on-uri-of-subdirec
             */
            val action : CreateFile = actions["CreateFile"] as CreateFile
            val docUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, destinationDocId)
            action.handle(docUri)
            observeCurrent(destinationDocId)
        }
        // Move
        binding.actionMove.setOnClickListener {
            val moveFromUri = receiver.getState("moveFromUri")
            if (moveFromUri == null) {
                val docIdToMove = tracker.selection.toList()[0]
                val uriToMove = DocumentsContract.buildDocumentUriUsingTree(destinationUri, docIdToMove)
                receiver.setState("moveFromUri", uriToMove.toString())
                receiver.setState("moveFromParentUri", destinationUri.toString())
                receiver.setState("moveFromParentDocId", destinationDocId)
                // ^ removing decode() operation on these strings enabled else{} block to work. WHY??
            } else {
                val moveFromParentUri = Utils.decode(receiver.getState("moveFromParentUri")!!)
                val moveFromParentDocId = Utils.decode(receiver.getState("moveFromParentDocId")!!)
                val sourceDocumentParentUri = DocumentsContract.buildDocumentUriUsingTree(moveFromParentUri.toUri(), moveFromParentDocId)
                val targetDocumentParentUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, destinationDocId)
                Log.v("sourceDocumentUri", Utils.decode(moveFromUri))
                Log.v("sourceDocumentParentUri", Utils.decode(sourceDocumentParentUri.toString()))
                Log.v("targetDocumentParentUri", Utils.decode(targetDocumentParentUri.toString()))
                DocumentsContract.moveDocument(requireContext().contentResolver, moveFromUri.toUri(), sourceDocumentParentUri, targetDocumentParentUri)
            }
        }
        // Delete
        binding.actionDelete.setOnClickListener {
            val docIdToDelete = tracker.selection.toList()[0]
            val uriToDelete = DocumentsContract.buildDocumentUriUsingTree(destinationUri, docIdToDelete)
            DocumentsContract.deleteDocument(requireContext().contentResolver, uriToDelete)
            //sanFilesViewModel.removeSanFile(docIdToDelete)
            observeCurrent(destinationDocId)
        }
        // Open
        binding.actionOpen.setOnClickListener {
            val selections = tracker.selection
            val action : Open = actions["Open"] as Open
            action.handle(requireContext(), selections, destinationUri)
        }
        // Rename
        binding.actionRename.setOnClickListener {
            val selections = tracker.selection
            val action : Rename = actions["Rename"] as Rename
            val docId = selections.toList()[0]
            action.handle(docId, destinationUri)
            observeCurrent(destinationDocId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = (activity as MainReceiver)

        val settings: SharedPreferences = requireActivity().getSharedPreferences("UserInfo", 0)

        // set destinationStr and destinationUri
        val destinationStr = if (arguments?.getString("destination").isNullOrEmpty()) {
            settings.getString("root", null)
        } else {
            arguments?.getString("destination")
        }
        destinationUri = destinationStr!!.toUri()

        // set destinationDocId
        destinationDocId = if (arguments?.getString("docid").isNullOrEmpty()) {
            DocumentsContract.getTreeDocumentId(destinationUri)
        } else {
            arguments?.getString("docid")!!
        }

        val uriPermissions = requireActivity().contentResolver.persistedUriPermissions
        var havePermissions = false

        for (uriPermission in uriPermissions) {
            if (uriPermission.isReadPermission && uriPermission.isWritePermission) {
                if (Utils.decode(destinationStr).contains(Utils.decode(uriPermission.uri.toString()))) {
                    havePermissions = true
                }
            }
        }

        // If user lost permissions somehow, regain them
        if (!havePermissions) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "")
            startActivityForResult(intent, OPEN_DOCUMENT_TREE_REQUEST_CODE)
        }
    }

    private fun observeCurrent(docId: String?) {
        val mutableList: MutableList<SanFile> = Utils.getChildren(requireActivity(), destinationUri, docId)

        // Observe the current directory
        liveData = sanFilesViewModel.initSanFiles(mutableList)
        liveData.observe(viewLifecycleOwner, Observer { updatedList ->
            // onChange(): https://developer.android.com/reference/androidx/lifecycle/Observer
            sanFilesAdapter.submitList(updatedList as MutableList<SanFile>)
        })
    }

    // Not currently used, but available from adapter
    private fun adapterOnClick(sanFile: SanFile) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_DOCUMENT_TREE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
            destinationUri = uri
            val contentResolver = requireActivity().contentResolver
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val settings: SharedPreferences = requireActivity().getSharedPreferences("UserInfo", 0)
            val editor = settings.edit()
            editor.putString("root", Utils.decode(uri.toString()))
            editor.commit()
            observeCurrent(null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker.onSaveInstanceState(outState)
    }
}



