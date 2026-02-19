package org.wordle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class WordlePanel extends JPanel {
    private WordleGame game;
    private List<String> wordList;
    private GridPanel gridPanel;
    private JPanel keyboardPanel;
    private JButton newGameButton;
    private int currentAttempt = 0, currentPos = 0;
    private Map<Character, JLabel> keyMap = new HashMap<>();
    private Set<String> validWords;
    private static JLabel tempMsgLabel = null;
    private javax.swing.Timer tempMsgTimer = null;

    static class GridPanel extends JPanel {
        private JLabel[] cells = new JLabel[30];
        private int lastFontSize = -1;

        public GridPanel() {
            setLayout(null);
            for (int i = 0; i < 30; i++) {
                JLabel l = new JLabel("", SwingConstants.CENTER);
                l.setOpaque(true);
                l.setBackground(Color.WHITE);
                l.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                l.setFont(new Font("Verdana", Font.BOLD, 42));
                add(l);
                cells[i] = l;
            }
            setPreferredSize(new Dimension(500, 600));
        }

        @Override
        public void doLayout() {
            int rows = 6, cols = 5;
            int w = getWidth() / cols;
            int h = getHeight() / rows;
            int size = Math.min(w, h);
            int xOffset = (getWidth() - size * cols) / 2;
            int yOffset = (getHeight() - size * rows) / 2;
            int fontSize = size / 2;

            if (tempMsgLabel != null && tempMsgLabel.isVisible())
            {
                int labelWidth = Math.min(getWidth() / 2, size * cols / 2);
                int labelHeight = getHeight() / 10;
                int x = (getWidth() - labelWidth) / 2;
                int y = (getHeight() - labelHeight) / 2;
                tempMsgLabel.setBounds(x, y, labelWidth, labelHeight);
            }

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int idx = r * cols + c;
                    cells[idx].setBounds(xOffset + c * size, yOffset + r * size, size, size);

                    if (fontSize != lastFontSize) {
                        cells[idx].setFont(new Font("Verdana", Font.BOLD, fontSize));
                    }
                }
            }

            lastFontSize = fontSize;
        }

        public JLabel getCell(int row, int col) {
            return cells[row * 5 + col];
        }
    }

    public WordlePanel() {
        setLayout(new BorderLayout());
        wordList = WordUtils.loadWords("src/main/resources/words.txt");
        validWords = new HashSet<>(wordList);

        gridPanel = new GridPanel();
        add(gridPanel, BorderLayout.CENTER);

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 18));
        newGameButton.addActionListener(e -> startNewGame());
        add(newGameButton, BorderLayout.NORTH);

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!game.hasAttemptsLeft())
                    return;
                char c = e.getKeyChar();
                
                if (Character.isLetter(c) && currentPos < 5) {
                    c = Character.toUpperCase(c);
                    gridPanel.getCell(currentAttempt, currentPos).setText("" + c);
                    currentPos++;
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && currentPos > 0) {
                    currentPos--;
                    gridPanel.getCell(currentAttempt, currentPos).setText("");

                    if (tempMsgLabel != null && tempMsgLabel.isVisible()) {
                        tempMsgLabel.repaint();
                    }
                    
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && currentPos == 5) {
                    String guess = getCurrentGuess().trim().toUpperCase();
                    if(!validWords.contains(guess)) {
                        showTemporaryMessage("Invalid word!");
                        return;
                    }
                    String result = game.checkGuess(guess);
                    applyResult(result);

                    currentAttempt++;
                    currentPos = 0;
                    if (game.hasWon(guess))
                        SwingUtilities.invokeLater(() -> showEndDialog("You won! The word was: " + game.getHiddenWord(),
                                "Congratulations"));
                    else if (!game.hasAttemptsLeft())
                        SwingUtilities.invokeLater(
                                () -> showEndDialog("You lost! The word was: " + game.getHiddenWord(), "Game Over"));
                }
            }
        });

        // QWERTY keyboard
        String[][] rows = { { "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P" },
                { "A", "S", "D", "F", "G", "H", "J", "K", "L" },
                { "Z", "X", "C", "V", "B", "N", "M" } };
        keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));
        for (String[] row : rows) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            for (String s : row) {
                JLabel key = new JLabel(s, SwingConstants.CENTER);
                key.setOpaque(true);
                key.setBackground(Color.WHITE);
                key.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                key.setFont(new Font("Arial", Font.BOLD, 18));
                key.setPreferredSize(new Dimension(40, 40));
                rowPanel.add(key);
                keyMap.put(s.charAt(0), key);
            }
            keyboardPanel.add(rowPanel);
        }
        add(keyboardPanel, BorderLayout.SOUTH);

        startNewGame();
    }

    private void startNewGame() {
        game = new WordleGame(wordList);
        currentAttempt = 0;
        currentPos = 0;
        for (int r = 0; r < 6; r++)
            for (int c = 0; c < 5; c++) {
                JLabel l = gridPanel.getCell(r, c);
                l.setText("");
                l.setBackground(Color.WHITE);
            }
        for (JLabel key : keyMap.values())
            key.setBackground(Color.WHITE);

        gridPanel.repaint();
        keyboardPanel.repaint();
        requestFocusInWindow();
    }

    private String getCurrentGuess() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++)
            sb.append(gridPanel.getCell(currentAttempt, i).getText().isEmpty() ? ' '
                    : gridPanel.getCell(currentAttempt, i).getText());
        return sb.toString();
    }

    private void applyResult(String r) {
        String guess = getCurrentGuess();
        for (int i = 0; i < 5; i++) {
            JLabel l = gridPanel.getCell(currentAttempt, i);
            switch (r.charAt(i)) {
                case 'G' -> l.setBackground(Color.GREEN);
                case 'Y' -> l.setBackground(Color.YELLOW);
                case '-' -> l.setBackground(Color.LIGHT_GRAY);
            }
            JLabel key = keyMap.get(guess.charAt(i));
            if (key == null)
                continue;
            switch (r.charAt(i)) {
                case 'G' -> key.setBackground(Color.GREEN);
                case 'Y' -> key.setBackground(Color.YELLOW);
                case '-' -> {
                    if (key.getBackground() != Color.GREEN && key.getBackground() != Color.YELLOW)
                        key.setBackground(Color.LIGHT_GRAY);
                }
            }
            gridPanel.repaint();
            keyboardPanel.repaint();
        }
    }

    private void showEndDialog(String msg, String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(msg);
        l.setFont(new Font("Arial", Font.BOLD, 18));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(l);
        Object[] options = { "New Game", "Close" };
        int choice = JOptionPane.showOptionDialog(this, panel, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 0)
            startNewGame();
        else
            System.exit(0);
    }

    private void showTemporaryMessage(String message)
    {
        // Create label once if needed
        if (tempMsgLabel == null) {
            tempMsgLabel = new JLabel();
            tempMsgLabel.setOpaque(true);
            tempMsgLabel.setBackground(new Color(255, 255, 200)); // light yellow
            tempMsgLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            tempMsgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            tempMsgLabel.setFont(tempMsgLabel.getFont().deriveFont(16f));

            gridPanel.add(tempMsgLabel);
            gridPanel.setComponentZOrder(tempMsgLabel, 0); 
        }

        tempMsgLabel.setText(message);
        tempMsgLabel.setForeground(new Color(0, 0, 0, 255));
        tempMsgLabel.setVisible(true);
        gridPanel.repaint();

        if (tempMsgTimer != null && tempMsgTimer.isRunning()) {
            tempMsgTimer.stop();
        }

        tempMsgTimer = new javax.swing.Timer(800, e -> {
            tempMsgLabel.setVisible(false);
            ((javax.swing.Timer) e.getSource()).stop();
        });
        tempMsgTimer.setRepeats(false); 
        tempMsgTimer.start();
    }
}
