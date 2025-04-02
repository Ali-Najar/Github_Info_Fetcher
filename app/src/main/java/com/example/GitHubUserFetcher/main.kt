package com.example.GitHubUserFetcher
import kotlinx.coroutines.runBlocking

import java.util.Scanner

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import com.google.gson.annotations.SerializedName

val red = "\u001B[31m"
val green = "\u001B[32m"
val yellow = "\u001B[33m"
val blue = "\u001B[34m"
val reset = "\u001B[0m"
val cyan = "\u001B[36m"

data class GitHubUser(
    val login: String,
    @SerializedName("followers") val followersCount: Int,
    @SerializedName("following") val followingCount: Int,
    @SerializedName("created_at") val createdAt: String,
    val public_repos: Int
)

data class Repository(
    val name: String,
    val description: String?,
    val language: String?,
    val html_url: String
)

interface GitHubApiService {
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): GitHubUser

    @GET("users/{username}/repos")
    suspend fun getUserRepositories(@Path("username") username: String): List<Repository>
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.github.com/"

    val api: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }
}

object CacheManager {
    private val userCache = mutableMapOf<String, GitHubUser>()
    private val repoCache = mutableMapOf<String, List<Repository>>()

    fun getUser(username: String): GitHubUser? = userCache[username]
    fun saveUser(username: String, user: GitHubUser) {
        userCache[username] = user
    }

    fun getRepos(username: String): List<Repository>? = repoCache[username]
    fun saveRepos(username: String, repos: List<Repository>) {
        repoCache[username] = repos
    }

    fun getAllUsers(): List<GitHubUser> = userCache.values.toList()
}


class GitHubService {
    private val api = RetrofitInstance.api

    fun fetchUser(username: String): GitHubUser? = runBlocking {
        // Check if user data is already cached
        CacheManager.getUser(username.lowercase())?.let {
            println("$green Data retrieved from cache!$reset")
            return@runBlocking it
        }

        // Fetch user data from GitHub API
        return@runBlocking try {
            val user = api.getUser(username)
            CacheManager.saveUser(username.lowercase(), user)
            println("$green Data retrieved from API and cached.$reset")
            user
        } catch (e: Exception) {
            println("$red Error fetching user data: ${e.message}$reset")
            null
        }
    }

    fun fetchUserRepositories(username: String): List<Repository>? = runBlocking {
        // Check if repositories are already cached
        CacheManager.getRepos(username.lowercase())?.let {
            println("$green Repositories retrieved from cache!$reset")
            return@runBlocking it
        }

        // Fetch repositories from GitHub API
        return@runBlocking try {
            val repos = api.getUserRepositories(username)
            CacheManager.saveRepos(username.lowercase(), repos)
            println("$green Repositories retrieved from API and cached.$reset")
            repos
        } catch (e: Exception) {
            println("$red Error fetching repositories: ${e.message}$reset")
            null
        }
    }
}


fun main() {
    val scanner = Scanner(System.`in`)
    val gitHubService = GitHubService()

    while (true) {
        println("$yellow\n Program Menu:")
        println(" 1. Fetch user information by username")
        println(" 2. Display list of cached users")
        println(" 3. Search for a user in cache by username")
        println(" 4. Search repositories in cache by repository name")
        println(" 5. Exit the program")
        print(" >> Choose an option: $reset")

        when (scanner.nextInt()) {
            1 -> {
                print("$yellow Enter username: $reset")
                val username = scanner.next()
                val user = gitHubService.fetchUser(username)
                val repos = gitHubService.fetchUserRepositories(username)
                user?.let {
                    println("$yellow\n User Information:$reset")
                    println("$cyan Username: ${it.login}")
                    println(" Followers: ${it.followersCount}")
                    println(" Following: ${it.followingCount}")
                    println(" Account Created: ${it.createdAt}")
                    println(" Public Repositories: ${it.public_repos}$reset")
                    if (repos != null) {
                        for (repo in repos) {
                            println("$cyan${repo}$reset")
                        }
                    }
                }
            }
            2 -> {
                println("$yellow\n Cached Users:$reset")
                CacheManager.getAllUsers().forEach {
                    println("$cyan ${it.login}$reset")
                }
            }
            3 -> {
                print("$yellow Enter username to search: $reset")
                val username = scanner.next()
                val user = CacheManager.getUser(username.lowercase())
                val repos = CacheManager.getRepos(username.lowercase())
                if (user != null) {
                    println("$cyan Username: ${user.login}")
                    println(" Followers: ${user.followersCount}")
                    println(" Following: ${user.followingCount}")
                    println(" Account Created: ${user.createdAt}")
                    println(" Public Repositories: ${user.public_repos}$reset")
                    if (repos != null) {
                        for (repo in repos) {
                            println("$cyan${repo}$reset")
                        }
                    }
                } else {
                    println("$red User not found in cache.$reset")
                }
            }
            4 -> {
                print("$yellow Enter repository name to search: $reset")
                val repoName = scanner.next()
                val foundRepos = CacheManager.getAllUsers()
                    .flatMap { CacheManager.getRepos(it.login.lowercase()) ?: listOf() }
                    .filter { it.name.contains(repoName, ignoreCase = true) }

                if (foundRepos.isNotEmpty()) {
                    println("$yellow Found Repositories:$reset")
                    foundRepos.forEach { println("$cyan ${it.name} - ${it.html_url}$reset") }
                } else {
                    println("$red No repositories found.$reset")
                }
            }
            5 -> {
                println("$yellow Exiting the program...$reset")
                break
            }
            else -> println("️$red Invalid option. Please try again.$reset")
        }
    }
}