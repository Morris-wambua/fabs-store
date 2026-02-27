package com.morrislabs.fabs_store.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.morrislabs.fabs_store.data.model.ChatMessage
import com.morrislabs.fabs_store.data.model.Conversation
import com.morrislabs.fabs_store.data.repository.ChatRepository
import com.morrislabs.fabs_store.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ChatViewModel"
    }

    private val chatRepository = ChatRepository()
    private val tokenManager = TokenManager.getInstance(application.applicationContext)

    private val _conversationsState = MutableStateFlow<ConversationsState>(ConversationsState.Idle)
    val conversationsState: StateFlow<ConversationsState> = _conversationsState.asStateFlow()

    private val _messagesState = MutableStateFlow<MessagesState>(MessagesState.Idle)
    val messagesState: StateFlow<MessagesState> = _messagesState.asStateFlow()

    private val _sendState = MutableStateFlow<SendState>(SendState.Idle)
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()

    fun observeConversations(storeId: String) {
        _conversationsState.value = ConversationsState.Loading
        viewModelScope.launch {
            chatRepository.observeConversations(storeId)
                .catch { e ->
                    Log.e(TAG, "Error observing conversations", e)
                    _conversationsState.value = ConversationsState.Error(e.message ?: "Failed to load conversations")
                }
                .collect { conversations ->
                    _conversationsState.value = ConversationsState.Success(conversations)
                }
        }
    }

    fun observeMessages(conversationId: String) {
        _messagesState.value = MessagesState.Loading
        viewModelScope.launch {
            chatRepository.observeMessages(conversationId)
                .catch { e ->
                    Log.e(TAG, "Error observing messages", e)
                    _messagesState.value = MessagesState.Error(e.message ?: "Failed to load messages")
                }
                .collect { messages ->
                    _messagesState.value = MessagesState.Success(messages)
                }
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        val storeId = tokenManager.getUserId() ?: return
        if (text.isBlank()) return

        _sendState.value = SendState.Sending
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(conversationId, storeId, text)
                _sendState.value = SendState.Sent
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message", e)
                _sendState.value = SendState.Error(e.message ?: "Failed to send message")
            }
        }
    }

    fun markAsRead(conversationId: String, storeId: String) {
        viewModelScope.launch {
            try {
                chatRepository.markMessagesAsRead(conversationId, storeId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark messages as read", e)
            }
        }
    }

    fun registerFcmToken(storeId: String, storeName: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val ownerId = tokenManager.getUserId() ?: return@launch
                chatRepository.registerStoreFcmToken(storeId, ownerId, storeName, token)
                Log.d(TAG, "FCM token registered for store: $storeId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register FCM token", e)
            }
        }
    }

    fun resetSendState() {
        _sendState.value = SendState.Idle
    }

    sealed class ConversationsState {
        data object Idle : ConversationsState()
        data object Loading : ConversationsState()
        data class Success(val conversations: List<Conversation>) : ConversationsState()
        data class Error(val message: String) : ConversationsState()
    }

    sealed class MessagesState {
        data object Idle : MessagesState()
        data object Loading : MessagesState()
        data class Success(val messages: List<ChatMessage>) : MessagesState()
        data class Error(val message: String) : MessagesState()
    }

    sealed class SendState {
        data object Idle : SendState()
        data object Sending : SendState()
        data object Sent : SendState()
        data class Error(val message: String) : SendState()
    }
}
