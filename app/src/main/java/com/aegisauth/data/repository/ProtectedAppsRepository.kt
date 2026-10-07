package com.aegisauth.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.aegisauth.data.local.dao.ProtectedAppDao
import com.aegisauth.data.local.entity.ProtectedApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isProtected: Boolean,
    val isSystemApp: Boolean
)

@Singleton
class ProtectedAppsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val protectedAppDao: ProtectedAppDao
) {
    val protectedAppsFlow: Flow<List<ProtectedApplication>> = protectedAppDao.getAllProtectedApps()

    suspend fun getInstalledApplications(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val myPackage = context.packageName

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val protectedList = protectedAppDao.getAllProtectedApps()

        val appItems = mutableListOf<AppItem>()
        val seenPackages = mutableSetOf<String>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == myPackage || seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val appName = info.loadLabel(pm).toString()
            val icon = try {
                info.loadIcon(pm)
            } catch (e: Exception) {
                null
            }
            val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isProtected = protectedAppDao.isAppProtected(pkg)

            appItems.add(
                AppItem(
                    packageName = pkg,
                    appName = appName,
                    icon = icon,
                    isProtected = isProtected,
                    isSystemApp = isSystem
                )
            )
        }

        appItems.sortedBy { it.appName.lowercase() }
    }

    suspend fun setAppProtection(packageName: String, appName: String, isProtected: Boolean) = withContext(Dispatchers.IO) {
        val myPackage = context.packageName
        // AEGISAUTH itself is permanently protected by default and cannot be disabled via normal UI
        val effectiveProtected = if (packageName == myPackage || packageName == "com.aegisauth") true else isProtected

        val existing = protectedAppDao.getByPackageName(packageName)
        if (existing != null) {
            protectedAppDao.updateProtectionStatus(packageName, effectiveProtected)
        } else {
            protectedAppDao.insertOrUpdate(
                ProtectedApplication(
                    packageName = packageName,
                    appName = appName,
                    isProtected = effectiveProtected
                )
            )
        }
    }

    suspend fun isPackageProtected(packageName: String): Boolean = withContext(Dispatchers.IO) {
        protectedAppDao.isAppProtected(packageName)
    }
}
