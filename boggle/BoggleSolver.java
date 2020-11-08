/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com 
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Boggle, Grade 89/100, https://coursera.cs.princeton.edu/algs4/assignments/boggle/specification.php
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.TST;

public class BoggleSolver {

    private static int[][] NEIGHBORS = new int[][] { //
            { -1, -1 }, { -1, +0 }, { -1, +1 }, //
            { +0, -1 }, /* { +0, +0 }, */ { +0, +1 }, //
            { +1, -1 }, { +1, +0 }, { +1, +1 }, //
    };

    private static Map<Integer, Integer> SCORE = new HashMap<>() {
        private static final long serialVersionUID = 1L;
        {
            put(0, 0);
            put(1, 0);
            put(2, 0);
            put(3, 1);
            put(4, 1);
            put(5, 2);
            put(6, 3);
            put(7, 5);
            put(8, 11);
        }
    };

    private final TST<Boolean> dictionary = new TST<Boolean>();

    // Initializes the data structure using the given array of strings as the
    // dictionary.
    // (You can assume each word in the dictionary contains only the uppercase
    // letters A through Z.)
    public BoggleSolver(String[] dictionary) {
        for (String word : dictionary) {
            if (word.length() >= 3) {
                this.dictionary.put(word, true);
            }
        }
    }

    // Returns the score of the given word if it is in the dictionary, zero
    // otherwise.
    // (You can assume the word contains only the uppercase letters A through Z.)
    public int scoreOf(String word) {
        if (!this.dictionary.contains(word)) {
            return 0;
        }

        return SCORE.getOrDefault(word.length(), 11);
    }

    // Returns the set of all valid words in the given Boggle board, as an Iterable.
    public Iterable<String> getAllValidWords(BoggleBoard board) {
        Set<String> words = new HashSet<>();

        for (int row = 0; row < board.rows(); row++) {
            for (int col = 0; col < board.cols(); col++) {
                boolean[][] visited = new boolean[board.rows()][board.cols()];
                StringBuilder prefix = new StringBuilder();

                exploreBoardForWords(board, visited, row, col, prefix, words);
            }
        }

        return words;
    }

    private void exploreBoardForWords(BoggleBoard board, boolean[][] visited, int row, int col, StringBuilder prefix,
            Set<String> words) {

        // append the new letter
        char letter = board.getLetter(row, col);
        prefix.append(letter);
        if (letter == 'Q') {
            prefix.append('U');
        }

        // mark the cell as visited
        visited[row][col] = true;
//        toString(visited, prefix.toString());

        // is word ?
        if (prefix.length() >= 3) {
            if (this.dictionary.contains(prefix.toString())) {
                words.add(prefix.toString());
            }
        }

        // is prefix ?
        Queue<String> wordsWithPrefix = (Queue<String>) this.dictionary.keysWithPrefix(prefix.toString());
        if (wordsWithPrefix.isEmpty()) {
//            StdOut.println(prefix + " is a dead end");
            backtrack(visited, row, col, prefix);
            return;
        }

        // traverse the neighbors
        for (int[] nrowcol : NEIGHBORS) {
            int nrow = nrowcol[0] + row;
            int ncol = nrowcol[1] + col;
            if (!isNeighborValid(board, nrow, ncol)) {
                continue;
            }
            if (isNeighborVisited(visited, nrow, ncol)) {
                continue;
            }

            exploreBoardForWords(board, visited.clone(), nrow, ncol, prefix, words);
        }

        backtrack(visited, row, col, prefix);
    }

    private void backtrack(boolean[][] visited, int row, int col, StringBuilder prefix) {
        visited[row][col] = false;
        if (prefix.length() >= 2) {
            char alc = prefix.charAt(prefix.length() - 2);
            if (alc == 'Q') {
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }
        prefix.deleteCharAt(prefix.length() - 1);
    }

    private boolean isNeighborValid(BoggleBoard board, int row, int col) {
        if (row >= 0 && row < board.rows()) {
            if (col >= 0 && col < board.cols()) {
                return true;
            }
        }

        return false;
    }

    private boolean isNeighborVisited(boolean[][] boardVisits, int row, int col) {
        return boardVisits[row][col] == true;
    }

    private void toString(boolean[][] boardVisits, String prefix) {
        StdOut.println("-----------------\n" + prefix);
        int rows = boardVisits.length;
        int cols = boardVisits[0].length;

        for (int row = 0; row < rows; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < cols; col++) {
                line.append(boardVisits[row][col] ? "x " : "o ");
            }
            StdOut.println(line.toString());
        }
    }
}
