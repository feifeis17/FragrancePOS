package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class LaporanPanel extends JPanel {
    private JLabel lblTotalItem, lblTotalTransaksi, lblTotalOmzet, lblTotalPelanggan;
    private DefaultCategoryDataset lineDataset;
    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset pieDataset;
    private DefaultTableModel modelTopProduk;
    private DefaultTableModel modelTransaksiTerbaru;

    public LaporanPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeConfig.BG_PRIMARY);
        
        initUI();
        loadTopProduk();
        loadTransaksiTerbaru();
        loadChartAndCardsData();
    }
    private void initUI() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(ThemeConfig.BG_PRIMARY);
        mainContent.setBorder(new EmptyBorder(16, 16, 16, 16));

        mainContent.add(buildTopCardsRow());
        mainContent.add(Box.createVerticalStrut(16));
        mainContent.add(buildChartsRow());
        mainContent.add(Box.createVerticalStrut(16));
        mainContent.add(buildTableAndPieRow());
        mainContent.add(Box.createVerticalStrut(16));
        mainContent.add(buildBottomTableRow());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeConfig.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }
    private JPanel buildTopCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row.setPreferredSize(new Dimension(0, 100));

        lblTotalItem      = statLbl();
        lblTotalTransaksi = statLbl();
        lblTotalOmzet     = statLbl();
        lblTotalPelanggan = statLbl();

        row.add(createMiniCard("TOTAL ITEM TERJUAL", lblTotalItem, new Color(0x4A, 0x90, 0xE2))); 
        row.add(createMiniCard("TOTAL TRANSAKSI", lblTotalTransaksi, new Color(0x81, 0xC9, 0x95))); 
        row.add(createMiniCard("TOTAL INCOME", lblTotalOmzet, ThemeConfig.ACCENT)); 
        row.add(createMiniCard("PELANGGAN AKTIF", lblTotalPelanggan, new Color(0xE7, 0x4C, 0x3C))); 

        return row;
    }

    private JPanel buildChartsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        row.setPreferredSize(new Dimension(0, 350));

        lineDataset = new DefaultCategoryDataset();
        barDataset  = new DefaultCategoryDataset();
        ChartPanel cpLine = new ChartPanel(createDarkLineChart(lineDataset, "Tren Penjualan Bulanan"));
        ChartPanel cpBar  = new ChartPanel(createDarkBarChart(barDataset, "Penjualan 7 Hari Terakhir"));
        cpLine.setOpaque(false); cpLine.setBackground(new Color(0,0,0,0));
        cpBar.setOpaque(false);  cpBar.setBackground(new Color(0,0,0,0));

        RoundedPanel pnlLine = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        pnlLine.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlLine.add(cpLine, BorderLayout.CENTER);

        RoundedPanel pnlBar = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        pnlBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlBar.add(cpBar, BorderLayout.CENTER);

        row.add(pnlLine);
        row.add(pnlBar);
        return row;
    }

    private JPanel buildTableAndPieRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        row.setPreferredSize(new Dimension(0, 300));

        RoundedPanel pnlTable = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        pnlTable.setBorder(new EmptyBorder(16, 16, 16, 16));
        
        JLabel lblTopProd = new JLabel("Penjualan Barang Terbesar");
        lblTopProd.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTopProd.setForeground(ThemeConfig.TEXT_HEAD);
        pnlTable.add(lblTopProd, BorderLayout.NORTH);
        
        String[] cols = {"No", "Nama Produk", "Harga", "Qty", "Total"};
        modelTopProduk = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblTop = new JTable(modelTopProduk);
        styleTable(tblTop);
        tblTop.getColumnModel().getColumn(0).setPreferredWidth(30); 
        tblTop.getColumnModel().getColumn(1).setPreferredWidth(180); 
        tblTop.getColumnModel().getColumn(3).setPreferredWidth(40);
        JScrollPane spTop = new JScrollPane(tblTop);
        spTop.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        spTop.getViewport().setBackground(ThemeConfig.BG_CARD);
        pnlTable.add(spTop, BorderLayout.CENTER);

        pieDataset = new DefaultPieDataset();
        ChartPanel cpPie = new ChartPanel(createDarkPieChart(pieDataset, "Paling Banyak Terjual (Brand)"));
        cpPie.setOpaque(false); cpPie.setBackground(new Color(0,0,0,0));
        RoundedPanel pnlPie = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        pnlPie.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlPie.add(cpPie, BorderLayout.CENTER);

        row.add(pnlTable);
        row.add(pnlPie);
        return row;
    }

    private JPanel buildBottomTableRow() {
        RoundedPanel row = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        row.setLayout(new BorderLayout());
        row.setBorder(new EmptyBorder(16, 16, 16, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        row.setPreferredSize(new Dimension(0, 250));

        JLabel lblRecent = new JLabel("Transaksi Terbaru");
        lblRecent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRecent.setForeground(ThemeConfig.TEXT_HEAD);
        row.add(lblRecent, BorderLayout.NORTH);

        String[] cols = {"No", "Nama Pembeli", "Jml Item", "Nilai", "Tanggal", "Status"};
        modelTransaksiTerbaru = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        JTable tblRecent = new JTable(modelTransaksiTerbaru);
        styleTable(tblRecent);
        tblRecent.getColumnModel().getColumn(0).setPreferredWidth(30); // No
        tblRecent.getColumnModel().getColumn(1).setPreferredWidth(180); // Nama Pembeli
        tblRecent.getColumnModel().getColumn(2).setPreferredWidth(60); // Jml Item
        tblRecent.getColumnModel().getColumn(3).setPreferredWidth(120); // Nilai
        tblRecent.getColumnModel().getColumn(4).setPreferredWidth(130); // Tanggal
        tblRecent.getColumnModel().getColumn(5).setPreferredWidth(70); // Status
        
        tblRecent.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground(ThemeConfig.SUCCESS); 
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(CENTER);
                return l;
            }
        });
        
        JScrollPane spRecent = new JScrollPane(tblRecent);
        spRecent.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        spRecent.getViewport().setBackground(ThemeConfig.BG_CARD);
        row.add(spRecent, BorderLayout.CENTER);

        return row;
    }
    private JLabel statLbl() {
        JLabel l = new JLabel("0");
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        return l;
    }

    private JPanel createMiniCard(String title, JLabel valLabel, Color titleColor) {
        RoundedPanel card = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(titleColor);

        valLabel.setForeground(ThemeConfig.TEXT_HEAD);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(valLabel);
        return card;
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(ThemeConfig.BG_CARD);
        tbl.setForeground(ThemeConfig.TEXT_BODY);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setRowHeight(30);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(0x2A, 0x28, 0x48));
        tbl.setSelectionBackground(new Color(0x3A, 0x38, 0x60));
        tbl.setSelectionForeground(ThemeConfig.ACCENT);
        JTableHeader h = tbl.getTableHeader();
        h.setBackground(ThemeConfig.BG_PRIMARY);
        h.setForeground(ThemeConfig.TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setPreferredSize(new Dimension(0, 32));
    }
    private JFreeChart createDarkLineChart(DefaultCategoryDataset dataset, String title) {
        JFreeChart chart = ChartFactory.createLineChart(title, "", "Omzet (Rp)", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(ThemeConfig.BG_CARD);
        chart.getTitle().setPaint(ThemeConfig.TEXT_HEAD);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(ThemeConfig.BG_CARD);
        plot.setOutlinePaint(null);
        plot.setDomainGridlinePaint(new Color(0x2A, 0x28, 0x48));
        plot.setRangeGridlinePaint(new Color(0x2A, 0x28, 0x48));
        plot.getDomainAxis().setLabelPaint(ThemeConfig.TEXT_MUTED);
        plot.getDomainAxis().setTickLabelPaint(ThemeConfig.TEXT_BODY);
        plot.getRangeAxis().setLabelPaint(ThemeConfig.TEXT_MUTED);
        plot.getRangeAxis().setTickLabelPaint(ThemeConfig.TEXT_BODY);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0x4A, 0x90, 0xE2));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        return chart;
    }

    private JFreeChart createDarkBarChart(DefaultCategoryDataset dataset, String title) {
        JFreeChart chart = ChartFactory.createBarChart(title, "", "Omzet (Rp)", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(ThemeConfig.BG_CARD);
        chart.getTitle().setPaint(ThemeConfig.TEXT_HEAD);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(ThemeConfig.BG_CARD);
        plot.setOutlinePaint(null);
        plot.setDomainGridlinePaint(new Color(0x2A, 0x28, 0x48));
        plot.setRangeGridlinePaint(new Color(0x2A, 0x28, 0x48));
        plot.getDomainAxis().setTickLabelPaint(ThemeConfig.TEXT_BODY);
        plot.getRangeAxis().setTickLabelPaint(ThemeConfig.TEXT_BODY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ThemeConfig.ACCENT);
        renderer.setShadowVisible(false);
        return chart;
    }

    private JFreeChart createDarkPieChart(DefaultPieDataset dataset, String title) {
        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        chart.setBackgroundPaint(ThemeConfig.BG_CARD);
        chart.getTitle().setPaint(ThemeConfig.TEXT_HEAD);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getLegend().setBackgroundPaint(ThemeConfig.BG_CARD);
        chart.getLegend().setItemPaint(ThemeConfig.TEXT_BODY);
        chart.getLegend().setBorder(0,0,0,0);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(ThemeConfig.BG_CARD);
        plot.setOutlinePaint(null);
        plot.setLabelBackgroundPaint(ThemeConfig.BG_CARD);
        plot.setLabelPaint(ThemeConfig.TEXT_HEAD);
        plot.setLabelShadowPaint(null);
        plot.setLabelOutlinePaint(new Color(0x2A, 0x28, 0x48));
        return chart;
    }
    private void loadTopProduk() {
        new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground() throws Exception {
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
               String sql = "SELECT p.id_penjualan, " +
                             "COALESCE(pl.nama_pelanggan, 'Umum / Non-Member') AS nama_pembeli, " +
                             "(SELECT COALESCE(SUM(qty), 0) FROM tb_detail_penjualan WHERE id_penjualan = p.id_penjualan) AS jml_item, " +
                             "p.total_harga, " +
                             "p.tanggal " +
                             "FROM tb_penjualan p " +
                             "LEFT JOIN tb_pelanggan pl ON p.id_pelanggan = pl.id_pelanggan " +
                             "ORDER BY p.tanggal DESC LIMIT 10";
                
                try (Connection conn = Koneksi.configDB();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    int no = 1;

                    while (rs.next()) {
                        String nama = rs.getString("nama_produk");
                        String harga = rp.format(rs.getDouble("harga_jual")).replace(",00", "");
                        int qty = rs.getInt("total_qty");
                        String total = rp.format(rs.getDouble("total_nilai")).replace(",00", "");
                        rows.add(new Object[]{no++, nama, harga, qty, total});
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    modelTopProduk.setRowCount(0);
                    for (Object[] row : get()) {
                        modelTopProduk.addRow(row);
                    }
                } catch (Exception e) {
                    String msg = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Gagal memuat Top Produk!\nError: " + msg, "Error SQL", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadTransaksiTerbaru() {
        new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground() throws Exception {
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                String sql = "SELECT p.id_penjualan, " +
                             "COALESCE(pl.nama_pelanggan, 'Umum / Non-Member') AS nama_pembeli, " +
                             "COALESCE(SUM(dp.qty), 0) AS jml_item, " +
                             "p.total_harga, " +
                             "p.tanggal " +
                             "FROM tb_penjualan p " +
                             "LEFT JOIN tb_pelanggan pl ON p.id_pelanggan = pl.id_pelanggan " +
                             "LEFT JOIN tb_detail_penjualan dp ON p.id_penjualan = dp.id_penjualan " +
                             "GROUP BY p.id_penjualan, pl.nama_pelanggan, p.total_harga, p.tanggal " +
                             "ORDER BY p.tanggal DESC LIMIT 10";
                             
                try (Connection conn = Koneksi.configDB();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm");
                    int no = 1;
                    
                    while (rs.next()) {
                        String pembeli = rs.getString("nama_pembeli");
                        int jmlItem = rs.getInt("jml_item");
                        String nilai = rp.format(rs.getDouble("total_harga")).replace(",00", "");

                        Timestamp ts = rs.getTimestamp("tanggal");
                        String tgl = (ts != null) ? sdf.format(ts) : "-";
                        String status = "Lunas"; 

                        rows.add(new Object[]{no++, pembeli, jmlItem, nilai, tgl, status});
                    }
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    modelTransaksiTerbaru.setRowCount(0);
                    for (Object[] row : get()) {
                        modelTransaksiTerbaru.addRow(row);
                    }
                } catch (Exception e) {
                    String msg = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Gagal memuat Transaksi Terbaru!\nError: " + msg, "Error SQL", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadChartAndCardsData() {
        new SwingWorker<java.util.Map<String, Object>, Void>() {
            @Override
            protected java.util.Map<String, Object> doInBackground() throws Exception {
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                java.util.List<Object[]> lineData = new java.util.ArrayList<>();
                java.util.List<Object[]> barData = new java.util.ArrayList<>();
                java.util.List<Object[]> pieData = new java.util.ArrayList<>();
                
                try (Connection conn = Koneksi.configDB();
                     Statement stmt = conn.createStatement()) {

                    String sqlCards = "SELECT " +
                        "(SELECT COALESCE(SUM(dp.qty), 0) FROM tb_detail_penjualan dp JOIN tb_penjualan p ON dp.id_penjualan = p.id_penjualan WHERE MONTH(p.tanggal) = MONTH(CURDATE()) AND YEAR(p.tanggal) = YEAR(CURDATE())) AS total_item, " +
                        "(SELECT COUNT(*) FROM tb_penjualan WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE())) AS total_trx, " +
                        "(SELECT COALESCE(SUM(total_harga), 0) FROM tb_penjualan WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE())) AS total_omzet, " +
                        "(SELECT COUNT(DISTINCT id_pelanggan) FROM tb_penjualan WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE()) AND id_pelanggan IS NOT NULL) AS pelanggan_aktif";
                        
                    ResultSet rsCards = stmt.executeQuery(sqlCards);
                    if (rsCards.next()) {
                        data.put("total_item", rsCards.getInt("total_item"));
                        data.put("total_trx", rsCards.getInt("total_trx"));
                        data.put("total_omzet", rsCards.getDouble("total_omzet"));
                        data.put("pelanggan_aktif", rsCards.getInt("pelanggan_aktif"));
                    }

                    ResultSet rsLine = stmt.executeQuery(
                        "SELECT DATE_FORMAT(tanggal, '%b') AS bulan, COALESCE(SUM(total_harga), 0) AS total " +
                        "FROM tb_penjualan WHERE YEAR(tanggal) = YEAR(CURDATE()) " +
                        "GROUP BY MONTH(tanggal), DATE_FORMAT(tanggal, '%b') ORDER BY MONTH(tanggal)"
                    );
                    while (rsLine.next()) {
                        lineData.add(new Object[]{rsLine.getDouble("total"), "Omzet", rsLine.getString("bulan")});
                    }

                    ResultSet rsBar = stmt.executeQuery(
                        "SELECT DATE_FORMAT(tanggal, '%d %b') AS hari, COALESCE(SUM(total_harga), 0) AS total " +
                        "FROM tb_penjualan WHERE tanggal >= DATE(NOW()) - INTERVAL 6 DAY " +
                        "GROUP BY DATE(tanggal), DATE_FORMAT(tanggal, '%d %b') ORDER BY DATE(tanggal)"
                    );
                    while (rsBar.next()) {
                        barData.add(new Object[]{rsBar.getDouble("total"), "Omzet", rsBar.getString("hari")});
                    }

                    ResultSet rsPie = stmt.executeQuery(
                        "SELECT p.brand, COALESCE(SUM(dp.qty), 0) as qty FROM tb_detail_penjualan dp " +
                        "JOIN tb_produk p ON dp.id_produk = p.id_produk " +
                        "GROUP BY p.brand ORDER BY qty DESC LIMIT 5"
                    );
                    while (rsPie.next()) {
                        pieData.add(new Object[]{rsPie.getString("brand"), rsPie.getInt("qty")});
                    }
                }
                
                data.put("lineData", lineData);
                data.put("barData", barData);
                data.put("pieData", pieData);
                return data;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    java.util.Map<String, Object> data = get();

                    NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    lblTotalItem.setText(String.valueOf(data.getOrDefault("total_item", 0)));
                    lblTotalTransaksi.setText(String.valueOf(data.getOrDefault("total_trx", 0)));
                    lblTotalOmzet.setText(rp.format(data.getOrDefault("total_omzet", 0.0)).replace(",00", ""));
                    lblTotalPelanggan.setText(String.valueOf(data.getOrDefault("pelanggan_aktif", 0)));

                    lineDataset.clear();
                    for (Object[] row : (java.util.List<Object[]>) data.get("lineData")) {
                        lineDataset.addValue((Double) row[0], (String) row[1], (String) row[2]);
                    }

                    barDataset.clear();
                    for (Object[] row : (java.util.List<Object[]>) data.get("barData")) {
                        barDataset.addValue((Double) row[0], (String) row[1], (String) row[2]);
                    }

                    pieDataset.clear();
                    for (Object[] row : (java.util.List<Object[]>) data.get("pieData")) {
                        pieDataset.setValue((String) row[0], (Integer) row[1]);
                    }

                } catch (Exception e) {
                    String msg = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                    JOptionPane.showMessageDialog(LaporanPanel.this, "Gagal memuat Grafik!\nError: " + msg, "Error SQL", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}