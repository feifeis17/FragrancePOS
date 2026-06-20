package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;

public class DisplayProdukPanel extends JPanel {

    private JTextField txtSearchBarcode;
    private JPanel katalogContainer;
    private String activeCategory = "Semua";

    public DisplayProdukPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(ThemeConfig.BG_PRIMARY);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        loadKatalog(""); 
    }

    private void initUI() {
        // --- 1. HEADER (SEARCH, SCAN & FILTER AREA) ---
        RoundedPanel headerPanel = new RoundedPanel(16, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Baris Atas: Judul & Pencarian
        JPanel topRow = new JPanel(new BorderLayout(15, 0));
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Katalog Digital Decium");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeConfig.ACCENT);

        txtSearchBarcode = new JTextField();
        txtSearchBarcode.setPreferredSize(new Dimension(350, 45));
        txtSearchBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtSearchBarcode.setBackground(ThemeConfig.BG_PRIMARY);
        txtSearchBarcode.setForeground(ThemeConfig.TEXT_HEAD);
        txtSearchBarcode.setCaretColor(ThemeConfig.ACCENT);
        txtSearchBarcode.putClientProperty("JTextField.placeholderText", "Ketik Nama atau Scan Barcode...");
        txtSearchBarcode.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        txtSearchBarcode.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadKatalog(txtSearchBarcode.getText()); }
            public void removeUpdate(DocumentEvent e) { loadKatalog(txtSearchBarcode.getText()); }
            public void changedUpdate(DocumentEvent e) { loadKatalog(txtSearchBarcode.getText()); }
        });

        txtSearchBarcode.addActionListener(e -> {
            String keyword = txtSearchBarcode.getText().trim();
            if (!keyword.isEmpty()) {
                cekHargaByScan(keyword);
                txtSearchBarcode.setText(""); 
                txtSearchBarcode.requestFocusInWindow(); 
            }
        });

        topRow.add(lblTitle, BorderLayout.WEST);
        topRow.add(txtSearchBarcode, BorderLayout.EAST);

        // Baris Bawah: Tombol Filter Kategori
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] categories = {"Semua", "Lokal", "Niche", "Designer", "Timur Tengah"};
        ButtonGroup btnGroup = new ButtonGroup();
        
        for (String cat : categories) {
            JToggleButton btnCat = new JToggleButton(cat);
            btnCat.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCat.setFocusPainted(false);
            btnCat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCat.setBackground(ThemeConfig.BG_PRIMARY);
            btnCat.setForeground(ThemeConfig.TEXT_BODY);
            btnCat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));

            if (cat.equals("Semua")) btnCat.setSelected(true);

            // Logika Hover dan Klik (Merubah Warna & Memuat Data)
            btnCat.addActionListener(e -> {
                activeCategory = cat;
                loadKatalog(txtSearchBarcode.getText());
            });

            btnCat.addChangeListener(e -> {
                if (btnCat.isSelected()) {
                    btnCat.setBackground(ThemeConfig.ACCENT);
                    btnCat.setForeground(ThemeConfig.BG_PRIMARY);
                } else {
                    btnCat.setBackground(ThemeConfig.BG_PRIMARY);
                    btnCat.setForeground(ThemeConfig.TEXT_BODY);
                }
            });

            btnGroup.add(btnCat);
            filterRow.add(btnCat);
        }

        headerPanel.add(topRow);
        headerPanel.add(filterRow);

        // --- 2. KATALOG CONTAINER (GRID FOTO) ---
        katalogContainer = new JPanel();
        katalogContainer.setLayout(new GridLayout(0, 4, 20, 20)); 
        katalogContainer.setBackground(ThemeConfig.BG_PRIMARY);
        
        JScrollPane scrollKatalog = new JScrollPane(katalogContainer);
        scrollKatalog.setBorder(null);
        scrollKatalog.getViewport().setBackground(ThemeConfig.BG_PRIMARY);
        scrollKatalog.getVerticalScrollBar().setUnitIncrement(20);
        scrollKatalog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollKatalog, BorderLayout.CENTER);
    }

    private void loadKatalog(String keyword) {
        katalogContainer.removeAll();
        
        // Logika SQL Dinamis berdasarkan Kategori (Join ke tb_kategori)
        String sql = "SELECT p.id_produk, p.nama_produk, p.brand, p.harga_jual, p.stok " +
                     "FROM tb_produk p " +
                     "LEFT JOIN tb_kategori k ON p.id_kategori = k.id_kategori " +
                     "WHERE p.nama_produk LIKE ? ";
        
        if (!activeCategory.equals("Semua")) {
            sql += "AND k.nama_kategori = ? ";
        }
        sql += "ORDER BY p.nama_produk";

        try (Connection conn = Koneksi.configDB(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.trim() + "%");
            if (!activeCategory.equals("Semua")) {
                ps.setString(2, activeCategory);
            }
            
            ResultSet rs = ps.executeQuery();
            NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            
            while (rs.next()) {
                int id = rs.getInt("id_produk");
                String nama = rs.getString("nama_produk");
                String brand = rs.getString("brand");
                double harga = rs.getDouble("harga_jual");
                int stok = rs.getInt("stok");
                
                katalogContainer.add(createDisplayCard(id, nama, brand, rp.format(harga).replace(",00", ""), harga, stok));
            }
            katalogContainer.revalidate();
            katalogContainer.repaint();
        } catch (Exception e) {}
    }

    private JPanel createDisplayCard(int id, String nama, String brand, String hargaStr, double hargaReal, int stok) {
        RoundedPanel card = new RoundedPanel(20, ThemeConfig.BG_CARD, new Color(0x3D, 0x3B, 0x60));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 15, 20, 15)); 
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel lblImg = new JLabel();
        lblImg.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        
        try {
            File imgFile = new File("src/com/fragrance/resources/images/products/" + id + ".jpg");
            if (imgFile.exists()) {
                java.awt.image.BufferedImage origImg = javax.imageio.ImageIO.read(imgFile);
                int origW = origImg.getWidth(); int origH = origImg.getHeight();
                int newW = 140, newH = 140; 
                if (origW > origH) newH = (origH * 140) / origW;
                else newW = (origW * 140) / origH;
                Image img = origImg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
            } else {
                lblImg.setText("🌸");
                lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
            }
        } catch (Exception e) {
            lblImg.setText("🌸");
            lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        }

        JLabel lblBrand = new JLabel(brand.toUpperCase());
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblBrand.setForeground(ThemeConfig.TEXT_MUTED);

        JLabel lblNama = new JLabel("<html><div style='width: 160px; text-align: center;'>" + nama + "</div></html>");
        lblNama.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNama.setHorizontalAlignment(SwingConstants.CENTER);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNama.setForeground(ThemeConfig.TEXT_HEAD);
        
        JLabel lblHarga = new JLabel(hargaStr);
        lblHarga.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHarga.setForeground(ThemeConfig.ACCENT);

        JLabel lblStok = new JLabel(stok > 0 ? "Tersedia" : "Habis");
        lblStok.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStok.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStok.setForeground(stok > 0 ? ThemeConfig.SUCCESS : ThemeConfig.DANGER);

        content.add(lblImg);
        content.add(Box.createVerticalStrut(20));
        content.add(lblBrand);
        content.add(Box.createVerticalStrut(5));
        content.add(lblNama);
        content.add(Box.createVerticalStrut(10));
        content.add(lblHarga);
        content.add(Box.createVerticalStrut(10));
        content.add(lblStok);

        card.add(content, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(0x3A, 0x38, 0x60)); }
            public void mouseExited(MouseEvent e) { card.setBackground(ThemeConfig.BG_CARD); }
            public void mouseClicked(MouseEvent e) { 
                showPriceCheckerDialog(id, nama, brand, hargaReal, stok, lblImg.getIcon()); 
            }
        });
        return card;
    }

    private void cekHargaByScan(String keyword) {
        String sql = "SELECT id_produk, nama_produk, brand, harga_jual, stok FROM tb_produk WHERE id_produk = ? OR nama_produk LIKE ? LIMIT 1";
        try (Connection conn = Koneksi.configDB(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try { ps.setInt(1, Integer.parseInt(keyword)); } catch(Exception ex) { ps.setInt(1, -1); }
            ps.setString(2, keyword + "%");
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id_produk");
                
                Icon productIcon = null;
                File imgFile = new File("src/com/fragrance/resources/images/products/" + id + ".jpg");
                if (imgFile.exists()) {
                    java.awt.image.BufferedImage origImg = javax.imageio.ImageIO.read(imgFile);
                    int origW = origImg.getWidth(); int origH = origImg.getHeight();
                    int newW = 200, newH = 200; 
                    if (origW > origH) newH = (origH * 200) / origW;
                    else newW = (origW * 200) / origH;
                    productIcon = new ImageIcon(origImg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH));
                }
                
                showPriceCheckerDialog(id, rs.getString("nama_produk"), rs.getString("brand"), rs.getDouble("harga_jual"), rs.getInt("stok"), productIcon);
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, "Barang tidak ditemukan!", "Peringatan Scanner", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {}
    }

    private void showPriceCheckerDialog(int id, String nama, String brand, double harga, int stok, Icon imgIcon) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Cek Harga", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true); 
        dialog.setBackground(new Color(0,0,0,0)); 

        RoundedPanel panel = new RoundedPanel(24, ThemeConfig.BG_CARD, ThemeConfig.ACCENT);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(ThemeConfig.ACCENT, 2)); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel lblHeader = new JLabel("PRICE CHECKER");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(ThemeConfig.ACCENT);
        panel.add(lblHeader, gbc);

        gbc.gridy++;
        JLabel lblImg = new JLabel();
        if (imgIcon != null) {
            lblImg.setIcon(imgIcon);
        } else {
            lblImg.setText("🌸");
            lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 100));
        }
        panel.add(lblImg, gbc);

        gbc.gridy++;
        JLabel lblBrand = new JLabel(brand.toUpperCase());
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBrand.setForeground(ThemeConfig.TEXT_MUTED);
        panel.add(lblBrand, gbc);

        gbc.gridy++; gbc.insets = new Insets(0, 10, 15, 10);
        JLabel lblNama = new JLabel("<html><div style='text-align: center;'>" + nama + "</div></html>");
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblNama.setForeground(ThemeConfig.TEXT_HEAD);
        panel.add(lblNama, gbc);

        gbc.gridy++;
        NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        JLabel lblHarga = new JLabel(rp.format(harga).replace(",00", ""));
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblHarga.setForeground(ThemeConfig.ACCENT);
        panel.add(lblHarga, gbc);

        gbc.gridy++; gbc.insets = new Insets(20, 0, 10, 0);
        JButton btnTutup = new JButton("Tutup (Atau Scan Lagi)");
        btnTutup.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTutup.setForeground(ThemeConfig.BG_PRIMARY);
        btnTutup.setBackground(ThemeConfig.ACCENT);
        btnTutup.setFocusPainted(false);
        btnTutup.setBorder(new EmptyBorder(10, 30, 10, 30));
        btnTutup.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTutup.addActionListener(e -> dialog.dispose());
        panel.add(btnTutup, gbc);

        dialog.setContentPane(panel);
        
        Timer timer = new Timer(5000, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();

        dialog.setVisible(true);
    }
}