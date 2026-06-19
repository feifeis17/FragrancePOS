package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GantiPasswordPanel extends JPanel {

    private JPasswordField txtPasswordLama;
    private JPasswordField txtPasswordBaru;
    private JPasswordField txtKonfirmasi;

    public GantiPasswordPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeConfig.BG_PRIMARY);
        initUI();
    }
    private void initUI() {
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(ThemeConfig.BG_PRIMARY);
        RoundedPanel card = new RoundedPanel(16, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel lblTitle = new JLabel("Ubah Kata Sandi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeConfig.TEXT_HEAD);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Pastikan menggunakan kombinasi yang aman.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeConfig.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 16, 0);

        txtPasswordLama = formField();
        txtPasswordBaru = formField();
        txtKonfirmasi   = formField();

        addRow(form, g, 0, "Kata Sandi Saat Ini", txtPasswordLama);
        addRow(form, g, 1, "Kata Sandi Baru",     txtPasswordBaru);
        addRow(form, g, 2, "Konfirmasi Baru",     txtKonfirmasi);

        card.add(form);
        card.add(Box.createVerticalStrut(10));
        JButton btnSimpan = new JButton("Perbarui Kata Sandi") {
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
        btnSimpan.setForeground(ThemeConfig.ACCENT_TEXT);
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimpan.setContentAreaFilled(false);
        btnSimpan.setFocusPainted(false);
        btnSimpan.setBorderPainted(false);
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSimpan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnSimpan.addActionListener(e -> updatePassword());

        card.add(btnSimpan);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(ThemeConfig.TEXT_BODY);
        l.setPreferredSize(new Dimension(140, 36));
        p.add(l, g);
        
        g.gridx = 1; g.weightx = 1;
        p.add(field, g);
    }

    private JPasswordField formField() {
        JPasswordField f = new JPasswordField();
        f.setPreferredSize(new Dimension(200, 38));
        f.setBackground(ThemeConfig.BG_PRIMARY);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        return f;
    }
    private void updatePassword() {
        String passLama = new String(txtPasswordLama.getPassword());
        String passBaru = new String(txtPasswordBaru.getPassword());
        String konfirm  = new String(txtKonfirmasi.getPassword());

        if (passLama.isEmpty() || passBaru.isEmpty() || konfirm.isEmpty()) {
            showInfo("Semua kolom harus diisi!", true);
            return;
        }

        if (!passBaru.equals(konfirm)) {
            showInfo("Kata sandi baru dan konfirmasi tidak cocok!", true);
            return;
        }

        int idUser = SessionManager.getUserId();

        try (Connection conn = Koneksi.configDB()) {
            PreparedStatement cekPs = conn.prepareStatement("SELECT password FROM tb_user WHERE id_user = ?");
            cekPs.setInt(1, idUser);
            ResultSet rs = cekPs.executeQuery();

            if (rs.next()) {
                String dbPassLama = rs.getString("password");
                if (!dbPassLama.equals(passLama)) {
                    showInfo("Kata sandi saat ini salah!", true);
                    return;
                }
            } else {
                showInfo("User tidak ditemukan di sistem.", true);
                return;
            }
            PreparedStatement updPs = conn.prepareStatement("UPDATE tb_user SET password = ? WHERE id_user = ?");
            updPs.setString(1, passBaru);
            updPs.setInt(2, idUser);
            updPs.executeUpdate();
            txtPasswordLama.setText("");
            txtPasswordBaru.setText("");
            txtKonfirmasi.setText("");

            JOptionPane.showMessageDialog(this, 
                "Kata sandi berhasil diperbarui!\nSandi baru akan aktif pada login berikutnya.", 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showInfo(String msg, boolean isWarning) {
        JOptionPane.showMessageDialog(this, msg, isWarning ? "Peringatan" : "Info", 
            isWarning ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }
}