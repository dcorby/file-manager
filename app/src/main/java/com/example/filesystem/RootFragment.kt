package com.example.filesystem

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.filesystem.data.SanFile
import com.example.filesystem.databinding.FragmentRootBinding

/**
 * If the user has already initialized the app, land on this fragment.
 * This could happen when they open the app, or after the
 * activity result callback if just getting started
 */

const val SAN_FILE_ID = "san file id"

class RootFragment : Fragment() {

    private var _binding: FragmentRootBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val newSanFileActivityRequestCode = 1
    private val sanFilesListViewModel by viewModels<SanFilesListViewModel> {
        SanFilesListViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Instantiates headerAdapter and filesAdapter. Both adapters are added to concatAdapter.
          which displays the contents sequentially
        */
        val headerAdapter = HeaderAdapter()
        val myFilesAdapter = SanFilesAdapter { file -> adapterOnClick(file) }
        val concatAdapter = ConcatAdapter(headerAdapter, myFilesAdapter)

        val recyclerView: RecyclerView = requireActivity().findViewById(R.id.recycler_view)
        recyclerView.adapter = concatAdapter

        sanFilesListViewModel.sanFilesLiveData.observe(viewLifecycleOwner) {
            it?.let {
                myFilesAdapter.submitList(it as MutableList<SanFile>)
                headerAdapter.updateMyFileCount(it.size)
            }
        }
    }

    /* Opens SanFileDetailActivity when RecyclerView item is clicked. */
    private fun adapterOnClick(sanFile: SanFile) {
        //val intent = Intent(this, SanFileDetailActivity()::class.java)
        //intent.putExtra(FLOWER_ID, flower.id)
        //startActivity(intent)
        Toast.makeText(context,"clicked", Toast.LENGTH_SHORT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}