package com.fragrance.ui;

import com.fragrance.util.Koneksi;
import com.fragrance.util.SecurityHelper;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame { 

    private JTextField    txtUsername;
    private JPasswordField txtPassword;
    private JButton       btnLogin;
    private JLabel        lblStatus;
    private JCheckBox     chkShowPass;

    public LoginFrame() {
        setTitle("FragrancePOS — Login");
        setSize(820, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    // ─────────────────────────────────────────────
    // UI SETUP
    // ─────────────────────────────────────────────
    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(ThemeConfig.BG_PRIMARY);

        root.add(buildLeftPanel());
        root.add(buildRightPanel());

        setContentPane(root);
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel();
        left.setBackground(ThemeConfig.BG_SIDEBAR);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));

        JLabel lblIcon = new JLabel("🌸");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblApp = new JLabel("FragrancePOS");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblApp.setForeground(ThemeConfig.ACCENT);
        lblApp.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(100, 1));
        sep.setForeground(ThemeConfig.ACCENT);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTagline = new JLabel(
            "<html><center>Sistem Manajemen Stok<br>& Penjualan Parfum</center></html>"
        );
        lblTagline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTagline.setForeground(ThemeConfig.TEXT_MUTED);
        lblTagline.setHorizontalAlignment(SwingConstants.CENTER);
        lblTagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblVer = new JLabel("v1.0 · Teknik Informatika UTB");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVer.setForeground(new Color(0x3A, 0x38, 0x60));
        lblVer.setAlignmentX(Component.CENTER_ALIGNMENT);

        left.add(Box.createVerticalGlue());
        left.add(lblIcon);
        left.add(Box.createVerticalStrut(14));
        left.add(lblApp);
        left.add(Box.createVerticalStrut(12));
        left.add(sep);
        left.add(Box.createVerticalStrut(14));
        left.add(lblTagline);
        left.add(Box.createVerticalGlue());
        left.add(lblVer);

        return left;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(ThemeConfig.BG_PRIMARY);

        JPanel form = new JPanel();
        form.setBackground(ThemeConfig.BG_PRIMARY);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel lblTitle = new JLabel("Selamat Datang");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeConfig.TEXT_HEAD);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Masuk untuk melanjutkan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeConfig.TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = styledTextField();
        txtUsername.setToolTipText("Masukkan username");

        txtPassword = new JPasswordField();
        stylePasswordField(txtPassword);
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });

        chkShowPass = new JCheckBox("Tampilkan password");
        chkShowPass.setBackground(ThemeConfig.BG_PRIMARY);
        chkShowPass.setForeground(ThemeConfig.TEXT_MUTED);
        chkShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkShowPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPass.addActionListener(e ->
            txtPassword.setEchoChar(chkShowPass.isSelected() ? (char) 0 : '•')
        );

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(ThemeConfig.DANGER);
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnLogin = new JButton("Masuk");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setBackground(ThemeConfig.ACCENT);
        btnLogin.setForeground(ThemeConfig.ACCENT_TEXT);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> doLogin());

        form.add(lblTitle);
        form.add(Box.createVerticalStrut(4));
        form.add(lblSub);
        form.add(Box.createVerticalStrut(28));
        form.add(fieldLabel("Username"));
        form.add(Box.createVerticalStrut(6));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(16));
        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(6));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(8));
        form.add(chkShowPass);
        form.add(Box.createVerticalStrut(4));
        form.add(lblStatus);
        form.add(Box.createVerticalStrut(16));
        form.add(btnLogin);

        right.add(form);
        return right;
    }

    // ─────────────────────────────────────────────
    // LOGIC LOGIN
    // ─────────────────────────────────────────────
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Username dan password tidak boleh kosong", false);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Memeriksa...");
        lblStatus.setText(" ");

        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            int    foundId   = -1;
            String foundUser = null;
            String foundRole = null;

            @Override
            protected String[] doInBackground() throws Exception {
                // SINKRONISASI 1: Sesuaikan dengan method di SecurityHelper
                String hashed = SecurityHelper.hashPassword(password);

                String sql = "SELECT u.id_user, u.username, r.nama_role " +
                             "FROM tb_user u " +
                             "JOIN tb_role r ON u.id_role = r.id_role " +
                             "WHERE u.username = ? AND u.password = ?";

                // SINKRONISASI 2: Sesuaikan dengan method di Koneksi
                try (Connection conn = Koneksi.configDB();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, username);
                    ps.setString(2, hashed);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        foundId   = rs.getInt("id_user");
                        foundUser = rs.getString("username");
                        foundRole = rs.getString("nama_role");

                        // Catat waktu login ke tb_log_user
                        String logSql = "INSERT INTO tb_log_user (id_user, waktu_login) VALUES (?, NOW())";
                        try (PreparedStatement lps = conn.prepareStatement(logSql)) {
                            lps.setInt(1, foundId);
                            lps.executeUpdate();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Masuk");

                try {
                    get(); 
                } catch (Exception ex) {
                    setStatus("Gagal koneksi database: " + ex.getMessage(), false);
                    return;
                }

                if (foundRole == null) {
                    setStatus("Username atau password salah", false);
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                } else {
                    SessionManager.setSession(foundId, foundUser, foundRole);
                    SwingUtilities.invokeLater(() -> {
                        
                        JOptionPane.showMessageDialog(null, "Berhasil Login!\nRole Anda: " + foundRole, "Akses Diterima", JOptionPane.INFORMATION_MESSAGE);
                        new MainFrame().setVisible(true); 
                        dispose();
                    });
                }
            }
        };

        worker.execute();
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    private void setStatus(String msg, boolean isSuccess) {
        lblStatus.setForeground(isSuccess ? ThemeConfig.SUCCESS : ThemeConfig.DANGER);
        lblStatus.setText(msg);
    }

    private JTextField styledTextField() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(ThemeConfig.BG_CARD);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private void stylePasswordField(JPasswordField f) {
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(ThemeConfig.BG_CARD);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setEchoChar('•');
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeConfig.TEXT_BODY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}