package xyz.junerver.composehooks.net.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    val login: String,
    val name: String,
    val avatar_url: String,
    val bio: String,
    val blog: String,
    val company: String? = null,
    val created_at: String,
    val email: String? = null,
    val events_url: String,
    val followers: Int,
    val followers_url: String,
    val following: Int,
    val following_url: String,
    val gists_url: String,
    val gravatar_id: String,
    val hireable: String? = null,
    val html_url: String,
    val id: Int,
    val location: String? = null,
    val node_id: String,
    val organizations_url: String,
    val public_gists: Int,
    val public_repos: Int,
    val received_events_url: String,
    val repos_url: String,
    val site_admin: Boolean,
    val starred_url: String,
    val subscriptions_url: String,
    val twitter_username: String? = null,
    val type: String,
    val updated_at: String,
    val url: String,
) : Parcelable
