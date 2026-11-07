package com.example.aurorafilemanager.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val latestVersionTag: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean
)

object UpdateChecker {

    private const val GITHUB_OWNER = "Computer-genius1"
    private const val GITHUB_REPO = "AuroraFileManager"

    suspend fun checkForUpdates(currentVersionName: String): AppUpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // First try latest release endpoint, fallback to all releases list if 404
                var json = fetchJsonObject("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
                if (json == null) {
                    val jsonArray = fetchJsonArray("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases")
                    if (jsonArray != null && jsonArray.length() > 0) {
                        json = jsonArray.getJSONObject(0)
                    }
                }

                if (json != null) {
                    val tagName = json.optString("tag_name", "")
                    val body = json.optString("body", "New release available on GitHub.")
                    val htmlUrl = json.optString("html_url", "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases")

                    var apkDownloadUrl = htmlUrl
                    val assets = json.optJSONArray("assets")
                    if (assets != null && assets.length() > 0) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = asset.optString("browser_download_url", htmlUrl)
                                break
                            }
                        }
                    }

                    val isPrerelease = json.optBoolean("prerelease", false)
                    val lowerTag = tagName.lowercase()

                    // Only filter if explicitly marked as pre-release or contains explicit beta/alpha/rc keywords
                    val isBetaOrPreRelease = isPrerelease ||
                            lowerTag.contains("beta") ||
                            lowerTag.contains("alpha") ||
                            lowerTag.contains("-rc") ||
                            lowerTag.contains("pre-release") ||
                            lowerTag.contains("prerelease")

                    val updateAvailable = !isBetaOrPreRelease && isNewerVersion(currentVersionName, tagName)

                    AppUpdateInfo(
                        latestVersionTag = tagName,
                        releaseNotes = body,
                        downloadUrl = apkDownloadUrl,
                        isUpdateAvailable = updateAvailable
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = current.lowercase().replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }
        val cleanLatest = latest.lowercase().replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }

        if (cleanCurrent.isEmpty() || cleanLatest.isEmpty()) {
            return latest.trim().lowercase() != current.trim().lowercase()
        }

        val maxLen = maxOf(cleanCurrent.size, cleanLatest.size)
        for (i in 0 until maxLen) {
            val currPart = cleanCurrent.getOrElse(i) { 0 }
            val latestPart = cleanLatest.getOrElse(i) { 0 }
            if (latestPart > currPart) return true
            if (latestPart < currPart) return false
        }
        return false
    }

    private fun fetchJsonObject(urlString: String): JSONObject? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "AuroraFileManager-App")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().readText()
                JSONObject(jsonStr)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchJsonArray(urlString: String): JSONArray? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "AuroraFileManager-App")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().readText()
                JSONArray(jsonStr)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}