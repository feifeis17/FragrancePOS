package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private JLabel valTotalProduk, valStokMenipis, valTerjual, valOmzet;
    private DefaultTableModel tableModel;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeConfig.BG_PRIMARY);
        initUI();
        loadData();
    }

    // ─────────────────────────────────────────────
    // UI SETUP
    // ─────────────────────────────────────────────
    private void initUI() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(ThemeConfig.BG_PRIMARY);

        wrap.add(buildStatRow());
        wrap.add(Box.createVerticalStrut(24));
        wrap.add(buildLowStockSection());

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(ThemeConfig.BG_PRIMARY);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────
    // STAT CARDS
    // ─────────────────────────────────────────────
    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        valTotalProduk = statValueLabel();
        valStokMenipis = statValueLabel();
        valTerjual     = statValueLabel();
        valOmzet       = statValueLabel();

        row.add(buildStatCard("Total Produk",     valTotalProduk, ThemeConfig.TEXT_HEAD));
        row.add(buildStatCard("Stok Menipis",     valStokMenipis, ThemeConfig.DANGER));
        row.add(buildStatCard("Terjual Hari Ini", valTerjual,     ThemeConfig.ACCENT));
        row.add(buildStatCard("Omzet Hari Ini",   valOmzet,       ThemeConfig.SUCCESS));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color valColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeConfig.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x2A, 0x28, 0x48), 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTitle.setForeground(ThemeConfig.TEXT_MUTED);

        valueLabel.setForeground(valColor);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        return card;
    }

    private JLabel statValueLabel() {
        JLabel l = new JLabel("—");
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        return l;
    }

    // ─────────────────────────────────────────────
    // LOW STOCK TABLE
    // ─────────────────────────────────────────────
    private JPanel buildLowStockSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // Header
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("Peringatan Stok Menipis");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(ThemeConfig.TEXT_HEAD);

        JLabel lblSub = new JLabel("Produk dengan stok ≤ 5 unit");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(ThemeConfig.TEXT_MUTED);

        sectionHeader.add(lblTitle, BorderLayout.WEST);
        sectionHeader.add(lblSub, BorderLayout.EAST);
        section.add(sectionHeader, BorderLayout.NORTH);

        // Table
        String[] cols = {"Nama Produk", "Brand", "Kategori", "Kondisi", "Stok"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        styleTable(table);
        section.add(wrapTable(table), BorderLayout.CENTER);

        return section;
    }

    private void styleTable(JTable table) {
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

        // Header style
        table.getTableHeader().setBackground(new Color(0x1E, 0x1D, 0x38));
        table.getTableHeader().setForeground(ThemeConfig.TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Kolom default — padding kiri
        DefaultTableCellRenderer leftPad = new DefaultTableCellRenderer();
        leftPad.setBorder(new EmptyBorder(0, 12, 0, 0));
        leftPad.setBackground(ThemeConfig.BG_TABLE);
        leftPad.setForeground(ThemeConfig.TEXT_BODY);
        for (int i = 0; i < 4; i++) table.getColumnModel().getColumn(i).setCellRenderer(leftPad);

        // Kolom Stok — center + warna
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBackground(ThemeConfig.BG_TABLE);
                try {
                    int stok = Integer.parseInt(val.toString());
                    l.setForeground(stok == 0 ? ThemeConfig.DANGER : ThemeConfig.WARNING);
                    l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                } catch (Exception ignored) {
                    l.setForeground(ThemeConfig.TEXT_MUTED);
                }
                return l;
            }
        });

        // Lebar kolom
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
    }

    private JScrollPane wrapTable(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(ThemeConfig.BG_TABLE);
        sp.getViewport().setBackground(ThemeConfig.BG_TABLE);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0x2A, 0x28, 0x48), 1, true));
        return sp;
    }

    // ─────────────────────────────────────────────
    // LOAD DATA DARI DB (non-blocking)
    // ─────────────────────────────────────────────
    private void loadData() {
        new SwingWorker<Void, Void>() {
            int    totalProduk = 0, stokMenipis = 0, terjual = 0;
            double omzet = 0;
            List<Object[]> rows = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = Koneksi.configDB()) {

                    // ── Total produk ──
                    try (ResultSet rs = conn.createStatement()
                            .executeQuery("SELECT COUNT(*) FROM tb_produk")) {
                        if (rs.next()) totalProduk = rs.getInt(1);
                    }

                    // ── Stok menipis (≤5) ──
                    try (ResultSet rs = conn.createStatement()
                            .executeQuery("SELECT COUNT(*) FROM tb_produk WHERE stok <= 5")) {
                        if (rs.next()) stokMenipis = rs.getInt(1);
                    }

                    // ── Terjual hari ini ──
                    try (ResultSet rs = conn.createStatement().executeQuery(
                            "SELECT COALESCE(SUM(dp.qty),0) " +
                            "FROM tb_detail_penjualan dp " +
                            "JOIN tb_penjualan p ON dp.id_penjualan = p.id_penjualan " +
                            "WHERE DATE(p.tanggal) = CURDATE()")) {
                        if (rs.next()) terjual = rs.getInt(1);
                    }

                    // ── Omzet hari ini ──
                    try (ResultSet rs = conn.createStatement().executeQuery(
                            "SELECT COALESCE(SUM(total_harga),0) " +
                            "FROM tb_penjualan WHERE DATE(tanggal) = CURDATE()")) {
                        if (rs.next()) omzet = rs.getDouble(1);
                    }

                    // ── Detail stok menipis ──
                    try (ResultSet rs = conn.createStatement().executeQuery(
                            "SELECT p.nama_produk, p.brand, " +
                            "COALESCE(k.nama_kategori,'-') AS kategori, " +
                            "p.kondisi, p.stok " +
                            "FROM tb_produk p " +
                            "LEFT JOIN tb_kategori k ON p.id_kategori = k.id_kategori " +
                            "WHERE p.stok <= 5 ORDER BY p.stok ASC LIMIT 15")) {
                        while (rs.next()) {
                            rows.add(new Object[]{
                                rs.getString("nama_produk"),
                                rs.getString("brand"),
                                rs.getString("kategori"),
                                rs.getString("kondisi"),
                                rs.getInt("stok")
                            });
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    valTotalProduk.setText(String.valueOf(totalProduk));
                    valStokMenipis.setText(String.valueOf(stokMenipis));
                    valTerjual.setText(String.valueOf(terjual) + " item");

                    NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    valOmzet.setText(rp.format(omzet));

                    tableModel.setRowCount(0);
                    if (rows.isEmpty()) {
                        tableModel.addRow(new Object[]{"Semua stok aman ✓", "-", "-", "-", "-"});
                    } else {
                        for (Object[] r : rows) tableModel.addRow(r);
                    }

                } catch (Exception e) {
                    valTotalProduk.setText("Err");
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}