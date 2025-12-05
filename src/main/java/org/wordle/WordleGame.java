package org.wordle;

import java.util.List;
import java.util.Random;

public class WordleGame {
    private String hiddenWord;
    private int attempts;

    public WordleGame(List<String> wordList) {
        hiddenWord = wordList.get(new Random().nextInt(wordList.size())).toUpperCase();
        attempts = 0;
    }

    public String checkGuess(String guess) {
        guess = guess.toUpperCase();
        StringBuilder res = new StringBuilder("-----");
        int[] counts = new int[26];
        for (int i = 0; i < hiddenWord.length(); i++)
            if (guess.charAt(i) != hiddenWord.charAt(i))
                counts[hiddenWord.charAt(i) - 'A']++;
        for (int i = 0; i < guess.length(); i++)
            if (guess.charAt(i) == hiddenWord.charAt(i))
                res.setCharAt(i, 'G');
        for (int i = 0; i < guess.length(); i++) {
            if (res.charAt(i) == 'G')
                continue;
            int idx = guess.charAt(i) - 'A';
            if (counts[idx] > 0) {
                res.setCharAt(i, 'Y');
                counts[idx]--;
            }
        }
        attempts++;
        return res.toString();
    }

    public boolean hasWon(String guess) {
        return guess.equalsIgnoreCase(hiddenWord);
    }

    public boolean hasAttemptsLeft() {
        return attempts < 6;
    }

    public String getHiddenWord() {
        return hiddenWord;
    }

    public int getAttempts() {
        return attempts;
    }
}