package com.fragrance.panel;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.SecurityHelper;
import com.fragrance.util.SessionManager;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable tableUser;
    private JPasswordField txtPassLama, txtPassBaru, txtPassKonfirm;

    public UserPanel() {
        try {
            setLayout(new BorderLayout());
            setBackground(ThemeConfig.BG_PRIMARY);
            setBorder(new EmptyBorder(20, 20, 20, 20));
            if (SessionManager.isAdmin()) {
                initAdminUI();
                loadUserData();
            } else {
                initOperatorUI();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat User Panel: " + e.getMessage(), "Error Fatal UI", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void initAdminUI() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lblInfo = new JLabel("Manajemen Akun Sistem");
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblInfo.setForeground(ThemeConfig.TEXT_HEAD);

        JPanel btnArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnArea.setOpaque(false);

        JButton btnTambah = createGoldButton("+ Tambah User");
        btnTambah.addActionListener(e -> showAddUserDialog());

        JButton btnResetPass = createOutlineButton("Reset Password");
        btnResetPass.addActionListener(e -> {
            if (getSelectedId() == -1) { showInfo("Pilih user yang ingin di-reset passwordnya."); return; }
            showResetPasswordDialog();
        });

        JButton btnHapus = createDangerButton("Hapus User");
        btnHapus.addActionListener(e -> deleteUser());

        btnArea.add(btnTambah);
        btnArea.add(btnResetPass);
        btnArea.add(btnHapus);

        topBar.add(lblInfo, BorderLayout.WEST);
        topBar.add(btnArea, BorderLayout.EAST);

        String[] cols = {"ID", "Username", "Role / Hak Akses"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableUser = new JTable(tableModel);
        styleTable(tableUser);
        tableUser.getColumnModel().getColumn(0).setMinWidth(0);
        tableUser.getColumnModel().getColumn(0).setMaxWidth(0);
        
        JScrollPane sp = new JScrollPane(tableUser);
        sp.getViewport().setBackground(ThemeConfig.BG_TABLE);
        sp.setBorder(BorderFactory.createEmptyBorder());

        RoundedPanel tableWrap = new RoundedPanel(12, ThemeConfig.BG_TABLE, new Color(0x2A, 0x28, 0x48));
        tableWrap.setLayout(new BorderLayout());
        tableWrap.setBorder(new EmptyBorder(2, 2, 2, 2));
        tableWrap.add(sp, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(tableWrap, BorderLayout.CENTER);
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        String sql = "SELECT u.id_user, u.username, r.nama_role FROM tb_user u " +
                     "JOIN tb_role r ON u.id_role = r.id_role ORDER BY r.id_role ASC, u.username ASC";
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_user"),
                    rs.getString("username"),
                    rs.getString("nama_role")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tambah User Baru", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(ThemeConfig.BG_PRIMARY);
        content.setBorder(new EmptyBorder(20, 30, 20, 30));

        JTextField txtUser = styledTextField();
        JPasswordField txtPass = styledPasswordField();
        JComboBox<String> cmbRole = styledComboBox();

        Map<String, Integer> roleMap = new LinkedHashMap<>();
        try (Connection conn = Koneksi.configDB(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM tb_role")) {
            while (rs.next()) {
                roleMap.put(rs.getString("nama_role"), rs.getInt("id_role"));
                cmbRole.addItem(rs.getString("nama_role"));
            }
        } catch (Exception ignored) {}

        JPanel form = new JPanel(new GridLayout(6, 1, 0, 4));
        form.setOpaque(false);
        form.add(fieldLabel("Username"));
        form.add(txtUser);
        form.add(fieldLabel("Password"));
        form.add(txtPass);
        form.add(fieldLabel("Hak Akses (Role)"));
        form.add(cmbRole);

        JButton btnSimpan = createGoldButton("Simpan Akun");
        btnSimpan.setPreferredSize(new Dimension(0, 40));
        btnSimpan.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());
            if (user.isEmpty() || pass.isEmpty()) { showInfo("Username dan password wajib diisi!"); return; }

            try (Connection conn = Koneksi.configDB();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO tb_user (username, password, id_role, email) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, user);
                ps.setString(2, SecurityHelper.hashPassword(pass));
                ps.setInt(3, roleMap.get(cmbRole.getSelectedItem().toString()));

                String dummyEmail = user.toLowerCase().replaceAll("\\s+", "") + "@decium.com";
                ps.setString(4, dummyEmail); 

                ps.executeUpdate();

                loadUserData(); 
                dialog.dispose();

                JOptionPane.showMessageDialog(this, "User '" + user + "' berhasil ditambahkan!");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Gagal menambahkan user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        content.add(form, BorderLayout.CENTER);
        content.add(btnSimpan, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void showResetPasswordDialog() {
        int id = getSelectedId();
        String username = (String) tableModel.getValueAt(tableUser.getSelectedRow(), 1);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reset Password: " + username, true);
        dialog.setSize(380, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(ThemeConfig.BG_PRIMARY);
        content.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPasswordField txtPassBaru = styledPasswordField();
        JPanel form = new JPanel(new GridLayout(2, 1, 0, 4));
        form.setOpaque(false);
        form.add(fieldLabel("Ketik Password Baru"));
        form.add(txtPassBaru);

        JButton btnSimpan = createGoldButton("Reset Sekarang");
        btnSimpan.setPreferredSize(new Dimension(0, 40));
        btnSimpan.addActionListener(e -> {
            String pass = new String(txtPassBaru.getPassword());
            if (pass.isEmpty()) { showInfo("Password tidak boleh kosong!"); return; }
            
            try (Connection conn = Koneksi.configDB();
                 PreparedStatement ps = conn.prepareStatement("UPDATE tb_user SET password = ? WHERE id_user = ?")) {
                ps.setString(1, SecurityHelper.hashPassword(pass));
                ps.setInt(2, id);
                ps.executeUpdate();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Password untuk " + username + " berhasil di-reset!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Gagal: " + ex.getMessage()); }
        });

        content.add(form, BorderLayout.CENTER);
        content.add(btnSimpan, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private void deleteUser() {
        int id = getSelectedId();
        if (id == -1) { showInfo("Pilih user yang ingin dihapus."); return; }
        if (id == SessionManager.getUserId()) { showInfo("Anda tidak bisa menghapus akun Anda sendiri yang sedang aktif!"); return; }

        String username = (String) tableModel.getValueAt(tableUser.getSelectedRow(), 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus akses untuk kasir/user '" + username + "'?", "Hapus Akun", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Koneksi.configDB(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_user WHERE id_user = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                loadUserData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Gagal menghapus: " + ex.getMessage()); }
        }
    }
    private void initOperatorUI() {
        setLayout(new GridBagLayout());
        
        RoundedPanel card = new RoundedPanel(16, ThemeConfig.BG_CARD, new Color(0x3D, 0x3B, 0x60));
        card.setPreferredSize(new Dimension(450, 400));
        card.setLayout(new BorderLayout(0, 20));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Ubah Kata Sandi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeConfig.TEXT_HEAD);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSub = new JLabel("Amankan akun kasir Anda secara berkala");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeConfig.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        header.add(lblTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);

        JPanel form = new JPanel(new GridLayout(6, 1, 0, 4));
        form.setOpaque(false);
        
        txtPassLama = styledPasswordField();
        txtPassBaru = styledPasswordField();
        txtPassKonfirm = styledPasswordField();

        form.add(fieldLabel("Password Lama"));
        form.add(txtPassLama);
        form.add(fieldLabel("Password Baru"));
        form.add(txtPassBaru);
        form.add(fieldLabel("Konfirmasi Password Baru"));
        form.add(txtPassKonfirm);

        JButton btnSimpan = createGoldButton("Simpan Password Baru");
        btnSimpan.setPreferredSize(new Dimension(0, 46));
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimpan.addActionListener(e -> updateOwnPassword());

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(btnSimpan, BorderLayout.SOUTH);

        add(card);
    }

    private void updateOwnPassword() {
        String lama = new String(txtPassLama.getPassword());
        String baru = new String(txtPassBaru.getPassword());
        String konf = new String(txtPassKonfirm.getPassword());

        if (lama.isEmpty() || baru.isEmpty() || konf.isEmpty()) { showInfo("Semua kolom harus diisi!"); return; }
        if (!baru.equals(konf)) { showInfo("Password baru dan konfirmasi tidak cocok!"); return; }

        try (Connection conn = Koneksi.configDB()) {
            PreparedStatement psCek = conn.prepareStatement("SELECT password FROM tb_user WHERE id_user = ?");
            psCek.setInt(1, SessionManager.getUserId());
            ResultSet rs = psCek.executeQuery();
            
            if (rs.next()) {
                String dbPass = rs.getString("password");
                if (!dbPass.equals(SecurityHelper.hashPassword(lama))) {
                    JOptionPane.showMessageDialog(this, "Password lama yang Anda masukkan salah!", "Gagal", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            PreparedStatement psUpd = conn.prepareStatement("UPDATE tb_user SET password = ? WHERE id_user = ?");
            psUpd.setString(1, SecurityHelper.hashPassword(baru));
            psUpd.setInt(2, SessionManager.getUserId());
            psUpd.executeUpdate();

            JOptionPane.showMessageDialog(this, "Password Anda berhasil diperbarui!\nHarap ingat baik-baik password baru Anda.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            txtPassLama.setText("");
            txtPassBaru.setText("");
            txtPassKonfirm.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan sistem: " + e.getMessage());
        }
    }
    private int getSelectedId() {
        int row = tableUser.getSelectedRow();
        if (row == -1) return -1;
        return (int) tableModel.getValueAt(tableUser.convertRowIndexToModel(row), 0);
    }

    private void showInfo(String msg) { JOptionPane.showMessageDialog(this, msg, "Informasi", JOptionPane.INFORMATION_MESSAGE); }

    private void styleTable(JTable tbl) {
        tbl.setBackground(ThemeConfig.BG_TABLE);
        tbl.setForeground(ThemeConfig.TEXT_BODY);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(38);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(0x2A, 0x28, 0x48));
        tbl.setSelectionBackground(new Color(0x3A, 0x38, 0x60));
        tbl.setSelectionForeground(ThemeConfig.ACCENT);
        tbl.setBorder(BorderFactory.createEmptyBorder());
        JTableHeader h = tbl.getTableHeader();
        h.setBackground(new Color(0x1E, 0x1D, 0x38));
        h.setForeground(ThemeConfig.TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer padLeft = new DefaultTableCellRenderer();
        padLeft.setBorder(new EmptyBorder(0, 14, 0, 0));
        tbl.getColumnModel().getColumn(1).setCellRenderer(padLeft);
        tbl.getColumnModel().getColumn(2).setCellRenderer(padLeft);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(ThemeConfig.TEXT_BODY);
        l.setBorder(new EmptyBorder(4, 0, 0, 0));
        return l;
    }

    private JTextField styledTextField() {
        JTextField f = new JTextField();
        f.setBackground(ThemeConfig.BG_CARD);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setBackground(ThemeConfig.BG_CARD);
        f.setForeground(ThemeConfig.TEXT_HEAD);
        f.setCaretColor(ThemeConfig.ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return f;
    }

    private JComboBox<String> styledComboBox() {
        JComboBox<String> cb = new JComboBox<>();
        cb.setBackground(ThemeConfig.BG_CARD);
        cb.setForeground(ThemeConfig.TEXT_HEAD);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return cb;
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
        b.setContentAreaFilled(false); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JButton createOutlineButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) g2.setColor(new Color(0x1E, 0x1D, 0x38)); 
                else if (getModel().isRollover()) g2.setColor(new Color(0x3A, 0x38, 0x60)); 
                else g2.setColor(ThemeConfig.BG_PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setForeground(ThemeConfig.TEXT_BODY);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true), BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton createDangerButton(String text) {
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
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setContentAreaFilled(false); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        return b;
    }
}