package com.fragrance.ui;

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
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Profil user
        JLabel lblUser = new JLabel("👤 " + SessionManager.getUsername());
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblUser.setForeground(ThemeConfig.ACCENT);
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRole = new JLabel(SessionManager.getRole());
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(ThemeConfig.TEXT_MUTED);
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(180, 1));
        sep.setForeground(new Color(0x3D, 0x3B, 0x60));

        sidebar.add(lblUser);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(lblRole);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));

        // ── Menu items dengan ActionListener ──
        btnDashboard  = createMenuButton("📊", "Dashboard");
        btnKatalog    = createMenuButton("🛍️", "Katalog Produk");
        btnTransaksi  = createMenuButton("🛒", "Transaksi Kasir");
        btnStokMasuk  = createMenuButton("📦", "Stok Masuk");
        btnMaster     = createMenuButton("📁", "Master Data");
        btnLogUser    = createMenuButton("🔐", "Log Aktivitas");

        // ActionListeners — panel switching
        btnDashboard.addActionListener(e  -> switchPanel(buildPlaceholder("Dashboard"), "Dashboard Overview", btnDashboard));
        btnKatalog.addActionListener(e    -> switchPanel(buildPlaceholder("Katalog Produk"), "Katalog Produk", btnKatalog));
        btnTransaksi.addActionListener(e  -> switchPanel(buildPlaceholder("Transaksi Kasir"), "Transaksi Kasir", btnTransaksi));
        btnStokMasuk.addActionListener(e  -> switchPanel(buildPlaceholder("Stok Masuk"), "Stok Masuk", btnStokMasuk));
        btnMaster.addActionListener(e     -> switchPanel(buildPlaceholder("Master Data"), "Master Data", btnMaster));
        btnLogUser.addActionListener(e    -> switchPanel(buildPlaceholder("Log Aktivitas"), "Log Aktivitas User", btnLogUser));

        sidebar.add(btnDashboard);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnKatalog);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnTransaksi);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnStokMasuk);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnMaster);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnLogUser);

        sidebar.add(Box.createVerticalGlue());

        // Tombol logout
        JButton btnLogout = createMenuButton("🚪", "Keluar");
        btnLogout.setBackground(new Color(0x4A, 0x20, 0x20));
        btnLogout.setForeground(ThemeConfig.DANGER);
        btnLogout.addActionListener(e -> doLogout());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private JButton createMenuButton(String icon, String text) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBackground(ThemeConfig.BG_CARD);
        btn.setForeground(ThemeConfig.TEXT_HEAD);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
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
        // Update header
        lblHeaderTitle.setText(title);

        // Highlight tombol aktif
        if (activeBtn != null) {
            activeBtn.setBackground(ThemeConfig.BG_CARD);
            activeBtn.setForeground(ThemeConfig.TEXT_HEAD);
        }
        sourceBtn.setBackground(ThemeConfig.ACCENT);
        sourceBtn.setForeground(ThemeConfig.ACCENT_TEXT);
        activeBtn = sourceBtn;

        // Swap panel
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

    // ─────────────────────────────────────────────
    // RBAC — FIX: Admin lihat semua, bukan sebaliknya
    // ─────────────────────────────────────────────
    private void applyRBAC() {
        String role = SessionManager.getRole();
        switch (role) {
            case "Admin" -> {
                // Admin: semua menu terlihat, tidak perlu ubah apapun
            }
            case "Operator" -> {
                // Operator: tidak bisa akses Master & Log
                btnMaster.setVisible(false);
                btnLogUser.setVisible(false);
            }
            case "User" -> {
                // User (display): hanya Dashboard & Katalog (read-only)
                btnTransaksi.setVisible(false);
                btnStokMasuk.setVisible(false);
                btnMaster.setVisible(false);
                btnLogUser.setVisible(false);
            }
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