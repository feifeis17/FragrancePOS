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

public class PelangganPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;

    private JTextField txtNama, txtKontak;

    public PelangganPanel() {
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari nama / kontak pelanggan...");
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
                if (getSelectedId() == -1) { showInfo("Pilih pelanggan yang ingin diedit."); return; }
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
        String[] cols = {"ID", "Nama Pelanggan", "Kontak", "Total Transaksi"};
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
                return l;
            }
        });

        DefaultTableCellRenderer padLeft = new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0));
              setBackground(ThemeConfig.BG_TABLE);
              setForeground(ThemeConfig.TEXT_BODY); }
        };
        table.getColumnModel().getColumn(2).setCellRenderer(padLeft);

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setHorizontalAlignment(CENTER);
                l.setBackground(ThemeConfig.BG_TABLE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                int total = val != null ? Integer.parseInt(val.toString()) : 0;
                l.setForeground(total > 0 ? ThemeConfig.ACCENT : ThemeConfig.TEXT_MUTED);
                l.setText(total > 0 ? total + "x" : "—");
                return l;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(280);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
    }
//load data
    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                try (Connection conn = Koneksi.configDB();
                     ResultSet rs = conn.createStatement().executeQuery(
                         "SELECT p.id_pelanggan, p.nama_pelanggan, " +
                         "COALESCE(p.kontak, '-') AS kontak, " +
                         "COUNT(pj.id_penjualan) AS total_transaksi " +
                         "FROM tb_pelanggan p " +
                         "LEFT JOIN tb_penjualan pj ON p.id_pelanggan = pj.id_pelanggan " +
                         "GROUP BY p.id_pelanggan, p.nama_pelanggan, p.kontak " +
                         "ORDER BY p.nama_pelanggan")) {
                    while (rs.next()) rows.add(new Object[]{
                        rs.getInt("id_pelanggan"),
                        rs.getString("nama_pelanggan"),
                        rs.getString("kontak"),
                        rs.getInt("total_transaksi")
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
    private void showFormDialog(boolean isEdit) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Pelanggan" : "Tambah Pelanggan", true);
        dialog.setSize(400, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ThemeConfig.BG_PRIMARY);
        content.setBorder(new EmptyBorder(24, 28, 20, 28));

        txtNama   = formField();
        txtKontak = formField();

        if (isEdit) prefillForm();

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 12, 0);

        addRow(form, g, 0, "Nama Pelanggan *", txtNama);
        addRow(form, g, 1, "No. Kontak",       txtKontak);

        content.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnBatal  = outlineButton("Batal");
        JButton btnSimpan = goldButton("Simpan");
        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> doSave(dialog, isEdit));

        txtNama.addActionListener(e -> doSave(dialog, isEdit));
        txtKontak.addActionListener(e -> doSave(dialog, isEdit));

        btnRow.add(btnBatal);
        btnRow.add(btnSimpan);
        content.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
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

    private void prefillForm() {
        int id = getSelectedId();
        if (id == -1) return;
        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM tb_pelanggan WHERE id_pelanggan = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNama.setText(rs.getString("nama_pelanggan"));
                txtKontak.setText(rs.getString("kontak") != null ? rs.getString("kontak") : "");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
// CRUD
    private void doSave(JDialog dialog, boolean isEdit) {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                "Nama pelanggan wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String kontak = txtKontak.getText().trim();

        try (Connection conn = Koneksi.configDB()) {
            String sql = isEdit
                ? "UPDATE tb_pelanggan SET nama_pelanggan=?, kontak=? WHERE id_pelanggan=?"
                : "INSERT INTO tb_pelanggan (nama_pelanggan, kontak) VALUES (?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, kontak.isEmpty() ? null : kontak);
            if (isEdit) ps.setInt(3, getSelectedId());
            ps.executeUpdate();

            dialog.dispose();
            loadData();
            showInfo(isEdit ? "Pelanggan berhasil diupdate!" : "Pelanggan berhasil ditambahkan!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        int id = getSelectedId();
        if (id == -1) { showInfo("Pilih pelanggan yang ingin dihapus."); return; }

        // Cek apakah pelanggan punya riwayat transaksi
        try (Connection conn = Koneksi.configDB();
             PreparedStatement cek = conn.prepareStatement(
                 "SELECT COUNT(*) FROM tb_penjualan WHERE id_pelanggan = ?")) {
            cek.setInt(1, id);
            ResultSet rs = cek.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Pelanggan ini memiliki riwayat transaksi.\n" +
                    "Data penjualan terkait akan diset ke pelanggan umum (null).\n" +
                    "Lanjutkan hapus?",
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Yakin hapus pelanggan ini?",
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
        } catch (Exception e) { e.printStackTrace(); return; }

        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM tb_pelanggan WHERE id_pelanggan = ?")) {
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
//helpers
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