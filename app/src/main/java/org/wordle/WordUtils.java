package org.wordle;

import java.io.*;
import java.util.*;

public class WordUtils {
    public static List<String> loadWords(String fileName) {
        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 5)
                    words.add(line.toUpperCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
}