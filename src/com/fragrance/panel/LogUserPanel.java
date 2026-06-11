package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LogUserPanel extends JPanel {

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        txtSearch;

    public LogUserPanel() {
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

// Search Field
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari username / role...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(txtSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

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
        String[] cols = {"ID Log", "Username", "Role", "Waktu Login", "Waktu Logout"};
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

        DefaultTableCellRenderer padLeft = new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0)); setBackground(ThemeConfig.BG_TABLE); setForeground(ThemeConfig.TEXT_BODY);}
        };

        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0)); setBackground(ThemeConfig.BG_TABLE); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                l.setForeground(ThemeConfig.TEXT_HEAD);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                return l;
            }
        });

        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                String role = val != null ? val.toString() : "";
                JLabel badge = new JLabel(role);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setHorizontalAlignment(CENTER);
                
                Color fg, bg;
                if ("Admin".equals(role)) { fg = ThemeConfig.ACCENT; bg = new Color(0x40, 0x33, 0x15); } 
                else { fg = ThemeConfig.TEXT_MUTED; bg = new Color(0x2A, 0x28, 0x48); }
                
                badge.setForeground(fg);
                badge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(fg, 1, true),
                    BorderFactory.createEmptyBorder(2, 10, 2, 10)
                ));

                JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
                wrap.setBackground(sel ? t.getSelectionBackground() : ThemeConfig.BG_TABLE);
                wrap.add(badge);
                return wrap;
            }
        });

        table.getColumnModel().getColumn(3).setCellRenderer(padLeft); // Waktu Login

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            { setBorder(new EmptyBorder(0, 14, 0, 0)); setBackground(ThemeConfig.BG_TABLE); }
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (val == null || val.toString().equals("-") || val.toString().isEmpty()) {
                    l.setText("● Sedang Aktif");
                    l.setForeground(ThemeConfig.SUCCESS);
                    l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    l.setForeground(ThemeConfig.TEXT_MUTED);
                    l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return l;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(250);
    }
//load data
    private void loadData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
                
                try (Connection conn = Koneksi.configDB();
                     ResultSet rs = conn.createStatement().executeQuery(
                         "SELECT l.id_log, u.username, u.role, l.waktu_login, l.waktu_logout " +
                         "FROM tb_log_user l " +
                         "JOIN tb_user u ON l.id_user = u.id_user " +
                         "ORDER BY l.waktu_login DESC")) {
                    
                    while (rs.next()) {
                        Timestamp tLogin = rs.getTimestamp("waktu_login");
                        Timestamp tLogout = rs.getTimestamp("waktu_logout");
                        
                        rows.add(new Object[]{
                            rs.getInt("id_log"),
                            rs.getString("username"),
                            rs.getString("role"),
                            tLogin != null ? sdf.format(tLogin) : "-",
                            tLogout != null ? sdf.format(tLogout) : "-"
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

    private void filterTable() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        String q = txtSearch.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + q, 1, 2));
    }
//helper
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
}