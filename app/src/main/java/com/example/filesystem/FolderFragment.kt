package com.example.filesystem

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.R
import com.example.filesystem.databinding.FragmentFolderBinding

/**
 * If the user has already initialized the app, land on this fragment.
 * This could happen when they open the app, or after the
 * activity result callback if just getting started
 */

const val OPEN_DOCUMENT_TREE_REQUEST_CODE = 1

class FolderFragment : Fragment() {

    private var _binding: FragmentFolderBinding? = null
    // this property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    // https://stackoverflow.com/questions/54313453/how-to-instantiate-viewmodel-in-androidx
    // Initialize VM as Class Instance Val
    private val sanFilesViewModel: SanFilesViewModel by viewModels()
    // this is functionally equal to:
    // private val sanFilesViewModel by lazy {
    //     ViewModelProvider(this).get(SanFilesViewModel::class.java)
    // }
    // ... which internally will use ViewModelProvider and scope your ViewModel to your Activity

    private var headerAdapter: HeaderAdapter? = null
    private var sanFilesAdapter: SanFilesAdapter? = null
    private var destination: String? = null

    private var curSanFile: SanFile? = null
    private lateinit var tracker: SelectionTracker<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        destination = "/" + (arguments?.getString("destination", "") ?: "")

        headerAdapter = HeaderAdapter()
        sanFilesAdapter = SanFilesAdapter{ sanFile ->
            adapterOnClick(sanFile)
        }
        //val concatAdapter = ConcatAdapter(headerAdapter, sanFilesAdapter)
        val recyclerView: RecyclerView = binding.recyclerView
        //recyclerView.adapter = concatAdapter
        recyclerView.adapter = sanFilesAdapter

        val settings: SharedPreferences = requireActivity().getSharedPreferences("UserInfo", 0)
        destination = settings.getString("root", null)

        val uriPermissions = requireActivity().contentResolver.persistedUriPermissions
        var havePermissions = false
        // TODO - check that destination contains p.uri, not ==
        for (p in uriPermissions) {
            Log.v("File-San", "uriPermission=${p.uri.toString()} (r=${p.isReadPermission}/w=${p.isWritePermission})")
            if (p.uri.toString() == destination && p.isReadPermission && p.isWritePermission) {
                havePermissions = true
            }
        }

        // If user lost permissions somehow, regain them
        if (!havePermissions) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "")
            startActivityForResult(intent, OPEN_DOCUMENT_TREE_REQUEST_CODE)
        } else {
            observeCurrent()
        }

        setupUi()
    }

    private fun observeCurrent() {
        // Difference between mutableList and arrayList:
        // https://stackoverflow.com/questions/43114367/difference-between-arrayliststring-and-mutablelistofstring-in-kotlin
        val mutableList: MutableList<SanFile> = Utils.getChildren(requireActivity(), destination!!.toUri())
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

    private fun adapterOnClick(sanFile: SanFile) {
        //if (curSanFile != null) {

        //}
        //curSanFile.
        Toast.makeText(context,"clicked", Toast.LENGTH_SHORT).show()
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

            observeCurrent()
        }
    }

    private fun setupUi() {

        tracker = SelectionTracker.Builder<String>(
            "selectionItem",
            binding.recyclerView,
            ItemsKeyProvider(sanFilesAdapter!!),
            ItemsDetailsLookup(binding.recyclerView),
            StorageStrategy.createStringStorage()
            ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(
        object : SelectionTracker.SelectionObserver<String>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                // Good place to update livedata?
            }
        })

        sanFilesAdapter?.tracker = tracker
    }
}