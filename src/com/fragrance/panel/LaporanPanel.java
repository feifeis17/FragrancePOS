package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.ThemeConfig;
import com.fragrance.util.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class LaporanPanel extends JPanel {

    private DefaultCategoryDataset barDataset;
    private DefaultPieDataset pieDataset;
    
    private JPanel pnlBarChart, pnlPieChart;
    private JLabel lblTotalOmzet, lblTotalTransaksi;

    public LaporanPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeConfig.BG_PRIMARY);
        setBorder(new EmptyBorder(0, 0, 10, 0));

        initUI();
        loadChartData();
    }
//ui
    private void initUI() {
        JPanel topBar = new JPanel(new GridLayout(1, 2, 16, 0));
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 80));

        lblTotalOmzet = new JLabel("Rp 0", SwingConstants.CENTER);
        lblTotalOmzet.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalOmzet.setForeground(new Color(0x81, 0xC9, 0x95)); 

        lblTotalTransaksi = new JLabel("0 Transaksi", SwingConstants.CENTER);
        lblTotalTransaksi.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalTransaksi.setForeground(ThemeConfig.ACCENT); 

        topBar.add(createMiniCard("TOTAL OMZET (BULAN INI)", lblTotalOmzet));
        topBar.add(createMiniCard("TOTAL TRANSAKSI (BULAN INI)", lblTotalTransaksi));

        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        barDataset = new DefaultCategoryDataset();
        pieDataset = new DefaultPieDataset();

        JFreeChart barChart = createDarkBarChart(barDataset);
        JFreeChart pieChart = createDarkPieChart(pieDataset);

        ChartPanel cpBar = new ChartPanel(barChart);
        cpBar.setOpaque(false);
        cpBar.setBackground(new Color(0,0,0,0)); 

        ChartPanel cpPie = new ChartPanel(pieChart);
        cpPie.setOpaque(false);
        cpPie.setBackground(new Color(0,0,0,0));

        pnlBarChart = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        pnlBarChart.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlBarChart.add(cpBar, BorderLayout.CENTER);

        pnlPieChart = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        pnlPieChart.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlPieChart.add(cpPie, BorderLayout.CENTER);

        centerGrid.add(pnlBarChart);
        centerGrid.add(pnlPieChart);

        add(topBar, BorderLayout.NORTH);
        add(centerGrid, BorderLayout.CENTER);
    }

    private JPanel createMiniCard(String title, JLabel valLabel) {
        RoundedPanel card = new RoundedPanel(12, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(ThemeConfig.TEXT_MUTED);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        valLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(valLabel);
        return card;
    }
//jfreechart
    private JFreeChart createDarkBarChart(DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                "TREN PENJUALAN BULANAN", "Bulan", "Omzet (Rp)", 
                dataset, PlotOrientation.VERTICAL, false, true, false);

        chart.setBackgroundPaint(ThemeConfig.BG_CARD);
        chart.getTitle().setPaint(ThemeConfig.TEXT_HEAD);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(ThemeConfig.BG_PRIMARY);
        plot.setOutlinePaint(null);

        plot.setDomainGridlinePaint(new Color(0x2A, 0x28, 0x48));
        plot.setRangeGridlinePaint(new Color(0x2A, 0x28, 0x48));

        plot.getDomainAxis().setLabelPaint(ThemeConfig.TEXT_MUTED);
        plot.getDomainAxis().setTickLabelPaint(ThemeConfig.TEXT_BODY);
        plot.getRangeAxis().setLabelPaint(ThemeConfig.TEXT_MUTED);
        plot.getRangeAxis().setTickLabelPaint(ThemeConfig.TEXT_BODY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, ThemeConfig.ACCENT); 
        renderer.setShadowVisible(false);

        return chart;
    }

    private JFreeChart createDarkPieChart(DefaultPieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(
                "TOP 5 PARFUM TERLARIS", dataset, true, true, false);

        chart.setBackgroundPaint(ThemeConfig.BG_CARD);
        chart.getTitle().setPaint(ThemeConfig.TEXT_HEAD);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));

        chart.getLegend().setBackgroundPaint(ThemeConfig.BG_CARD);
        chart.getLegend().setItemPaint(ThemeConfig.TEXT_BODY);
        chart.getLegend().setBorder(0, 0, 0, 0);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(ThemeConfig.BG_PRIMARY);
        plot.setOutlinePaint(null);
        plot.setLabelBackgroundPaint(ThemeConfig.BG_CARD);
        plot.setLabelPaint(ThemeConfig.TEXT_HEAD);
        plot.setLabelShadowPaint(null);
        plot.setLabelOutlinePaint(new Color(0x2A, 0x28, 0x48));

        Color[] palette = {
            ThemeConfig.ACCENT,              // Gold
            new Color(0x81, 0xC9, 0x95),     // Emerald Green
            new Color(0x6A, 0x5A, 0xCD),     // Slate Blue
            new Color(0xF4, 0xA2, 0x61),     // Amber Orange
            new Color(0x3A, 0x38, 0x60)      // Muted Purple
        };
        
        plot.addChangeListener(e -> {
            for (int i = 0; i < dataset.getItemCount(); i++) {
                plot.setSectionPaint(dataset.getKey(i), palette[i % palette.length]);
            }
        });

        return chart;
    }
//db
    private void loadChartData() {
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                Map<String, Object> data = new HashMap<>();
                
                // Struktur penampung sementara
                java.util.List<Object[]> barData = new ArrayList<>();
                java.util.List<Object[]> pieData = new ArrayList<>();
                double totalOmzet = 0;
                int totalTransaksi = 0;

                try (Connection conn = Koneksi.configDB();
                     Statement stmt = conn.createStatement()) {

                    // 1. Ambil Ringkasan Atas (Bulan Berjalan)
                    ResultSet rsSum = stmt.executeQuery(
                        "SELECT COALESCE(SUM(total_harga),0), COUNT(id_penjualan) " +
                        "FROM tb_penjualan WHERE MONTH(tanggal) = MONTH(CURDATE()) AND YEAR(tanggal) = YEAR(CURDATE())");
                    if (rsSum.next()) {
                        totalOmzet = rsSum.getDouble(1);
                        totalTransaksi = rsSum.getInt(2);
                    }

                    // 2. Ambil Data Tren Omzet Bulanan (Bar Chart)
                    ResultSet rsBar = stmt.executeQuery(
                        "SELECT DATE_FORMAT(tanggal, '%b') AS bulan, SUM(total_harga) AS total " +
                        "FROM tb_penjualan WHERE YEAR(tanggal) = YEAR(CURDATE()) " +
                        "GROUP BY MONTH(tanggal) ORDER BY MONTH(tanggal)");
                    while (rsBar.next()) {
                        barData.add(new Object[]{rsBar.getDouble("total"), "Omzet", rsBar.getString("bulan")});
                    }

                    // 3. Ambil Data Parfum Terlaris (Pie Chart)
                    ResultSet rsPie = stmt.executeQuery(
                        "SELECT p.nama_produk, SUM(dp.qty) AS qty " +
                        "FROM tb_detail_penjualan dp " +
                        "JOIN tb_produk p ON dp.id_produk = p.id_produk " +
                        "GROUP BY dp.id_produk ORDER BY qty DESC LIMIT 5");
                    while (rsPie.next()) {
                        pieData.add(new Object[]{rsPie.getString("nama_produk"), rsPie.getInt("qty")});
                    }
                }

                data.put("totalOmzet", totalOmzet);
                data.put("totalTransaksi", totalTransaksi);
                data.put("barData", barData);
                data.put("pieData", pieData);
                return data;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> res = get();
                    
                    // Update Text Atas
                    double omzet = (double) res.get("totalOmzet");
                    int trx = (int) res.get("totalTransaksi");
                    NumberFormat rp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    lblTotalOmzet.setText(rp.format(omzet).replace(",00", ""));
                    lblTotalTransaksi.setText(trx + " Transaksi");

                    // Update Bar Chart Dataset
                    barDataset.clear();
                    java.util.List<Object[]> barList = (java.util.List<Object[]>) res.get("barData");
                    for (Object[] b : barList) {
                        barDataset.addValue((Double) b[0], (String) b[1], (String) b[2]);
                    }

                    // Update Pie Chart Dataset
                    pieDataset.clear();
                    java.util.List<Object[]> pieList = (java.util.List<Object[]>) res.get("pieData");
                    for (Object[] p : pieList) {
                        pieDataset.setValue((String) p[0], (Integer) p[1]);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}