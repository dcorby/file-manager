package com.example.filesystem

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.filesystem.data.DataSource
import com.example.filesystem.data.MyFile
import kotlin.random.Random

class MyFilesListViewModel(val dataSource: DataSource) : ViewModel() {

    val myFilesLiveData = dataSource.getMyFilesList()

    /* If the name and description are present, create new MyFile and add it to the datasource */
    fun insertMyFile(myFileName: String?, myFileDescription: String?) {
        if (myFileName == null || myFileDescription == null) {
            return
        }

        val image = dataSource.getRandomMyFileImageAsset()
        val newMyFile = MyFile(
            Random.nextLong(),
            myFileName,
            image,
            myFileDescription
        )

        dataSource.addMyFile(newMyFile)
    }
}

class MyFilesListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyFilesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyFilesListViewModel(
                dataSource = DataSource.getDataSource(context.resources)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}