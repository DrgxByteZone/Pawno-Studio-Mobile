package com.pawno.studio.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.pawno.studio.data.project.ProjectFile
import com.pawno.studio.ui.navigation.PawnoNavGraph
import com.pawno.studio.ui.screens.ide.MainIdeViewModel
import com.pawno.studio.ui.theme.BgRoot
import com.pawno.studio.ui.theme.PawnoStudioTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val ideViewModel: MainIdeViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    private val openFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val resolvedFile = resolveTreeUriToFile(treeUri)
            if (resolvedFile != null && resolvedFile.exists()) {
                ideViewModel.openProjectFolder(resolvedFile)
            } else {
                Log.w(TAG, "Could not resolve physical directory for tree URI: $treeUri")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            handleIntent(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Error handling initial intent", e)
        }

        setContent {
            PawnoStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgRoot
                ) {
                    PawnoNavGraph(
                        ideViewModel = ideViewModel,
                        onOpenFolderPicker = { openFolderPicker() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        try {
            handleIntent(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Error handling new intent", e)
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            val filePath = resolveFilePath(data)
            if (filePath != null) {
                val file = File(filePath)
                if (file.exists() && (file.name.endsWith(".pwn") || file.name.endsWith(".inc"))) {
                    val projectFile = ProjectFile(
                        file = file,
                        name = file.name,
                        relativePath = file.name,
                        absolutePath = file.absolutePath,
                        isGamemode = file.name.endsWith(".pwn"),
                        isInclude = file.name.endsWith(".inc")
                    )
                    ideViewModel.openFile(projectFile)
                }
            }
        }
    }

    private fun resolveFilePath(uri: Uri): String? {
        return try {
            if ("file".equals(uri.scheme, ignoreCase = true)) {
                uri.path
            } else {
                uri.path
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveTreeUriToFile(treeUri: Uri): File? {
        try {
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            if (docId.startsWith("primary:")) {
                val relativePath = docId.removePrefix("primary:")
                val primaryStorage = Environment.getExternalStorageDirectory()
                val target = File(primaryStorage, relativePath)
                if (target.exists()) return target
            }

            // Fallback: Check raw path
            val path = treeUri.path
            if (path != null) {
                if (path.contains("/document/primary:")) {
                    val rel = path.substringAfter("/document/primary:")
                    val target = File(Environment.getExternalStorageDirectory(), rel)
                    if (target.exists()) return target
                } else if (path.contains("/tree/primary:")) {
                    val rel = path.substringAfter("/tree/primary:")
                    val target = File(Environment.getExternalStorageDirectory(), rel)
                    if (target.exists()) return target
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error resolving tree uri to file", e)
        }
        return null
    }

    fun openFolderPicker() {
        requestStoragePermissions()
        openFolderLauncher.launch(null)
    }

    fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(fallback)
                    } catch (e2: Exception) {
                        Log.w(TAG, "Unable to open all files access settings", e2)
                    }
                }
            }
        } else {
            val permissionsToRequest = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
