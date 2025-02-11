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
import com.example.dreamary.models.entities.Tag
import com.example.dreamary.models.states.DreamResponse
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class AddDreamViewModel(private val repository: DreamRepository) : ViewModel() {
    private val _dreamState = MutableLiveData<DreamResponse>(DreamResponse.Loading)
    private val _tags = MutableLiveData<Tag>()
    val tag: MutableLiveData<Tag> = _tags

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

    fun addTag(tag: Tag, coroutineScope: CoroutineScope, context: Context) {
        if (tag.name.isEmpty()) {
            _dreamState.value = DreamResponse.Error("Veuillez remplir le champ")
            coroutineScope.launch {
                SnackbarManager.showMessage("Veuillez remplir le champ", R.drawable.error)
            }
            return
        }

        viewModelScope.launch {
            repository.addTag(
                tag,
                onSuccess = {
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Tag ajouté avec succès", R.drawable.success)
                    }
                },
                onFailure = { e ->
                    coroutineScope.launch {
                        SnackbarManager.showMessage("Erreur lors de l'ajout du tag", R.drawable.error)
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
}