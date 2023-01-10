package com.example.filesystem

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.filesystem.data.DataSource
import com.example.filesystem.data.SanFile
import kotlin.random.Random

class SanFilesListViewModel(val dataSource: DataSource) : ViewModel() {

    val sanFilesLiveData = dataSource.getSanFilesList()

    /* If the name and description are present, create new SanFile and add it to the datasource */
    fun insertSanFile(sanFileName: String?, sanFileDescription: String?) {
        if (sanFileName == null || sanFileDescription == null) {
            return
        }

        val image = dataSource.getRandomSanFileImageAsset()
        val newSanFile = SanFile(
            Random.nextLong(),
            sanFileName,
            image,
            sanFileDescription
        )

        dataSource.addSanFile(newSanFile)
    }
}

class SanFilesListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SanFilesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SanFilesListViewModel(
                dataSource = DataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}