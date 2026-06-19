package com.fragrance.panel;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;

import com.fragrance.util.Koneksi;
import com.fragrance.util.RoundedPanel;
import com.fragrance.util.ThemeConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class CetakBarcodePanel extends JPanel {
    private JComboBox<String> cmbKertas, cmbTampilAngka;
    private JSlider slideTinggi, slideLebar;
    private JComboBox<String> cmbProduk;
    private DefaultTableModel barcodeModel;
    private JTable tableBarcode;
    private final Map<String, Integer> produkIdMap = new LinkedHashMap<>();
    private final Map<String, String> produkBarcodeMap = new LinkedHashMap<>();

    public CetakBarcodePanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(ThemeConfig.BG_PRIMARY);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        initUI();
        loadProduk();
    }

    private void initUI() {
        RoundedPanel panelPengaturan = new RoundedPanel(16, ThemeConfig.BG_CARD, new Color(0x2A, 0x28, 0x48));
        panelPengaturan.setLayout(new GridBagLayout());
        panelPengaturan.setBorder(BorderFactory.createTitledBorder(
                new EmptyBorder(10, 20, 10, 20), "Pengaturan Cetak Barcode", 
                0, 0, new Font("Segoe UI", Font.BOLD, 16), ThemeConfig.TEXT_HEAD));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 16, 8, 16);

        g.gridx = 0; g.gridy = 0; g.weightx = 0.2;
        panelPengaturan.add(formLabel("Ukuran Kertas"), g);
        g.gridx = 1; g.weightx = 0.8;
        cmbKertas = styledCombo(new String[]{"A4 (210 x 297 mm)", "F4 (210 x 330 mm)", "Stiker Thermal (80mm)"});
        panelPengaturan.add(cmbKertas, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0.2;
        panelPengaturan.add(formLabel("Tampilkan Angka"), g);
        g.gridx = 1; g.weightx = 0.8;
        cmbTampilAngka = styledCombo(new String[]{"Ya", "Tidak"});
        panelPengaturan.add(cmbTampilAngka, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 0.2;
        panelPengaturan.add(formLabel("Tinggi Barcode"), g);
        g.gridx = 1; g.weightx = 0.8;
        slideTinggi = createSlider(50, 150, 100);
        panelPengaturan.add(slideTinggi, g);

        g.gridx = 0; g.gridy = 3; g.weightx = 0.2;
        panelPengaturan.add(formLabel("Lebar Barcode"), g);
        g.gridx = 1; g.weightx = 0.8;
        slideLebar = createSlider(1, 5, 2);
        panelPengaturan.add(slideLebar, g);

        g.gridx = 0; g.gridy = 4; g.weightx = 0.2;
        panelPengaturan.add(formLabel("Pilih Produk"), g);
        
        JPanel panelPencarian = new JPanel(new BorderLayout(10, 0));
        panelPencarian.setOpaque(false);
        cmbProduk = styledCombo(new String[]{"Memuat Data Produk..."});
        JButton btnTambah = createGoldButton("+ Masukkan Antrean", null);
        btnTambah.setPreferredSize(new Dimension(160, 36));
        btnTambah.addActionListener(e -> tambahKeAntrean());
        
        panelPencarian.add(cmbProduk, BorderLayout.CENTER);
        panelPencarian.add(btnTambah, BorderLayout.EAST);
        
        g.gridx = 1; g.weightx = 0.8;
        panelPengaturan.add(panelPencarian, g);

        JPanel panelBawah = new JPanel(new BorderLayout(0, 10));
        panelBawah.setOpaque(false);

        String[] cols = {"ID", "Nama Produk", "Kode Barcode", "Jumlah Cetak"};
        barcodeModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; } 
        };
        tableBarcode = new JTable(barcodeModel);
        styleTable(tableBarcode);

        tableBarcode.getColumnModel().getColumn(0).setMinWidth(0);
        tableBarcode.getColumnModel().getColumn(0).setMaxWidth(0);
        tableBarcode.getColumnModel().getColumn(1).setPreferredWidth(300);
        tableBarcode.getColumnModel().getColumn(2).setPreferredWidth(150);
        tableBarcode.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollTable = new JScrollPane(tableBarcode);
        scrollTable.getViewport().setBackground(ThemeConfig.BG_TABLE);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(0x2A, 0x28, 0x48)));

        JPanel panelAksi = new JPanel(new BorderLayout());
        panelAksi.setOpaque(false);
        
        JButton btnHapus = createDangerButton("Hapus Item Terpilih");
        btnHapus.addActionListener(e -> hapusItemTabel());
        
        JPanel panelExport = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelExport.setOpaque(false);
        
        JButton btnPrint = createGoldButton("Print Langsung", null);
        JButton btnPDF = createOutlineButton("Export PDF", null);
        
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Fitur Print Langsung ke mesin pencetak akan segera hadir. Saat ini silakan gunakan Export PDF lalu print dari file PDF tersebut."));
        btnPDF.addActionListener(e -> exportToPDF());

        panelExport.add(btnPDF);
        panelExport.add(btnPrint);

        panelAksi.add(btnHapus, BorderLayout.WEST);
        panelAksi.add(panelExport, BorderLayout.EAST);

        panelBawah.add(scrollTable, BorderLayout.CENTER);
        panelBawah.add(panelAksi, BorderLayout.SOUTH);

        add(panelPengaturan, BorderLayout.NORTH);
        add(panelBawah, BorderLayout.CENTER);
    }

    private void loadProduk() {
        produkIdMap.clear();
        produkBarcodeMap.clear();
        
        try (Connection conn = Koneksi.configDB();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT id_produk, nama_produk FROM tb_produk ORDER BY nama_produk")) {
            
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            while (rs.next()) {
                int id = rs.getInt("id_produk");
                String nama = rs.getString("nama_produk");
                
                String dummyBarcode = String.format("899356000%03d", id);
                
                produkIdMap.put(nama, id);
                produkBarcodeMap.put(nama, dummyBarcode);
                model.addElement(nama);
            }
            cmbProduk.setModel(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tambahKeAntrean() {
        String namaProd = (String) cmbProduk.getSelectedItem();
        if (namaProd == null) return;

        int id = produkIdMap.get(namaProd);
        String barcode = produkBarcodeMap.get(namaProd);

        for (int i = 0; i < barcodeModel.getRowCount(); i++) {
            if ((int) barcodeModel.getValueAt(i, 0) == id) {
                int qtyLama = Integer.parseInt(barcodeModel.getValueAt(i, 3).toString());
                barcodeModel.setValueAt(qtyLama + 10, i, 3);
                return;
            }
        }

        barcodeModel.addRow(new Object[]{id, namaProd, barcode, "10"});
    }

    private void hapusItemTabel() {
        int row = tableBarcode.getSelectedRow();
        if (row != -1) {
            barcodeModel.removeRow(row);
        } else {
            JOptionPane.showMessageDialog(this, "Pilih item di tabel yang ingin dihapus.");
        }
    }
    private void exportToPDF() {
        if (barcodeModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Antrean cetak masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File PDF Barcode");
        fileChooser.setSelectedFile(new File("Cetak_Barcode_Decium.pdf"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return; 
        }
        
        File fileToSave = fileChooser.getSelectedFile();
        
        try {
            Rectangle ukuranKertas = PageSize.A4;
            int pilihanKertas = cmbKertas.getSelectedIndex();
            if (pilihanKertas == 1) ukuranKertas = PageSize.LEGAL; 
            else if (pilihanKertas == 2) ukuranKertas = new Rectangle(226, 800); 
            
            Document document = new Document(ukuranKertas, 20, 20, 20, 20);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();
            
            PdfContentByte cb = writer.getDirectContent();
            
            int jumlahKolom = (pilihanKertas == 2) ? 1 : 3; 
            PdfPTable pdfTable = new PdfPTable(jumlahKolom);
            pdfTable.setWidthPercentage(100);

            boolean tampilAngka = cmbTampilAngka.getSelectedIndex() == 0;
            float tinggiBarcode = slideTinggi.getValue() / 2.0f; 

            for (int i = 0; i < barcodeModel.getRowCount(); i++) {
                String namaProduk = barcodeModel.getValueAt(i, 1).toString();
                String kodeBarcode = barcodeModel.getValueAt(i, 2).toString();
                int qtyCetak = Integer.parseInt(barcodeModel.getValueAt(i, 3).toString());

                for (int j = 0; j < qtyCetak; j++) {

                    Barcode128 code128 = new Barcode128();
                    code128.setCode(kodeBarcode);
                    code128.setBarHeight(tinggiBarcode);
                    
                    if (!tampilAngka) {
                        code128.setFont(null); 
                    }

                    com.lowagie.text.Image imageBarcode = code128.createImageWithBarcode(cb, null, null);

                    PdfPCell cell = new PdfPCell();
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setPadding(15);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                    com.lowagie.text.Paragraph pNama = new com.lowagie.text.Paragraph(namaProduk, com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 8));
                    pNama.setAlignment(Element.ALIGN_CENTER);
                    
                    cell.addElement(pNama);
                    imageBarcode.setAlignment(Element.ALIGN_CENTER);
                    cell.addElement(imageBarcode);
                    
                    pdfTable.addCell(cell);
                }
            }
            pdfTable.completeRow();
            
            document.add(pdfTable);
            document.close();
            
            JOptionPane.showMessageDialog(this, "Berhasil! File PDF Barcode telah disimpan di:\n" + fileToSave.getAbsolutePath(), "Export Sukses", JOptionPane.INFORMATION_MESSAGE);
            Desktop.getDesktop().open(fileToSave);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showPlaceholder(String message) {
        if (barcodeModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tabel antrean kosong! Tambahkan produk dulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, message + "\n\n(Membutuhkan Library seperti ZXing & iTextPDF untuk diimplementasikan ke dalam sistem).", "Fitur Integrasi", JOptionPane.INFORMATION_MESSAGE);
    }
    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(ThemeConfig.TEXT_BODY);
        return l;
    }

    private JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setPreferredSize(new Dimension(0, 36));
        cb.setBackground(ThemeConfig.BG_CARD);
        cb.setForeground(ThemeConfig.TEXT_HEAD);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return cb;
    }

    private JSlider createSlider(int min, int max, int val) {
        JSlider slider = new JSlider(min, max, val);
        slider.setOpaque(false);
        slider.setForeground(ThemeConfig.ACCENT);
        return slider;
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(ThemeConfig.BG_TABLE);
        tbl.setForeground(ThemeConfig.TEXT_BODY);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(36);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(new Color(0x2A, 0x28, 0x48));
        tbl.setSelectionBackground(new Color(0x3A, 0x38, 0x60));
        tbl.setSelectionForeground(ThemeConfig.ACCENT);
        
        JTableHeader h = tbl.getTableHeader();
        h.setBackground(new Color(0x1E, 0x1D, 0x38));
        h.setForeground(ThemeConfig.TEXT_MUTED);
        h.setFont(new Font("Segoe UI", Font.BOLD, 12));
        h.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer padLeft = new DefaultTableCellRenderer();
        padLeft.setBorder(new EmptyBorder(0, 14, 0, 0));
        tbl.getColumnModel().getColumn(1).setCellRenderer(padLeft);
        tbl.getColumnModel().getColumn(2).setCellRenderer(padLeft);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tbl.getColumnModel().getColumn(3).setCellRenderer(center);
    }

    private JButton createGoldButton(String text, String iconName) {
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

    private JButton createOutlineButton(String text, String iconName) {
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
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x3D, 0x3B, 0x60), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
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