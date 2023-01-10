package com.example.filesystem.data

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/* Handles operations on sanFilesLiveData and holds details about it. */
class DataSource(resources: Resources) {
    private val initialSanFilesList = sanFilesList(resources)
    private val sanFilesLiveData = MutableLiveData(initialSanFilesList)

    /* Adds sanFile to liveData and posts value. */
    fun addSanFile(sanFile: SanFile) {
        val currentList = sanFilesLiveData.value
        if (currentList == null) {
            sanFilesLiveData.postValue(listOf(sanFile))
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, sanFile)
            sanFilesLiveData.postValue(updatedList)
        }
    }

    /* Removes sanFile from liveData and posts value. */
    fun removeSanFile(sanFile: SanFile) {
        val currentList = sanFilesLiveData.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.remove(sanFile)
            sanFilesLiveData.postValue(updatedList)
        }
    }

    /* Returns sanFile given an ID */
    fun getSanFileForId(id: Long): SanFile? {
        sanFilesLiveData.value?.let { sanFiles ->
            return sanFiles.firstOrNull{ it.id == id }
        }
        return null
    }

    fun getSanFilesList(): LiveData<List<SanFile>> {
        return sanFilesLiveData
    }

    /* Returns a random sanFile asset for sanFiles that are added. */
    fun getRandomSanFileImageAsset(): Int? {
        val randomNumber = (initialSanFilesList.indices).random()
        return initialSanFilesList[randomNumber].image
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