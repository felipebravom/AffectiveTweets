/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fantail.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

/**
 *
 * @author Quan Sun quan.sun.nz@gmail.com
 * @author <http://mitlab.hit.edu.cn/2011summerschool/related/Correlation.java>
 */
public class Correlation {

    public static double rankKendallTauBeta(double[] x, double[] y) {
        assert x.length == y.length;
        int x_n = x.length;
        int y_n = y.length;
        double[] x_rank = new double[x_n];
        double[] y_rank = new double[y_n];

        TreeMap<Double, HashSet<Integer>> sorted = new TreeMap<>();
        for (int i = 0; i < x_n; i++) {
            double v = x[i];
            if (sorted.containsKey(v) == false) {
                sorted.put(v, new HashSet<Integer>());
            }
            sorted.get(v).add(i);
        }

        int c = 1;
        for (double v : sorted.descendingKeySet()) {
            double r = 0;
            for (int i : sorted.get(v)) {
                r += c;
                c++;
            }

            r /= sorted.get(v).size();

            for (int i : sorted.get(v)) {
                x_rank[i] = r;
            }
        }

        sorted.clear();
        for (int i = 0; i < y_n; i++) {
            double v = y[i];
            if (sorted.containsKey(v) == false) {
                sorted.put(v, new HashSet<Integer>());
            }
            sorted.get(v).add(i);
        }

        c = 1;
        for (double v : sorted.descendingKeySet()) {
            double r = 0;
            for (int i : sorted.get(v)) {
                r += c;
                c++;
            }

            r /= (sorted.get(v).size());

            for (int i : sorted.get(v)) {
                y_rank[i] = r;
            }
        }

        return kendallTauBeta(x_rank, y_rank);
    }

    public static double kendallTauBeta(double[] x, double[] y) {
        assert x.length == y.length;

        int c = 0;
        int d = 0;
        HashMap<Double, HashSet<Integer>> xTies = new HashMap<>();
        HashMap<Double, HashSet<Integer>> yTies = new HashMap<>();

        for (int i = 0; i < x.length - 1; i++) {
            for (int j = i + 1; j < x.length; j++) {
                if (x[i] > x[j] && y[i] > y[j]) {
                    c++;
                } else if (x[i] < x[j] && y[i] < y[j]) {
                    c++;
                } else if (x[i] > x[j] && y[i] < y[j]) {
                    d++;
                } else if (x[i] < x[j] && y[i] > y[j]) {
                    d++;
                } else {
                    if (x[i] == x[j]) {
                        if (xTies.containsKey(x[i]) == false) {
                            xTies.put(x[i], new HashSet<Integer>());
                        }
                        xTies.get(x[i]).add(i);
                        xTies.get(x[i]).add(j);
                    }

                    if (y[i] == y[j]) {
                        if (yTies.containsKey(y[i]) == false) {
                            yTies.put(y[i], new HashSet<Integer>());
                        }
                        yTies.get(y[i]).add(i);
                        yTies.get(y[i]).add(j);
                    }
                }
            }
        }

        int diff = c - d;
        double denom = 0;

        double n0 = (x.length * (x.length - 1)) / 2.0;
        double n1 = 0;
        double n2 = 0;

        for (double t : xTies.keySet()) {
            double s = xTies.get(t).size();
            n1 += (s * (s - 1)) / 2;
        }

        for (double t : yTies.keySet()) {
            double s = yTies.get(t).size();
            n2 += (s * (s - 1)) / 2;
        }

        denom = Math.sqrt((n0 - n1) * (n0 - n2));

        if (denom == 0) {
            denom += 0.000000001;
        }

        double t = diff / (denom); // 0.000..1 added on 11/02/2013 fixing NaN error

        assert t >= -1 && t <= 1 : t;

        return t;
    }

    public static double pearson(double[] x, double[] y) {
        assert x.length == y.length;
        double mean_x = 0;
        double mean_y = 0;
        int n_x = x.length;
        int n_y = y.length;
        for (int i = 0; i < n_x; i++) {
            mean_x += x[i];
            mean_y += y[i];
        }
        mean_x /= n_x;
        mean_y /= n_y;

        double cov = 0;
        double sd_x = 0;
        double sd_y = 0;

        for (int i = 0; i < n_x; i++) {
            cov += (x[i] - mean_x) * (y[i] - mean_y);
            sd_x += (x[i] - mean_x) * (x[i] - mean_x);
            sd_y += (y[i] - mean_y) * (y[i] - mean_y);
        }

        if (cov == 0) {
            return 0;
        } else {
            double r = cov / (Math.sqrt(sd_x) * Math.sqrt(sd_y));
            assert r >= -1 && r <= 1 : r + "\n" + printArray(x) + printArray(y) + "Mean x = " + mean_x + ", Mean y = " + mean_y + ", Cov = " + cov + ", SD x = " + sd_x + ", SD y = " + sd_y + "\n";
            return r;
        }
    }

    private static String printArray(double[] a) {
        String ret = "";
        for (double d : a) {
            ret = ret + d + " ";
        }
        ret = ret + "\n";
        return ret;
    }

    public static double spearman(double[] x, double[] y) {
        assert x.length == y.length;
        int x_n = x.length;
        int y_n = y.length;
        double[] x_rank = new double[x_n];
        double[] y_rank = new double[y_n];

        TreeMap<Double, HashSet<Integer>> sorted = new TreeMap<>();
        for (int i = 0; i < x_n; i++) {
            double v = x[i];
            if (sorted.containsKey(v) == false) {
                sorted.put(v, new HashSet<Integer>());
            }
            sorted.get(v).add(i);
        }

        int c = 1;
        for (double v : sorted.descendingKeySet()) {
            double r = 0;
            for (int i : sorted.get(v)) {
                r += c;
                c++;
            }

            r /= sorted.get(v).size();

            for (int i : sorted.get(v)) {
                x_rank[i] = r;
            }
        }

        sorted.clear();
        for (int i = 0; i < y_n; i++) {
            double v = y[i];
            if (sorted.containsKey(v) == false) {
                sorted.put(v, new HashSet<Integer>());
            }
            sorted.get(v).add(i);
        }

        c = 1;
        for (double v : sorted.descendingKeySet()) {
            double r = 0;
            for (int i : sorted.get(v)) {
                r += c;
                c++;
            }

            r /= sorted.get(v).size();

            for (int i : sorted.get(v)) {
                y_rank[i] = r;
            }
        }

        return pearson(x_rank, y_rank);
    }

    public static void main(String[] args) {
        double[] x = {0.5, 0.7, 0, 0.3, 0, 0.3, 0.9};
        double[] y = {0, 0.3, 0, 0.4, 0, 0.1, 0.6};
        System.out.println("Pearson's r: " + pearson(x, y));
        System.out.println("Spearman's rho: " + spearman(x, y));
        System.out.println("Kendall's tau: " + rankKendallTauBeta(x, y));
    }
}
