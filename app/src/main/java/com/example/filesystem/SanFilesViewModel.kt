package com.example.filesystem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.transition.R

class SanFilesViewModel: ViewModel() {

    private val sanFilesInitialList = getInitialSanFiles()
    private val sanFilesLiveData = MutableLiveData(sanFilesInitialList)

    fun insertSanFile(sanFile: SanFile) {
        val currentList = sanFilesLiveData.value
        if (currentList == null) {
            sanFilesLiveData.postValue(listOf(sanFile).toMutableList())
        } else {
            val updatedList = currentList.toMutableList()
            updatedList.add(0, sanFile)
            sanFilesLiveData.postValue(updatedList)
        }
    }

    fun removeSanFile(sanFile: SanFile) {
        val currentList = sanFilesLiveData.value
        if (currentList != null) {
            val updatedList = currentList.toMutableList()
            updatedList.remove(sanFile)
            sanFilesLiveData.postValue(updatedList)
        }
    }

    fun getSanFiles(): LiveData<MutableList<SanFile>> {
        return sanFilesLiveData
    }

    private fun getInitialSanFiles(): MutableList<SanFile> {
        // "What you want is to actually assign a new list to the MutableLiveData"
        // https://stackoverflow.com/questions/69137842/how-to-add-data-to-list-in-mutablelivedata
        val tmpList:MutableList<SanFile> = ArrayList()
        tmpList.add(
            SanFile(
                id = 1,
                name = "SanFile1",
                image = R.drawable.abc_btn_default_mtrl_shape,
                description = "SanFile1 description"
            )
        )
        tmpList.add(
            SanFile(
                id = 2,
                name = "SanFile2",
                image = R.drawable.abc_btn_default_mtrl_shape,
                description = "SanFile2 description"
            )
        )
        tmpList.add(
            SanFile(
                id = 3,
                name = "SanFile3",
                image = R.drawable.abc_btn_default_mtrl_shape,
                description = "SanFile3 description"
            )
        )
        return tmpList
    }
}

//class SanFilesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(SanFilesListViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return SanFilesListViewModel(
//                dataSource = DataSource.getDataSource(context.resources)
//            ) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}