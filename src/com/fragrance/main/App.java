package com.fragrance.main;

import com.fragrance.ui.LoginFrame;
import com.fragrance.util.ThemeConfig;
import javax.swing.SwingUtilities;

public class App {
    
    public static void main(String[] args) {
        
        ThemeConfig.apply(); 
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        
    }
    
}