package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class PenjualanPanel extends JPanel {

    private DefaultTableModel cartModel;
    private JTable            tableCart;
    private JComboBox<String> cmbProduk, cmbPelanggan;
    private JTextField        txtQty, txtBayar;
    private JLabel            lblTotalBesar, lblKembalian, lblInfoStok;
    
    private final Map<String, Integer> produkIdMap     = new LinkedHashMap<>();
    private final Map<String, Double>  produkHargaMap  = new HashMap<>();
    private final Map<String, Integer> produkStokMap   = new HashMap<>();
    private final Map<String, Integer> pelangganMap    = new LinkedHashMap<>();

    private double totalBelanja = 0;

    public PenjualanPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(ThemeConfig.BG_PRIMARY);
        setBorder(new EmptyBorder(0, 0, 10, 0));

        initUI();
        loadPelanggan();
        loadProduk();
    }
//ui
    private void initUI() {
        add(buildLeftPanel(),  BorderLayout.CENTER); 
        add(buildRightPanel(), BorderLayout.EAST);   
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        RoundedPanel formInput = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        formInput.setLayout(new GridBagLayout());
        formInput.setBorder(new EmptyBorder(16, 16, 16, 16)); 

        cmbProduk   = styledCombo(new String[]{"Loading produk..."});
        txtQty      = formField();
        txtQty.setText("1");

        lblInfoStok = new JLabel("Stok: -");
        lblInfoStok.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfoStok.setForeground(ThemeConfig.TEXT_MUTED);

        cmbProduk.addActionListener(e -> updateInfoStok());

        JButton btnAddCart = goldButton("+ Masukkan Keranjang");
        btnAddCart.addActionListener(e -> addToCart());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 8, 12);

        g.gridx = 0; g.gridy = 0; g.weightx = 1; g.gridwidth = 2;
        formInput.add(new JLabel("Pilih Produk") {{ setForeground(ThemeConfig.TEXT_BODY); }}, g);
        g.gridy = 1;
        formInput.add(cmbProduk, g);
        g.gridy = 2;
        formInput.add(lblInfoStok, g);

        g.gridwidth = 1;
        g.gridy = 3; g.weightx = 0.2;
        formInput.add(new JLabel("Qty") {{ setForeground(ThemeConfig.TEXT_BODY); }}, g);
        g.gridx = 1; g.weightx = 0.8;
        formInput.add(new JLabel(""), g);

        g.gridx = 0; g.gridy = 4;
        formInput.add(txtQty, g);
        g.gridx = 1;
        formInput.add(btnAddCart, g);

        String[] cols = {"ID", "Nama Produk", "Harga", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCart = new JTable(cartModel);
        styleTable(tableCart);
        
        tableCart.getColumnModel().getColumn(0).setMinWidth(0);
        tableCart.getColumnModel().getColumn(0).setMaxWidth(0); 
        tableCart.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableCart.getColumnModel().getColumn(3).setPreferredWidth(50); 

        JScrollPane scrollCart = new JScrollPane(tableCart);
        scrollCart.setBackground(ThemeConfig.BG_TABLE);
        scrollCart.getViewport().setBackground(ThemeConfig.BG_TABLE);
        scrollCart.setBorder(BorderFactory.createEmptyBorder()); 

        RoundedPanel roundedCartArea = new RoundedPanel(12, ThemeConfig.BG_TABLE, new Color(0x2A, 0x28, 0x48));
        roundedCartArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        roundedCartArea.add(scrollCart, BorderLayout.CENTER);

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        cartActions.setOpaque(false);
        JButton btnHapusItem = dangerButton("Hapus Item Terpilih");
        btnHapusItem.addActionListener(e -> hapusItemCart());
        cartActions.add(btnHapusItem);

        panel.add(formInput, BorderLayout.NORTH);
        panel.add(roundedCartArea, BorderLayout.CENTER); 
        panel.add(cartActions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildRightPanel() {
        RoundedPanel panel = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        panel.setPreferredSize(new Dimension(340, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topSummary = new JPanel();
        topSummary.setLayout(new BoxLayout(topSummary, BoxLayout.Y_AXIS));
        topSummary.setOpaque(false);

        JLabel lblTitleTotal = new JLabel("TOTAL BELANJA");
        lblTitleTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitleTotal.setForeground(ThemeConfig.TEXT_MUTED);
        lblTitleTotal.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTotalBesar = new JLabel("Rp0");
        lblTotalBesar.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTotalBesar.setForeground(ThemeConfig.ACCENT);
        lblTotalBesar.setAlignmentX(Component.CENTER_ALIGNMENT);

        topSummary.add(lblTitleTotal);
        topSummary.add(Box.createVerticalStrut(10));
        topSummary.add(lblTotalBesar);
        
        JPanel formPay = new JPanel(new GridBagLayout());
        formPay.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);

        cmbPelanggan = styledCombo(new String[]{"Umum / Non-Member"});
        txtBayar     = formField();
        txtBayar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        lblKembalian = new JLabel("Rp0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblKembalian.setForeground(ThemeConfig.SUCCESS);

        g.gridx = 0; g.gridy = 0; g.weightx = 1;
        formPay.add(new JLabel("Pelanggan") {{ setForeground(ThemeConfig.TEXT_BODY); }}, g);
        g.gridy = 1; formPay.add(cmbPelanggan, g);
        
        g.gridy = 2; formPay.add(new JLabel("Uang Bayar (Rp)") {{ setForeground(ThemeConfig.TEXT_BODY); }}, g);
        g.gridy = 3; formPay.add(txtBayar, g);

        g.gridy = 4; formPay.add(new JLabel("Kembalian") {{ setForeground(ThemeConfig.TEXT_BODY); }}, g);
        g.gridy = 5; formPay.add(lblKembalian, g);

        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { hitungKembalian(); }
            public void removeUpdate(DocumentEvent e)  { hitungKembalian(); }
            public void changedUpdate(DocumentEvent e) { hitungKembalian(); }
        });

        JButton btnProses = new JButton("PROSES PEMBAYARAN") {
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
        btnProses.setForeground(ThemeConfig.ACCENT_TEXT);
        btnProses.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnProses.setPreferredSize(new Dimension(0, 50));
        btnProses.setContentAreaFilled(false);
        btnProses.setFocusPainted(false);
        btnProses.setBorderPainted(false);
        btnProses.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProses.addActionListener(e -> processCheckout());

        panel.add(topSummary, BorderLayout.NORTH);
        panel.add(formPay, BorderLayout.CENTER);
        panel.add(btnProses, BorderLayout.SOUTH);

        return panel;
    }
    private void addToCart() {
        String selected = (String) cmbProduk.getSelectedItem();
        if (selected == null || !produkIdMap.containsKey(selected)) return;

        int idProduk   = produkIdMap.get(selected);
        double harga   = produkHargaMap.get(selected);
        int stokReal   = produkStokMap.get(selected);
        int qtyReq;

        try {
            qtyReq = Integer.parseInt(txtQty.getText().trim());
            if (qtyReq <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            showInfo("Qty harus berupa angka lebih dari 0.");
            return;
        }

        int qtyInCart = 0;
        int targetRow = -1;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == idProduk) {
                qtyInCart = (int) cartModel.getValueAt(i, 3);
                targetRow = i;
                break;
            }
        }

        if (qtyReq + qtyInCart > stokReal) {
            JOptionPane.showMessageDialog(this,
                "Stok tidak cukup! Stok tersedia: " + stokReal, "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (targetRow != -1) {
            int newQty = qtyInCart + qtyReq;
            double newSub = newQty * harga;
            cartModel.setValueAt(newQty, targetRow, 3);
            cartModel.setValueAt(newSub, targetRow, 4);
        } else {
            String namaProduk = selected.split(" — ")[0];
            cartModel.addRow(new Object[]{ idProduk, namaProduk, harga, qtyReq, harga * qtyReq });
        }

        txtQty.setText("1");
        updateTotalBelanja();
    }

    private void hapusItemCart() {
        int row = tableCart.getSelectedRow();
        if (row != -1) {
            cartModel.removeRow(row);
            updateTotalBelanja();
        }
    }

    private void updateTotalBelanja() {
        totalBelanja = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            totalBelanja += (double) cartModel.getValueAt(i, 4);
        }
        NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        lblTotalBesar.setText(rp.format(totalBelanja).replace(",00", ""));
        hitungKembalian(); 
    }

    private void hitungKembalian() {
        try {
            String bayarStr = txtBayar.getText().trim();
            if (bayarStr.isEmpty()) {
                lblKembalian.setText("Rp0");
                lblKembalian.setForeground(ThemeConfig.TEXT_BODY);
                return;
            }
            double bayar = Double.parseDouble(bayarStr);
            double kembali = bayar - totalBelanja;

            NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            if (kembali < 0) {
                lblKembalian.setText("Kurang " + rp.format(Math.abs(kembali)).replace(",00", ""));
                lblKembalian.setForeground(ThemeConfig.DANGER);
            } else {
                lblKembalian.setText(rp.format(kembali).replace(",00", ""));
                lblKembalian.setForeground(ThemeConfig.SUCCESS);
            }
        } catch (NumberFormatException e) {
            lblKembalian.setText("Input tidak valid");
            lblKembalian.setForeground(ThemeConfig.DANGER);
        }
    }

    private void processCheckout() {
        if (cartModel.getRowCount() == 0) {
            showInfo("Keranjang belanja masih kosong!");
            return;
        }

        double uangBayar = 0;
        try {
            uangBayar = Double.parseDouble(txtBayar.getText().trim());
        } catch (Exception e) {
            showInfo("Masukkan jumlah uang bayar yang valid.");
            return;
        }

        if (uangBayar < totalBelanja) {
            showInfo("Uang pembayaran kurang!");
            return;
        }

        String pelKey = (String) cmbPelanggan.getSelectedItem();
        int idPelanggan = pelangganMap.getOrDefault(pelKey, 0);
        int idUser = SessionManager.getUserId();

        try {
            Connection conn = Koneksi.configDB();
            conn.setAutoCommit(false); 

            try {
                String sqlJual = "INSERT INTO tb_penjualan (tanggal, id_pelanggan, id_user, total_harga) VALUES (NOW(), ?, ?, ?)";
                PreparedStatement psJual = conn.prepareStatement(sqlJual, Statement.RETURN_GENERATED_KEYS);
                if (idPelanggan == 0) psJual.setNull(1, Types.INTEGER); else psJual.setInt(1, idPelanggan);
                psJual.setInt(2, idUser);
                psJual.setDouble(3, totalBelanja);
                psJual.executeUpdate();

                ResultSet rsKeys = psJual.getGeneratedKeys();
                int idPenjualanBaru = 0;
                if (rsKeys.next()) {
                    idPenjualanBaru = rsKeys.getInt(1);
                } else {
                    throw new SQLException("Gagal mendapatkan ID Penjualan.");
                }

                PreparedStatement psDetail = conn.prepareStatement(
                    "INSERT INTO tb_detail_penjualan (id_penjualan, id_produk, qty, harga_satuan, subtotal) VALUES (?, ?, ?, ?, ?)");
                PreparedStatement psStok = conn.prepareStatement(
                    "UPDATE tb_produk SET stok = stok - ? WHERE id_produk = ?");

                for (int i = 0; i < cartModel.getRowCount(); i++) {
                    int idProd  = (int) cartModel.getValueAt(i, 0);
                    double hrg  = (double) cartModel.getValueAt(i, 2);
                    int qty     = (int) cartModel.getValueAt(i, 3);
                    double sub  = (double) cartModel.getValueAt(i, 4);

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
                    "Transaksi Berhasil Dicatat!\nKembalian: " + lblKembalian.getText(), 
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

                cartModel.setRowCount(0);
                txtBayar.setText("");
                updateTotalBelanja();
                loadProduk(); 
                cmbProduk.setSelectedIndex(0);

            } catch (Exception ex) {
                conn.rollback(); 
                conn.setAutoCommit(true);
                conn.close();
                throw ex;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Transaksi Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private void loadPelanggan() {
        pelangganMap.clear();
        pelangganMap.put("Umum / Non-Member", 0);
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement().executeQuery("SELECT id_pelanggan, nama_pelanggan FROM tb_pelanggan ORDER BY nama_pelanggan")) {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("Umum / Non-Member");
            while (rs.next()) {
                String nama = rs.getString("nama_pelanggan");
                pelangganMap.put(nama, rs.getInt("id_pelanggan"));
                model.addElement(nama);
            }
            cmbPelanggan.setModel(model);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadProduk() {
        produkIdMap.clear();
        produkHargaMap.clear();
        produkStokMap.clear();
        
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT id_produk, nama_produk, brand, harga_jual, stok FROM tb_produk WHERE stok > 0 ORDER BY nama_produk")) {
            
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("— Ketik atau Pilih Produk —");
            
            NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            while (rs.next()) {
                String key = rs.getString("nama_produk") + " (" + rs.getString("brand") + ") — " + 
                             rp.format(rs.getDouble("harga_jual")).replace(",00", "");
                
                produkIdMap.put(key, rs.getInt("id_produk"));
                produkHargaMap.put(key, rs.getDouble("harga_jual"));
                produkStokMap.put(key, rs.getInt("stok"));
                model.addElement(key);
            }
            cmbProduk.setModel(model);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateInfoStok() {
        String sel = (String) cmbProduk.getSelectedItem();
        if (sel == null || !produkStokMap.containsKey(sel)) {
            lblInfoStok.setText("Stok: -");
        } else {
            int stok = produkStokMap.get(sel);
            lblInfoStok.setText("Stok tersedia: " + stok);
        }
    }
    private void styleTable(JTable tbl) {
        tbl.setBackground(ThemeConfig.BG_TABLE);
        tbl.setForeground(ThemeConfig.TEXT_BODY);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(36);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(0x2A, 0x28, 0x48));
        tbl.setSelectionBackground(new Color(0x3A, 0x38, 0x60));
        tbl.setSelectionForeground(ThemeConfig.ACCENT);
        tbl.setBorder(BorderFactory.createEmptyBorder());

        JTableHeader h = tbl.getTableHeader();
        h.setBackground(new Color(0x1E, 0x1D, 0x38));
        h.setForeground(ThemeConfig.TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer rpRight = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(RIGHT);
                l.setBorder(new EmptyBorder(0, 0, 0, 14));
                try {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    l.setText(nf.format(Double.parseDouble(v.toString())).replace(",00", ""));
                } catch (Exception ignored) {}
                return l;
            }
        };
        tbl.getColumnModel().getColumn(2).setCellRenderer(rpRight);
        tbl.getColumnModel().getColumn(4).setCellRenderer(rpRight);
        
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(3).setCellRenderer(center);
    }

    private JTextField formField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(0, 36));
        f.setBackground(ThemeConfig.BG_PRIMARY);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return f;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setPreferredSize(new Dimension(0, 36));
        cb.setBackground(ThemeConfig.BG_PRIMARY);
        cb.setForeground(ThemeConfig.TEXT_HEAD);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return cb;
    }

    private JButton goldButton(String text) {
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
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setContentAreaFilled(false); 
        b.setFocusPainted(false); 
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JButton dangerButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0x30, 0x10, 0x10)); 
                else if (getModel().isRollover()) g2.setColor(new Color(0x60, 0x2A, 0x2A)); 
                else g2.setColor(new Color(0x4A, 0x20, 0x20));
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(ThemeConfig.DANGER);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setContentAreaFilled(false); 
        b.setFocusPainted(false); 
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        return b;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}