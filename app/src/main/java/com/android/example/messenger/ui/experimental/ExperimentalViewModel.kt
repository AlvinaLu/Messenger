package com.android.example.messenger.ui.experimental

import androidx.lifecycle.*
import com.android.example.messenger.ui.main.ContactsFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ExperimentalViewModel(private val repository: ExperimentalRepository): ViewModel() {

    private val observer = Observer<List<ContactsModel>> {
       listLoadedMutableLiveData.value = if (it.isEmpty()) { Status.EMPTY } else { Status.NOT_EMPTY }
    }

    private val listLoadedMutableLiveData : MutableLiveData<Status> by lazy {
        MutableLiveData<Status>().also { it.value = Status.NOT_LOADED }
    }

    val listLoadedLiveData : LiveData<Status> = listLoadedMutableLiveData

    val liveData: LiveData<List<ContactsModel>> = repository.allContacts.asLiveData()

    fun insert(vararg contact: ContactsModel) =  viewModelScope.launch {
        repository.insert(*contact)
    }

    //first - data loading
    //second - data loading ok
    private val fetchFromWebStatusMutableLiveData: MutableLiveData<Pair<Boolean, Boolean>> = MutableLiveData(
        Pair(first = false, second = true)
    )
    val fetchFromWebStatusLiveData: MutableLiveData<Pair<Boolean, Boolean>> = fetchFromWebStatusMutableLiveData

    fun fetchFromWeb(){
        viewModelScope.launch {
            fetchFromWebStatusMutableLiveData.value = Pair(true, true)
            val result = withContext(Dispatchers.IO){
                repository.fetchAll()
            }
            fetchFromWebStatusMutableLiveData.value = Pair(true, result)
            fetchFromWebStatusMutableLiveData.value = Pair(false, true)
        }
    }
    fun fetchFromWebStatusConsumed(){
        fetchFromWebStatusMutableLiveData.value = Pair(false, true)
    }
    init {
        liveData.observeForever(observer)
    }

    override fun onCleared() {
        liveData.removeObserver(observer)
        super.onCleared()
    }


}

class ExperimentalViewModelFactory(private val repository: ExperimentalRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExperimentalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExperimentalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class Status {
    NOT_LOADED,
    EMPTY,
    NOT_EMPTY
}