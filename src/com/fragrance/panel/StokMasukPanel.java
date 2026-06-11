package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class StokMasukPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;

    // Form fields
    private JComboBox<String> cmbSupplier, cmbProduk;
    private JTextField        txtQty, txtHargaBeli;
    private JLabel            lblStokSekarang;

    private final Map<String, Integer> supplierMap = new LinkedHashMap<>();
    private final Map<String, Integer> produkMap   = new LinkedHashMap<>();

    public StokMasukPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeConfig.BG_PRIMARY);
        initUI();
        loadData();
    }
//ui
    private void initUI() {
        add(buildTopBar(), BorderLayout.NORTH);
        RoundedPanel roundedTableArea = new RoundedPanel(12, ThemeConfig.BG_TABLE, new Color(0x2A, 0x28, 0x48));
        roundedTableArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); // Jarak agar tabel tidak nabrak
        roundedTableArea.add(buildTableArea(), BorderLayout.CENTER);
        add(roundedTableArea, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.setBackground(ThemeConfig.BG_CARD);
        txtSearch.setForeground(ThemeConfig.TEXT_HEAD);
        txtSearch.setCaretColor(ThemeConfig.ACCENT);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari produk / supplier...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(txtSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Admin & Operator bisa tambah
        if (SessionManager.isAdmin() || SessionManager.isOperator()) {
            JButton btnTambah = goldButton("+ Stok Masuk");
            btnTambah.addActionListener(e -> showFormDialog());
            right.add(btnTambah);
        }

        // Hanya Admin bisa hapus
        if (SessionManager.isAdmin()) {
            JButton btnHapus = dangerButton("Hapus");
            btnHapus.addActionListener(e -> doDelete());
            right.add(btnHapus);
        }

        JButton btnRefresh = outlineButton(""); 
        btnRefresh.setPreferredSize(new Dimension(36, 36)); 
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(9, 9, 9, 9) 
        ));

       
        ImageIcon icRefreshW = loadScaledIcon("segarkan_w.png", 16);
        ImageIcon icRefreshG = loadScaledIcon("segarkan_g.png", 16);

        if (icRefreshW != null) {
            btnRefresh.setIcon(icRefreshW);
        }

        btnRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (icRefreshG != null) btnRefresh.setIcon(icRefreshG);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (icRefreshW != null) btnRefresh.setIcon(icRefreshW);
            }
        });

        btnRefresh.addActionListener(e -> loadData());
        right.add(btnRefresh);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTableArea() {
        String[] cols = {"ID", "Tanggal", "Supplier", "Produk", "Qty", "Harga Beli/unit", "Subtotal"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        styleTable();

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(ThemeConfig.BG_TABLE);
        sp.getViewport().setBackground(ThemeConfig.BG_TABLE);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private void styleTable() {
        table.setBackground(ThemeConfig.BG_TABLE);
        table.setForeground(ThemeConfig.TEXT_BODY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(0x2A, 0x28, 0x48));
        table.setSelectionBackground(new Color(0x2A, 0x28, 0x48));
        table.setSelectionForeground(ThemeConfig.ACCENT);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBorder(BorderFactory.createEmptyBorder());

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0x1E, 0x1D, 0x38));
        header.setForeground(ThemeConfig.TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 38));

        // Default pad kiri
        DefaultTableCellRenderer padLeft = new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0));
              setBackground(ThemeConfig.BG_TABLE);
              setForeground(ThemeConfig.TEXT_BODY); }
        };

        // Tanggal
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0)); setBackground(ThemeConfig.BG_TABLE); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setForeground(ThemeConfig.TEXT_MUTED);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                return l;
            }
        });

        // Supplier
        table.getColumnModel().getColumn(2).setCellRenderer(padLeft);

        // Produk — terang
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0)); setBackground(ThemeConfig.BG_TABLE); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setForeground(ThemeConfig.TEXT_HEAD);
                return l;
            }
        });

        // Qty — center, success
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(CENTER);
                l.setBackground(ThemeConfig.BG_TABLE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setForeground(ThemeConfig.SUCCESS);
                l.setText("+" + val);
                return l;
            }
        });

        // Harga Beli — currency kanan
        DefaultTableCellRenderer currencyRight = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(RIGHT);
                l.setBackground(ThemeConfig.BG_TABLE);
                l.setForeground(ThemeConfig.TEXT_BODY);
                l.setBorder(new EmptyBorder(0, 0, 0, 14));
                try {
                    double v = Double.parseDouble(val.toString());
                    if (v == 0) { l.setText("—"); l.setForeground(ThemeConfig.TEXT_MUTED); }
                    else {
                        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        l.setText(nf.format(v));
                    }
                } catch (Exception ignored) { l.setText("—"); }
                return l;
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(currencyRight);

        // Subtotal — gold
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(RIGHT);
                l.setBackground(ThemeConfig.BG_TABLE);
                l.setBorder(new EmptyBorder(0, 0, 0, 14));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                try {
                    double v = Double.parseDouble(val.toString());
                    if (v == 0) { l.setText("—"); l.setForeground(ThemeConfig.TEXT_MUTED); }
                    else {
                        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        l.setText(nf.format(v));
                        l.setForeground(ThemeConfig.ACCENT);
                    }
                } catch (Exception ignored) { l.setText("—"); }
                return l;
            }
        });

        // Lebar kolom
        int[] widths = {0, 150, 160, 200, 60, 150, 160};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    // ─────────────────────────────────────────────
    // LOAD DATA
    // ─────────────────────────────────────────────
    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                try (Connection conn = Koneksi.configDB();
                     ResultSet rs = conn.createStatement().executeQuery(
                         "SELECT sm.id_masuk, sm.tanggal, " +
                         "COALESCE(s.nama_supplier, 'Tanpa Supplier') AS supplier, " +
                         "p.nama_produk, sm.qty, " +
                         "COALESCE(sm.harga_beli_satuan, 0) AS harga_beli, " +
                         "COALESCE(sm.harga_beli_satuan, 0) * sm.qty AS subtotal " +
                         "FROM tb_stok_masuk sm " +
                         "LEFT JOIN tb_supplier s ON sm.id_supplier = s.id_supplier " +
                         "LEFT JOIN tb_produk p ON sm.id_produk = p.id_produk " +
                         "ORDER BY sm.tanggal DESC")) {
                    while (rs.next()) {
                        Timestamp ts = rs.getTimestamp("tanggal");
                        rows.add(new Object[]{
                            rs.getInt("id_masuk"),
                            ts != null ? sdf.format(ts) : "-",
                            rs.getString("supplier"),
                            rs.getString("nama_produk"),
                            rs.getInt("qty"),
                            rs.getDouble("harga_beli"),
                            rs.getDouble("subtotal")
                        });
                    }
                }
                return rows;
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Object[] row : get()) tableModel.addRow(row);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    private void loadSupplierMap() {
        supplierMap.clear();
        supplierMap.put("— Tanpa Supplier —", 0);
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement()
                 .executeQuery("SELECT id_supplier, nama_supplier FROM tb_supplier ORDER BY nama_supplier")) {
            while (rs.next())
                supplierMap.put(rs.getString("nama_supplier"), rs.getInt("id_supplier"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadProdukMap() {
        produkMap.clear();
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement()
                 .executeQuery("SELECT id_produk, nama_produk, brand, stok FROM tb_produk ORDER BY nama_produk")) {
            while (rs.next())
                produkMap.put(
                    rs.getString("nama_produk") + " — " + rs.getString("brand") +
                    " (Stok: " + rs.getInt("stok") + ")",
                    rs.getInt("id_produk")
                );
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────
    // FORM DIALOG
    // ─────────────────────────────────────────────
    private void showFormDialog() {
        loadSupplierMap();
        loadProdukMap();

        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Input Stok Masuk", true);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ThemeConfig.BG_PRIMARY);
        content.setBorder(new EmptyBorder(24, 28, 20, 28));

        cmbSupplier = styledCombo(supplierMap.keySet().toArray(new String[0]));
        cmbProduk   = styledCombo(produkMap.keySet().toArray(new String[0]));
        txtQty      = formField();
        txtHargaBeli = formField();
        txtHargaBeli.putClientProperty("JTextField.placeholderText", "Opsional");

        // Label stok sekarang — update otomatis saat pilih produk
        lblStokSekarang = new JLabel("Pilih produk dahulu");
        lblStokSekarang.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStokSekarang.setForeground(ThemeConfig.TEXT_MUTED);

        cmbProduk.addActionListener(e -> updateStokLabel());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);

        addRow(form, g, 0, "Supplier",       cmbSupplier);
        addRow(form, g, 1, "Produk *",        cmbProduk);

        // Baris info stok sekarang
        g.gridx = 1; g.gridy = 2; g.weightx = 1;
        form.add(lblStokSekarang, g);

        addRow(form, g, 3, "Qty Masuk *",     txtQty);
        addRow(form, g, 4, "Harga Beli/unit", txtHargaBeli);

        content.add(form, BorderLayout.CENTER);

        // Info total di bawah field
        JLabel lblInfo = new JLabel("Stok produk akan otomatis bertambah setelah disimpan.");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(ThemeConfig.TEXT_MUTED);

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel btnRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRight.setOpaque(false);
        JButton btnBatal  = outlineButton("Batal");
        JButton btnSimpan = goldButton("Simpan & Update Stok");
        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> doSave(dialog));

        btnRight.add(btnBatal);
        btnRight.add(btnSimpan);
        btnRow.add(lblInfo,    BorderLayout.WEST);
        btnRow.add(btnRight,   BorderLayout.EAST);

        content.add(btnRow, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void updateStokLabel() {
        String selected = (String) cmbProduk.getSelectedItem();
        if (selected == null) return;
        int idProduk = produkMap.getOrDefault(selected, 0);
        if (idProduk == 0) return;
        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT stok FROM tb_produk WHERE id_produk = ?")) {
            ps.setInt(1, idProduk);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int stok = rs.getInt("stok");
                lblStokSekarang.setText("Stok saat ini: " + stok + " unit");
                lblStokSekarang.setForeground(stok <= 5 ? ThemeConfig.WARNING : ThemeConfig.SUCCESS);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addRow(JPanel p, GridBagConstraints g, int row,
                        String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeConfig.TEXT_BODY);
        l.setPreferredSize(new Dimension(130, 36));
        p.add(l, g);
        g.gridx = 1; g.weightx = 1;
        p.add(field, g);
    }

    // ─────────────────────────────────────────────
    // SAVE — TRANSAKSI DB (atomic)
    // ─────────────────────────────────────────────
    private void doSave(JDialog dialog) {
        String produkKey = (String) cmbProduk.getSelectedItem();
        String qtyStr    = txtQty.getText().trim();
        String hargaStr  = txtHargaBeli.getText().trim();

        if (produkKey == null || produkMap.getOrDefault(produkKey, 0) == 0) {
            JOptionPane.showMessageDialog(dialog,
                "Pilih produk terlebih dahulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                "Qty masuk wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int    idProduk  = produkMap.get(produkKey);
        String supplierKey = (String) cmbSupplier.getSelectedItem();
        int    idSupplier  = supplierMap.getOrDefault(supplierKey, 0);

        try {
            int    qty   = Integer.parseInt(qtyStr);
            double harga = hargaStr.isEmpty() ? 0 : Double.parseDouble(hargaStr);

            if (qty <= 0) {
                JOptionPane.showMessageDialog(dialog,
                    "Qty harus lebih dari 0.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Connection conn = Koneksi.configDB();
            conn.setAutoCommit(false); // mulai transaksi DB

            try {
                // 1. Insert ke tb_stok_masuk
                PreparedStatement ps1 = conn.prepareStatement(
                    "INSERT INTO tb_stok_masuk " +
                    "(id_supplier, id_produk, qty, harga_beli_satuan) " +
                    "VALUES (?, ?, ?, ?)");
                if (idSupplier == 0) ps1.setNull(1, Types.INTEGER);
                else ps1.setInt(1, idSupplier);
                ps1.setInt(2, idProduk);
                ps1.setInt(3, qty);
                if (harga == 0) ps1.setNull(4, Types.DECIMAL);
                else ps1.setDouble(4, harga);
                ps1.executeUpdate();

                // 2. Update stok produk
                PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE tb_produk SET stok = stok + ? WHERE id_produk = ?");
                ps2.setInt(1, qty);
                ps2.setInt(2, idProduk);
                ps2.executeUpdate();

                conn.commit(); // commit kedua query sekaligus
                conn.setAutoCommit(true);
                conn.close();

                dialog.dispose();
                loadData();
                JOptionPane.showMessageDialog(this,
                    "Stok masuk berhasil dicatat!\nStok produk bertambah " + qty + " unit.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                conn.rollback(); // batalkan kalau salah satu gagal
                conn.setAutoCommit(true);
                conn.close();
                throw ex;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog,
                "Qty dan Harga harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────
    // DELETE — balik stok
    // ─────────────────────────────────────────────
    private void doDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { showInfo("Pilih data stok masuk yang ingin dihapus."); return; }

        int idMasuk = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        int qty     = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 4);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Hapus data ini?\nStok produk akan berkurang " + qty + " unit kembali.",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = Koneksi.configDB();
            conn.setAutoCommit(false);
            try {
                // Ambil id_produk dulu
                PreparedStatement cek = conn.prepareStatement(
                    "SELECT id_produk FROM tb_stok_masuk WHERE id_masuk = ?");
                cek.setInt(1, idMasuk);
                ResultSet rs = cek.executeQuery();
                if (!rs.next()) { conn.rollback(); conn.close(); return; }
                int idProduk = rs.getInt("id_produk");

                // Cek stok tidak minus
                PreparedStatement cekStok = conn.prepareStatement(
                    "SELECT stok FROM tb_produk WHERE id_produk = ?");
                cekStok.setInt(1, idProduk);
                ResultSet rsStok = cekStok.executeQuery();
                if (rsStok.next() && rsStok.getInt("stok") - qty < 0) {
                    conn.rollback(); conn.close();
                    JOptionPane.showMessageDialog(this,
                        "Tidak bisa hapus — stok produk akan menjadi negatif.",
                        "Gagal", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Hapus record
                PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM tb_stok_masuk WHERE id_masuk = ?");
                del.setInt(1, idMasuk);
                del.executeUpdate();

                // Kurangi stok
                PreparedStatement upd = conn.prepareStatement(
                    "UPDATE tb_produk SET stok = stok - ? WHERE id_produk = ?");
                upd.setInt(1, qty);
                upd.setInt(2, idProduk);
                upd.executeUpdate();

                conn.commit();
                conn.setAutoCommit(true);
                conn.close();

                loadData();
                showInfo("Data dihapus. Stok produk berkurang " + qty + " unit.");

            } catch (Exception ex) {
                conn.rollback();
                conn.setAutoCommit(true);
                conn.close();
                throw ex;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────
    // FILTER
    // ─────────────────────────────────────────────
    private void filterTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        String q = txtSearch.getText().trim();
        // Search di kolom Supplier (2) dan Produk (3)
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + q, 2, 3));
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    private JTextField formField() {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(0, 36));
        f.setBackground(ThemeConfig.BG_CARD);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return f;
    }
        private ImageIcon loadScaledIcon(String filename, int size) {
        try {
            java.net.URL url = getClass().getResource("/com/fragrance/resources/icons/" + filename);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) { 
            return null; 
        }
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
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }
    private JButton outlineButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0x1E, 0x1D, 0x38)); // Navy Gelap
                else if (getModel().isRollover()) g2.setColor(new Color(0x3A, 0x38, 0x60)); // Navy Terang
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
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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