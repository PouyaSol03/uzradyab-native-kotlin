package com.example.uzradyab

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.example.uzradyab.ui.theme.UzradyabTheme
import androidx.fragment.app.FragmentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.uzradyab.core.biometric.BiometricHelper
import com.example.uzradyab.core.network.SessionEventBus
import com.example.uzradyab.domain.manager.FcmTokenManager
import com.example.uzradyab.domain.repository.AuthRepository
import com.example.uzradyab.domain.repository.AppConfigRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var biometricHelper: BiometricHelper

    @Inject
    lateinit var sessionEventBus: SessionEventBus

    @Inject
    lateinit var networkEventBus: com.example.uzradyab.core.network.NetworkEventBus

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var appConfigRepository: AppConfigRepository

    @Inject
    lateinit var themeRepository: com.example.uzradyab.domain.repository.ThemeRepository

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("FCM_TOKEN", "MainActivity OnCreate FCM Token: $token")
                
                lifecycleScope.launch {
                    val session = authRepository.currentSession.firstOrNull()
                    if (session != null && token != null) {
                        android.util.Log.d("FCM_SYNC", "User is logged in (UserId: ${session.id}) and FCM token exists. Starting sync on launch...")
                        fcmTokenManager.syncToken(token)
                    } else {
                        android.util.Log.e("FCM_SYNC", "Cannot sync FCM on launch: UserId is null (${session == null}) or Token is null (${token == null})")
                    }
                }
            } else {
                android.util.Log.e("FCM_TOKEN", "MainActivity OnCreate FCM Token fetch failed", task.exception)
                android.util.Log.e("FCM_SYNC", "Cannot sync FCM on launch: Token fetch failed")
            }
        }

        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContent {
            UzradyabAppRoot(
                biometricHelper = biometricHelper,
                sessionEventBus = sessionEventBus,
                networkEventBus = networkEventBus,
                authRepository = authRepository,
                appConfigRepository = appConfigRepository,
                themeRepository = themeRepository
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    UzradyabAppRoot()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UzradyabAppRoot(
    biometricHelper: BiometricHelper? = null,
    sessionEventBus: SessionEventBus? = null,
    networkEventBus: com.example.uzradyab.core.network.NetworkEventBus? = null,
    authRepository: AuthRepository? = null,
    appConfigRepository: AppConfigRepository? = null,
    themeRepository: com.example.uzradyab.domain.repository.ThemeRepository? = null
) {
    val themeMode by (themeRepository?.themeMode ?: kotlinx.coroutines.flow.flowOf(com.example.uzradyab.domain.model.ThemeMode.SYSTEM))
        .collectAsStateWithLifecycle(initialValue = com.example.uzradyab.domain.model.ThemeMode.SYSTEM)

    val isDarkTheme = false

    UzradyabTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalIndication provides NoIndication,
            LocalRippleConfiguration provides null,
        ) {
            UzradyabApp(
                biometricHelper = biometricHelper,
                sessionEventBus = sessionEventBus,
                networkEventBus = networkEventBus,
                authRepository = authRepository,
                appConfigRepository = appConfigRepository
            )
        }
    }
}

private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = NoIndicationNode()

    override fun hashCode(): Int = 0

    override fun equals(other: Any?): Boolean = other === this
}

private class NoIndicationNode : Modifier.Node(), DrawModifierNode {
    override fun ContentDrawScope.draw() {
        drawContent()
    }
}
