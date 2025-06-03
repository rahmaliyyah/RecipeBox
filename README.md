<h1>📱 Aplikasi RecipeBox Android dengan Firebase</h1>

<p>
Aplikasi ini merupakan aplikasi mobile Android sederhana yang dibuat untuk memenuhi <strong>Ujian Akhir Praktikum</strong> mata kuliah <em>Pengembangan Aplikasi Mobile</em>. <strong>RecipeBox</strong> dirancang untuk membantu pengguna mengelola resep masak pribadi dengan mudah dan efisien.
</p>

<h2>✨ Fitur Utama</h2>

<ul>
  <li><strong>Autentikasi</strong><br>
  Proses login menggunakan <em>Firebase Authentication</em> untuk memastikan keamanan dan privasi data resep pengguna.</li>

  <li><strong>Penyimpanan Data</strong><br>
  Data resep disimpan secara realtime menggunakan <em>Firebase Realtime Database</em>. Gambar resep dikonversi menjadi string menggunakan <strong>Base64 Encoding</strong> agar dapat disimpan di dalam database.</li>

  <li><strong>Tampilan Data</strong><br>
  Daftar resep ditampilkan dalam bentuk daftar menggunakan <em>RecyclerView</em>, lengkap dengan nama resep dan durasi memasak. Gambar resep dimuat menggunakan <strong>Glide Library</strong> agar proses loading gambar lebih cepat dan efisien.</li>

  <li><strong>CRUD Data Resep</strong><br>
  Aplikasi mendukung <em>Create, Read, Update, dan Delete</em> untuk data resep. Pengguna dapat menambahkan resep baru, melihat detail resep, memperbarui informasi resep, dan menghapus resep yang tidak lagi diperlukan.</li>
</ul>

<hr>

<h2>🎯 Tujuan</h2>
<p>
Membantu pengguna menyimpan dan mengelola resep pribadi secara digital agar lebih praktis dan terorganisir.
</p>

<h2>📌 Teknologi yang Digunakan</h2>
<ul>
  <li>Android (Java)</li>
  <li>Firebase Authentication</li>
  <li>Firebase Realtime Database</li>
  <li>Glide Library</li>
  <li>Base64 Encoding (untuk konversi gambar)</li>
  <li>RecyclerView</li>
</ul>


