package LCE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Extractor {
    LCS lcs;
    private String pool_dir;
    private String vector_dir;
    private String meta_pool_dir;

    private List<String> source_files;
    private int[] max_N_index_list;
    private List<List<String>> meta_pool_list = new ArrayList<>();
    private List<List<String>> cleaned_meta_pool_list = new ArrayList<>();

    public Extractor() {
        super();
        lcs = new LCS();
        pool_dir = "D:\\repository_d\\LCE\\target\\gumtree_vector.csv";
        vector_dir = "D:\\repository_d\\LCE\\target\\testVector.csv";
        meta_pool_dir = "D:\\repository_d\\LCE\\target\\commit_file.csv";
    }

    public void config(String pool_dir, String vector_dir, String meta_pool_dir, String result_dir) {
        this.pool_dir = pool_dir;
        this.vector_dir = vector_dir;
        this.meta_pool_dir = meta_pool_dir;
    }

    public void run() {
        int[][] pool_array;
        int[][] vector_array;
        float[] sim_score_array;
        Vector<Integer> index_list_to_remove = new Vector<Integer>();
        try {
            pool_array = list_to_int_array2d(CSV_to_ArrayList(pool_dir));
            System.out.println("[debug] original pool array size = " + pool_array.length);

            meta_pool_list = CSV_to_ArrayList(meta_pool_dir);
            System.out.println("[debug] meta pool list size = " + meta_pool_list.size());

            int[][] cleaned_pool_array = remove_empty_lines(pool_array, index_list_to_remove);
            cleaned_meta_pool_list = sync_removal(meta_pool_list, index_list_to_remove);
            System.out.println("[debug] cleaned pool array size = " + cleaned_pool_array.length);
            System.out.println("[debug] index_list_to_remove size : " + index_list_to_remove.size());
            System.out.println("[debug] cleaned meta pool list size = " + cleaned_meta_pool_list.size());

            vector_array = list_to_int_array2d(CSV_to_ArrayList(vector_dir));

            sim_score_array = new float[cleaned_pool_array.length];

            // score similarity on each line of pool and vector
            for (int i = 0; i < cleaned_pool_array.length; i++) {
                sim_score_array[i] = lcs.ScoreSimilarity(cleaned_pool_array[i], vector_array[0]);
            }

            max_N_index_list = indexesOfTopElements(sim_score_array, 10);
            System.out.println("[debug] max_N_index_list size = " + max_N_index_list.length);
            print_array(max_N_index_list);

            /*
             * for (int i = 0; i < 10; i++) {
             * System.out.println("[debug] target : ");
             * print_array(cleaned_pool_array[max_N_index_list[i]]);
             * System.out.println("[debug] tester : ");
             * print_array(vector_array[0]);
             * 
             * System.out.println("[debug] max score : " +
             * sim_score_array[max_N_index_list[i]]);
             * }
             */

        } catch (FileNotFoundException e) {
            System.out.println("[error] file not found exception"); // ERROR
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("[error] exception"); // ERROR
            e.printStackTrace();
        }
    }

    public List<String> extract() {
        source_files = new ArrayList<>();
        for (int i = 0; i < max_N_index_list.length; i++) {
            source_files.add(combine(cleaned_meta_pool_list.get(max_N_index_list[i])));
        }
        return source_files;
    }

    private List<List<String>> CSV_to_ArrayList(String filename) throws FileNotFoundException {
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename));) {
            while (scanner.hasNextLine()) {
                records.add(getRecordFromLine(scanner.nextLine()));
            }
        } catch (FileNotFoundException e) {
            System.out.println("[debug] file not found : " + filename); // DEBUG
            e.printStackTrace();
        }
        return records;
    }

    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }

    private int[] array_to_int(String[] array) {
        int[] converted = new int[array.length];
        // convert array to int array
        for (int i = 0; i < array.length; i++) {
            converted[i] = Integer.parseInt(array[i]);
        }
        // return int array
        return converted;
    }

    private int[][] list_to_int_array2d(List<List<String>> list) {
        int[][] array = new int[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            array[i] = array_to_int(list.get(i).toArray(new String[list.get(i).size()]));
        }
        return array;
    }

    public void print_array2d(int[][] dp) {
        for (int i = 0; i < dp.length; i++) {
            for (int j = 0; j < dp[i].length; j++) {
                System.out.print(dp[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void print_arraylist(List<List<String>> dp) {
        for (int i = 0; i < dp.size(); i++) {
            for (int j = 0; j < dp.get(i).size(); j++) {
                System.out.print(dp.get(i).get(j) + " ");
            }
            System.out.println();
        }
    }

    public void print_array(int[] dp) {
        for (int i = 0; i < dp.length; i++) {
            System.out.print(dp[i] + " ");
        }
        System.out.println();
    }

    public void print_list(List<Integer> dp) {
        for (int i = 0; i < dp.size(); i++) {
            System.out.print(dp.get(i) + " ");
        }
        System.out.println();
    }

    private String combine(List<String> list) {
        String combined = "";
        for (int i = 0; i < list.size(); i++) {
            combined += list.get(i) + ",";
        }
        // remove last comma of combined
        combined = combined.substring(0, combined.length() - 1);
        return combined;
    }

    // locate and remove empty lines in pool
    private int[][] remove_empty_lines(int[][] pool, Vector<Integer> index_list) {
        List<int[]> new_pool = new ArrayList<>();
        for (int i = 0; i < pool.length; i++) {
            if (pool[i].length != 0) {
                new_pool.add(pool[i]);
            } else {
                index_list.add(i);
            }
        }
        int[][] new_pool_array = new int[new_pool.size()][];
        for (int i = 0; i < new_pool.size(); i++) {
            new_pool_array[i] = new_pool.get(i);
        }
        return new_pool_array;
    }

    private List<List<String>> sync_removal(List<List<String>> meta_pool, Vector<Integer> index_list) {
        List<List<String>> synced_meta_pool = new ArrayList<>();
        for (int i = 0; i < meta_pool.size(); i++) {
            if (!index_list.contains(i)) {
                synced_meta_pool.add(meta_pool.get(i));
            } else {
                // print_line(meta_pool, i);
                // System.out.println("[debug] index " + i + " is removed"); // DEBUG
            }
        }
        return synced_meta_pool;
    }

    public void print_line(List<List<String>> test, int index) {
        // print element of test at index
        for (int i = 0; i < test.get(index).size(); i++) {
            System.out.println(test.get(index).get(i) + " ");
        }
    }

    public void print_line(List<String> strings){
        for (int i = 0; i < strings.size(); i++) {
            System.out.println(strings.get(i) + " ");
        }
    }

    // find index of top N max values in array and return index list
    private int[] indexesOfTopElements(float[] orig, int nummax) {
        float[] copy = Arrays.copyOf(orig, orig.length);
        Arrays.sort(copy);
        float[] honey = Arrays.copyOfRange(copy, copy.length - nummax, copy.length);
        int[] result = new int[nummax];
        int resultPos = 0;
        for (int i = 0; i < orig.length; i++) {
            float onTrial = orig[i];
            int index = Arrays.binarySearch(honey, onTrial);
            if (index < 0)
                continue;
            if (resultPos < nummax)
                result[resultPos++] = i;
        }
        return result;
    }
}
