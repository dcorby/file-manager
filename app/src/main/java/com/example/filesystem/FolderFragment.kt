package com.example.filesystem

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
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
    private var sanFilesAdapter: SanFilesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val headerAdapter = HeaderAdapter()
        sanFilesAdapter = SanFilesAdapter { sanFile ->
            adapterOnClick(sanFile)
        }
        val concatAdapter = ConcatAdapter(headerAdapter, sanFilesAdapter)
        val recyclerView: RecyclerView = requireActivity().findViewById(R.id.recycler_view)
        recyclerView.adapter = concatAdapter

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "")
        startActivityForResult(intent, OPEN_DOCUMENT_TREE_REQUEST_CODE)

        _binding = FragmentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun adapterOnClick(sanFile: SanFile) {
        Toast.makeText(context,"clicked", Toast.LENGTH_SHORT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        /* Create new file or folder */
        if (requestCode == OPEN_DOCUMENT_TREE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            sanFilesViewModel.getSanFiles().observe(viewLifecycleOwner, Observer {
                it?.let {
                    sanFilesAdapter!!.submitList(it as MutableList<SanFile>)
                    //headerAdapter.updateHeader(...)
                }
            })
        }
    }
}