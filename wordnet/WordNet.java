/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Wordnet, Grade 81/100, https://coursera.cs.princeton.edu/algs4/assignments/wordnet/specification.php 
 */

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordNet {
    private final Map<Integer, List<String>> synsets;
    private final Map<String, Bag<Integer>> nouns;
    private final Map<Integer, Bag<Integer>> hypernyms;
    private final Digraph digraph;
    private final SAP sap;

    /**
     * constructor takes the name of the two input files
     * 
     * @param synsets
     * @param hypernyms
     */
    public WordNet(String synsets, String hypernyms) {
        if (synsets == null) {
            throw new IllegalArgumentException();
        }
        if (hypernyms == null) {
            throw new IllegalArgumentException();
        }
        this.synsets = new HashMap<>();
        this.nouns = new HashMap<>();

        In sin = new In(synsets);
        In hin = new In(hypernyms);
        while (sin.hasNextLine()) {
            // 0 id
            // 1 synset
            // 2 gloss
            String[] parts = sin.readLine().split(",");
            int id = Integer.parseInt(parts[0].trim());
            String[] nounsArray = parts[1].split(" ");
            List<String> nounsList = new ArrayList<String>();
            for (int i = 0; i < nounsArray.length; i++) {
                String noun = nounsArray[i].trim();
                nounsList.add(noun);
                // build up nouns to id
                {
                    Bag<Integer> nounIds = this.nouns.get(noun);
                    if (nounIds == null) {
                        nounIds = new Bag<>();
                        this.nouns.put(noun, nounIds);
                    }
                    this.nouns.get(noun).add(id);
                }
            }
            this.synsets.put(id, nounsList);
        }

        this.hypernyms = new HashMap<>();
        while (hin.hasNextLine()) {
            String[] parts = hin.readLine().split(",");
            int synId = Integer.parseInt(parts[0].trim());
            Bag<Integer> hypIds = new Bag<>();
            for (int i = 1; i < parts.length; i++) {
                hypIds.add(Integer.parseInt(parts[i].trim()));
            }
            this.hypernyms.put(synId, hypIds);
        }

        this.digraph = new Digraph(this.synsets.size());
        for (int synId : this.synsets.keySet()) {
            if (!this.hypernyms.containsKey(synId)) {
                throw new IllegalArgumentException();
            }

            this.hypernyms.get(synId).forEach(hypId -> {
                digraph.addEdge(synId, hypId);
            });
        }

        // is graph valid?
        {
            // Check for cycles
            DirectedCycle cycle = new DirectedCycle(this.digraph);
            if (cycle.hasCycle()) {
                throw new IllegalArgumentException("Not a valid DAG");
            }

            // Check if not rooted
            int rooted = 0;
            for (int i = 0; i < digraph.V(); i++) {
                if (!this.digraph.adj(i).iterator().hasNext())
                    rooted++;
            }

            if (rooted != 1) {
                throw new IllegalArgumentException("Not a rooted DAG");
            }
        }

        this.sap = new SAP(digraph);
    }

    /**
     * @return returns all WordNet nouns
     */
    public Iterable<String> nouns() {
        return nouns.keySet();
    }

    /**
     * @param word
     * @return true if the word is a WordNet noun, false otherwise
     */
    public boolean isNoun(String word) {
        if (word == null) {
            throw new IllegalArgumentException();
        }
        return nouns.containsKey(word);
    }

    /**
     * @param nounA
     * @param nounB
     * @return distance between nounA and nounB (defined below)
     */
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }

        Iterable<Integer> v = nouns.get(nounA);
        Iterable<Integer> w = nouns.get(nounB);

        return sap.length(v, w);
    }

    /**
     * a synset (second field of synsets.txt) that is the common ancestor of nounA
     * and nounB in a shortest ancestral path (defined below)
     * 
     * @param nounA
     * @param nounB
     * @return
     */
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) {
            throw new IllegalArgumentException();
        }

        Iterable<Integer> v = nouns.get(nounA);
        Iterable<Integer> w = nouns.get(nounB);

        int id = sap.ancestor(v, w);
        return String.join(" ", synsets.get(id));
    }

    private static void toSapAndDistance(WordNet wn, String v, String w) {
        StdOut.println(String.format("sap = %s, distance = %d", wn.sap(v, w), wn.distance(v, w)));
    }

    public static void main(String[] args) {
        WordNet wn = new WordNet("synsets.txt", "hypernyms.txt");
        toSapAndDistance(wn, "self-education", "vaticination");
        toSapAndDistance(wn, "worm", "bird");
        toSapAndDistance(wn, "municipality", "region");
        toSapAndDistance(wn, "individual", "edible_fruit");
        toSapAndDistance(wn, "white_marlin", "mileage");
        toSapAndDistance(wn, "Black_Plague", "black_marlin");
        toSapAndDistance(wn, "American_water_spaniel", "histology");
        toSapAndDistance(wn, "Brown_Swiss", "barrel_roll");
    }
}
