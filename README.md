📱 Aplikasi RecipeBox Android dengan Firebase  
Aplikasi ini merupakan aplikasi mobile Android sederhana yang dibuat untuk memenuhi Ujian Akhir Praktikum mata kuliah Pengembangan Aplikasi Mobile. RecipeBox dirancang untuk membantu pengguna mengelola resep masak pribadi dengan mudah dan efisien.

✨ Fitur Utama

**Autentikasi**  
Proses login menggunakan Firebase Authentication untuk memastikan keamanan dan privasi data resep pengguna.

**Penyimpanan Data**  
Data resep disimpan secara realtime menggunakan Firebase Realtime Database. Gambar resep dikonversi menjadi string menggunakan Base64 Encoding agar dapat disimpan di dalam database.

**Tampilan Data**  
Daftar resep ditampilkan dalam bentuk daftar menggunakan RecyclerView, lengkap dengan nama resep dan durasi memasak. Gambar resep dimuat menggunakan Glide Library agar proses loading gambar lebih cepat dan efisien.

**CRUD Data Resep**  
Aplikasi mendukung Create, Read, Update, dan Delete untuk data resep. Pengguna dapat menambahkan resep baru, melihat detail resep, memperbarui informasi resep, dan menghapus resep yang tidak lagi diperlukan.

---

🎯 Tujuan  
Membantu pengguna menyimpan dan mengelola resep pribadi secara digital agar lebih praktis dan terorganisir.

📌 Teknologi yang Digunakan  
- Android (Java)  
- Firebase Authentication  
- Firebase Realtime Database  
- Glide Library  
- Base64 Encoding (untuk konversi gambar)  
- RecyclerView
