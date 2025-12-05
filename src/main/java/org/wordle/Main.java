package org.wordle;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Wordle");
            WordlePanel panel = new WordlePanel();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);

            frame.pack(); 
            frame.setMinimumSize(new Dimension(500, 600)); 
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
