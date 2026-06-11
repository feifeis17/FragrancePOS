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
import java.util.ArrayList;
import java.util.List;

public class KategoriPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;

    private JTextField txtNamaKategori;

    public KategoriPanel() {
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
//src
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari nama kategori...");
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
        if (SessionManager.isAdmin()) {
            JButton btnTambah = goldButton("+ Tambah");
            btnTambah.addActionListener(e -> showFormDialog(false));

            JButton btnEdit = outlineButton("Edit");
            btnEdit.addActionListener(e -> {
                if (getSelectedId() == -1) { showInfo("Pilih kategori yang ingin diedit."); return; }
                showFormDialog(true);
            });

            JButton btnHapus = dangerButton("Hapus");
            btnHapus.addActionListener(e -> doDelete());

            right.add(btnTambah);
            right.add(btnEdit);
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
        String[] cols = {"ID", "Nama Kategori", "Total Produk (Item)"};
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
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0)); setBackground(ThemeConfig.BG_TABLE); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setForeground(ThemeConfig.TEXT_HEAD);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                return l;
            }
        });
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(CENTER);
                l.setBackground(ThemeConfig.BG_TABLE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                
                int total = val != null ? Integer.parseInt(val.toString()) : 0;
                l.setForeground(total > 0 ? ThemeConfig.ACCENT : ThemeConfig.TEXT_MUTED);
                l.setText(total > 0 ? total + " Produk" : "Kosong");
                return l;
            }
        });
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(600); // Dibuat lebar
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
    }
    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                try (Connection conn = Koneksi.configDB();
                     ResultSet rs = conn.createStatement().executeQuery(
                         "SELECT k.id_kategori, k.nama_kategori, " +
                         "COUNT(p.id_produk) AS total_produk " +
                         "FROM tb_kategori k " +
                         "LEFT JOIN tb_produk p ON k.id_kategori = p.id_kategori " +
                         "GROUP BY k.id_kategori, k.nama_kategori " +
                         "ORDER BY k.nama_kategori")) {
                    while (rs.next()) {
                        rows.add(new Object[]{
                            rs.getInt("id_kategori"),
                            rs.getString("nama_kategori"),
                            rs.getInt("total_produk")
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

    private void showFormDialog(boolean isEdit) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Kategori" : "Tambah Kategori", true);
        dialog.setSize(380, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ThemeConfig.BG_PRIMARY);
        content.setBorder(new EmptyBorder(24, 28, 20, 28));

        txtNamaKategori = formField();

        if (isEdit) prefillForm();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        JLabel lblNama = new JLabel("Nama Kategori *");
        lblNama.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNama.setForeground(ThemeConfig.TEXT_BODY);
        lblNama.setPreferredSize(new Dimension(110, 36));
        form.add(lblNama, g);
        
        g.gridx = 1; g.weightx = 1;
        form.add(txtNamaKategori, g);

        content.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnBatal  = outlineButton("Batal");
        JButton btnSimpan = goldButton("Simpan");
        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> doSave(dialog, isEdit));
        txtNamaKategori.addActionListener(e -> doSave(dialog, isEdit));

        btnRow.add(btnBatal);
        btnRow.add(btnSimpan);
        content.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void prefillForm() {
        int id = getSelectedId();
        if (id == -1) return;
        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM tb_kategori WHERE id_kategori = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNamaKategori.setText(rs.getString("nama_kategori"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void doSave(JDialog dialog, boolean isEdit) {
        String nama = txtNamaKategori.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                "Nama kategori wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.configDB()) {
            String sql = isEdit
                ? "UPDATE tb_kategori SET nama_kategori=? WHERE id_kategori=?"
                : "INSERT INTO tb_kategori (nama_kategori) VALUES (?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            if (isEdit) ps.setInt(2, getSelectedId());
            ps.executeUpdate();

            dialog.dispose();
            loadData();
            showInfo(isEdit ? "Kategori berhasil diupdate!" : "Kategori berhasil ditambahkan!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        int id = getSelectedId();
        if (id == -1) { showInfo("Pilih kategori yang ingin dihapus."); return; }

        try (Connection conn = Koneksi.configDB();
             PreparedStatement cek = conn.prepareStatement(
                 "SELECT COUNT(*) FROM tb_produk WHERE id_kategori = ?")) {
            cek.setInt(1, id);
            ResultSet rs = cek.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Kategori ini masih digunakan oleh " + rs.getInt(1) + " produk.\n" +
                    "Jika dihapus, status kategori pada produk tersebut akan menjadi Kosong.\n" +
                    "Lanjutkan hapus?",
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin hapus kategori ini?",
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
        } catch (Exception e) { e.printStackTrace(); return; }

        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM tb_kategori WHERE id_kategori = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal hapus data.\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        String q = txtSearch.getText().trim();

        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + q, 1));
    }

    private int getSelectedId() {
        int row = table.getSelectedRow();
        if (row == -1) return -1;
        return (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
    }

// COMPONENT HELPERS
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