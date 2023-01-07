package com.example.filesystem.data

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/* Handles operations on myFilesLiveData and holds details about it. */
class DataSource(resources: Resources) {
    private val initialMyFilesList = myFilesList(resources)
    private val myFilesLiveData = MutableLiveData(initialMyFilesList)

    /* Adds myFile to liveData and posts value. */
    fun addMyFile(myFile: MyFile) {
        val currentList = myFilesLiveData.value
        if (currentList == null) {
            myFilesLiveData.postValue(listOf(myFile))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, myFile)
            myFilesLiveData.postValue(updatedList)
        }
    }

    /* Removes myFile from liveData and posts value. */
    fun removeMyFile(myFile: MyFile) {
        val currentList = myFilesLiveData.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.remove(myFile)
            myFilesLiveData.postValue(updatedList)
        }
    }

    /* Returns myFile given an ID. */
    fun getMyFileForId(id: Long): MyFile? {
        myFilesLiveData.value?.let { myFiles ->
            return myFiles.firstOrNull{ it.id == id }
        }
        return null
    }

    fun getMyFilesList(): LiveData<List<MyFile>> {
        return myFilesLiveData
    }

    /* Returns a random myFile asset for myFiles that are added. */
    fun getRandomMyFileImageAsset(): Int? {
        val randomNumber = (initialMyFilesList.indices).random()
        return initialMyFilesList[randomNumber].image
    }

    companion object {
        private var INSTANCE: DataSource? = null

        fun getDataSource(resources: Resources): DataSource {
            return synchronized(DataSource::class) {
                val newInstance = INSTANCE ?: DataSource(resources)
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}