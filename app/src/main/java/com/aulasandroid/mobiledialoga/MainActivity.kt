package com.aulasandroid.mobiledialoga

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.aulasandroid.mobiledialoga.core.notification.NotificationHelper
import com.aulasandroid.mobiledialoga.data.AuthProvider
import com.aulasandroid.mobiledialoga.navigation.AppNavigation
import com.aulasandroid.mobiledialoga.ui.theme.MobileDialogaTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (!concedida && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Notificações desativadas")
                .setMessage("Você pode permitir os avisos do Dialoga nas configurações do aparelho.")
                .setPositiveButton("Abrir configurações") { _, _ ->
                    startActivity(android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, packageName)
                    })
                }
                .setNegativeButton("Agora não", null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cria o canal de notificações locais
        NotificationHelper.criarCanal(this)

        // Solicita permissão de notificação no Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                val preferencias = getSharedPreferences("permissoes", MODE_PRIVATE)
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Receber notificações")
                        .setMessage("Permita as notificações para receber avisos de consultas, tarefas e solicitações.")
                        .setPositiveButton("Continuar") { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("Agora não", null)
                        .show()
                } else if (!preferencias.getBoolean("notificacoes_solicitadas", false)) {
                    preferencias.edit().putBoolean("notificacoes_solicitadas", true).apply()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContent {
            MobileDialogaTheme {
                AuthProvider {
                    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            AppNavigation()
                        }
                    }
                }
            }
        }
    }
}
