package com.example.filesystem

import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.example.filesystem.actions.*
import com.example.filesystem.databinding.FragmentFolderBinding
import java.util.*

/*
  https://github.com/material-components/material-components-android/issues/2291
 */

interface DialogCallback {
    fun onDialogClickYes(uri: Uri)
    fun onDialogClickNo()
    fun onCancel()
    fun onDismiss()
}

class FolderFragment : Fragment(), DialogCallback, MainActivity.StateRestoredListener {

    val OPEN_DOCUMENT_TREE_REQUEST_CODE = 1
    val OPEN_DOCUMENT_REQUEST_CODE = 2
    val AUTHORITY = "com.android.externalstorage.documents"

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
    lateinit var actions: kotlin.collections.HashMap<String, Action>
    var savedText: String? = null

    private var viewCreated = false
    private var stateRestored = false

    // popup windows
    private var popup: PopupWindow? = null
    private var prompt: PopupWindow? = null
    private var dialogCancelled: Boolean = false

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

        savedText = savedInstanceState?.getString("savedText", "")

        checkBackStackToPop()
        (requireActivity() as MainActivity).setStateRestoredListener(this)

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

        // Check for an active copy or move
        if (receiver.getActionState("copy", "sourceUri") != null) {
            UI.handleActiveCopy(receiver, binding)
            binding.close.setOnClickListener { actions["copy"]?.finish() }
        }
        if (receiver.getActionState("move", "sourceUri") != null) {
            UI.handleActiveMove(receiver, binding)
            binding.close.setOnClickListener { actions["move"]?.finish() }
        }

        UI.setPath(binding, layoutInflater, fragmentDocId)
        UI.setPathOnClick(binding, receiver) { checkBackStackToPop() }

        // Actions
        fun callback() {}
        actions = Actions(this, binding, tracker.selection, fragmentUri, fragmentDocId, ::callback).map
        binding.actionCreateFile.setOnClickListener { actions["createFile"]?.handle(true) }
        binding.actionCreateFolder.setOnClickListener { actions["createFolder"]?.handle(true) }
        binding.actionOpen.setOnClickListener { actions["open"]?.handle(true) }
        binding.actionRename.setOnClickListener { actions["rename"]?.handle(true) }
        binding.actionCopy.setOnClickListener { actions["copy"]?.handle(true) }
        binding.actionMove.setOnClickListener { actions["move"]?.handle(true) }
        binding.actionDelete.setOnClickListener { actions["delete"]?.handle(true) }
        viewCreated = true
        callAction()
    }

    override fun onStop() {
        super.onStop()
        binding.toggleGroup.uncheck(R.id.action_open)
    }

    private fun checkBackStackToPop() {
        if (receiver.getBackStackPopCount() > 0) {
            receiver.setBackStackPopCount(receiver.getBackStackPopCount() - 1)
            this.parentFragmentManager.popBackStack()
        }
    }

    private fun callAction() {
        if (viewCreated && stateRestored) {
            if (receiver.getCurrentAction() != null) {
                actions[receiver.getCurrentAction()]?.handle(false)
                viewCreated = false
                stateRestored = false
            }
        }
    }

    override fun onStateRestored() {
        stateRestored = true
        callAction()
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

    fun observeCurrent(docId: String?) {
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
    }

    override fun onResume() {
        super.onResume()

        // Check for an active copy or move
        UI.handleActiveCopy(receiver, binding)
        UI.handleActiveMove(receiver, binding)

        // Dismiss the delete dialog fragment if necessary
        // Provides a smoother UI effect here
        // https://stackoverflow.com/questions/9325238/proper-way-of-dismissing-dialogfragment-while-application-is-in-background
        val fragmentManager: FragmentManager = parentFragmentManager
        val dialogFragment: DialogFragment? = fragmentManager.findFragmentByTag("dialog") as DialogFragment?
        dialogFragment?.dismiss()
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

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
            }
            binding.toggleGroup.uncheck(R.id.action_open)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::tracker.isInitialized) {
            tracker.onSaveInstanceState(outState)
        }

        if (prompt != null) {
            val str = prompt?.contentView?.findViewById<EditText>(R.id.edit_text)?.text.toString()
            outState.putString("savedText", str)
        }
    }

    fun getPopupWindow(type: String) : PopupWindow {
        // popup
        if (type == "popup") {
            val layout = layoutInflater.inflate(R.layout.popup, binding.fragmentParent, false)
            popup = PopupWindow(
                layout,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                true
            )
            popup!!.showAtLocation(layout, Gravity.CENTER, 0, 0)
            return popup!!
        }
        // prompt
        if (type == "prompt") {
            val layout = layoutInflater.inflate(R.layout.prompt, binding.fragmentParent, false)
            prompt = PopupWindow(
                layout,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                true
            )
            prompt!!.showAtLocation(layout, Gravity.CENTER, 0, 0)
            return prompt!!
        }
        throw Exception("Unknown popup type")
    }

    // Delete callbacks
    override fun onDialogClickYes(uri: Uri) {
        DocumentsContract.deleteDocument(this.requireActivity().contentResolver, uri)
        actions["delete"]?.finish()
        observeCurrent(fragmentDocId)
    }
    override fun onDialogClickNo() {
        actions["delete"]?.finish()
    }
    // Need to know whether the dialog has been dismissed by click (which will cancel first), or activity destroy
    override fun onCancel() {
        dialogCancelled = true
    }
    override fun onDismiss() {
        if (dialogCancelled) {
            Utils.withDelay({ binding.toggleGroup.uncheck(R.id.action_delete) })
            receiver.setCurrentAction(null)
        }
        dialogCancelled = false
    }
}