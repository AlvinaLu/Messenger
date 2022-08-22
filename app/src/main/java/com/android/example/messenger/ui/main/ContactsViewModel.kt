package com.android.example.messenger.ui.main

import androidx.lifecycle.*
import com.android.example.messenger.data.db.ContactsRepository
import com.android.example.messenger.models.ContactsModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ContactsViewModel(private val repository: ContactsRepository): ViewModel() {

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
    private val fetchFromWebStatusMutableLiveData: MutableLiveData<Status> by lazy{
        MutableLiveData<Status>().also { it.value = Status.NOT_LOADED }
    }
    val fetchFromWebStatusLiveData: MutableLiveData<Status> = fetchFromWebStatusMutableLiveData

    fun fetchFromWeb(){
        viewModelScope.launch {
            fetchFromWebStatusMutableLiveData.value = Status.NOT_LOADED
            val result = withContext(Dispatchers.IO){
                repository.fetchAll()
            }
            if(result){
                fetchFromWebStatusLiveData.value = Status.NOT_EMPTY
            }
        }
    }

    init {
        liveData.observeForever(observer)
    }

    override fun onCleared() {
        liveData.removeObserver(observer)
        super.onCleared()
    }



}

class ExperimentalViewModelFactory(private val repository: ContactsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

