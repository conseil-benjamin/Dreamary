package com.example.dreamary.viewmodels.dreams

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.utils.SnackbarManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.dreamary.R
import androidx.lifecycle.viewModelScope
import com.example.dreamary.models.states.DreamResponse

class AddDreamViewModel(private val repository: DreamRepository) : ViewModel() {
    private val _dreamState = MutableLiveData<DreamResponse>(DreamResponse.Loading)

    fun addDream (navController: NavController, dream: Dream, coroutineScope: CoroutineScope) {

        if (dream.title.isEmpty() || dream.content.isEmpty()) {
            Log.d("title", dream.title)
            Log.d("title", dream.content)
            Log.d("title", dream.emotions.toString())
            _dreamState.value = DreamResponse.Error("Veuillez remplir tous les champs")
            coroutineScope.launch {
                SnackbarManager.showMessage("Veuillez remplir tous les champs", R.drawable.error)
            }
            return
        }
        viewModelScope.launch {
            repository.addDream(
                dream,
                onSuccess = {
                    // Gérer le succès
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Rêve ajouté avec succès", R.drawable.success)
                    }
                },
                onFailure = { e ->
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Erreur lors de l'ajout du rêve", R.drawable.error)
                    }
                }
            )
                .collect { response ->
                    _dreamState.value = when(response) {
                        is DreamResponse.Success -> DreamResponse.Success()
                        is DreamResponse.Error -> DreamResponse.Error(response.message)
                        else -> { DreamResponse.Loading }
                    }
                }
        }
    }
}