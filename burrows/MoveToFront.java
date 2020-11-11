/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Boggle, Grade 87/100, https://coursera.cs.princeton.edu/algs4/assignments/burrows/specification.php
 */

import edu.princeton.cs.algs4.BinaryStdIn;
import edu.princeton.cs.algs4.BinaryStdOut;

public class MoveToFront {
    private static final int SYMBOL_RADIUS = 256; // 6
    private static final int SYMBOL_LENGTH = 8;
    private static final char SYMBOL_ZERO_INDEX = 0; // 'A'

    /**
     * Apply move-to-front encoding, reading from standard input and writing to
     * standard output
     */
    public static void encode() {
        // initialize the alphabet
        char[] symbols = new char[SYMBOL_RADIUS];
        {
            char symbol = SYMBOL_ZERO_INDEX;
            for (int i = 0; i < symbols.length; i++) {
                symbols[i] = symbol++;
            }
        }

        // e.g,
        // symbols: A B C D E F
        // input: CAAABCCCACCF
        // move chars in alphabet to the front based on current input char
        while (!BinaryStdIn.isEmpty()) {
            char symbol = BinaryStdIn.readChar(SYMBOL_LENGTH);

            // find the index of current char
            int symbolIndex = 0;
            for (symbolIndex = 0; symbolIndex < symbols.length; symbolIndex++) {
                if (symbol == symbols[symbolIndex]) {
                    break;
                }
            }

            // output the code
            char encodedSymbol = (char) symbolIndex;
            BinaryStdOut.write(encodedSymbol);

            // move the char to the front
            for (int i = symbolIndex; i >= 1; i--) {
                symbols[i] = symbols[i - 1];
            }
            symbols[0] = symbol;
        }
        BinaryStdOut.close();
    }

    /**
     * Apply move-to-front decoding, reading from standard input and writing to
     * standard output
     */
    public static void decode() {
        // initialize the alphabet
        char[] symbols = new char[SYMBOL_RADIUS];
        {
            char symbol = SYMBOL_ZERO_INDEX;
            for (int i = 0; i < symbols.length; i++) {
                symbols[i] = symbol++;
            }
        }

        // e.g.
        // 02 01 00 00 02 02 00 00 02 01 00 05
        // move symbol in alphabet to the front based on index of current input symbol
        while (!BinaryStdIn.isEmpty()) {
            // 02
            int symbolIndex = BinaryStdIn.readInt(SYMBOL_LENGTH);
            // A B C ... => C
            char decodedSymbol = symbols[symbolIndex];
            // C
            BinaryStdOut.write(decodedSymbol);

            // move the char related to symbolIndex to the front
            // A B C D E F
            // 00 01 02 03 04 05
            // =>
            // C A B D E F
            // 02 00 01 03 04 05
            for (int i = symbolIndex; i >= 1; i--) {
                symbols[i] = symbols[i - 1];
            }
            symbols[0] = decodedSymbol;
        }
        BinaryStdOut.close();
    }

    // if args[0] is "-", apply move-to-front encoding
    // if args[0] is "+", apply move-to-front decoding
    public static void main(String[] args) {
        String cmd = args[0];

        if (cmd.equalsIgnoreCase("-")) {
            encode();
        } else if (cmd.equalsIgnoreCase("+")) {
            decode();
        } else {
            throw new IllegalArgumentException("unknown command");
        }
    }

}
