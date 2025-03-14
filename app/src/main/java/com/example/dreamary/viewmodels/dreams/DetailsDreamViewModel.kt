package com.example.dreamary.viewmodels.dreams

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.dreamary.R
import com.example.dreamary.models.entities.Dream
import com.example.dreamary.models.entities.Share
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.DreamRepository
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.models.states.DreamResponse
import com.example.dreamary.utils.SnackbarManager
import com.example.dreamary.utils.SnackbarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailsDreamViewModel(private val repository: DreamRepository, private val socialRepository: SocialRepository) : ViewModel() {
    private var _dream = MutableStateFlow<Dream?>(null)
    var dream = _dream.asStateFlow()

    private var _friendsAndGroup =  MutableStateFlow<Share>(Share( emptyList(), emptyList()))
    var friendsAndGroup: StateFlow<Share> = _friendsAndGroup

    private val _dreamState = MutableLiveData<DreamResponse>(DreamResponse.Loading)

    fun getDreamById(idDream: String, userId: String){
        viewModelScope.launch {
            repository.getDreamById(idDream, userId).collect { dream ->
                _dream.value = dream
                Log.d("DetailsDreamViewModel", "getDreamById: $dream")
            }
        }
    }

    fun updateDream (navController: NavController, dream: Dream, coroutineScope: CoroutineScope, onSaved: () -> Unit, onFailure: () -> Unit) {

        if (dream.title.isEmpty()  || dream.emotions.isEmpty() || dream.dreamType.isEmpty() || (dream.content.isEmpty() && dream.audio["path"] == "")) {
            _dreamState.value = DreamResponse.Error("Veuillez renseigner au minimum un titre, un contenu, une émotion et un type de rêve")
            coroutineScope.launch {
                SnackbarManager.showMessage("Veuillez renseigner au minimum un titre, un contenu et une émotion", SnackbarType.ERROR)
            }
            onFailure()
            return
        }
        viewModelScope.launch {
            repository.updateDream(
                dream,
                onSuccess = {
                    onSaved()
                },
                onFailure = { e ->
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Erreur lors de la modification du rêve",
                            SnackbarType.ERROR)
                    }
                    onSaved()
                },
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

    fun addTag(tag: Tag, coroutineScope: CoroutineScope, context: Context) {
        if (tag.name.isEmpty()) {
            _dreamState.value = DreamResponse.Error("Veuillez remplir le champ")
            coroutineScope.launch {
                SnackbarManager.showMessage("Veuillez remplir le champ", SnackbarType.ERROR)
            }
            return
        }

        viewModelScope.launch {
            repository.addTag(
                tag,
                onSuccess = {
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Tag ajouté avec succès", SnackbarType.SUCCESS)
                    }
                },
                onFailure = { e ->
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Erreur lors de l'ajout du tag", SnackbarType.ERROR)
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

    fun getFriendsAndGroupForCurrentUser(userId: String) {
        viewModelScope.launch {
            socialRepository.getFriendsAndGroupForCurrentUser(userId)
                .collect { friendsAndGroup ->
                    Log.d("FriendsAndGroup", "Friends and group: $friendsAndGroup")
                    _friendsAndGroup.value = friendsAndGroup
                }
        }
    }

}