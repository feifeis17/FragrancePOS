package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;

    // Field form
    private JTextField txtNama, txtKontak;
    private JTextArea txtAlamat;

    public SupplierPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ThemeConfig.BG_PRIMARY);
        initUI();
        loadData();
    }
    // UI
    private void initUI() {
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        // Search
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari nama supplier...");
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
                if (getSelectedId() == -1) { showInfo("Pilih supplier yang ingin diedit."); return; }
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
        String[] cols = {"ID", "Nama Supplier", "Kontak", "Alamat"};
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
        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0));
              setBackground(ThemeConfig.BG_TABLE); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setForeground(ThemeConfig.TEXT_HEAD);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return l;
            }
        };

        table.getColumnModel().getColumn(1).setCellRenderer(nameRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(padLeft);
        table.getColumnModel().getColumn(3).setCellRenderer(padLeft);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(400);
    }
    // LOAD DATA
    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                try (Connection conn = Koneksi.configDB();
                     ResultSet rs = conn.createStatement().executeQuery(
                         "SELECT id_supplier, nama_supplier, " +
                         "COALESCE(kontak,'-') AS kontak, " +
                         "COALESCE(alamat,'-') AS alamat " +
                         "FROM tb_supplier ORDER BY nama_supplier")) {
                    while (rs.next()) rows.add(new Object[]{
                        rs.getInt("id_supplier"),
                        rs.getString("nama_supplier"),
                        rs.getString("kontak"),
                        rs.getString("alamat")
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
    // FORM DIALOG
    private void showFormDialog(boolean isEdit) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Supplier" : "Tambah Supplier", true);
        dialog.setSize(440, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ThemeConfig.BG_PRIMARY);
        content.setBorder(new EmptyBorder(24, 28, 20, 28));

        txtNama   = formField();
        txtKontak = formField();

        txtAlamat = new JTextArea(3, 20); 
        txtAlamat.setBackground(ThemeConfig.BG_CARD);
        txtAlamat.setForeground(ThemeConfig.TEXT_HEAD);
        txtAlamat.setCaretColor(ThemeConfig.ACCENT);
        txtAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        txtAlamat.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        
        JScrollPane scrollAlamat = new JScrollPane(txtAlamat);
        scrollAlamat.setBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true));

        if (isEdit) prefillForm();

        JPanel form = new JPanel(new GridBagLayout());
        addRow(form, g, 0, "Nama Supplier *", txtNama);
        addRow(form, g, 1, "No. Kontak",      txtKontak);
        addRow(form, g, 2, "Alamat",          scrollAlamat);

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

    private void addRow(JPanel p, GridBagConstraints g, int row,
                        String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeConfig.TEXT_BODY);
        l.setPreferredSize(new Dimension(120, 36));
        p.add(l, g);
        g.gridx = 1; g.weightx = 1;
        p.add(field, g);
    }

    private void prefillForm() {
        int id = getSelectedId();
        if (id == -1) return;
        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM tb_supplier WHERE id_supplier = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNama.setText(rs.getString("nama_supplier"));
                txtKontak.setText(rs.getString("kontak") != null ? rs.getString("kontak") : "");
                txtAlamat.setText(rs.getString("alamat") != null ? rs.getString("alamat") : "");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    // CRUD
    private void doSave(JDialog dialog, boolean isEdit) {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                "Nama supplier wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String kontak = txtKontak.getText().trim();
        String alamat = txtAlamat.getText().trim();

        try (Connection conn = Koneksi.configDB()) {
            String sql = isEdit
                ? "UPDATE tb_supplier SET nama_supplier=?, kontak=?, alamat=? WHERE id_supplier=?"
                : "INSERT INTO tb_supplier (nama_supplier, kontak, alamat) VALUES (?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, kontak.isEmpty() ? null : kontak);
            ps.setString(3, alamat.isEmpty() ? null : alamat);
            if (isEdit) ps.setInt(4, getSelectedId());
            ps.executeUpdate();

            dialog.dispose();
            loadData();
            showInfo(isEdit ? "Supplier berhasil diupdate!" : "Supplier berhasil ditambahkan!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        int id = getSelectedId();
        if (id == -1) { showInfo("Pilih supplier yang ingin dihapus."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin hapus supplier ini?",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM tb_supplier WHERE id_supplier = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal hapus — supplier mungkin masih terhubung ke data stok masuk.\nError: "
                + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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