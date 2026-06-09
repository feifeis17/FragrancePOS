package com.fragrance.ui;

import com.fragrance.panel.DashboardPanel;
import com.fragrance.util.Koneksi;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    // ── Instance fields ────────────────────────────────
    private JPanel  contentPanel;
    private JLabel  lblHeaderTitle;
    private JButton activeBtn;

    // Tombol sidebar (perlu diakses di applyRBAC)
    private JButton btnDashboard, btnKatalog, btnTransaksi,
                    btnStokMasuk, btnMaster, btnLogUser;

    // ── Constructor ────────────────────────────────────
    public MainFrame() {
        setTitle("FragrancePOS");
        setSize(1100, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        applyRBAC();
        // Buka dashboard sebagai panel awal
        btnDashboard.doClick();
    }

    // ─────────────────────────────────────────────
    // UI SETUP
    // ─────────────────────────────────────────────
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ThemeConfig.BG_PRIMARY);
        root.add(buildSidebar(),      BorderLayout.WEST);
        root.add(buildMainContent(),  BorderLayout.CENTER);
        setContentPane(root);
    }

    // ─────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────
    private JPanel buildSidebar() {
    JPanel sidebar = new JPanel();
    sidebar.setPreferredSize(new Dimension(210, 0));
    sidebar.setBackground(ThemeConfig.BG_SIDEBAR);
    sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
    sidebar.setBorder(new EmptyBorder(24, 8, 16, 8));

    // ── Profil user — tanpa simbol kotak ──
    JPanel profilePanel = new JPanel();
    profilePanel.setOpaque(false);
    profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
    profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    profilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
    profilePanel.setBorder(new EmptyBorder(0, 10, 0, 0));

    JLabel lblUser = new JLabel(SessionManager.getUsername());
    lblUser.setFont(new Font("Segoe UI", Font.BOLD, 15));
    lblUser.setForeground(ThemeConfig.ACCENT);

    // Role dibungkus pill kecil
    JLabel lblRole = new JLabel("  " + SessionManager.getRole() + "  ");
    lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
    lblRole.setForeground(ThemeConfig.TEXT_MUTED);
    lblRole.setBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true));

    profilePanel.add(lblUser);
    profilePanel.add(Box.createVerticalStrut(4));
    profilePanel.add(lblRole);

    // Divider
    JSeparator sep = new JSeparator();
    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    sep.setForeground(new Color(0x2A, 0x28, 0x48));

    sidebar.add(profilePanel);
    sidebar.add(Box.createVerticalStrut(16));
    sidebar.add(sep);
    sidebar.add(Box.createVerticalStrut(10));

    // ── Section: MASTER ──
    sidebar.add(sectionLabel("MASTER"));
    btnDashboard = createMenuButton("📊", "Dashboard");
    btnKatalog   = createMenuButton("🛍️", "Katalog Produk");
    btnMaster    = createMenuButton("📁", "Master Data");
    sidebar.add(btnDashboard);
    sidebar.add(btnKatalog);
    sidebar.add(btnMaster);
    sidebar.add(Box.createVerticalStrut(4));

    // ── Section: TRANSAKSI ──
    sidebar.add(sectionLabel("TRANSAKSI"));
    btnTransaksi = createMenuButton("🛒", "Transaksi Kasir");
    btnStokMasuk = createMenuButton("📦", "Stok Masuk");
    sidebar.add(btnTransaksi);
    sidebar.add(btnStokMasuk);
    sidebar.add(Box.createVerticalStrut(4));

    // ── Section: UTILITAS ──
    sidebar.add(sectionLabel("UTILITAS"));
    btnLogUser   = createMenuButton("🔐", "Log Aktivitas");
    sidebar.add(btnLogUser);

    // ActionListeners
   btnDashboard.addActionListener(e ->
    switchPanel(new DashboardPanel(), "Dashboard Overview", btnDashboard));
    btnKatalog.addActionListener(e    -> switchPanel(buildPlaceholder("Katalog"),        "Katalog Produk",      btnKatalog));
    btnMaster.addActionListener(e     -> switchPanel(buildPlaceholder("Master Data"),    "Master Data",         btnMaster));
    btnTransaksi.addActionListener(e  -> switchPanel(buildPlaceholder("Transaksi"),      "Transaksi Kasir",     btnTransaksi));
    btnStokMasuk.addActionListener(e  -> switchPanel(buildPlaceholder("Stok Masuk"),     "Stok Masuk",          btnStokMasuk));
    btnLogUser.addActionListener(e    -> switchPanel(buildPlaceholder("Log Aktivitas"),  "Log Aktivitas User",  btnLogUser));

    sidebar.add(Box.createVerticalGlue());

    // ── Keluar ──
    JSeparator sepBottom = new JSeparator();
    sepBottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    sepBottom.setForeground(new Color(0x2A, 0x28, 0x48));
    sidebar.add(sepBottom);
    sidebar.add(Box.createVerticalStrut(10));

    JButton btnLogout = createMenuButton("🚪", "Keluar");
    btnLogout.setForeground(ThemeConfig.DANGER);
    btnLogout.addActionListener(e -> doLogout());
    sidebar.add(btnLogout);

    return sidebar;
}

// Helper — label section kecil
private JLabel sectionLabel(String text) {
    JLabel l = new JLabel(text);
    l.setFont(new Font("Segoe UI", Font.BOLD, 10));
    l.setForeground(new Color(0x3A, 0x38, 0x60));
    l.setBorder(new EmptyBorder(10, 14, 4, 0));
    l.setAlignmentX(Component.LEFT_ALIGNMENT);
    l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
    return l;
}

    // Ganti parameter dari (iconFile, text) kembali ke (icon, text)
private JButton createMenuButton(String icon, String text) {
    JButton btn = new JButton("  " + icon + "  " + text);
    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    btn.setBackground(ThemeConfig.BG_SIDEBAR);
    btn.setForeground(ThemeConfig.TEXT_BODY);
    btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13)); 
    btn.setHorizontalAlignment(SwingConstants.LEFT);
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);
    btn.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    return btn;
}

    // ─────────────────────────────────────────────
    // MAIN CONTENT
    // ─────────────────────────────────────────────
    private JPanel buildMainContent() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ThemeConfig.BG_PRIMARY);
        wrapper.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header bar atas
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        lblHeaderTitle = new JLabel("Dashboard");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeaderTitle.setForeground(ThemeConfig.TEXT_HEAD);
        headerBar.add(lblHeaderTitle, BorderLayout.WEST);

        wrapper.add(headerBar, BorderLayout.NORTH);

        // Area konten — diganti-ganti via switchPanel()
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(ThemeConfig.BG_PRIMARY);
        wrapper.add(contentPanel, BorderLayout.CENTER);

        return wrapper;
    }

    // ─────────────────────────────────────────────
    // PANEL SWITCHING
    // ─────────────────────────────────────────────
    private void switchPanel(JPanel panel, String title, JButton sourceBtn) {
    lblHeaderTitle.setText(title);

    if (activeBtn != null) {
        activeBtn.setBackground(ThemeConfig.BG_SIDEBAR); 
        activeBtn.setForeground(ThemeConfig.TEXT_BODY);  
    }
    sourceBtn.setBackground(ThemeConfig.ACCENT);
    sourceBtn.setForeground(ThemeConfig.ACCENT_TEXT);
    activeBtn = sourceBtn;

    contentPanel.removeAll();
    contentPanel.add(panel, BorderLayout.CENTER);
    contentPanel.revalidate();
    contentPanel.repaint();
}

    // Placeholder sementara — nanti diganti ProdukPanel, dll.
    private JPanel buildPlaceholder(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(ThemeConfig.BG_PRIMARY);
        JLabel lbl = new JLabel("Panel " + name + " — coming soon");
        lbl.setForeground(ThemeConfig.TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(lbl);
        return p;
    }

    // RBAC — FIX: 
    private void applyRBAC() {
    String role = SessionManager.getRole();

    if (role.equals("Operator")) {
        btnMaster.setVisible(false);
        btnLogUser.setVisible(false);
    } else if (role.equals("User")) {
        btnTransaksi.setVisible(false);
        btnStokMasuk.setVisible(false);
        btnMaster.setVisible(false);
        btnLogUser.setVisible(false);
    }
    }

    // ─────────────────────────────────────────────
    // LOGOUT + LOG KE DB
    // ─────────────────────────────────────────────
    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin keluar dari sistem?",
            "Konfirmasi Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Catat waktu logout ke DB
        try (Connection conn = Koneksi.configDB();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE tb_log_user SET waktu_logout = NOW() " +
                "WHERE id_user = ? AND waktu_logout IS NULL")) {
            ps.setInt(1, SessionManager.getUserId());
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace(); // Tetap lanjut logout walau log gagal
        }

        SessionManager.clearSession();
        new LoginFrame().setVisible(true);
        dispose();
    }
}