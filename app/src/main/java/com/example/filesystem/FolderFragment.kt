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

const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 1
const val AUTHORITY = "com.android.externalstorage.documents"

class FolderFragment : Fragment() {
    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!
    private val sanFilesViewModel: SanFilesViewModel by viewModels()
    lateinit var sanFilesAdapter: SanFilesAdapter
    lateinit var fragmentUri: Uri
    lateinit var fragmentDocId: String
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
        observeCurrent(fragmentDocId)

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

        // Check whether we have an active copy
        if (receiver.getActionState("Copy", "sourceUri") != null) {
            binding.toggleGroup.check(R.id.action_copy)
        }

        // Set the path parts
        val pathParts = Utils.getPathPartsFromDocId(fragmentDocId)
        binding.pathParts.removeAllViews()
        for (pathPart in pathParts) {
            val textView = layoutInflater.inflate(R.layout.path_part, null) as TextView
            textView.text = pathPart
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            textView.setLayoutParams(params)
            binding.pathParts.addView(textView)
        }

        // Actions
        val actions = Actions(this as Fragment)

        // Copy
        binding.actionCopy.setOnClickListener {
            val action = actions.get("Copy") as Copy
            val success = action.handle(requireActivity(), tracker.selection, fragmentUri, fragmentDocId)
            if (success) {
                observeCurrent(fragmentDocId)
            }
        }
        // Create Folder
        binding.actionCreateFolder.setOnClickListener {
            val action = actions.get("CreateFolder") as CreateFolder
            action.handle(requireActivity(), binding, fragmentUri, fragmentDocId) {
                observeCurrent(fragmentDocId)
            }
        }
        // Create File
        binding.actionCreateFile.setOnClickListener {
            val action = actions.get("CreateFile") as CreateFile
            action.handle(requireActivity(), binding, fragmentUri, fragmentDocId) {
                observeCurrent(fragmentDocId)
            }
        }
        // Move
        binding.actionMove.setOnClickListener {
            val action = actions.get("Move") as Move
            action.handle(requireActivity(), tracker.selection, fragmentUri, fragmentDocId)
        }
        // Delete
        binding.actionDelete.setOnClickListener {
            val action = actions.get("Delete") as Delete
            action.handle(requireActivity(), binding, tracker.selection, fragmentUri)
            observeCurrent(fragmentDocId)
        }
        // Open
        binding.actionOpen.setOnClickListener {
            val action = actions.get("Open") as Open
            action.handle(requireActivity(), binding, tracker.selection, fragmentUri)
        }
        // Rename
        binding.actionRename.setOnClickListener {
            val action = actions.get("Rename") as Rename
            action.handle(requireActivity(), binding, tracker.selection, fragmentUri) {
                observeCurrent(fragmentDocId)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = (activity as MainReceiver)

        val settings: SharedPreferences = requireActivity().getSharedPreferences("UserInfo", 0)

        // set fragmentStr and fragmentUri
        val fragmentStr = if (arguments?.getString("fragmentUri").isNullOrEmpty()) {
            settings.getString("root", null)
        } else {
            arguments?.getString("fragmentUri")
        }
        fragmentUri = fragmentStr!!.toUri()

        // set fragmentDocId
        fragmentDocId = if (arguments?.getString("fragmentDocId").isNullOrEmpty()) {
            DocumentsContract.getTreeDocumentId(fragmentUri)
        } else {
            arguments?.getString("fragmentDocId")!!
        }

        val uriPermissions = requireActivity().contentResolver.persistedUriPermissions
        var havePermissions = false

        for (uriPermission in uriPermissions) {
            if (uriPermission.isReadPermission && uriPermission.isWritePermission) {
                if (Utils.decode(fragmentStr).contains(Utils.decode(uriPermission.uri.toString()))) {
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
        val mutableList: MutableList<SanFile> = Utils.getChildren(requireActivity(), fragmentUri, docId)

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
            fragmentUri = uri
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