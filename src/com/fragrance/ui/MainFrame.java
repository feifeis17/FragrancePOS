package com.fragrance.ui;

import com.fragrance.panel.GantiPasswordPanel;
import com.fragrance.panel.KategoriPanel;
import com.fragrance.panel.LaporanPanel;
import com.fragrance.panel.LogUserPanel;
import com.fragrance.panel.PelangganPanel;
import com.fragrance.panel.PenjualanPanel;
import com.fragrance.panel.ProdukPanel;
import com.fragrance.panel.StokMasukPanel;
import com.fragrance.panel.SupplierPanel;
import com.fragrance.util.Koneksi;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private JPanel  contentPanel;
    private JLabel  lblHeaderTitle;
    private JLabel  lblSubtitle;
    private JButton activeBtn;

    private JButton btnProduk, btnSupplier, btnKategori, btnPelanggan,
                    btnStokMasuk, btnPenjualan, btnGantiPassword, btnLogUser,btnLaporan;

    //Constructor 
    public MainFrame() {
        setTitle("FragrancePOS");
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        applyRBAC();
        btnProduk.doClick(); 
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeConfig.BG_PRIMARY);
        root.add(buildSidebar(),     BorderLayout.WEST);
        root.add(buildMainContent(), BorderLayout.CENTER);
        setContentPane(root);
    }
    // SIDEBAR
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(ThemeConfig.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(24, 8, 16, 8));

        //Profil User
        JPanel profilePanel = new JPanel();
        profilePanel.setOpaque(false);
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        profilePanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel lblUser = new JLabel(SessionManager.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblUser.setForeground(ThemeConfig.ACCENT);

        JLabel lblRole = new JLabel("  " + SessionManager.getRole() + "  ");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblRole.setForeground(ThemeConfig.TEXT_MUTED);
        lblRole.setBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true));

        profilePanel.add(lblUser);
        profilePanel.add(Box.createVerticalStrut(4));
        profilePanel.add(lblRole);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(0x2A, 0x28, 0x48));

        sidebar.add(profilePanel);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));

        //MASTER
        sidebar.add(sectionLabel("MASTER"));
        btnProduk    = createMenuButton("produk",    "Produk");
        btnSupplier  = createMenuButton("supplier",  "Supplier");
        btnKategori  = createMenuButton("kategori",  "Kategori");
        btnPelanggan = createMenuButton("pelanggan", "Pelanggan");
        sidebar.add(btnProduk);
        sidebar.add(btnSupplier);
        sidebar.add(btnKategori);
        sidebar.add(btnPelanggan);
        sidebar.add(Box.createVerticalStrut(4));

        //TRANSAKSI
        sidebar.add(sectionLabel("TRANSAKSI"));
        btnStokMasuk = createMenuButton("stok",      "Stok Masuk");
        btnPenjualan = createMenuButton("penjualan", "Penjualan");
        btnLaporan = createMenuButton("report", "Laporan Bisnis");
        sidebar.add(btnStokMasuk);
        sidebar.add(btnPenjualan);
        sidebar.add(btnLaporan);
        sidebar.add(Box.createVerticalStrut(4));

        //UTILITAS
        sidebar.add(sectionLabel("UTILITAS"));
        btnGantiPassword = createMenuButton("password", "Ganti Password");
        btnLogUser       = createMenuButton("log",      "Log User");
        sidebar.add(btnGantiPassword);
        sidebar.add(btnLogUser);

        //ActionListeners
        btnProduk.addActionListener(e ->
            switchPanel(new ProdukPanel(), "Data Produk",
                "File Master › Produk", btnProduk));
        btnSupplier.addActionListener(e ->
            switchPanel(new SupplierPanel(), "Data Supplier", "File Master › Supplier", btnSupplier));
        btnKategori.addActionListener(e ->
            switchPanel(new KategoriPanel(), "Data Kategori",
                "File Master › Kategori", btnKategori));
        btnPelanggan.addActionListener(e ->
            switchPanel(new PelangganPanel(), "Data Pelanggan", "File Master › Pelanggan", btnPelanggan));
        btnStokMasuk.addActionListener(e ->
    switchPanel(new StokMasukPanel(), "Stok Masuk",
        "Transaksi › Stok Masuk", btnStokMasuk));
        btnPenjualan.addActionListener(e ->
            switchPanel(new PenjualanPanel(), "Transaksi Kasir", "Transaksi › Penjualan", btnPenjualan));
        btnGantiPassword.addActionListener(e ->
            switchPanel(new GantiPasswordPanel(), "Ganti Password", "Utilitas › Ganti Password", btnGantiPassword));
        btnLogUser.addActionListener(e ->
            switchPanel(new LogUserPanel(), "Log Aktivitas User", "Utilitas › Log User", btnLogUser));
            btnLaporan.addActionListener(e -> 
    switchPanel(new LaporanPanel(), "Laporan Analitik", "Transaksi › Laporan Bisnis", btnLaporan)
);

        sidebar.add(Box.createVerticalGlue());

        //Logout
        JSeparator sepBottom = new JSeparator();
        sepBottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sepBottom.setForeground(new Color(0x2A, 0x28, 0x48));
        sidebar.add(sepBottom);
        sidebar.add(Box.createVerticalStrut(10));

        JButton btnLogout = createMenuButton("logout", "Keluar");
        btnLogout.setForeground(ThemeConfig.DANGER);
        btnLogout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btnLogout.setForeground(ThemeConfig.DANGER);
            }
            @Override public void mouseExited(MouseEvent e) {
                btnLogout.setForeground(ThemeConfig.DANGER);
            }
        });
        btnLogout.addActionListener(e -> doLogout());
        sidebar.add(btnLogout);

        return sidebar;
    }
    // HELPERS SIDEBAR
    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(0x3A, 0x38, 0x60));
        l.setBorder(new EmptyBorder(10, 14, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    private ImageIcon loadScaledIcon(String filename, int size) {
        try {
            java.net.URL url = getClass().getResource("/com/fragrance/resources/icons/" + filename);
            if (url == null) return null;
            Image img = new ImageIcon(url).getImage()
                            .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) { return null; }
    }

    private JButton createMenuButton(String iconName, String text) {
        ImageIcon icWhite = loadScaledIcon(iconName + "_w.png", 16);
        ImageIcon icGold  = loadScaledIcon(iconName + "_g.png", 16);

        JButton btn = new JButton("  " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (this == activeBtn) {
                    g2.setColor(ThemeConfig.BG_CARD); 
                    // Menggambar background kapsul proporsional (margin atas/bawah 2px agar tidak menempel)
                    g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 16, 16); 
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 15)); 
                    g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 16, 16);
                }
                g2.dispose();
                super.paintComponent(g); 
            }
        };

        if (icWhite != null) { btn.setIcon(icWhite); btn.setIconTextGap(8); }
        btn.setPreferredSize(new Dimension(190, 42));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setMinimumSize(new Dimension(0, 42));
        
        btn.setForeground(ThemeConfig.TEXT_BODY);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
        btn.setContentAreaFilled(false); 
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);

        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 0)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.putClientProperty("icW", icWhite);
        btn.putClientProperty("icG", icGold);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) {
                    btn.setForeground(ThemeConfig.ACCENT);
                    if (icGold != null) btn.setIcon(icGold);
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeBtn) {
                    btn.setForeground(ThemeConfig.TEXT_BODY);
                    if (icWhite != null) btn.setIcon(icWhite);
                }
            }
        });

        return btn;
    }
    // MAIN CONTENT
    private JPanel buildMainContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ThemeConfig.BG_PRIMARY);
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));

        lblHeaderTitle = new JLabel("Data Produk");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeaderTitle.setForeground(ThemeConfig.TEXT_HEAD);

        lblSubtitle = new JLabel("File Master › Produk");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(ThemeConfig.TEXT_MUTED);

        titleStack.add(lblHeaderTitle);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(lblSubtitle);

        headerBar.add(titleStack, BorderLayout.WEST);
        wrapper.add(headerBar, BorderLayout.NORTH);

        // Area konten
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ThemeConfig.BG_PRIMARY);
        wrapper.add(contentPanel, BorderLayout.CENTER);

        return wrapper;
    }

    // PANEL SWITCHING
    private void switchPanel(JPanel panel, String title, String subtitle, JButton sourceBtn) {
        lblHeaderTitle.setText(title);
        lblSubtitle.setText(subtitle);
        if (activeBtn != null) {
            activeBtn.setForeground(ThemeConfig.TEXT_BODY);
            ImageIcon icW = (ImageIcon) activeBtn.getClientProperty("icW");
            if (icW != null) activeBtn.setIcon(icW);
        }

        activeBtn = sourceBtn;
        sourceBtn.setForeground(ThemeConfig.ACCENT); 
        ImageIcon icG = (ImageIcon) sourceBtn.getClientProperty("icG");
        if (icG != null) sourceBtn.setIcon(icG);
        if (sourceBtn.getParent() != null) {
            sourceBtn.getParent().repaint();
        }
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildPlaceholder(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ThemeConfig.BG_PRIMARY);
        JLabel lbl = new JLabel("Panel " + name + " — coming soon");
        lbl.setForeground(ThemeConfig.TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(lbl);
        return p;
    }
    // RBAC
    private void applyRBAC() {
        String role = SessionManager.getRole();
        if (role.equals("Operator")) {
            btnSupplier.setVisible(false);
            btnKategori.setVisible(false);
            btnPelanggan.setVisible(false);
            btnLogUser.setVisible(false);
        } else if (role.equals("User")) {
            btnSupplier.setVisible(false);
            btnKategori.setVisible(false);
            btnPelanggan.setVisible(false);
            btnStokMasuk.setVisible(false);
            btnPenjualan.setVisible(false);
            btnGantiPassword.setVisible(false);
            btnLogUser.setVisible(false);
            btnLaporan.setVisible(false);
        }
    }
    // LOGOUT + LOG KE DB
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin keluar dari sistem?",
            "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE tb_log_user SET waktu_logout = NOW() " +
                "WHERE id_user = ? AND waktu_logout IS NULL")) {
            ps.setInt(1, SessionManager.getUserId());
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SessionManager.clearSession();
        new LoginFrame().setVisible(true);
        dispose();
    }
}