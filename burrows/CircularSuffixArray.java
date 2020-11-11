
/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Boggle, Grade 87/100, https://coursera.cs.princeton.edu/algs4/assignments/burrows/specification.php
 */

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.SuffixArray;

public class CircularSuffixArray {
    private final String s;
    private final SuffixArray suffices;

    // circular suffix array of s
    public CircularSuffixArray(String s) {
        if (s == null) {
            throw new IllegalArgumentException();
        }
        this.s = s;
        this.suffices = new SuffixArray(s);
    }

    // length of s
    public int length() {
        return s.length();
    }

    // returns index of i(th) sorted suffix
    public int index(int i) {
        if (i >= s.length()) {
            throw new IllegalArgumentException();
        }

        return suffices.index(i);
    }

    // unit testing (required)
    public static void main(String[] args) {
        String str = "ABRACADABRA!";

        CircularSuffixArray suffices = new CircularSuffixArray(str);
        for (int i = 0; i < suffices.length(); i++) {
            int index = suffices.index(i);
            StdOut.println(i + " - " + index + "\t - " + str.charAt(i));
        }
    }

}