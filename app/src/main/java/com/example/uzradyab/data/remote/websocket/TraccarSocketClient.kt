package com.example.uzradyab.data.remote.websocket

import android.util.Log
import com.example.uzradyab.BuildConfig
import com.example.uzradyab.data.remote.dto.SocketMessageDto
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

sealed interface SocketEvent {
    data object Opened : SocketEvent
    data class Message(val data: SocketMessageDto) : SocketEvent
    data class Closed(val code: Int, val reason: String) : SocketEvent
    data class Failed(val error: Throwable) : SocketEvent
}

class TraccarSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
) {
    /**
     * WebSocket-specific client with a 25-second ping interval.
     * Keeps connection alive across mobile NAT timeouts (30-60 s)
     * and detects dead sockets within seconds instead of waiting
     * for TCP keepalive timeout (2+ hours).
     */
    private val wsClient: OkHttpClient by lazy {
        okHttpClient.newBuilder()
            .pingInterval(25, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    fun connect(): Flow<SocketEvent> = callbackFlow {
        val request = Request.Builder()
            .url(com.example.uzradyab.BuildConfig.SOCKET_BASE_URL)
            .build()
        val socket = wsClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (BuildConfig.DEBUG) Log.d("TraccarSocket", "WebSocket Opened")
                    trySend(SocketEvent.Opened)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    if (BuildConfig.DEBUG) Log.d("TraccarSocket", "WebSocket Message Received: ${text.take(200)}...")
                    runCatching {
                        gson.fromJson(text, SocketMessageDto::class.java)
                    }.onSuccess { 
                        trySend(SocketEvent.Message(it)) 
                    }.onFailure {
                        if (BuildConfig.DEBUG) Log.e("TraccarSocket", "Failed to parse message", it)
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (BuildConfig.DEBUG) Log.d("TraccarSocket", "WebSocket Closed: $code / $reason")
                    trySend(SocketEvent.Closed(code, reason))
                    close()
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (BuildConfig.DEBUG) Log.e("TraccarSocket", "WebSocket Failed", t)
                    trySend(SocketEvent.Failed(t))
                    close(t)
                }
            },
        )
        awaitClose { socket.close(4000, "client stopped") }
    }
}
