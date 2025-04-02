# 👥 GitHub User Info Fetcher (Kotlin Terminal App)

This is a Kotlin terminal-based application that fetches and displays GitHub user information using the GitHub REST API. It also uses in-memory caching to avoid repeated API calls and improve performance.

---

## 📌 Features

- 🔍 Fetch GitHub user info by username
- 📦 View list of cached users
- 🧑‍💻 Search for users in cache
- 📁 Search public repositories from cached data
- 🚀 Uses Retrofit and Gson to communicate with GitHub API
- 🧠 Smart caching system to avoid duplicate requests
- ✅ Graceful error handling with helpful messages

---

## 🧪 Sample Menu

```text
📌 Program Menu:
1️⃣ Fetch user information by username
2️⃣ Display list of cached users
3️⃣ Search for a user in cache by username
4️⃣ Search repositories in cache by repository name
5️⃣ Exit the program
