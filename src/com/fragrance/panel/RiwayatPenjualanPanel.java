package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RiwayatPenjualanPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable tableRiwayat;
    private JTextField txtSearch;

    public RiwayatPenjualanPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeConfig.BG_PRIMARY);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        initUI();
        loadDataRiwayat();
    }

    private void initUI() {
        JPanel topBar = new JPanel(new BorderLayout(16, 0));
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel titleArea = new JPanel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Riwayat Data Penjualan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeConfig.TEXT_HEAD);
        
        JLabel lblSub = new JLabel("Daftar transaksi kasir yang telah berhasil diproses");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeConfig.TEXT_MUTED);
        
        titleArea.add(lblTitle);
        titleArea.add(Box.createVerticalStrut(4));
        titleArea.add(lblSub);

        JPanel searchArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchArea.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(280, 40));
        txtSearch.setBackground(ThemeConfig.BG_CARD);
        txtSearch.setForeground(ThemeConfig.TEXT_HEAD);
        txtSearch.setCaretColor(ThemeConfig.ACCENT);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari No. Invoice / Pelanggan...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 14, 4, 14)
        ));

// Fitur Live Search
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JButton btnDetail = createGoldButton("Lihat Detail Item");
        btnDetail.setPreferredSize(new Dimension(150, 40));
        btnDetail.addActionListener(e -> lihatDetailTransaksi());

        searchArea.add(txtSearch);
        searchArea.add(btnDetail);

        topBar.add(titleArea, BorderLayout.WEST);
        topBar.add(searchArea, BorderLayout.EAST);

// --- 2. AREA TABEL ---
        String[] cols = {"ID Hidden", "No. Invoice", "Tanggal Transaksi", "Nama Pelanggan", "Metode Bayar", "Total Belanja"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableRiwayat = new JTable(tableModel);
        styleTable(tableRiwayat);

        tableRiwayat.getColumnModel().getColumn(0).setMinWidth(0);
        tableRiwayat.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(tableRiwayat);
        scrollPane.getViewport().setBackground(ThemeConfig.BG_TABLE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        RoundedPanel tableWrap = new RoundedPanel(16, ThemeConfig.BG_TABLE, new Color(0x2A, 0x28, 0x48));
        tableWrap.setLayout(new BorderLayout());
        tableWrap.setBorder(new EmptyBorder(4, 4, 4, 4));
        tableWrap.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(tableWrap, BorderLayout.CENTER);
    }

    private void loadDataRiwayat() {
        tableModel.setRowCount(0);
        String sql = "SELECT p.id_penjualan, p.tanggal, p.total_harga, p.metode_bayar, " +
                     "COALESCE(pl.nama_pelanggan, 'Umum / Non-Member') AS nama_pelanggan " +
                     "FROM tb_penjualan p " +
                     "LEFT JOIN tb_pelanggan pl ON p.id_pelanggan = pl.id_pelanggan " +
                     "ORDER BY p.tanggal DESC";

        try (Connection conn = Koneksi.configDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("id", "ID"));
            
            while (rs.next()) {
                int id = rs.getInt("id_penjualan");
                java.sql.Timestamp tgl = rs.getTimestamp("tanggal");

                int tahun = tgl.toLocalDateTime().getYear();
                String noInvoice = String.format("%04d/INV/DEC/%d", id, tahun);
                
                String tanggalStr = sdf.format(tgl);
                String pelanggan = rs.getString("nama_pelanggan");
                String metode = rs.getString("metode_bayar");
                double total = rs.getDouble("total_harga");

                tableModel.addRow(new Object[]{id, noInvoice, tanggalStr, pelanggan, metode, total});
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tableRiwayat.setRowSorter(sorter);
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 1, 2, 3));
        }
    }
    private void lihatDetailTransaksi() {
        int row = tableRiwayat.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silakan klik/pilih salah satu Invoice di tabel terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int realRow = tableRiwayat.convertRowIndexToModel(row);
        String noInvoice = tableModel.getValueAt(realRow, 1).toString();
        
        JOptionPane.showMessageDialog(this, "Fitur Lihat Detail / Cetak Struk untuk Invoice " + noInvoice + " akan segera dibangun!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(ThemeConfig.BG_TABLE);
        tbl.setForeground(ThemeConfig.TEXT_BODY);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(42);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(0x2A, 0x28, 0x48));
        tbl.setSelectionBackground(new Color(0x3A, 0x38, 0x60));
        tbl.setSelectionForeground(ThemeConfig.ACCENT);
        tbl.setBorder(BorderFactory.createEmptyBorder());
        
        JTableHeader h = tbl.getTableHeader();
        h.setBackground(new Color(0x1E, 0x1D, 0x38));
        h.setForeground(ThemeConfig.TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setPreferredSize(new Dimension(0, 46));

        DefaultTableCellRenderer padLeft = new DefaultTableCellRenderer();
        padLeft.setBorder(new EmptyBorder(0, 14, 0, 0));
        tbl.getColumnModel().getColumn(1).setCellRenderer(padLeft);
        tbl.getColumnModel().getColumn(2).setCellRenderer(padLeft);
        tbl.getColumnModel().getColumn(3).setCellRenderer(padLeft);
        tbl.getColumnModel().getColumn(4).setCellRenderer(padLeft);

        DefaultTableCellRenderer rpRight = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setHorizontalAlignment(RIGHT);
                l.setBorder(new EmptyBorder(0, 0, 0, 14));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setForeground(ThemeConfig.ACCENT);
                try {
                    NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    l.setText(nf.format(Double.parseDouble(v.toString())).replace(",00", ""));
                } catch (Exception ignored) {}
                return l;
            }
        };
        tbl.getColumnModel().getColumn(5).setCellRenderer(rpRight);
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
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setContentAreaFilled(false); 
        b.setFocusPainted(false); 
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }
}