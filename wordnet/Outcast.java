/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Wordnet, Grade 81/100, https://coursera.cs.princeton.edu/algs4/assignments/wordnet/specification.php 
 */

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class Outcast {
    
    private final WordNet wordnet;
    
    public Outcast(WordNet wordnet) {
        this.wordnet = wordnet;
    }

    public String outcast(String[] nouns) {
        int maxDistance = 0;
        String outcast = "";
        
        for (int i0 = 0; i0 < nouns.length; i0++) {
            int di = 0; 
            for (int i1 = 0; i1 < nouns.length; i1++) {
                di += wordnet.distance(nouns[i0], nouns[i1]);
            }
            
            if (di > maxDistance) {
                maxDistance = di;
                outcast = nouns[i0];
            }
        }
        
        return outcast;
    }

    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}