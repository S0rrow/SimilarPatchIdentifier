package LCE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Extractor {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    LCS lcs;
    private String gumtree_vector_path;
    private String target_vector_path;
    private String commit_file_path;

    private List<String> source_files;
    private int[] max_N_index_list;
    private List<List<String>> meta_pool_list = new ArrayList<>();
    private List<List<String>> cleaned_meta_pool_list = new ArrayList<>();
    private int nummax;

    public Extractor(String[] args) {
        super();
        lcs = new LCS();
        gumtree_vector_path = args[3]; // gumtree vector path
        commit_file_path = args[0]; // commit file path
        target_vector_path = args[4]; // target dir
        nummax = args[7].equals("") ? 10 : Integer.parseInt(args[7]); // nummax
    }

    public void config(String pool_dir, String vector_dir, String meta_pool_dir, String result_dir) {
        this.gumtree_vector_path = pool_dir;
        this.target_vector_path = vector_dir;
        this.commit_file_path = meta_pool_dir;
    }

    public void run() {
        int[][] pool_array;
        int[][] vector_array;
        float[] sim_score_array;
        Vector<Integer> index_list_to_remove = new Vector<Integer>();
        try {
            pool_array = list_to_int_array2d(CSV_to_ArrayList(gumtree_vector_path));
            System.out.println(ANSI_BLUE + "[status] original pool array size = " + pool_array.length);

            meta_pool_list = CSV_to_ArrayList(commit_file_path);
            System.out.println(ANSI_BLUE + "[status] meta pool list size = " + meta_pool_list.size());

            int[][] cleaned_pool_array = remove_empty_lines(pool_array, index_list_to_remove);
            cleaned_meta_pool_list = sync_removal(meta_pool_list, index_list_to_remove);
            System.out.println(ANSI_BLUE + "[status] cleaned pool array size = " + cleaned_pool_array.length);
            System.out.println(ANSI_BLUE + "[status] index_list_to_remove size : " + index_list_to_remove.size());
            System.out.println(ANSI_BLUE + "[status] cleaned meta pool list size = " + cleaned_meta_pool_list.size());

            vector_array = list_to_int_array2d(CSV_to_ArrayList(target_vector_path));

            sim_score_array = new float[cleaned_pool_array.length];

            // score similarity on each line of pool and vector
            for (int i = 0; i < cleaned_pool_array.length; i++) {
                sim_score_array[i] = lcs.ScoreSimilarity(cleaned_pool_array[i], vector_array[0]);
            }

            max_N_index_list = indexesOfTopElements(sim_score_array, nummax);
            System.out.println(ANSI_BLUE + "[status] max_N_index_list size = " + max_N_index_list.length);
        } catch (FileNotFoundException e) {
            System.out.println(ANSI_RED + "[error] file not found exception"); // ERROR
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(ANSI_RED + "[error] exception"); // ERROR
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
            System.out.println(ANSI_RED + "[status] file not found : " + filename); // DEBUG
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
                // System.out.println("[debug] index " + i + " is removed"); // DEBUG
            }
        }
        return synced_meta_pool;
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
