package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class PenjualanPanel extends JPanel {

    private JTextField txtSearchBarcode;
    private JPanel katalogContainer;

    private JLabel lblInvoice;
    private JComboBox<String> cmbPelanggan, cmbMetodeBayar;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JTextField txtDiskon, txtBayar;
    private JLabel lblSubtotal, lblTotalAkhir, lblKembalian, lblDiskonPersen;
 
    private DocumentListener calcListener;

    private final Map<String, Integer> pelangganMap = new LinkedHashMap<>();
    private final Map<Integer, Integer> stokMapById = new HashMap<>(); 
    private double totalBelanja = 0;
    private boolean isUpdatingCart = false; 

    public PenjualanPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(ThemeConfig.BG_PRIMARY);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        initUI();
        
        loadPelanggan(); 
        loadProduk(""); 
    }

    private void initUI() {

        JPanel leftPanel = new JPanel(new BorderLayout(0, 16));
        leftPanel.setOpaque(false);

        JPanel searchBarArea = new JPanel(new BorderLayout(10, 0));
        searchBarArea.setOpaque(false);

        txtSearchBarcode = new JTextField();
        txtSearchBarcode.setPreferredSize(new Dimension(0, 42));
        txtSearchBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchBarcode.setBackground(ThemeConfig.BG_CARD);
        txtSearchBarcode.setForeground(ThemeConfig.TEXT_HEAD);
        txtSearchBarcode.setCaretColor(ThemeConfig.ACCENT);
        txtSearchBarcode.putClientProperty("JTextField.placeholderText", "🔍 Cari Nama Produk...");
        txtSearchBarcode.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 14, 4, 14)
        ));

        txtSearchBarcode.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadProduk(txtSearchBarcode.getText()); }
            public void removeUpdate(DocumentEvent e) { loadProduk(txtSearchBarcode.getText()); }
            public void changedUpdate(DocumentEvent e) { loadProduk(txtSearchBarcode.getText()); }
        });

        JPanel btnSearchArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnSearchArea.setOpaque(false);

        JButton btnCari = createOutlineButton("Cari");
        btnCari.setPreferredSize(new Dimension(80, 42));
        btnCari.addActionListener(e -> loadProduk(txtSearchBarcode.getText()));
        
        JButton btnScan = createGoldButton("📷 Scan Barcode");
        btnScan.setPreferredSize(new Dimension(150, 42));
        btnScan.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Scanner Integrasi Hardware akan segera hadir!", "Gimmick POS", JOptionPane.INFORMATION_MESSAGE));

        btnSearchArea.add(btnCari);
        btnSearchArea.add(btnScan);

        searchBarArea.add(txtSearchBarcode, BorderLayout.CENTER);
        searchBarArea.add(btnSearchArea, BorderLayout.EAST);

        katalogContainer = new JPanel(new GridLayout(0, 3, 12, 12));
        katalogContainer.setBackground(ThemeConfig.BG_PRIMARY);
        
        JScrollPane scrollKatalog = new JScrollPane(katalogContainer);
        scrollKatalog.setBorder(null);
        scrollKatalog.getViewport().setBackground(ThemeConfig.BG_PRIMARY);
        scrollKatalog.getVerticalScrollBar().setUnitIncrement(16);
        scrollKatalog.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        leftPanel.add(searchBarArea, BorderLayout.NORTH);
        leftPanel.add(scrollKatalog, BorderLayout.CENTER);

        RoundedPanel rightPanel = new RoundedPanel(16, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        rightPanel.setLayout(new BorderLayout(0, 10));
        rightPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        rightPanel.setPreferredSize(new Dimension(420, 0));

        JPanel cartHeader = new JPanel(new BorderLayout(0, 12));
        cartHeader.setOpaque(false);
        
        JPanel titleInvArea = new JPanel(new BorderLayout());
        titleInvArea.setOpaque(false);
        
        JLabel lblTitleCart = new JLabel("Keranjang Belanja");
        lblTitleCart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitleCart.setForeground(ThemeConfig.TEXT_HEAD);
        
        lblInvoice = new JLabel(generateInvoiceNumber());
        lblInvoice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInvoice.setForeground(ThemeConfig.ACCENT);
        
        titleInvArea.add(lblTitleCart, BorderLayout.WEST);
        titleInvArea.add(lblInvoice, BorderLayout.EAST);
        
        cmbPelanggan = new JComboBox<>();
        cmbPelanggan.setPreferredSize(new Dimension(0, 38));
        cmbPelanggan.setBackground(ThemeConfig.BG_PRIMARY);
        cmbPelanggan.setForeground(ThemeConfig.TEXT_HEAD);
        
        cartHeader.add(titleInvArea, BorderLayout.NORTH);
        cartHeader.add(cmbPelanggan, BorderLayout.CENTER);

        // Tabel Cart
        String[] cols = {"ID", "Produk", "Harga", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; } 
        };
        cartModel.addTableModelListener(e -> handleQtyEdit(e));

        cartTable = new JTable(cartModel);
        styleTable(cartTable);
        
        cartTable.getColumnModel().getColumn(0).setMinWidth(0);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(0);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(160); 
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(40);  

        JScrollPane scrollCart = new JScrollPane(cartTable);
        scrollCart.getViewport().setBackground(ThemeConfig.BG_CARD);
        scrollCart.setBorder(BorderFactory.createLineBorder(new Color(0x2A, 0x28, 0x48)));

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        cartActions.setOpaque(false);
        JButton btnHapusItem = new JButton("Hapus Item");
        btnHapusItem.setForeground(ThemeConfig.DANGER);
        btnHapusItem.setContentAreaFilled(false);
        btnHapusItem.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        btnHapusItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusItem.addActionListener(e -> hapusItemCart());
        cartActions.add(btnHapusItem);

        // Kalkulasi & Pembayaran
        JPanel paymentArea = new JPanel();
        paymentArea.setLayout(new BoxLayout(paymentArea, BoxLayout.Y_AXIS));
        paymentArea.setOpaque(false);

        lblSubtotal   = calcLabel("Rp 0");
        
        JPanel panelDiskon = new JPanel(new BorderLayout(8, 0));
        panelDiskon.setOpaque(false);
        txtDiskon = calcTextField();
        lblDiskonPersen = new JLabel("(0%)");
        lblDiskonPersen.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDiskonPersen.setForeground(new Color(0x81,0xC9,0x95));
        lblDiskonPersen.setPreferredSize(new Dimension(50, 30));
        panelDiskon.add(txtDiskon, BorderLayout.CENTER);
        panelDiskon.add(lblDiskonPersen, BorderLayout.EAST);

        lblTotalAkhir = calcLabel("Rp 0");
        lblTotalAkhir.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalAkhir.setForeground(ThemeConfig.ACCENT);

        cmbMetodeBayar = new JComboBox<>(new String[]{"Tunai (Cash)", "QRIS"});
        cmbMetodeBayar.setPreferredSize(new Dimension(140, 30));
        cmbMetodeBayar.setBackground(ThemeConfig.BG_PRIMARY);
        cmbMetodeBayar.setForeground(ThemeConfig.TEXT_HEAD);
        cmbMetodeBayar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        txtBayar      = calcTextField();
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKembalian  = calcLabel("Rp 0");
        lblKembalian.setForeground(ThemeConfig.SUCCESS);

        cmbMetodeBayar.addActionListener(e -> {
            if (cmbMetodeBayar.getSelectedIndex() == 1) { 
                txtBayar.setEditable(false); 
                txtBayar.setBackground(new Color(0x1E, 0x1D, 0x38)); 
            } else { 
                txtBayar.setEditable(true);
                txtBayar.setBackground(ThemeConfig.BG_PRIMARY);
                if (cartModel.getRowCount() == 0) txtBayar.setText(""); 
            }
            hitungTotalDanKembalian(); 
        });

        paymentArea.add(cartActions);
        paymentArea.add(Box.createVerticalStrut(10));
        paymentArea.add(calcRow("Subtotal:", lblSubtotal));
        paymentArea.add(Box.createVerticalStrut(8));
        paymentArea.add(calcRow("Diskon (Rp):", panelDiskon));
        paymentArea.add(Box.createVerticalStrut(8));
        paymentArea.add(calcRow("Total Akhir:", lblTotalAkhir));
        paymentArea.add(Box.createVerticalStrut(16));
        
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x3D, 0x3B, 0x60));
        paymentArea.add(sep);
        paymentArea.add(Box.createVerticalStrut(16));

        paymentArea.add(calcRow("Metode Bayar:", cmbMetodeBayar));
        paymentArea.add(Box.createVerticalStrut(8));
        paymentArea.add(calcRow("Uang Bayar:", txtBayar));
        paymentArea.add(Box.createVerticalStrut(8));
        paymentArea.add(calcRow("Kembalian:", lblKembalian));
        paymentArea.add(Box.createVerticalStrut(16));

        JButton btnBayar = createGoldButton("PROSES PEMBAYARAN");
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnBayar.setPreferredSize(new Dimension(0, 46));
        btnBayar.addActionListener(e -> processCheckout());
        paymentArea.add(btnBayar);

        rightPanel.add(cartHeader, BorderLayout.NORTH);
        rightPanel.add(scrollCart, BorderLayout.CENTER);
        rightPanel.add(paymentArea, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        calcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { hitungTotalDanKembalian(); }
            public void removeUpdate(DocumentEvent e) { hitungTotalDanKembalian(); }
            public void changedUpdate(DocumentEvent e) { hitungTotalDanKembalian(); }
        };
        txtDiskon.getDocument().addDocumentListener(calcListener);
        txtBayar.getDocument().addDocumentListener(calcListener);
    }
    private void handleQtyEdit(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
            if (isUpdatingCart) return; 
            
            int row = e.getFirstRow();
            isUpdatingCart = true;
            
            try {
                Object val = cartModel.getValueAt(row, 3);
                int newQty = Integer.parseInt(val.toString());
                int idProd = (int) cartModel.getValueAt(row, 0);
                double harga = (double) cartModel.getValueAt(row, 2);
                int stokMaks = stokMapById.getOrDefault(idProd, 0);
                
                if (newQty <= 0) {
                    JOptionPane.showMessageDialog(this, "Qty minimal 1. Jika ingin membatalkan produk, gunakan tombol 'Hapus Item'.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    cartModel.setValueAt(1, row, 3);
                    cartModel.setValueAt(harga, row, 4); 
                } else if (newQty > stokMaks) {
                    JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Maksimal stok yang tersedia: " + stokMaks, "Peringatan", JOptionPane.WARNING_MESSAGE);
                    cartModel.setValueAt(stokMaks, row, 3);
                    cartModel.setValueAt(stokMaks * harga, row, 4); 
                } else {
                    cartModel.setValueAt(newQty * harga, row, 4); 
                }
            } catch (Exception ex) {
                double harga = (double) cartModel.getValueAt(row, 2);
                cartModel.setValueAt(1, row, 3);
                cartModel.setValueAt(harga, row, 4);
            }
            
            isUpdatingCart = false;
            hitungTotalDanKembalian(); 
        }
    }
    private String generateInvoiceNumber() {
        String invoice = "";
        try (Connection conn = Koneksi.configDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id_penjualan) FROM tb_penjualan")) {
            
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            int year = java.time.Year.now().getValue();
            invoice = String.format("%04d/INV/DEC/%d", nextId, year);
            
        } catch (Exception e) {
            invoice = "NEW/INV/DEC/" + java.time.Year.now().getValue();
        }
        return invoice;
    }
    private void loadPelanggan() {
        pelangganMap.clear();
        pelangganMap.put("Umum / Non-Member", 0);
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement().executeQuery("SELECT id_pelanggan, nama_pelanggan, kontak FROM tb_pelanggan ORDER BY nama_pelanggan")) {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("Umum / Non-Member");
            while (rs.next()) {
                String displayNama = rs.getString("nama_pelanggan");
                String kontak = rs.getString("kontak");
                if (kontak != null && !kontak.trim().isEmpty()) {
                    displayNama += " - (" + kontak + ")";
                }
                
                pelangganMap.put(displayNama, rs.getInt("id_pelanggan"));
                model.addElement(displayNama);
            }
            cmbPelanggan.setModel(model);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadProduk(String keyword) {
        katalogContainer.removeAll();
        stokMapById.clear();
        String sql = "SELECT id_produk, nama_produk, brand, harga_jual, stok FROM tb_produk WHERE stok > 0 AND nama_produk LIKE ? ORDER BY nama_produk";
        
        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + keyword.trim() + "%");
            ResultSet rs = ps.executeQuery();
            
            NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            while (rs.next()) {
                int id = rs.getInt("id_produk");
                String nama = rs.getString("nama_produk");
                double harga = rs.getDouble("harga_jual");
                int stok = rs.getInt("stok");
                String hargaStr = rp.format(harga).replace(",00", "");

                stokMapById.put(id, stok); 
                katalogContainer.add(createProductCard(id, nama, hargaStr, harga, stok));
            }
            katalogContainer.revalidate();
            katalogContainer.repaint();
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void tambahKeKeranjang(int idProduk, String nama, double harga, int stokMax) {
        int targetRow = -1;
        int qtyInCart = 0;

        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == idProduk) {
                targetRow = i;
                qtyInCart = (int) cartModel.getValueAt(i, 3);
                break;
            }
        }

        if (qtyInCart + 1 > stokMax) {
            JOptionPane.showMessageDialog(this, "Stok tidak cukup! Maksimal: " + stokMax, "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (targetRow != -1) {
            cartModel.setValueAt(qtyInCart + 1, targetRow, 3);
        } else {
            cartModel.addRow(new Object[]{idProduk, nama, harga, 1, harga});
        }
        hitungTotalDanKembalian();
    }

    private void hapusItemCart() {
        int row = cartTable.getSelectedRow();
        if (row != -1) {
            cartModel.removeRow(row);

            if (cartModel.getRowCount() == 0) {
                SwingUtilities.invokeLater(() -> {
                    txtDiskon.setText("");
                    if (cmbMetodeBayar.getSelectedIndex() == 0) {
                        txtBayar.setText("");
                    }
                });
            }
            
            hitungTotalDanKembalian();
        } else {
            JOptionPane.showMessageDialog(this, "Pilih item di keranjang yang ingin dihapus.");
        }
    }

    private void hitungTotalDanKembalian() {
        if (cartModel.getRowCount() == 0) {
            lblSubtotal.setText("Rp 0");
            lblTotalAkhir.setText("Rp 0");
            lblKembalian.setText("Rp 0");
            lblKembalian.setForeground(ThemeConfig.SUCCESS);
            lblDiskonPersen.setText("(0%)");
            totalBelanja = 0;
            return; 
        }

        double subtotal = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            subtotal += (double) cartModel.getValueAt(i, 4);
        }

        double diskon = 0;
        try {
            if (!txtDiskon.getText().trim().isEmpty()) {
                diskon = Double.parseDouble(txtDiskon.getText().trim());
            }
        } catch (Exception ignored) {}

        if (subtotal > 0 && diskon > 0) {
            double persen = (diskon / subtotal) * 100;
            lblDiskonPersen.setText(String.format("(%.1f%%)", persen));
        } else {
            lblDiskonPersen.setText("(0%)");
        }

        totalBelanja = subtotal - diskon;
        if (totalBelanja < 0) totalBelanja = 0;

        NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        lblSubtotal.setText(rp.format(subtotal).replace(",00", ""));
        lblTotalAkhir.setText(rp.format(totalBelanja).replace(",00", ""));

        if (cmbMetodeBayar.getSelectedIndex() == 1) { 
            if (txtBayar.getDocument() != null) txtBayar.getDocument().removeDocumentListener(calcListener);
            
            txtBayar.setText(String.valueOf((long) totalBelanja));
            
            if (txtBayar.getDocument() != null) txtBayar.getDocument().addDocumentListener(calcListener);
        }

        try {
            if (txtBayar.getText().trim().isEmpty()) {
                lblKembalian.setText("Rp 0");
                lblKembalian.setForeground(ThemeConfig.SUCCESS);
                return;
            }
            double bayar = Double.parseDouble(txtBayar.getText().trim());
            double kembali = bayar - totalBelanja;

            if (kembali < 0) {
                lblKembalian.setText("Kurang " + rp.format(Math.abs(kembali)).replace(",00", ""));
                lblKembalian.setForeground(ThemeConfig.DANGER);
            } else {
                lblKembalian.setText(rp.format(kembali).replace(",00", ""));
                lblKembalian.setForeground(ThemeConfig.SUCCESS);
            }
        } catch (NumberFormatException e) {
            lblKembalian.setText("Input Invalid");
            lblKembalian.setForeground(ThemeConfig.DANGER);
        }
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang belanja masih kosong!");
            return;
        }

        double uangBayar = 0;
        try {
            uangBayar = Double.parseDouble(txtBayar.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah uang bayar yang valid.");
            return;
        }

        if (uangBayar < totalBelanja) {
            JOptionPane.showMessageDialog(this, "Uang pembayaran kurang!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pelKey = (String) cmbPelanggan.getSelectedItem();
        int idPelanggan = pelangganMap.getOrDefault(pelKey, 0);
        int idUser = SessionManager.getUserId();
        String metodeBayar = cmbMetodeBayar.getSelectedItem().toString(); 

        try {
            Connection conn = Koneksi.configDB();
            conn.setAutoCommit(false); 

            try {
                String sqlJual = "INSERT INTO tb_penjualan (tanggal, id_pelanggan, id_user, total_harga, metode_bayar) VALUES (NOW(), ?, ?, ?, ?)";
                PreparedStatement psJual = conn.prepareStatement(sqlJual, Statement.RETURN_GENERATED_KEYS);
                if (idPelanggan == 0) psJual.setNull(1, Types.INTEGER); else psJual.setInt(1, idPelanggan);
                psJual.setInt(2, idUser);
                psJual.setDouble(3, totalBelanja); 
                psJual.setString(4, metodeBayar);
                psJual.executeUpdate();
                
                ResultSet rsKeys = psJual.getGeneratedKeys();
                int idPenjualanBaru = 0;
                if (rsKeys.next()) {
                    idPenjualanBaru = rsKeys.getInt(1);
                }

                PreparedStatement psDetail = conn.prepareStatement(
                    "INSERT INTO tb_detail_penjualan (id_penjualan, id_produk, qty, harga_satuan, subtotal) VALUES (?, ?, ?, ?, ?)");
                PreparedStatement psStok = conn.prepareStatement(
                    "UPDATE tb_produk SET stok = stok - ? WHERE id_produk = ?");
                
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int idProd = (int) cartModel.getValueAt(i, 0);
                    double hrg = (double) cartModel.getValueAt(i, 2);
                    int qty    = (int) cartModel.getValueAt(i, 3);
                    double sub = (double) cartModel.getValueAt(i, 4);

                    psDetail.setInt(1, idPenjualanBaru);
                    psDetail.setInt(2, idProd);
                    psDetail.setInt(3, qty);
                    psDetail.setDouble(4, hrg);
                    psDetail.setDouble(5, sub);
                    psDetail.addBatch(); 

                    psStok.setInt(1, qty);
                    psStok.setInt(2, idProd);
                    psStok.addBatch();
                }

                psDetail.executeBatch();
                psStok.executeBatch();

                conn.commit(); 
                conn.setAutoCommit(true);
                conn.close();

                JOptionPane.showMessageDialog(this, 
                    "Transaksi Berhasil Dicatat!\nMetode: " + metodeBayar + "\nKembalian: " + lblKembalian.getText(), 
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

                cartModel.setRowCount(0);
                txtDiskon.setText("");
                cmbMetodeBayar.setSelectedIndex(0);
                txtBayar.setText("");
                txtSearchBarcode.setText("");
                lblInvoice.setText(generateInvoiceNumber()); 
                hitungTotalDanKembalian();
                loadProduk(""); 

            } catch (Exception ex) {
                conn.rollback();
                conn.setAutoCommit(true);
                conn.close();
                throw ex;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Transaksi Gagal. Pastikan Anda sudah menjalankan ALTER TABLE!\nError: " + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private JPanel createProductCard(int id, String nama, String hargaStr, double hargaReal, int stok) {
        RoundedPanel card = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x3D, 0x3B, 0x60));
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblImg = new JLabel("🌸", SwingConstants.CENTER);
        lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblImg.setPreferredSize(new Dimension(100, 100));
        lblImg.setOpaque(true);
        lblImg.setBackground(new Color(0x1E, 0x1D, 0x38));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel lblNama = new JLabel(nama);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNama.setForeground(ThemeConfig.TEXT_HEAD);
        
        JLabel lblHarga = new JLabel(hargaStr);
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHarga.setForeground(ThemeConfig.ACCENT);

        JLabel lblStok = new JLabel("Stok: " + stok);
        lblStok.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblStok.setForeground(stok > 0 ? ThemeConfig.SUCCESS : ThemeConfig.DANGER);

        infoPanel.add(lblNama);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblHarga);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(lblStok);

        card.add(lblImg, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { card.setBackground(new Color(0x3A, 0x38, 0x60)); }
            public void mouseExited(java.awt.event.MouseEvent e) { card.setBackground(ThemeConfig.BG_CARD); }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                tambahKeKeranjang(id, nama, hargaReal, stok);
            }
        });

        return card;
    }

    private JButton createGoldButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(200, 150, 40)); 
                else if (getModel().isRollover()) g2.setColor(new Color(255, 210, 80)); 
                else g2.setColor(ThemeConfig.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(ThemeConfig.ACCENT_TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JButton createOutlineButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0x1E, 0x1D, 0x38)); 
                else if (getModel().isRollover()) g2.setColor(new Color(0x3A, 0x38, 0x60)); 
                else g2.setColor(ThemeConfig.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(ThemeConfig.TEXT_BODY);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setContentAreaFilled(false); 
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(ThemeConfig.BG_CARD);
        tbl.setForeground(ThemeConfig.TEXT_BODY);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setRowHeight(30);
        tbl.setShowVerticalLines(false);
        tbl.setSelectionBackground(new Color(0x3A, 0x38, 0x60));
        tbl.setSelectionForeground(ThemeConfig.ACCENT);
        JTableHeader h = tbl.getTableHeader();
        h.setBackground(ThemeConfig.BG_PRIMARY);
        h.setForeground(ThemeConfig.TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private JPanel calcRow(String title, JComponent valComp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeConfig.TEXT_MUTED);
        p.add(l, BorderLayout.WEST);
        p.add(valComp, BorderLayout.EAST);
        return p;
    }

    private JLabel calcLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(ThemeConfig.TEXT_HEAD);
        return l;
    }

    private JTextField calcTextField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(140, 30));
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        f.setBackground(ThemeConfig.BG_PRIMARY);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60)),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        return f;
    }
}