# 🌸 FragrancePOS

**FragrancePOS** adalah Sistem Manajemen Stok & Penjualan (Point of Sale) khusus untuk butik parfum, dirancang dengan antarmuka modern (Noir Luxe Theme) dan arsitektur keamanan tingkat tinggi (*Multi-tier Security*). 
## ✨ Fitur Utama
* **Multi-tier Security:** Autentikasi berlapis dengan enkripsi *password* menggunakan algoritma SHA-256.
* **Role-Based Access Control (RBAC):** Pembatasan hak akses sistem berdasarkan *role* pengguna (Admin, Operator, User).
* **Modern User Interface:** Antarmuka elegan dan responsif menggunakan *library* **FlatLaf** (Flat Dark Look and Feel) dengan palet warna khusus *Noir Luxe*.
* **Standardized Architecture:** Mengimplementasikan pola desain MVC (Model-View-Controller) dan DAO (Data Access Object) untuk pemisahan logika bisnis dan antarmuka.

## 🛠️ Teknologi yang Digunakan
* **Bahasa Pemrograman:** Java (JDK)
* **GUI Framework:** Java Swing
* **Database:** MySQL
* **Driver:** MySQL Connector/J
* **UI Theme:** [FlatLaf](https://www.formdev.com/flatlaf/)

## 📂 Struktur Direktori (Packages)
Proyek ini mengadopsi standar industri dengan pembagian *package* sebagai berikut:
* `com.fragrance.main` : *Entry point* aplikasi (`App.java`).
* `com.fragrance.ui` : Kumpulan komponen *form* visual (contoh: `LoginFrame.java`).
* `com.fragrance.util` : Kelas utilitas / alat bantu (Koneksi DB, Security Hash, Session, Theme Config).
* `com.fragrance.model` : Kelas representasi entitas tabel *database*.
* `com.fragrance.dao` : Pusat eksekusi *query* SQL untuk manipulasi data (CRUD).

## 🚀 Cara Menjalankan Aplikasi
1. Lakukan *clone* repositori ini ke komputer lokal.
2. Pastikan XAMPP (Apache & MySQL) sudah berjalan.
3. Buat *database* baru bernama `db_fragrance` di phpMyAdmin.
4. *Import* atau *run script* SQL yang telah disediakan untuk membangun struktur tabel dan data *dummy*.
5. Pastikan *library* `mysql-connector-j` dan `flatlaf` sudah didaftarkan pada *Referenced Libraries* di IDE (VS Code/Eclipse/NetBeans).
6. Jalankan file `App.java` yang berada di dalam *package* `com.fragrance.main`.

---
