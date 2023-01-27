package com.example.filesystem

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
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
const val AUTHORITY = "com.android.externalstorage.documents"

class FolderFragment : Fragment() {
    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!
    private val sanFilesViewModel: SanFilesViewModel by viewModels()
    lateinit var sanFilesAdapter: SanFilesAdapter
    lateinit var destinationUri: Uri
    lateinit var destinationDocId: String
    lateinit var tracker: SelectionTracker<String>
    lateinit var liveData: LiveData<MutableList<SanFile>>
    lateinit var receiver: MainReceiver

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

        // Set the path parts
        val pathParts = Utils.getPathPartsFromDocId(destinationDocId)
        binding.pathParts.removeAllViews()
        for (pathPart in pathParts) {
            val textView = layoutInflater.inflate(R.layout.path_part, null) as TextView
            textView.text = pathPart
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            textView.setLayoutParams(params)
            binding.pathParts.addView(textView)
        }

        // Actions
        var obj = Actions()
        var actions = obj.get(this as Fragment)
        // Copy
        // https://stackoverflow.com/questions/61687463/documentscontract-copydocument-always-fails
        // https://stackoverflow.com/questions/13133579/android-save-a-file-from-an-existing-uri
        // copydocument() does not work, well-documented bug or non-implementation
        // Copying bytes has issues too, over MTP: https://issuetracker.google.com/issues/36956498
        // FileManager may not refresh on the host machine
        binding.actionCopy.setOnClickListener {
            val copyFromUri = receiver.getState("copyFromUri")
            if (copyFromUri == null) {
                var text = ""
                if (tracker.selection.size() == 0) {
                    text = "Select a file to copy"
                }
                if (tracker.selection.size() > 1) {
                    text = "Multi-file copy is not supported"
                }
                if (text != "") {
                    Utils.showPopup(layoutInflater, requireActivity(), text)
                    return@setOnClickListener
                }
                val docIdToCopy = tracker.selection.toList()[0]
                val uriToCopy = DocumentsContract.buildDocumentUriUsingTree(destinationUri, docIdToCopy)
                receiver.setState("copyFromUri", uriToCopy.toString())
                receiver.setState("copyName", Utils.getNameFromDocId(docIdToCopy))
            } else {
                val docUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, destinationDocId)
                val name = receiver.getState("copyName")!!
                val newUri = DocumentsContract.createDocument(requireActivity().contentResolver, docUri, "text/plain", name)
                val input = requireContext().contentResolver.openInputStream(copyFromUri.toUri())!!
                val bytes = input.readBytes()
                input.close()
                val output = requireContext().contentResolver.openOutputStream(newUri!!)!!
                output.write(bytes)
                output.close()
                // TODO: Android has no built-in method for file hash, but should implement an equal-bytes check of the two files

                // This fails on most Android devices up to SDK32 with "java.lang.UnsupportedOperationException: Copy not supported"
                // val targetDocumentParentUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, destinationDocId)
                // DocumentsContract.copyDocument(requireContext().contentResolver, copyFromUri.toUri(), targetDocumentParentUri)

                observeCurrent(null)
            }
        }
        // Create Folder
        binding.actionCreateFolder.setOnClickListener {
            val action : CreateFolder = actions["CreateFolder"] as CreateFolder
            val docUri = DocumentsContract.buildDocumentUriUsingTree(destinationUri, destinationDocId)
            action.handle(docUri)
            observeCurrent(null)
            Utils.withDelay{ binding.toggleGroup1.uncheck(R.id.action_create_folder) }
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
            val filename = binding.filename.text.trim().toString()
            if (filename == "") {
                Utils.showPopup(layoutInflater, requireActivity(), "Filename is empty")
                return@setOnClickListener
            }
            action.handle(receiver, docUri, filename)
            observeCurrent(destinationDocId)
            Utils.withDelay{ binding.toggleGroup1.uncheck(R.id.action_create_file) }
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
            Utils.withDelay{ binding.toggleGroup2.uncheck(R.id.action_delete) }
        }
        // Open
        binding.actionOpen.setOnClickListener {
            val selections = tracker.selection
            val action : Open = actions["Open"] as Open
            action.handle(requireContext(), selections, destinationUri)
            Utils.withDelay{ binding.toggleGroup2.uncheck(R.id.action_open) }
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