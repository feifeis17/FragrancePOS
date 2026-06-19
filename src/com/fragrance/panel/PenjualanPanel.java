package com.fragrance.panel;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

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
        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setOpaque(false);

        JPanel searchBarArea = new JPanel(new BorderLayout(10, 0));
        searchBarArea.setOpaque(false);

        txtSearchBarcode = new JTextField();
        txtSearchBarcode.setPreferredSize(new Dimension(0, 40));
        txtSearchBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchBarcode.setBackground(ThemeConfig.BG_CARD);
        txtSearchBarcode.setForeground(ThemeConfig.TEXT_HEAD);
        txtSearchBarcode.setCaretColor(ThemeConfig.ACCENT);
        txtSearchBarcode.putClientProperty("JTextField.placeholderText", "Ketik Nama Produk...");
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

        JButton btnCari = createOutlineButton("Cari", "search_w.png");
        btnCari.setPreferredSize(new Dimension(80, 40));
        btnCari.addActionListener(e -> loadProduk(txtSearchBarcode.getText()));
        
        JButton btnScan = createGoldButton("Scan Barcode", "barcode_w.png");
        btnScan.setPreferredSize(new Dimension(140, 40));
        btnScan.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Scanner segera hadir!", "Gimmick", JOptionPane.INFORMATION_MESSAGE));

        btnSearchArea.add(btnCari);
        btnSearchArea.add(btnScan);

        searchBarArea.add(txtSearchBarcode, BorderLayout.CENTER);
        searchBarArea.add(btnSearchArea, BorderLayout.EAST);

        katalogContainer = new JPanel(new GridLayout(0, 4, 12, 12));
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
        rightPanel.setPreferredSize(new Dimension(340, 0));

        JPanel cartHeader = new JPanel(new BorderLayout(0, 10));
        cartHeader.setOpaque(false);
        
        JPanel titleInvArea = new JPanel(new BorderLayout());
        titleInvArea.setOpaque(false);
        
        JLabel lblTitleCart = new JLabel("Keranjang Belanja");
        lblTitleCart.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitleCart.setForeground(ThemeConfig.TEXT_HEAD);
        
        lblInvoice = new JLabel(generateInvoiceNumber());
        lblInvoice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInvoice.setForeground(ThemeConfig.ACCENT);
        
        titleInvArea.add(lblTitleCart, BorderLayout.WEST);
        titleInvArea.add(lblInvoice, BorderLayout.EAST);
        
        cmbPelanggan = new JComboBox<>();
        cmbPelanggan.setPreferredSize(new Dimension(0, 34));
        cmbPelanggan.setBackground(ThemeConfig.BG_PRIMARY);
        cmbPelanggan.setForeground(ThemeConfig.TEXT_HEAD);
        
        cartHeader.add(titleInvArea, BorderLayout.NORTH);
        cartHeader.add(cmbPelanggan, BorderLayout.CENTER);

        String[] cols = {"ID", "Produk", "Harga", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; } 
        };
        cartModel.addTableModelListener(e -> handleQtyEdit(e));

        cartTable = new JTable(cartModel);
        styleTable(cartTable);
        
        cartTable.getColumnModel().getColumn(0).setMinWidth(0);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(0);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(140); 
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(35);  

        JScrollPane scrollCart = new JScrollPane(cartTable);
        scrollCart.getViewport().setBackground(ThemeConfig.BG_CARD);
        scrollCart.setBorder(BorderFactory.createLineBorder(new Color(0x2A, 0x28, 0x48)));
        scrollCart.setPreferredSize(new Dimension(0, 160)); 

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        cartActions.setOpaque(false);
        JButton btnHapusItem = new JButton("Hapus Item");
        btnHapusItem.setForeground(ThemeConfig.DANGER);
        btnHapusItem.setContentAreaFilled(false);
        btnHapusItem.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
        btnHapusItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusItem.addActionListener(e -> hapusItemCart());
        cartActions.add(btnHapusItem);

        JPanel paymentArea = new JPanel();
        paymentArea.setLayout(new BoxLayout(paymentArea, BoxLayout.Y_AXIS));
        paymentArea.setOpaque(false);

        lblSubtotal   = calcLabel("Rp 0");
        
        JPanel panelDiskon = new JPanel(new BorderLayout(8, 0));
        panelDiskon.setOpaque(false);
        txtDiskon = calcTextField();
        lblDiskonPersen = new JLabel("(0%)");
        lblDiskonPersen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDiskonPersen.setForeground(new Color(0x81,0xC9,0x95));
        lblDiskonPersen.setPreferredSize(new Dimension(45, 30));
        panelDiskon.add(txtDiskon, BorderLayout.CENTER);
        panelDiskon.add(lblDiskonPersen, BorderLayout.EAST);

        lblTotalAkhir = calcLabel("Rp 0");
        lblTotalAkhir.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalAkhir.setForeground(ThemeConfig.ACCENT);
        
        cmbMetodeBayar = new JComboBox<>(new String[]{"Tunai (Cash)", "QRIS"});
        cmbMetodeBayar.setPreferredSize(new Dimension(140, 28));
        cmbMetodeBayar.setBackground(ThemeConfig.BG_PRIMARY);
        cmbMetodeBayar.setForeground(ThemeConfig.TEXT_HEAD);
        cmbMetodeBayar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        txtBayar      = calcTextField();
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        paymentArea.add(Box.createVerticalStrut(5));
        paymentArea.add(calcRow("Subtotal:", lblSubtotal));
        paymentArea.add(Box.createVerticalStrut(6));
        paymentArea.add(calcRow("Diskon (Rp):", panelDiskon));
        paymentArea.add(Box.createVerticalStrut(6));
        paymentArea.add(calcRow("Total Akhir:", lblTotalAkhir));
        paymentArea.add(Box.createVerticalStrut(12));
        
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x3D, 0x3B, 0x60));
        paymentArea.add(sep);
        paymentArea.add(Box.createVerticalStrut(12));

        paymentArea.add(calcRow("Metode Bayar:", cmbMetodeBayar));
        paymentArea.add(Box.createVerticalStrut(6));
        paymentArea.add(calcRow("Uang Bayar:", txtBayar));
        paymentArea.add(Box.createVerticalStrut(6));
        paymentArea.add(calcRow("Kembalian:", lblKembalian));
        paymentArea.add(Box.createVerticalStrut(12));

        JButton btnBayar = createGoldButton("PROSES PEMBAYARAN", null);
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnBayar.setPreferredSize(new Dimension(0, 42));
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
                int newQty = Integer.parseInt(cartModel.getValueAt(row, 3).toString());
                int idProd = (int) cartModel.getValueAt(row, 0);
                double harga = (double) cartModel.getValueAt(row, 2);
                int stokMaks = stokMapById.getOrDefault(idProd, 0);
                
                if (newQty <= 0) {
                    JOptionPane.showMessageDialog(this, "Qty minimal 1. Gunakan 'Hapus Item' untuk membatalkan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    cartModel.setValueAt(1, row, 3);
                    cartModel.setValueAt(harga, row, 4); 
                } else if (newQty > stokMaks) {
                    JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Maksimal: " + stokMaks, "Peringatan", JOptionPane.WARNING_MESSAGE);
                    cartModel.setValueAt(stokMaks, row, 3);
                    cartModel.setValueAt(stokMaks * harga, row, 4); 
                } else {
                    cartModel.setValueAt(newQty * harga, row, 4); 
                }
            } catch (Exception ex) {
                cartModel.setValueAt(1, row, 3);
                cartModel.setValueAt((double)cartModel.getValueAt(row, 2), row, 4);
            }
            isUpdatingCart = false;
            hitungTotalDanKembalian(); 
        }
    }

    private String generateInvoiceNumber() {
        String invoice = "";
        try (Connection conn = Koneksi.configDB(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id_penjualan) FROM tb_penjualan")) {
            int nextId = rs.next() ? rs.getInt(1) + 1 : 1;
            invoice = String.format("%04d/INV/DEC/%d", nextId, java.time.Year.now().getValue());
        } catch (Exception e) { invoice = "NEW/INV/DEC/" + java.time.Year.now().getValue(); }
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
                String displayNama = rs.getString("nama_pelanggan") + (rs.getString("kontak") != null ? " - (" + rs.getString("kontak") + ")" : "");
                pelangganMap.put(displayNama, rs.getInt("id_pelanggan"));
                model.addElement(displayNama);
            }
            cmbPelanggan.setModel(model);
        } catch (Exception e) {}
    }

    private void loadProduk(String keyword) {
        katalogContainer.removeAll();
        stokMapById.clear();
        String sql = "SELECT id_produk, nama_produk, brand, harga_jual, stok FROM tb_produk WHERE stok > 0 AND nama_produk LIKE ? ORDER BY nama_produk";
        try (Connection conn = Koneksi.configDB(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.trim() + "%");
            ResultSet rs = ps.executeQuery();
            NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            while (rs.next()) {
                int id = rs.getInt("id_produk");
                String nama = rs.getString("nama_produk");
                double harga = rs.getDouble("harga_jual");
                int stok = rs.getInt("stok");
                stokMapById.put(id, stok); 
                katalogContainer.add(createProductCard(id, nama, rp.format(harga).replace(",00", ""), harga, stok));
            }
            katalogContainer.revalidate();
            katalogContainer.repaint();
        } catch (Exception e) {}
    }

    private void tambahKeKeranjang(int idProduk, String nama, double harga, int stokMax) {
        int targetRow = -1, qtyInCart = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == idProduk) {
                targetRow = i; qtyInCart = (int) cartModel.getValueAt(i, 3); break;
            }
        }
        if (qtyInCart + 1 > stokMax) {
            JOptionPane.showMessageDialog(this, "Stok tidak cukup! Maksimal: " + stokMax, "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (targetRow != -1) cartModel.setValueAt(qtyInCart + 1, targetRow, 3);
        else cartModel.addRow(new Object[]{idProduk, nama, harga, 1, harga});
        hitungTotalDanKembalian();
    }

    private void hapusItemCart() {
        int row = cartTable.getSelectedRow();
        if (row != -1) {
            cartModel.removeRow(row);
            if (cartModel.getRowCount() == 0) SwingUtilities.invokeLater(() -> {
                txtDiskon.setText(""); if (cmbMetodeBayar.getSelectedIndex() == 0) txtBayar.setText("");
            });
            hitungTotalDanKembalian();
        } else JOptionPane.showMessageDialog(this, "Pilih item di keranjang yang ingin dihapus.");
    }

    private void hitungTotalDanKembalian() {
        if (cartModel.getRowCount() == 0) {
            lblSubtotal.setText("Rp 0"); lblTotalAkhir.setText("Rp 0"); lblKembalian.setText("Rp 0");
            lblKembalian.setForeground(ThemeConfig.SUCCESS); lblDiskonPersen.setText("(0%)");
            totalBelanja = 0; return; 
        }
        double subtotal = 0, diskon = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) subtotal += (double) cartModel.getValueAt(i, 4);
        try { if (!txtDiskon.getText().trim().isEmpty()) diskon = Double.parseDouble(txtDiskon.getText().trim()); } catch (Exception ignored) {}

        lblDiskonPersen.setText(subtotal > 0 && diskon > 0 ? String.format("(%.1f%%)", (diskon / subtotal) * 100) : "(0%)");
        totalBelanja = Math.max(0, subtotal - diskon);

        NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        lblSubtotal.setText(rp.format(subtotal).replace(",00", ""));
        lblTotalAkhir.setText(rp.format(totalBelanja).replace(",00", ""));
        
        if (cmbMetodeBayar.getSelectedIndex() == 1) { 
            if (txtBayar.getDocument() != null) txtBayar.getDocument().removeDocumentListener(calcListener);
            txtBayar.setText(String.valueOf((long) totalBelanja));
            if (txtBayar.getDocument() != null) txtBayar.getDocument().addDocumentListener(calcListener);
        }

        try {
            if (txtBayar.getText().trim().isEmpty()) { lblKembalian.setText("Rp 0"); lblKembalian.setForeground(ThemeConfig.SUCCESS); return; }
            double bayar = Double.parseDouble(txtBayar.getText().trim());
            double kembali = bayar - totalBelanja;
            if (kembali < 0) { lblKembalian.setText("Kurang " + rp.format(Math.abs(kembali)).replace(",00", "")); lblKembalian.setForeground(ThemeConfig.DANGER); } 
            else { lblKembalian.setText(rp.format(kembali).replace(",00", "")); lblKembalian.setForeground(ThemeConfig.SUCCESS); }
        } catch (NumberFormatException e) { lblKembalian.setText("Input Invalid"); lblKembalian.setForeground(ThemeConfig.DANGER); }
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "Keranjang kosong!"); return; }
        double uangBayar = 0;
        try { uangBayar = Double.parseDouble(txtBayar.getText().trim()); } catch (Exception e) { JOptionPane.showMessageDialog(this, "Input bayar tidak valid."); return; }
        if (uangBayar < totalBelanja) { JOptionPane.showMessageDialog(this, "Uang pembayaran kurang!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }

        int idPelanggan = pelangganMap.getOrDefault((String) cmbPelanggan.getSelectedItem(), 0);
        try (Connection conn = Koneksi.configDB()) {
            conn.setAutoCommit(false); 
            try {
                PreparedStatement psJual = conn.prepareStatement("INSERT INTO tb_penjualan (tanggal, id_pelanggan, id_user, total_harga, metode_bayar) VALUES (NOW(), ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                if (idPelanggan == 0) psJual.setNull(1, Types.INTEGER); else psJual.setInt(1, idPelanggan);
                psJual.setInt(2, SessionManager.getUserId()); psJual.setDouble(3, totalBelanja); psJual.setString(4, cmbMetodeBayar.getSelectedItem().toString());
                psJual.executeUpdate();
                
                ResultSet rsKeys = psJual.getGeneratedKeys();
                int idPenjualanBaru = rsKeys.next() ? rsKeys.getInt(1) : 0;

                PreparedStatement psDetail = conn.prepareStatement("INSERT INTO tb_detail_penjualan (id_penjualan, id_produk, qty, harga_satuan, subtotal) VALUES (?, ?, ?, ?, ?)");
                PreparedStatement psStok = conn.prepareStatement("UPDATE tb_produk SET stok = stok - ? WHERE id_produk = ?");
                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int idProd = (int) cartModel.getValueAt(i, 0), qty = (int) cartModel.getValueAt(i, 3);
                    psDetail.setInt(1, idPenjualanBaru); psDetail.setInt(2, idProd); psDetail.setInt(3, qty);
                    psDetail.setDouble(4, (double) cartModel.getValueAt(i, 2)); psDetail.setDouble(5, (double) cartModel.getValueAt(i, 4));
                    psDetail.addBatch(); psStok.setInt(1, qty); psStok.setInt(2, idProd); psStok.addBatch();
                }
                psDetail.executeBatch(); psStok.executeBatch(); conn.commit(); 

                String invoiceNo = lblInvoice.getText();
                String namaPembeli = cmbPelanggan.getSelectedItem().toString();
                double uangKembali = 0;
                double diskon = 0;
                
                try {
                    uangKembali = Double.parseDouble(txtBayar.getText().trim()) - totalBelanja;
                    if (!txtDiskon.getText().trim().isEmpty()) {
                        diskon = Double.parseDouble(txtDiskon.getText().trim());
                    }
                } catch (Exception ignored) {}
                
                showPostCheckoutAction(idPenjualanBaru, invoiceNo, namaPembeli, totalBelanja, diskon, uangKembali);

                cartModel.setRowCount(0); 
                txtDiskon.setText(""); 
                cmbMetodeBayar.setSelectedIndex(0); 
                txtBayar.setText(""); 
                txtSearchBarcode.setText("");
                lblInvoice.setText(generateInvoiceNumber()); 
                hitungTotalDanKembalian(); 
                loadProduk("");
            } catch (Exception ex) { conn.rollback(); throw ex; }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private JPanel createProductCard(int id, String nama, String hargaStr, double hargaReal, int stok) {
        RoundedPanel card = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x3D, 0x3B, 0x60));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 8, 12, 8)); 
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
                int newW = 80, newH = 80;
                if (origW > origH) newH = (origH * 80) / origW;
                else newW = (origW * 80) / origH;
                
                Image img = origImg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
            } else {
                lblImg.setText("🌸");
                lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
            }
        } catch (Exception e) {
            lblImg.setText("🌸");
            lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        }

        JLabel lblNama = new JLabel("<html><div style='width: 110px; text-align: center;'>" + nama + "</div></html>");
        lblNama.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblNama.setHorizontalAlignment(SwingConstants.CENTER);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNama.setForeground(ThemeConfig.TEXT_HEAD);
        
        JLabel lblHarga = new JLabel(hargaStr);
        lblHarga.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblHarga.setHorizontalAlignment(SwingConstants.CENTER);
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHarga.setForeground(ThemeConfig.ACCENT);

        JLabel lblStok = new JLabel("Stok: " + stok);
        lblStok.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStok.setHorizontalAlignment(SwingConstants.CENTER);
        lblStok.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblStok.setForeground(stok > 0 ? ThemeConfig.SUCCESS : ThemeConfig.DANGER);
        content.add(lblImg);
        content.add(Box.createVerticalStrut(12));
        content.add(lblNama);
        content.add(Box.createVerticalStrut(6));
        content.add(lblHarga);
        content.add(Box.createVerticalStrut(4));
        content.add(lblStok);

        card.add(content, BorderLayout.CENTER);
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { card.setBackground(new Color(0x3A, 0x38, 0x60)); }
            public void mouseExited(java.awt.event.MouseEvent e) { card.setBackground(ThemeConfig.BG_CARD); }
            public void mouseClicked(java.awt.event.MouseEvent e) { tambahKeKeranjang(id, nama, hargaReal, stok); }
        });
        
        return card;
    }
    private ImageIcon loadIcon(String filename, int size) {
        try {
            java.net.URL url = getClass().getResource("/com/fragrance/resources/icons/" + filename);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) { return null; }
    }

    private JButton createGoldButton(String text, String iconName) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(200, 150, 40)); 
                else if (getModel().isRollover()) g2.setColor(new Color(255, 210, 80)); 
                else g2.setColor(ThemeConfig.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        if (iconName != null) {
            ImageIcon icon = loadIcon(iconName, 16);
            if (icon != null) { b.setIcon(icon); b.setIconTextGap(8); }
        }
        b.setForeground(ThemeConfig.ACCENT_TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setContentAreaFilled(false); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        return b;
    }

    private JButton createOutlineButton(String text, String iconName) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0x1E, 0x1D, 0x38)); 
                else if (getModel().isRollover()) g2.setColor(new Color(0x3A, 0x38, 0x60)); 
                else g2.setColor(ThemeConfig.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        if (iconName != null) {
            ImageIcon icon = loadIcon(iconName, 14);
            if (icon != null) { b.setIcon(icon); b.setIconTextGap(6); }
        }
        b.setForeground(ThemeConfig.TEXT_BODY);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(ThemeConfig.BG_CARD); tbl.setForeground(ThemeConfig.TEXT_BODY);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12)); tbl.setRowHeight(26); // Row height dikecilkan
        tbl.setShowVerticalLines(false); tbl.setSelectionBackground(new Color(0x3A, 0x38, 0x60));
        tbl.setSelectionForeground(ThemeConfig.ACCENT);
        JTableHeader h = tbl.getTableHeader(); h.setBackground(ThemeConfig.BG_PRIMARY);
        h.setForeground(ThemeConfig.TEXT_MUTED); h.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    private JPanel calcRow(String title, JComponent valComp) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel l = new JLabel(title); l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(ThemeConfig.TEXT_MUTED); p.add(l, BorderLayout.WEST); p.add(valComp, BorderLayout.EAST); return p;
    }

    private JLabel calcLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 13)); l.setForeground(ThemeConfig.TEXT_HEAD); return l;
    }

    private JTextField calcTextField() {
        JTextField f = new JTextField(); f.setPreferredSize(new Dimension(130, 26));
        f.setHorizontalAlignment(SwingConstants.RIGHT); f.setBackground(ThemeConfig.BG_PRIMARY);
        f.setForeground(ThemeConfig.TEXT_HEAD); f.setCaretColor(ThemeConfig.ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60)), BorderFactory.createEmptyBorder(2, 6, 2, 6))); return f;
    }
//fitur invoice
    private void showPostCheckoutAction(int idPenjualan, String noInvoice, String namaPelanggan, double total, double diskon, double kembali) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Transaksi Berhasil", true);
        dialog.setSize(450, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        RoundedPanel panel = new RoundedPanel(16, ThemeConfig.BG_PRIMARY, ThemeConfig.ACCENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel lblSukses = new JLabel("TRANSAKSI SUKSES!");
        lblSukses.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSukses.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblSukses.setForeground(ThemeConfig.SUCCESS);
        lblSukses.setAlignmentX(Component.CENTER_ALIGNMENT);

        try {
               java.net.URL gifUrl = getClass().getResource("/com/fragrance/resources/icons/success.gif");
               if (gifUrl != null) {
                   ImageIcon originalIcon = new ImageIcon(gifUrl);
                   Image img = originalIcon.getImage().getScaledInstance(64, 64, Image.SCALE_DEFAULT);
                   
                   lblSukses.setIcon(new ImageIcon(img));
                   lblSukses.setHorizontalTextPosition(JLabel.CENTER);
                   lblSukses.setVerticalTextPosition(JLabel.BOTTOM);
                   lblSukses.setIconTextGap(10);
               }
           } catch (Exception e) {
               lblSukses.setIcon(null);
           }

        JLabel lblInv = new JLabel(noInvoice);
        lblInv.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInv.setForeground(ThemeConfig.TEXT_MUTED);
        lblInv.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel emailPanel = new JPanel(new BorderLayout(10, 0));
        emailPanel.setOpaque(false);
        emailPanel.setBorder(BorderFactory.createTitledBorder(new EmptyBorder(10,0,0,0), "Kirim Invoice ke Email Pelanggan", 0, 0, new Font("Segoe UI", Font.PLAIN, 12), ThemeConfig.TEXT_MUTED));
        
        JTextField txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(0, 36));
        txtEmail.setBackground(ThemeConfig.BG_CARD);
        txtEmail.setForeground(ThemeConfig.TEXT_HEAD);
        txtEmail.setCaretColor(ThemeConfig.ACCENT);
        txtEmail.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60)), BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        
        JButton btnKirim = createOutlineButton("Kirim Invoice", "report_w.png");
        btnKirim.addActionListener(e -> {
            String emailTujuan = txtEmail.getText().trim();
            if(emailTujuan.isEmpty() || !emailTujuan.contains("@")) {
                JOptionPane.showMessageDialog(dialog, "Masukkan alamat email yang valid!");
                return;
            }

            final String emailPengirim    = AppConfig.EMAIL_PENGIRIM;
            final String passwordPengirim = AppConfig.EMAIL_PASSWORD;

            btnKirim.setText("Mengirim...");
            btnKirim.setEnabled(false);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");

                    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailPengirim, passwordPengirim);
                        }
                    });

                    // Membuat isi email
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(emailPengirim, "Decium Perfumery"));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTujuan));
                    message.setSubject("Invoice Pembelian - " + noInvoice);

                    // Body Text Email
                    MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText("Halo " + namaPelanggan + ",\n\nTerima kasih telah berbelanja di Decium Perfumery. " +
                                     "Silakan temukan rincian transaksi Anda pada lampiran PDF di bawah ini.\n\n" +
                                     "Salam Wangi,\nTim Decium");
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    File pdfFile = new File("Invoice_" + noInvoice.replace("/", "_") + ".pdf");
                
                    if (!pdfFile.exists()) {
                        throw new Exception("Silakan klik 'Cetak Invoice PDF' terlebih dahulu sebelum mengirim email!");
                    }
                    
                    attachmentPart.attachFile(pdfFile);
                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(textPart);
                    multipart.addBodyPart(attachmentPart);

                    message.setContent(multipart);
                    Transport.send(message); 
                    return true;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(dialog, "Sukses! Invoice berhasil dikirim ke " + emailTujuan);
                        btnKirim.setText("Kirim Invoice");
                        btnKirim.setEnabled(true);
                    } catch (Exception ex) {
                        String msg = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(dialog, "Gagal mengirim email:\n" + msg, "Error", JOptionPane.ERROR_MESSAGE);
                        btnKirim.setText("Kirim Invoice");
                        btnKirim.setEnabled(true);
                    }
                }
            }.execute();
        });

        emailPanel.add(txtEmail, BorderLayout.CENTER);
        emailPanel.add(btnKirim, BorderLayout.EAST);

        JButton btnCetak = createGoldButton("Cetak Invoice PDF (A4)", "report_w.png");
        btnCetak.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnCetak.addActionListener(e -> {
            exportInvoicePDF(idPenjualan, noInvoice, namaPelanggan, total, diskon, kembali);
        });

        JButton btnTutup = createOutlineButton("Tutup Layar", null);
        btnTutup.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        btnTutup.addActionListener(e -> dialog.dispose());

        panel.add(lblSukses);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lblInv);
        panel.add(Box.createVerticalStrut(20));
        panel.add(emailPanel);
        panel.add(Box.createVerticalStrut(24));
        panel.add(btnCetak);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnTutup);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void exportInvoicePDF(int idPenjualan, String noInvoice, String namaPelanggan, double total, double diskon, double kembali) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Invoice PDF");
        fileChooser.setSelectedFile(new File("Invoice_" + noInvoice.replace("/", "_") + ".pdf"));
        
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File fileToSave = fileChooser.getSelectedFile();

        try {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();

            com.lowagie.text.Font fHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(0x1E, 0x1D, 0x38));
            com.lowagie.text.Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            com.lowagie.text.Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            com.lowagie.text.Font fSmall = FontFactory.getFont(FontFactory.HELVETICA, 9);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1.5f, 1f});

            PdfPCell cellStore = new PdfPCell();
            cellStore.setBorder(Rectangle.NO_BORDER);
            cellStore.addElement(new Paragraph("Decium Perfumery", fHeader));
            cellStore.addElement(new Paragraph("Jl. Raya Cileunyi - Bandung, Jawa Barat", fNormal));
            cellStore.addElement(new Paragraph("Telp: 0812-3456-7890 | IG: @decium.perfume", fNormal));
            headerTable.addCell(cellStore);

            PdfContentByte cb = writer.getDirectContent();
            Barcode128 barcode = new Barcode128();
            barcode.setCode(noInvoice);
            barcode.setBarHeight(40f);
            com.lowagie.text.Image imgBarcode = barcode.createImageWithBarcode(cb, null, null);
            imgBarcode.setAlignment(Element.ALIGN_RIGHT);
            
            PdfPCell cellBarcode = new PdfPCell(imgBarcode, false);
            cellBarcode.setBorder(Rectangle.NO_BORDER);
            cellBarcode.setHorizontalAlignment(Element.ALIGN_RIGHT);
            headerTable.addCell(cellBarcode);

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            Paragraph title = new Paragraph("INVOICE PENJUALAN", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            
            PdfPCell infoKiri = new PdfPCell();
            infoKiri.setBorder(Rectangle.NO_BORDER);
            infoKiri.addElement(new Paragraph("Pembeli : " + namaPelanggan, fBold));
            
            PdfPCell infoKanan = new PdfPCell();
            infoKanan.setBorder(Rectangle.NO_BORDER);
            infoKanan.setHorizontalAlignment(Element.ALIGN_RIGHT);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
            infoKanan.addElement(new Paragraph("Tanggal : " + sdf.format(new java.util.Date()), fNormal));

            infoTable.addCell(infoKiri);
            infoTable.addCell(infoKanan);
            document.add(infoTable);
            document.add(new Paragraph("\n"));
            PdfPTable itemTable = new PdfPTable(5);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{0.5f, 3f, 1f, 1.5f, 1.5f});

            String[] headers = {"No", "Deskripsi / Nama Produk", "Qty", "Harga Satuan", "Total Harga"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, fBold));
                c.setBackgroundColor(new Color(0xF0, 0xF0, 0xF5)); 
                c.setPadding(8);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemTable.addCell(c);
            }

            NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            int no = 1;
            double subtotalAkhir = 0;
            
            try (java.sql.Connection conn = Koneksi.configDB();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.nama_produk, dp.qty, dp.harga_satuan, dp.subtotal " +
                     "FROM tb_detail_penjualan dp JOIN tb_produk p ON dp.id_produk = p.id_produk " +
                     "WHERE dp.id_penjualan = ?")) {
                ps.setInt(1, idPenjualan);
                java.sql.ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    // 1. Kolom Nomor
                    PdfPCell cNo = new PdfPCell(new Phrase(String.valueOf(no++), fNormal));
                    cNo.setPadding(6);
                    itemTable.addCell(cNo);
                    
                    // 2. Kolom Nama Produk
                    PdfPCell cNama = new PdfPCell(new Phrase(rs.getString("nama_produk"), fNormal));
                    cNama.setPadding(6);
                    itemTable.addCell(cNama);
                    
                    // 3. Kolom Qty
                    PdfPCell cQty = new PdfPCell(new Phrase(String.valueOf(rs.getInt("qty")), fNormal));
                    cQty.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cQty.setPadding(6);
                    itemTable.addCell(cQty);
                    
                    // 4. Kolom Harga Satuan
                    PdfPCell cHarga = new PdfPCell(new Phrase(rp.format(rs.getDouble("harga_satuan")).replace(",00",""), fNormal));
                    cHarga.setHorizontalAlignment(Element.ALIGN_RIGHT); 
                    cHarga.setPadding(6);
                    itemTable.addCell(cHarga);
                    
                    // 5. Kolom Subtotal
                    PdfPCell cSub = new PdfPCell(new Phrase(rp.format(rs.getDouble("subtotal")).replace(",00",""), fNormal));
                    cSub.setHorizontalAlignment(Element.ALIGN_RIGHT); 
                    cSub.setPadding(6);
                    itemTable.addCell(cSub);
                    
                    subtotalAkhir += rs.getDouble("subtotal");
                }
            }

            document.add(itemTable);
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{6f, 1.5f});

            addTotalRow(totalTable, "Subtotal", subtotalAkhir, fBold, fNormal, rp);
            addTotalRow(totalTable, "Diskon", diskon, fBold, fNormal, rp);
            addTotalRow(totalTable, "TOTAL BAYAR", total, fBold, fBold, rp);
            addTotalRow(totalTable, "Kembalian", kembali, fNormal, fNormal, rp);

            document.add(totalTable);

            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Terima kasih telah berbelanja di Decium Perfumery.\nBarang yang sudah dibeli tidak dapat ditukar atau dikembalikan.", fSmall);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            
            JOptionPane.showMessageDialog(this, "Invoice PDF berhasil dibuat!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            Desktop.getDesktop().open(fileToSave);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void addTotalRow(PdfPTable table, String label, double value, com.lowagie.text.Font fLabel, com.lowagie.text.Font fValue, NumberFormat rp) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fLabel));
        c1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(6);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(rp.format(value).replace(",00",""), fValue));
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorder(Rectangle.BOTTOM); 
        c2.setPadding(6);
        table.addCell(c2);
    }
}