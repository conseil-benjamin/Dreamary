package com.example.dreamary.viewmodels.dreams

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dreamary.models.entities.Conversation
import com.example.dreamary.models.entities.Group
import com.example.dreamary.models.entities.Share
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.entities.User
import com.example.dreamary.models.repositories.SocialRepository
import com.example.dreamary.models.states.DreamResponse
import com.example.dreamary.utils.SnackbarType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach

class AddDreamViewModel(private val repository: DreamRepository, private val socialRepository: SocialRepository) : ViewModel() {
    private val _dreamState = MutableLiveData<DreamResponse>(DreamResponse.Loading)
    private val _tags = MutableLiveData<Tag>()

    private var _friendsAndGroup =  MutableStateFlow<Share>(Share( emptyList(), emptyList()))
    var friendsAndGroup: StateFlow<Share> = _friendsAndGroup

    val tag: MutableLiveData<Tag> = _tags

    fun addDream(
        navController: NavController,
        dream: Dream,
        coroutineScope: CoroutineScope,
        onSaved: () -> Unit,
        onFailure: () -> Unit
    ) {

        if (dream.title.isEmpty() || dream.emotions.isEmpty() || dream.dreamType.isEmpty() || (dream.content.isEmpty() && dream.audio["path"] == "")) {
            _dreamState.value =
                DreamResponse.Error("Veuillez renseigner au minimum un titre, un contenu, une émotion et un type de rêve")
            coroutineScope.launch {
                SnackbarManager.showMessage(
                    "Veuillez renseigner au minimum un titre, un contenu et une émotion",
                    SnackbarType.ERROR
                )
            }
            onFailure()
            return
        }
        viewModelScope.launch {
            repository.addDream(
                dream,
                onSuccess = {
                    onSaved()
                },
                onFailure = { e ->
                    coroutineScope.launch {
                        SnackbarManager.showMessage(
                            "Erreur lors de l'ajout du rêve",
                            SnackbarType.ERROR
                        )
                    }
                    onSaved()
                },
            )
                .collect { response ->
                    _dreamState.value = when (response) {
                        is DreamResponse.Success -> DreamResponse.Success()
                        is DreamResponse.Error -> DreamResponse.Error(response.message)
                        else -> {
                            DreamResponse.Loading
                        }
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
                        SnackbarManager.showMessage("Tag ajouté avec succès", SnackbarType.ERROR)
                    }
                },
                onFailure = { e ->
                    coroutineScope.launch {
                        SnackbarManager.showMessage(
                            "Erreur lors de l'ajout du tag",
                            SnackbarType.ERROR
                        )
                    }
                }
            )
                .collect { response ->
                    _dreamState.value = when (response) {
                        is DreamResponse.Success -> DreamResponse.Success()
                        is DreamResponse.Error -> DreamResponse.Error(response.message)
                        else -> {
                            DreamResponse.Loading
                        }
                    }
                }
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun getCustomTag(userId: String, context: Context): Flow<List<Tag>> {
        return repository.getCustomTag(userId)
            .onEach { tagsList ->
                val tagSet = tagsList.map { tag ->
                    // Ajout des virgules manquantes pour la séparation des champs
                    "${tag.id}," +
                            "${tag.category}," +
                            "${tag.isCustom}," +
                            "${tag.name}," +
                            "${tag.usageCount}," +
                            tag.userId
                }.toSet()

                Log.d("TagsShared", "Saving tagSet: $tagSet")
                Log.d("TagsShared", "Using userId: $userId")

                context.getSharedPreferences("tags", Context.MODE_PRIVATE)
                    .edit()
                    .putStringSet(userId, tagSet)
                    .apply()
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