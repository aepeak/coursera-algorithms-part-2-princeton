/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Seam Carving, Grade 100/100, https://coursera.cs.princeton.edu/algs4/assignments/seam/specification.php 
 */

import edu.princeton.cs.algs4.AcyclicSP;
import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.Picture;

import java.util.ArrayList;
import java.util.List;

public class SeamCarver {

    private static final int MAX_ENERGY = 1000;

    private Picture picture;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException();
        }
        this.picture = new Picture(picture);
    }

    // current picture
    public Picture picture() {
        return new Picture(this.picture);
    }

    // width of current picture
    public int width() {
        return this.picture.width();
    }

    // height of current picture
    public int height() {
        return this.picture.height();
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || x > width() - 1) {
            throw new IllegalArgumentException();
        }
        if (y < 0 || y > height() - 1) {
            throw new IllegalArgumentException();
        }
        if (x == 0 || x == width() - 1) {
            return MAX_ENERGY;
        }
        if (y == 0 || y == height() - 1) {
            return MAX_ENERGY;
        }

        double energyEastWest;
        {
            int cn = picture.getRGB(x + 1, y);
            int cp = picture.getRGB(x - 1, y);
            energyEastWest = getEnergy(cn, cp);
        }

        double energyNorthSouth;
        {
            int cn = picture.getRGB(x, y + 1);
            int cp = picture.getRGB(x, y - 1);
            energyNorthSouth = getEnergy(cn, cp);
        }

        return Math.sqrt(energyEastWest + energyNorthSouth);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        SeamHorizontalVertical min = null;

        // get every elem from first row with every elem from last row
        EdgeWeightedDigraph g = buildVerticalGraph();
        int vertices = g.V() - 1;
        int rootId = vertices;
        AcyclicSP sp = new AcyclicSP(g, rootId);

        for (int v = vertices - picture.width(); v < vertices; v++) {
            if (sp.hasPathTo(v)) {
                SeamHorizontalVertical seam = new SeamHorizontalVertical(sp.pathTo(v));
                if (min == null) {
                    min = seam;
                } else if (seam.compareTo(min) < 0) {
                    min = seam;
                }
//                StdOut.println(String.format("s=%d->v=%d, \t(c=%d,r=%d), \t%s", rootId, v, vvertex2Col(v), vvertex2Row(v), seam.path));
            }
        }

        if (min == null) {
            return new int[] {};
        }
//        StdOut.println("!!!V winner: " + min.path + "!!!");
        return min.getVerticalSeam();
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        SeamHorizontalVertical min = null;

        // get every elem from first col with every elem from last col
        EdgeWeightedDigraph g = buildHorizontalGraph();
        int vertices = g.V() - 1;
        int rootId = vertices;
        AcyclicSP sp = new AcyclicSP(g, rootId);

        for (int v = vertices - picture.height(); v < vertices; v++) {
            if (sp.hasPathTo(v)) {
                SeamHorizontalVertical seam = new SeamHorizontalVertical(sp.pathTo(v));
                if (min == null) {
                    min = seam;
                } else if (seam.compareTo(min) < 0) {
                    min = seam;
                }
//                StdOut.println(String.format("s=%d->v=%d, \t(c=%d,r=%d), \t%s", rootId, v, hvertex2Col(v), hvertex2Row(v), seam.path));
            }
        }

        if (min == null) {
            return new int[] {};
        }
//        StdOut.println("!!!H winner: " + min.path + "!!!");
        return min.getHorizontalSeam();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("null vseam");
        }
        if (seam.length != height()) {
            throw new IllegalArgumentException("vseam length > picture height");
        }
        if (width() <= 1) {
            throw new IllegalArgumentException("picture width <= 1");
        }
        validateSeam(seam, 0, width());

        Picture pic = new Picture(width() - 1, height());

        // go col by col and skip col where the seam pixel is given by col
        // resize the width
        for (int row = 0; row < height(); row++) {
            int rcol = 0;

            for (int col = 0; col < width(); col++) {
                if (col == seam[row]) {
                    continue;
                }
                pic.setRGB(rcol++, row, this.picture.getRGB(col, row));
            }
        }

        this.picture = pic; // new Picture(pic);
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("null hseam");
        }
        if (seam.length != width()) {
            throw new IllegalArgumentException("hseam length > picture width");
        }
        if (this.height() <= 1) {
            throw new IllegalArgumentException("picture height <= 1");
        }
        validateSeam(seam, 0, height());

        Picture pic = new Picture(width(), height() - 1);

        // go row by row and skip row where the seam pixel is given by row
        // resize the height
        for (int col = 0; col < width(); col++) {
            int rrow = 0;

            for (int row = 0; row < height(); row++) {
                if (row == seam[col]) {
                    continue;
                }
                pic.setRGB(col, rrow++, this.picture.getRGB(col, row));
            }
        }

        this.picture = pic; // new Picture(pic);
    }

    private void validateSeam(int[] seam, int min, int max) {
        for (int i = 0; i < seam.length - 1; i++) {
            int c = seam[i];
            int n = seam[i + 1];
            if (c < min || c > max) {
                throw new IllegalArgumentException("seam value out of range: " + c);
            } else if (Math.abs(n - c) > 1) {
                throw new IllegalArgumentException("seam value out of adjacent deviation greater than 1: " + c);
            }
        }

        int last = seam[seam.length - 1];
        if (last == seam.length) {
            if (last < min || last > max) {
                throw new IllegalArgumentException("seam value out of range: " + last);
            }
        }
    }
    
    private int vvertex2Col(int vertex) {
        return vertex % picture.width();
    }

    private int vvertex2Row(int vertex) {
        return vertex / picture.width();
    }

    private int hvertex2Col(int vertex) {
        return vertex / picture.height();
    }

    private int hvertex2Row(int vertex) {
        return vertex % picture.height();
    }

    private EdgeWeightedDigraph buildVerticalGraph() {
        int vertices = picture.height() * picture.width();
        int rootId = vertices;
        EdgeWeightedDigraph graph = new EdgeWeightedDigraph(vertices + 1);

        if (picture.height() == 1) {
            for (int col = 0; col < picture.width(); col++) {
                int v = col;
                graph.addEdge(new DirectedEdge(rootId, v, 0));
            }
        }

        for (int row = 0; row < picture.height() - 1; row++) {
            for (int col = 0; col < picture.width(); col++) {
                int v = row * picture.width() + col;
                if (v < picture.width()) {
                    graph.addEdge(new DirectedEdge(rootId, v, 0));
                }
                if (picture.height() == 1) {
                    continue;
                }

                int S = v + picture.width();
                int SW = S - 1;
                int SE = S + 1;
                double energy = energy(col, row);

                // SW
                if (col > 0) {
                    graph.addEdge(new DirectedEdge(v, SW, energy));
                }
                // S
                graph.addEdge(new DirectedEdge(v, S, energy));
                // SE
                if (col < picture.width() - 1) {
                    graph.addEdge(new DirectedEdge(v, SE, energy));
                }
            }
        }

//        StdOut.println(graphToDot(vgraph));
        return graph;
    }

    private EdgeWeightedDigraph buildHorizontalGraph() {
        int vertices = picture.height() * picture.width();
        int rootId = vertices;
        EdgeWeightedDigraph graph = new EdgeWeightedDigraph(vertices + 1);

        if (picture.width() == 1) {
            for (int row = 0; row < picture.height(); row++) {
                int v = row;
                graph.addEdge(new DirectedEdge(rootId, v, 0));
            }
        }

        for (int col = 0; col < picture.width() - 1; col++) {
            for (int row = 0; row < picture.height(); row++) {
                int v = col * picture.height() + row;
                if (v < picture.height()) {
                    graph.addEdge(new DirectedEdge(rootId, v, 0));
                }
                int W = v + picture.height();
                int NW = W - 1;
                int SW = W + 1;
                double energy = energy(col, row);

                // NW
                if (row > 0) {
                    graph.addEdge(new DirectedEdge(v, NW, energy));
                }
                // W
                graph.addEdge(new DirectedEdge(v, W, energy));
                // SW
                if (row < picture.height() - 1) {
                    graph.addEdge(new DirectedEdge(v, SW, energy));
                }
            }
        }

//        StdOut.println(graphToDot(hgraph));
        return graph;
    }

    private String graphToDot(EdgeWeightedDigraph g) {
        StringBuilder s = new StringBuilder();
        s.append("digraph G {\n");
        for (int v = 0; v < g.V(); v++) {
            int count = 0;
            for (DirectedEdge e : g.adj(v)) {
                count++;
                s.append(
                        // String.format("%d->%d [weight=%d, label=%d]\n", e.from(), e.to(),
                        // (int) e.weight(),
                        // (int) e.weight())
                        String.format("%d->%d [weight=%d]\n", e.from(), e.to(), (int) e.weight()));
            }
            if (count == 0) {
                s.append(v + "\n");
            }
        }
        s.append("}\n");
        return s.toString();
    }

    private class SeamHorizontalVertical implements Comparable<SeamHorizontalVertical> {
        private double energy = Double.MAX_VALUE;
        private List<DirectedEdge> path;

        public SeamHorizontalVertical(Iterable<DirectedEdge> path) {
            this.path = toList(path);
        }

        public int compareTo(SeamHorizontalVertical that) {
            return Double.compare(this.energy(), that.energy());
        }

        public double energy() {
            if (this.energy != Double.MAX_VALUE) {
                return this.energy;
            }

            this.energy = 0.0;
            path.forEach(edge -> {
                this.energy += edge.weight();
            });
            this.energy += MAX_ENERGY;

            return this.energy;
        }

        public int[] getVerticalSeam() {
            List<Integer> seam = new ArrayList<>();
            if (path.size() == 1) {
                seam.add(vvertex2Col(path.get(0).to()));
                return toArray(seam);
            }

            for (int i = 1; i < path.size(); i++) {
                DirectedEdge edge = path.get(i);
                if (i == 1) {
                    seam.add(vvertex2Col(edge.from()));
                }
                seam.add(vvertex2Col(edge.to()));
            }
            return toArray(seam);
        }

        public int[] getHorizontalSeam() {
            List<Integer> seam = new ArrayList<>();
            if (path.size() == 1) {
                seam.add(hvertex2Row(path.get(0).to()));
                return toArray(seam);
            }

            for (int i = 1; i < path.size(); i++) {
                DirectedEdge edge = path.get(i);
                if (i == 1) {
                    seam.add(hvertex2Row(edge.from()));
                }
                seam.add(hvertex2Row(edge.to()));
            }
            return toArray(seam);
        }

        private <T> List<T> toList(Iterable<T> iterable) {
            List<T> list = new ArrayList<>();
            if (iterable != null) {
                iterable.forEach(list::add);
            }
            return list;
        }
        
        private int[] toArray(List<Integer> list) {
            int[] res = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                res[i] = list.get(i);
            }
            return res;
        }
    }

    private int getEnergy(int nextPixelRGB, int previousPixelRGB) {
        int r = getRed(nextPixelRGB) - getRed(previousPixelRGB);
        int g = getGreen(nextPixelRGB) - getGreen(previousPixelRGB);
        int b = getBlue(nextPixelRGB) - getBlue(previousPixelRGB);

        return r * r + g * g + b * b;
    }

    private int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    private int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private int getBlue(int rgb) {
        return (rgb >> 0) & 0xFF;
    }

    // unit testing (optional)
    public static void main(String[] args) {
    }
}
