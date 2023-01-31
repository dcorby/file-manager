package com.example.filesystem

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

/*
  https://github.com/material-components/material-components-android/issues/2291
 */

class FolderFragment : Fragment() {

    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!
    private val sanFilesViewModel: SanFilesViewModel by viewModels()
    lateinit var sanFilesAdapter: SanFilesAdapter
    lateinit var fragmentStr: String
    lateinit var fragmentUri: Uri
    lateinit var fragmentDocId: String
    lateinit var tracker: SelectionTracker<String>
    lateinit var liveData: LiveData<MutableList<SanFile>>
    lateinit var receiver: MainReceiver
    private var popup: PopupWindow? = null
    private var prompt: PopupWindow? = null
    private var builder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null
    private var currentAction: String? = null

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

        // Check whether we have an active copy or move
        if (receiver.getActionState("Copy", "sourceUri") != null) {
            binding.toggleGroup.check(R.id.action_copy)
        }
        if (receiver.getActionState("Move", "sourceUri") != null) {
            binding.toggleGroup.check(R.id.action_move)
        }

        // Set the path parts
        val pathParts = Utils.getPathPartsFromDocId(fragmentDocId)
        binding.pathParts.removeAllViews()
        for (pathPart in pathParts) {
            val textView = layoutInflater.inflate(R.layout.path_part, null) as TextView
            textView.text = pathPart
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            textView.setLayoutParams(params)
            binding.pathParts.addView(textView)
        }

        // Actions
        val actions = Actions(this)

        // Create File
        fun createFile() {
            val action = actions.get("CreateFile") as CreateFile
            action.handle(requireActivity(), binding, fragmentUri, fragmentDocId) {
                observeCurrent(fragmentDocId)
            }
        }
        binding.actionCreateFile.setOnClickListener {
            currentAction = "CreateFile"
            createFile()
            currentAction = null
        }
        fun createFolder() {
            val action = actions.get("CreateFolder") as CreateFolder
            action.handle(binding, fragmentUri, fragmentDocId) {
                observeCurrent(fragmentDocId)
            }
        }
        // Create Folder
        binding.actionCreateFolder.setOnClickListener {
            currentAction = "CreateFolder"
            createFolder()
            currentAction = null
        }
        // Open
        fun open() {
            val action = actions.get("Open") as Open
            action.handle(requireActivity(), binding, tracker.selection, fragmentUri)
        }
        binding.actionOpen.setOnClickListener {
            currentAction = "Open"
            open()
            currentAction = null
        }
        // Rename
        fun rename() {
            val action = actions.get("Rename") as Rename
            action.handle(requireActivity(), binding, tracker.selection, fragmentUri) {
                observeCurrent(fragmentDocId)
            }
        }
        binding.actionRename.setOnClickListener {
            currentAction = "Rename"
            rename()
            currentAction = null
        }
        // Copy
        fun copy() {
            val action = actions.get("Copy") as Copy
            val success = action.handle(requireActivity(), binding, tracker.selection, fragmentUri, fragmentDocId)
            if (success) {
                observeCurrent(fragmentDocId)
            }
        }
        binding.actionCopy.setOnClickListener {
            Log.v("TEST","Setting currentAction in onClick")
            currentAction = "Copy"
            copy()
            Log.v("TEST","Nulling currentAction in onClick")
            currentAction = null
        }
        // Move
        fun move() {
            val action = actions.get("Move") as Move
            val success = action.handle(requireActivity(), binding, tracker.selection, fragmentUri, fragmentDocId)
            if (success) {
                observeCurrent(fragmentDocId)
            }
        }
        binding.actionMove.setOnClickListener {
            currentAction = "Move"
            move()
            currentAction = null
        }
        // Delete
        fun delete() {
            val action = actions.get("Delete") as Delete
            action.handle(requireActivity(), binding, tracker.selection, fragmentUri) {
                observeCurrent(fragmentDocId)
            }
        }
        binding.actionDelete.setOnClickListener {
            currentAction = "Delete"
            delete()
            currentAction = null
        }

        // If we have savedInstanceState, check for a currentAction and initiate it
        // https://stackoverflow.com/questions/69622835/how-to-call-a-function-in-kotlin-from-a-string-name
        val actionFuncs = listOf(::Copy, ::CreateFile, ::CreateFolder, ::Delete, ::Move, ::Open, ::Rename).associateBy { it.name }
        if (savedInstanceState != null) {
            Log.v("TEST", "savedInstanceState is not null")
            if (savedInstanceState.getString("currentAction") != null) {
                Log.v("TEST", "currentAction is not null")
                val currentAction = savedInstanceState.getString("currentAction")!!
                val actionState = savedInstanceState.getSerializable(currentAction) as HashMap<String, String>
                for ((key, value) in actionState) {
                    receiver.setActionState(currentAction, key, value)
                }
                actionFuncs[currentAction]?.invoke(this)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = (activity as MainReceiver)

        val settings: SharedPreferences = requireActivity().getSharedPreferences("UserInfo", 0)
        fragmentStr = arguments?.getString("fragmentUri", settings.getString("root", null))!!
        fragmentUri = fragmentStr.toUri()
        fragmentDocId = arguments?.getString("fragmentDocId", DocumentsContract.getTreeDocumentId(fragmentUri))!!

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
        if (prompt != null && prompt!!.isShowing) {
            prompt!!.dismiss()
        }
        if (popup != null && popup!!.isShowing) {
            popup!!.dismiss()
        }
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        // Check for active copy
        if (receiver.getActionState("Copy", "sourceUri") != null) {
            binding.toggleGroup.check(R.id.action_copy)
        } else {
            binding.toggleGroup.uncheck(R.id.action_copy)
        }
        // Check for active move
        if (receiver.getActionState("Move", "sourceUri") != null) {
            binding.toggleGroup.check(R.id.action_move)
        } else {
            binding.toggleGroup.uncheck(R.id.action_move)
        }
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
        if (this::tracker.isInitialized) {
            tracker.onSaveInstanceState(outState)
        }
        // save ActionState data
        Log.v("TEST", "currentAction=$currentAction")
        if (currentAction != null) {
            Log.v("TEST", "Saving currentAction=$currentAction")
            outState.putString("currentAction", currentAction)
            outState.putSerializable(currentAction, receiver.getActionState(currentAction!!))
        }
    }

    fun getPopupWindow(type: String) : PopupWindow {
        when (type) {
            "popup" -> {
                if (popup == null) {
                    val layout = layoutInflater.inflate(R.layout.popup, null)
                    popup = PopupWindow(
                        layout,
                        ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT,
                        true
                    )
                    popup!!.showAtLocation(layout, Gravity.CENTER, 0, 0)
                }
                return popup as PopupWindow
            }
            "prompt" -> {
                if (prompt == null) {
                    val contentView = layoutInflater.inflate(R.layout.prompt, null)
                    prompt = PopupWindow(
                        contentView,
                        ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT,
                        true
                    )
                    prompt!!.showAtLocation(contentView, Gravity.CENTER, 0, 0)
                }
                return prompt as PopupWindow
            }
            else -> throw Exception("Unknown popup type")
        }
    }

    fun getAlertDialog() : Pair<AlertDialog.Builder, AlertDialog> {
        if (dialog == null) {
            builder = AlertDialog.Builder(requireActivity())
            dialog = builder!!.create()
        }
        return Pair(builder!!, dialog!!)
    }

}