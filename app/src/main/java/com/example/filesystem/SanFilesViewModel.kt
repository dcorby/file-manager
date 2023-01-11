package com.example.filesystem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SanFilesViewModel: ViewModel() {

    private var sanFilesLiveData: MutableLiveData<MutableList<SanFile>>? = null

    fun insertSanFile(sanFile: SanFile) {
        val currentList = sanFilesLiveData!!.value
        if (currentList == null) {
            sanFilesLiveData!!.postValue(listOf(sanFile).toMutableList())
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, sanFile)
            sanFilesLiveData!!.postValue(updatedList)
        }
    }

    fun removeSanFile(sanFile: SanFile) {
        val currentList = sanFilesLiveData!!.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.remove(sanFile)
            sanFilesLiveData!!.postValue(updatedList)
        }
    }

    fun getSanFiles(): LiveData<MutableList<SanFile>> {
        return sanFilesLiveData!!
    }

    fun initSanFiles(sanFiles: MutableList<SanFile>): LiveData<MutableList<SanFile>> {
        // "What you want is to actually assign a new list to the MutableLiveData"
        // https://stackoverflow.com/questions/69137842/how-to-add-data-to-list-in-mutablelivedata
        //sanFilesInitialList = sanFiles
        sanFilesLiveData = MutableLiveData(sanFiles)
        return sanFilesLiveData!!
    }
}