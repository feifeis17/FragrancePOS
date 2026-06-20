# FragrancePOS

**FragrancePOS** adalah Sistem Manajemen Stok & Penjualan (Point of Sale) khusus untuk butik parfum. Aplikasi ini dirancang dengan antarmuka modern (Noir Luxe Theme) dan integrasi perangkat keras kasir sungguhan.

## ✨ Fitur Utama
* **Smart Point of Sales (POS):** Sistem kasir terintegrasi dengan deteksi *Barcode Scanner* untuk proses *checkout* secepat kilat.
* **Dual Invoice System:** Mendukung cetak langsung ke mesin pencetak **Struk Thermal (58mm)** dan *export* dokumen ke format **Invoice PDF (Kertas A4)**.
* **Automated Email Gateway:** Fitur pengiriman otomatis *Invoice* PDF langsung ke *inbox* email pelanggan setelah transaksi selesai.
* **Customer Display (Kiosk Mode):** Mode layar khusus untuk pembeli berupa Katalog Digital interaktif dengan filter kategori (Lokal, Niche, Designer, Timur Tengah) dan animasi *Price Checker* via Scanner.
* **Business Analytics Dashboard:** Visualisasi data omzet, profit, dan tren penjualan menggunakan grafik garis (*Line Chart*), batang (*Bar Chart*), dan *Pie Chart*.
* **Multi-tier Security & RBAC:** Autentikasi berlapis dengan enkripsi sandi SHA-256. Hak akses dinamis untuk 3 level pengguna:
  * **Admin:** Akses penuh ke dapur keuangan, manajemen karyawan, dan master data.
  * **Operator (Kasir):** Akses khusus ke modul transaksi, cetak struk, dan penambahan member baru.
  * **User (Display):** Akses terbatas *read-only* untuk mode Kios/Katalog Pembeli.
* **Modern User Interface:** Antarmuka elegan dan responsif menggunakan *library* **FlatLaf** (Flat Dark Look and Feel) dengan palet warna kustom *Noir Luxe*.

## 🛠️ Teknologi yang Digunakan
* **Bahasa Pemrograman:** Java (JDK)
* **GUI Framework:** Java Swing
* **Database:** MySQL
* **UI Theme:** [FlatLaf](https://www.formdev.com/flatlaf/)
* **Database Driver:** MySQL Connector/J
* **PDF Generator:** iText (Lowagie)
* **Email Service:** JavaMail API & Activation Framework
* **Data Visualization:** JFreeChart & JCommon

## 📂 Struktur Direktori (Packages)
Proyek ini mengadopsi standar industri dengan pembagian *package* (Arsitektur MVC & DAO) sebagai berikut:
* `com.fragrance.main` : *Entry point* aplikasi (`App.java`).
* `com.fragrance.ui` : Kumpulan *form* visual kerangka utama (contoh: `LoginFrame.java`, `MainFrame.java`).
* `com.fragrance.panel` : Modul-modul menu fungsional aplikasi (Kasir, Katalog, Laporan Bisnis, dll).
* `com.fragrance.util` : Kelas utilitas (Koneksi DB, Security Hash, Session, Theme Config, Email Config).
* `com.fragrance.model` : Kelas representasi entitas tabel *database*.
* `com.fragrance.dao` : Pusat eksekusi *query* SQL untuk manipulasi data (CRUD).
* `com.fragrance.resources` : Direktori penyimpanan aset lokal (*Icons*, Animasi GIF, dan Foto Produk).

## 🚀 Cara Menjalankan Aplikasi
1. Lakukan *clone* atau ekstrak repositori ini ke komputer lokal.
2. Pastikan server lokal XAMPP (Apache & MySQL) sudah berjalan.
3. Buat *database* baru bernama `db_fragrance` di phpMyAdmin, lalu *Import* file `.sql` yang telah disediakan.
4. **PENTING (Konfigurasi Email):** Karena alasan keamanan kredensial, buatlah satu file baru bernama `AppConfig.java` di dalam folder `src/com/fragrance/util/` dan isi dengan kode berikut:
   ```java
   package com.fragrance.util;
   public class AppConfig {
       public static final String EMAIL_PENGIRIM = "email_gmail_anda@gmail.com";
       public static final String EMAIL_PASSWORD = "sandi_aplikasi_google_anda"; // 16-digit App Password
   }
