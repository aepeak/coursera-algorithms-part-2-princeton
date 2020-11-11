/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 *          Coursera, Algorithms, Part II, Princeton Assignment: Boggle, Grade
 *          87/100,
 *          https://coursera.cs.princeton.edu/algs4/assignments/burrows/specification.php
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

public class BurrowsWheeler {

    /**
     * Apply Burrows-Wheeler transform, reading from standard input and writing to
     * standard output
     */
    public static void transform() {
        int bwi = -1;
        StringBuilder bws = new StringBuilder();

        String s = BinaryStdIn.readString();
        CircularSuffixArray suffices = new CircularSuffixArray(s);
        for (int i = 0; i < suffices.length(); i++) {
            int index = suffices.index(i);
            if (index == 0) {
                index = s.length();
                bwi = i;
            }
            bws.append(s.charAt(index - 1));
        }

        BinaryStdOut.write(bwi);
        BinaryStdOut.write(bws.toString());
        BinaryStdOut.close();
    }

    /**
     * Apply Burrows-Wheeler inverse transform, reading from standard input and
     * writing to standard output
     */
    public static void inverseTransform() {
        int bwi = BinaryStdIn.readInt(); // 3
        String bws = BinaryStdIn.readString(); // "ARD!RCAAAABB";

        // initialize t[]
        Map<Character, List<Integer>> tmap = new HashMap<>();
        char[] t = new char[bws.length()];
        for (int i = 0; i < bws.length(); i++) {
            t[i] = bws.charAt(i);
            if (!tmap.containsKey(t[i])) {
                tmap.put(t[i], new ArrayList<Integer>());
            }

            tmap.get(t[i]).add(i);
        }
//        StdOut.println(Arrays.toString(t));

        // initialize first[]
        char[] first = t.clone();
        Arrays.sort(first);
//        StdOut.println(Arrays.toString(first));

        // initialize next[]
        int next[] = new int[t.length];
        for (int i = 0; i < next.length; i++) {
            char fc = first[i];
            next[i] = tmap.get(fc).remove(0);
        }
//        StdOut.println(Arrays.toString(next));

        // reconstruct the original stream
        char[] out = new char[next.length];
        {
            int i = bwi;
            int j = 0;
            while (j < out.length) {
                out[j] = first[i];

                // who's next ?
                i = next[i];
                j++;
            }
        }
//        StdOut.println(Arrays.toString(out));
        
        // spit out the original stream
        for (int i = 0; i < out.length; i++) {
            BinaryStdOut.write(out[i]);
        }
        BinaryStdOut.close();
    }

    // if args[0] is "-", apply Burrows-Wheeler transform
    // if args[0] is "+", apply Burrows-Wheeler inverse transform
    public static void main(String[] args) {
        String cmd = args[0];

        if (cmd.equalsIgnoreCase("-")) {
            transform();
        } else if (cmd.equalsIgnoreCase("+")) {
            inverseTransform();
        } else {
            throw new IllegalArgumentException("unknown command");
        }
    }

}