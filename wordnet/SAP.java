/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Wordnet, Grade 81/100, https://coursera.cs.princeton.edu/algs4/assignments/wordnet/specification.php 
 */

import edu.princeton.cs.algs4.BreadthFirstDirectedPaths;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SAP {

    private final Digraph digraph;
    private final Map<String, SAPResult> cache;
    private final BreadthFirstDirectedPaths bfs;
    private int root = Integer.MIN_VALUE;

    /**
     * @param digraph constructor takes a digraph (not necessarily a DAG)
     */
    public SAP(Digraph digraph) {
        if (digraph == null) {
            throw new IllegalArgumentException();
        }

        this.digraph = new Digraph(digraph);
        this.bfs = new BreadthFirstDirectedPaths(this.digraph.reverse(), root());
        this.cache = new HashMap<String, SAPResult>();
    }

    /**
     * @param v
     * @param w
     * @return length of shortest ancestral path between v and w; -1 if no such path
     */
    public int length(int v, int w) {
        return sap(v, w).getPathLength();
    }

    /**
     * @param v
     * @param w
     * @return a common ancestor of v and w that participates in a shortest
     *         ancestral path, -1 if no such path
     */
    public int ancestor(int v, int w) {
        return sap(v, w).ancestor;
    }

    /**
     * @param v
     * @param w
     * @return length of shortest ancestral path between any vertex in v and any
     *         vertex in w; -1 if no such path
     */
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        return sap(v, w).getPathLength();
    }

    /**
     * @param v
     * @param w
     * @return a common ancestor that participates in shortest ancestral path; -1 if
     *         no such path
     */
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        return sap(v, w).ancestor;
    }

    private static ArrayList<Integer> toList(Iterable<Integer> iterable) {
        ArrayList<Integer> list = new ArrayList<>();
        if (iterable != null) {
            iterable.forEach(i -> list.add(i));
        }

        return list;
    }

    private String getVWKey(int v, int w) {
        return String.format("%d:%d", v, w);
    }

    private String getVWKey(Iterable<Integer> v, Iterable<Integer> w) {
        List<Integer> vl = toList(v);
        List<Integer> wl = toList(w);
        Collections.sort(vl);
        Collections.sort(wl);

        StringBuilder sb = new StringBuilder();
        vl.forEach(vi -> sb.append(vi).append(","));
        sb.delete(sb.length() - 1, sb.length());
        sb.append(":");
        wl.forEach(wi -> sb.append(wi).append(","));
        sb.delete(sb.length() - 1, sb.length());

        return sb.toString();
    }

    private int root() {
        if (root == Integer.MIN_VALUE) {
            for (int vid = 0; vid < digraph.V(); vid++) {
                if (digraph.outdegree(vid) == 0) {
                    if (digraph.indegree(vid) == 0) {
                        // the node id is invalid or the node is completely isolated (not connected)
                        continue;
                    }
                    root = vid;
                    break;
                }
            }

            if (root == Integer.MIN_VALUE) {
                root = digraph.V() - 1;
                StdOut.println("Invalid root found!");
            }
        }

        StdOut.println("root is: " + root);
        return root;
    }

    private SAPResult sap(int v, int w) {
        if (v < 0 || v >= digraph.V()) {
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (digraph.V() - 1));
        }

        String vwkey = getVWKey(v, w);
        if (cache.containsKey(vwkey)) {
            return cache.get(vwkey);
        }
        if (v == w) {
            return new SAPResult(v, w, w, new ArrayList<>());
        }

        List<SAPResult> results = new ArrayList<SAPResult>();

        List<Integer> vwpath;
        // try direct v -> w
        {
            BreadthFirstDirectedPaths vwbfs = new BreadthFirstDirectedPaths(digraph, v);
            if (vwbfs.hasPathTo(w)) {
                vwpath = toList(vwbfs.pathTo(w));
                results.add(new SAPResult(v, w, w, new ArrayList<>(vwpath)));
            }
        }
        // try direct v <- w
        {
            BreadthFirstDirectedPaths wvbfs = new BreadthFirstDirectedPaths(digraph, w);
            if (wvbfs.hasPathTo(v)) {
                vwpath = toList(wvbfs.pathTo(v));
                results.add(new SAPResult(v, w, v, new ArrayList<>(vwpath)));
            }
        }

        List<Integer> tvpath = toList(bfs.pathTo(v));
        List<Integer> twpath = toList(bfs.pathTo(w));
        if (!tvpath.isEmpty() && !twpath.isEmpty()) {

            List<Integer> vpath;
            List<Integer> wpath;
            if (tvpath.size() < twpath.size()) {
                vpath = twpath;
                wpath = tvpath;
            } else {
                vpath = tvpath;
                wpath = twpath;
            }

            int ancestor = -1;
            vwpath = new ArrayList<Integer>();
            if (wpath.isEmpty() || vpath.isEmpty()) {
                return new SAPResult(v, w, ancestor, vwpath);
            }

            for (int i = 0; i < vpath.size(); i++) {
                int vi = vpath.get(i);
                int wi = wpath.size() > i ? wpath.get(i) : -1;

                if (vi == wi) {
                    ancestor = vi;
                } else {
                    for (int vl = vpath.size() - 1; vl >= i; vl--) {
                        vwpath.add(vpath.get(vl));
                    }
                    for (int wl = i - 1; wl < wpath.size(); wl++) {
                        vwpath.add(wpath.get(wl));
                    }
                    break;
                }
            }

            results.add(new SAPResult(v, w, ancestor, vwpath));
        }

        Collections.sort(results);
        if (!results.isEmpty()) {
            cache.put(vwkey, results.get(0));
            return results.get(0);
        } else {
            return new SAPResult(v, w, -1, new ArrayList<>());
        }
    }

    private SAPResult sap(Iterable<Integer> v, Iterable<Integer> w) {
        if (v == null) {
            throw new IllegalArgumentException();
        }
        if (w == null) {
            throw new IllegalArgumentException();
        }

        int vsize = 0;
        for (Integer i : v) {
            vsize++;
            if (i == null) {
                throw new IllegalArgumentException();
            }
        }
        if (vsize == 0) {
            return new SAPResult(0, 0, -1, new ArrayList<Integer>());
        }

        int wsize = 0;
        for (Integer i : w) {
            wsize++;
            if (i == null) {
                throw new IllegalArgumentException();
            }
        }
        if (wsize == 0) {
            return new SAPResult(0, 0, -1, new ArrayList<Integer>());
        }

        String vwkey = getVWKey(v, w);
        if (cache.containsKey(vwkey)) {
            return cache.get(vwkey);
        }

        List<SAPResult> results = new ArrayList<SAPResult>();
        for (Integer vi : v) {
            if (vi == null) {
                throw new IllegalArgumentException();
            }
            for (Integer wi : w) {
                if (wi == null) {
                    throw new IllegalArgumentException();
                }
                results.add(sap(vi, wi));
            }
        }

//        Collections.sort(results);
        return results.get(0);
    }

    private class SAPResult implements Comparable<SAPResult> {
        private final int v, w;
        private final List<Integer> path;
        private final int ancestor;

        public SAPResult(int v, int w, int ancestor, List<Integer> path) {
            this.v = v;
            this.w = w;
            this.ancestor = ancestor;
            this.path = path;
        }

        public int getPathLength() {
            if (ancestor == -1) {
                return -1;
            }
            return path.isEmpty() ? 0 : path.size() - 1;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("\nv = %d, w = %d", v, w));
            sb.append(String.format("\nshortest ancestral path: %s", path));
            sb.append(String.format("\nshortest common ancestor: %d", ancestor));
            sb.append(String.format("\nassociated length: %d", getPathLength()));

            return sb.toString();
        }

        @Override
        public int compareTo(SAPResult that) {
            if (that == null || that.getClass() != this.getClass()) {
                throw new IllegalArgumentException();
            }

            if (this == that) {
                return 0;
            }

            return this.path.size() - that.path.size();
        }
    }

    // do unit testing of this class
    public static void main(String[] args) {
    }
}