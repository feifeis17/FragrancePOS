package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class ProdukPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;
    private JLabel valTotal, valMenipis, valBrandNew, valPreloved;
    private JTextField    txtNama, txtBrand, txtVolume, txtHargaBeli, txtHargaJual, txtStok;
    private JComboBox<String> cmbKategori, cmbKondisi;
    private final Map<String, Integer> kategoriMap = new LinkedHashMap<>();

    public ProdukPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeConfig.BG_PRIMARY);
        initUI();
        loadData();
        loadStats();
    }

    private JPanel buildMiniStats() {
    JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
    row.setOpaque(false);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
    row.setBorder(new EmptyBorder(0, 0, 14, 0));

    valTotal    = statLbl();
    valMenipis  = statLbl();
    valBrandNew = statLbl();
    valPreloved = statLbl();

    row.add(miniCard("Total Produk",   valTotal,    ThemeConfig.TEXT_HEAD));
    row.add(miniCard("Stok Menipis",   valMenipis,  ThemeConfig.DANGER));
    row.add(miniCard("Brand New",      valBrandNew, ThemeConfig.SUCCESS));
    row.add(miniCard("Preloved",       valPreloved, ThemeConfig.WARNING));
    return row;
}

private void loadStats() {
    new SwingWorker<int[], Void>() {
        @Override protected int[] doInBackground() throws Exception {
            int[] s = new int[4]; 
            try (Connection conn = Koneksi.configDB()) {
                ResultSet r1 = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM tb_produk");
                if (r1.next()) s[0] = r1.getInt(1);

                ResultSet r2 = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM tb_produk WHERE stok <= 5");
                if (r2.next()) s[1] = r2.getInt(1);

                ResultSet r3 = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM tb_produk WHERE kondisi='Brand New'");
                if (r3.next()) s[2] = r3.getInt(1);

                ResultSet r4 = conn.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM tb_produk WHERE kondisi='Preloved'");
                if (r4.next()) s[3] = r4.getInt(1);
            }
            return s;
        }
        @Override protected void done() {
            try {
                int[] s = get();
                valTotal.setText(String.valueOf(s[0]));
                valMenipis.setText(String.valueOf(s[1]));
                valBrandNew.setText(String.valueOf(s[2]));
                valPreloved.setText(String.valueOf(s[3]));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }.execute();
}

private JLabel statLbl() {
    JLabel l = new JLabel("—");
    l.setFont(new Font("Segoe UI", Font.BOLD, 26));
    return l;
}

private JPanel miniCard(String title, JLabel val, Color valColor) {
    JPanel c = new JPanel();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
    c.setBackground(ThemeConfig.BG_CARD);
    c.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0x2A, 0x28, 0x48), 1, true),
        new EmptyBorder(12, 16, 12, 16)));
    JLabel t = new JLabel(title.toUpperCase());
    t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
    t.setForeground(ThemeConfig.TEXT_MUTED);
    val.setForeground(valColor);
    c.add(t);
    c.add(Box.createVerticalStrut(8));
    c.add(val);
    return c;
}
    // UI
    private void initUI() {
    JPanel wrap = new JPanel(new BorderLayout(0, 12));
    wrap.setBackground(ThemeConfig.BG_PRIMARY);
    wrap.add(buildMiniStats(), BorderLayout.NORTH);
    wrap.add(buildTopBar(),    BorderLayout.CENTER); // search + tombol
    
    add(wrap,             BorderLayout.NORTH);
    add(buildTableArea(), BorderLayout.CENTER);
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari nama produk / brand...");
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

        // Tombol CRUD
        if (SessionManager.isAdmin()) {
            JButton btnTambah = goldButton("+ Tambah");
            btnTambah.addActionListener(e -> showFormDialog(false));

            JButton btnEdit = outlineButton("Edit");
            btnEdit.addActionListener(e -> {
                if (getSelectedId() == -1) {
                    showInfo("Pilih produk yang ingin diedit.");
                    return;
                }
                showFormDialog(true);
            });

            JButton btnHapus = dangerButton("Hapus");
            btnHapus.addActionListener(e -> doDelete());

            right.add(btnTambah);
            right.add(btnEdit);
            right.add(btnHapus);
        }

        JButton btnRefresh = outlineButton("↻");
        btnRefresh.addActionListener(e -> loadData());
        right.add(btnRefresh);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildTableArea() {
        String[] cols = {"ID", "Nama Produk", "Brand", "Kategori", "Vol (ml)", "Kondisi", "Harga Jual", "Stok"};
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
        sp.setBorder(BorderFactory.createLineBorder(new Color(0x2A, 0x28, 0x48), 1, true));
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
        DefaultTableCellRenderer padLeft = new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0));
              setBackground(ThemeConfig.BG_TABLE);
              setForeground(ThemeConfig.TEXT_BODY); }
        };
        for (int i = 1; i <= 4; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(padLeft);
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(
            JTable t, Object val, boolean sel, boolean foc, int row, int col) {
        int stok = 0;
        try { stok = Integer.parseInt(t.getValueAt(row, 7).toString()); }
        catch (Exception ignored) {}

        String status  = stok == 0 ? "Habis" : (val != null ? val.toString() : "");
        Color  fg, bg;
        if      ("Brand New".equals(status)) { fg = new Color(0x81,0xC9,0x95); bg = new Color(0x1A,0x3A,0x22); }
        else if ("Preloved" .equals(status)) { fg = new Color(0xF4,0xA2,0x61); bg = new Color(0x3A,0x25,0x10); }
        else                                 { fg = ThemeConfig.DANGER;         bg = new Color(0x3A,0x15,0x15); }

        JLabel badge = new JLabel(status) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-8, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setForeground(fg);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setHorizontalAlignment(CENTER);
        badge.setOpaque(false);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(sel ? t.getSelectionBackground() : ThemeConfig.BG_TABLE);
        wrap.add(badge);
        return wrap;
    }
});

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(RIGHT);
                l.setBackground(ThemeConfig.BG_TABLE);
                l.setForeground(ThemeConfig.ACCENT);
                l.setBorder(new EmptyBorder(0, 0, 0, 14));
                try {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id","ID"));
                    l.setText(nf.format(Double.parseDouble(val.toString())));
                } catch (Exception ignored) {}
                return l;
            }
        });

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(CENTER);
                l.setBackground(ThemeConfig.BG_TABLE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                int stok = val != null ? Integer.parseInt(val.toString()) : 0;
                l.setForeground(stok == 0 ? ThemeConfig.DANGER
                              : stok <= 5 ? ThemeConfig.WARNING
                                           : ThemeConfig.SUCCESS);
                return l;
            }
        });
        int[] widths = {0, 200, 130, 110, 80, 100, 140, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }
    // LOAD DATA
    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                try (Connection conn = Koneksi.configDB();
                     ResultSet rs = conn.createStatement().executeQuery(
                         "SELECT p.id_produk, p.nama_produk, p.brand, " +
                         "COALESCE(k.nama_kategori,'-') AS kategori, " +
                         "p.volume_ml, p.kondisi, p.harga_jual, p.stok " +
                         "FROM tb_produk p " +
                         "LEFT JOIN tb_kategori k ON p.id_kategori = k.id_kategori " +
                         "ORDER BY p.nama_produk")) {
                    while (rs.next()) rows.add(new Object[]{
                        rs.getInt("id_produk"),
                        rs.getString("nama_produk"),
                        rs.getString("brand"),
                        rs.getString("kategori"),
                        rs.getInt("volume_ml") + " ml",
                        rs.getString("kondisi"),
                        rs.getDouble("harga_jual"),
                        rs.getInt("stok")
                    });
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

    private void loadKategori() {
        kategoriMap.clear();
        kategoriMap.put("— Pilih Kategori —", 0);
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement()
                 .executeQuery("SELECT id_kategori, nama_kategori FROM tb_kategori ORDER BY nama_kategori")) {
            while (rs.next())
                kategoriMap.put(rs.getString("nama_kategori"), rs.getInt("id_kategori"));
        } catch (Exception e) { e.printStackTrace(); }
    }
    // FORM DIALOG
    private void showFormDialog(boolean isEdit) {
        loadKategori();

        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Produk" : "Tambah Produk", true);
        dialog.setSize(460, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(ThemeConfig.BG_PRIMARY);
        content.setBorder(new EmptyBorder(24, 28, 20, 28));
        txtNama      = formField();
        txtBrand     = formField();
        txtVolume    = formField();
        txtHargaBeli = formField();
        txtHargaJual = formField();
        txtStok      = formField();
        cmbKondisi   = styledCombo(new String[]{"Brand New", "Preloved"});
        cmbKategori  = styledCombo(kategoriMap.keySet().toArray(new String[0]));

        if (isEdit) prefillForm();
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 10, 0);

        addRow(form, g, 0, "Nama Produk *", txtNama);
        addRow(form, g, 1, "Brand *",       txtBrand);
        addRow(form, g, 2, "Kategori",      cmbKategori);
        addRow(form, g, 3, "Volume (ml)",   txtVolume);
        addRow(form, g, 4, "Kondisi",       cmbKondisi);
        addRow(form, g, 5, "Harga Beli",    txtHargaBeli);
        addRow(form, g, 6, "Harga Jual *",  txtHargaJual);
        addRow(form, g, 7, "Stok *",        txtStok);

        content.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnBatal  = outlineButton("Batal");
        JButton btnSimpan = goldButton("Simpan");
        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> doSave(dialog, isEdit));

        btnRow.add(btnBatal);
        btnRow.add(btnSimpan);
        content.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeConfig.TEXT_BODY);
        l.setPreferredSize(new Dimension(110, 36));
        p.add(l, g);
        g.gridx = 1; g.weightx = 1;
        p.add(field, g);
    }

    private void prefillForm() {
        int id = getSelectedId();
        if (id == -1) return;
        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM tb_produk WHERE id_produk = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNama.setText(rs.getString("nama_produk"));
                txtBrand.setText(rs.getString("brand"));
                txtVolume.setText(String.valueOf(rs.getInt("volume_ml")));
                txtHargaBeli.setText(String.valueOf(rs.getDouble("harga_beli")));
                txtHargaJual.setText(String.valueOf(rs.getDouble("harga_jual")));
                txtStok.setText(String.valueOf(rs.getInt("stok")));
                cmbKondisi.setSelectedItem(rs.getString("kondisi"));
                int idKat = rs.getInt("id_kategori");
                kategoriMap.forEach((nama, id2) -> {
                    if (id2 == idKat) cmbKategori.setSelectedItem(nama);
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    // CRUD
    private void doSave(JDialog dialog, boolean isEdit) {
        String nama  = txtNama.getText().trim();
        String brand = txtBrand.getText().trim();
        String hjStr = txtHargaJual.getText().trim();
        String stStr = txtStok.getText().trim();

        if (nama.isEmpty() || brand.isEmpty() || hjStr.isEmpty() || stStr.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                "Field bertanda * wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int    vol   = txtVolume.getText().isEmpty() ? 0 : Integer.parseInt(txtVolume.getText().trim());
            double hBeli = txtHargaBeli.getText().isEmpty() ? 0 : Double.parseDouble(txtHargaBeli.getText().trim());
            double hJual = Double.parseDouble(hjStr);
            int    stok  = Integer.parseInt(stStr);
            String kdisi = (String) cmbKondisi.getSelectedItem();
            int    idKat = kategoriMap.getOrDefault((String) cmbKategori.getSelectedItem(), 0);

            try (Connection conn = Koneksi.configDB()) {
                String sql = isEdit
                    ? "UPDATE tb_produk SET nama_produk=?,brand=?,id_kategori=?,volume_ml=?,kondisi=?,harga_beli=?,harga_jual=?,stok=? WHERE id_produk=?"
                    : "INSERT INTO tb_produk (nama_produk,brand,id_kategori,volume_ml,kondisi,harga_beli,harga_jual,stok) VALUES(?,?,?,?,?,?,?,?)";

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nama);
                ps.setString(2, brand);
                if (idKat == 0) ps.setNull(3, Types.INTEGER); else ps.setInt(3, idKat);
                ps.setInt(4, vol);
                ps.setString(5, kdisi);
                ps.setDouble(6, hBeli);
                ps.setDouble(7, hJual);
                ps.setInt(8, stok);
                if (isEdit) ps.setInt(9, getSelectedId());
                ps.executeUpdate();
            }

            dialog.dispose();
            loadData();
            showInfo("Data berhasil disimpan!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog,
                "Volume, Harga, dan Stok harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        int id = getSelectedId();
        if (id == -1) { showInfo("Pilih produk yang ingin dihapus."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin hapus produk ini? Data tidak bisa dikembalikan.",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM tb_produk WHERE id_produk = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        String q = txtSearch.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + q, 1, 2));
    }

    private int getSelectedId() {
        int row = table.getSelectedRow();
        if (row == -1) return -1;
        return (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
    }
    // HELPERS
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

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setPreferredSize(new Dimension(0, 36));
        cb.setBackground(ThemeConfig.BG_CARD);
        cb.setForeground(ThemeConfig.TEXT_HEAD);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return cb;
    }

    private JButton goldButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ThemeConfig.ACCENT);
        b.setForeground(ThemeConfig.ACCENT_TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JButton outlineButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ThemeConfig.BG_CARD);
        b.setForeground(ThemeConfig.TEXT_BODY);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0x4A, 0x20, 0x20));
        b.setForeground(ThemeConfig.DANGER);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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