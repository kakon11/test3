/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package prefquery_filterthenverify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Kakon
 */
public class Prefquery_FilterthenVerify {

    public static ArrayList<Tuple> tuples; // tuples collection
    public static int total_cluster;    //total number of clusters
    public static Group cluster[];    //Pareto-optimal objects: c'    
    public static Group groups[][]; //Pareto-optimal objects: jth user of ith cluster
    public static int total_group; //total number of groups in current cluster 
    public static int total_dimension;
    public static int dimension_no[];
    /*matrix_cluster array stores the partial order of common profile. Index 1 represents #dimension (0 is for actor). 
     Index 2 reprensets #cluster. 
     Index 3 and 4 represents row and column of the partial order respectively.
     matrix[0][1][2][3]=true means it represents dimnesion#0, cluster1. 2 & 3 are two vlaues of dimnesion#0.*/
    public static boolean matrix_cluster[][][][];
    /*incomparableSetCluster array stores the information of whether a value of corresponding partial order
     remains incomparable with all other values.*/
    public static boolean incomparableSetCluster[][][];
    public static boolean matrix[][][][][];
    public static boolean incomparableSet[][][][];
    public static long start_time, single_tuple_time, cumulative_time;

    public static void load_Data(String data) throws FileNotFoundException, IOException {
        File data_file = new File(data);

        BufferedReader reader = new BufferedReader(new FileReader(data_file));

        tuples = new ArrayList<Tuple>();

        String line;
        int counter = 0;

        //reader.readLine();
        while ((line = reader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            int temp_measure_values[] = new int[total_dimension];

            tokenizer.nextToken();

            for (int i = 0; i < total_dimension; i++) {
                temp_measure_values[i] = Integer.parseInt(tokenizer.nextToken().toString());
            }

            tuples.add(new Tuple(counter, temp_measure_values));

            counter++;
        }

        ArrayList<Tuple> temp_tuples = new ArrayList<Tuple>();
        temp_tuples.addAll(tuples);

        for (int i = 0; i < 1000; i++) {//duplicationg tuples
            tuples.addAll(temp_tuples);
        }

        reader.close();
    }

    public static int comparison(int t, int t_, boolean submatrix[][]) {

        if (t == t_) {
            return 1;//equal
        }
        if (submatrix[t][t_]) {
            return 2; //t dominates t_
        }
        if (submatrix[t_][t]) {
            return -2;//t_ dominates t 
        }
        return -1; //incomparable
    }

    public static boolean load_Cluster(int tuple_id, int clusterNo) throws FileNotFoundException, IOException {

        boolean isPareto = true;

        if (cluster[clusterNo].pareto_obtimal_objects.isEmpty() == false) {
            for (int j = 0; j < cluster[clusterNo].pareto_obtimal_objects.size(); j++) {
                boolean isIncomparble = false;
                int dominate = 0;
                int dominated = 0;
                int dom_count = 0;
                int id = cluster[clusterNo].pareto_obtimal_objects.get(j);

                for (int k = 0; k < total_dimension; k++) {
                    int dom = comparison(tuples.get(tuple_id).measure_values[k], tuples.get(id).measure_values[k], matrix_cluster[k][clusterNo]);

                    if (dom == 1) {
                        dom_count++;
                    } else if (dom == -1) {
                        isIncomparble = true;
                        break;
                    } else if (dom == 2) {
                        dominate++;
                    } else if (dom == -2) {
                        dominated++;
                    }
                    if (dominate > 0 && dominated > 0) {
                        isIncomparble = true;
                        break;
                    }
                }
                /*Identical with an existing Pareto-optimal object. No more computation is needed. 
                 It is also a Pareto-optimal object and not going to dominate any existing 
                 Pareto-optimal object. If it is not the case, 
                 then the identical one is not supposed to be Pareto-optimal.*/
                if (dom_count == total_dimension) {
                    break;
                }
                if (isIncomparble == false && dominated > 0) {
                    isPareto = false;
                    break;
                }
                if (dominate > 0 && dominated == 0) {
                    cluster[clusterNo].pareto_obtimal_objects.removeAt(j);
                    j--;

                    for (int l = 0; l < total_group; l++) {
                        groups[clusterNo][l].pareto_obtimal_objects.remove(id);
                    }
                }
            }
        }

        if (isPareto) {
            cluster[clusterNo].pareto_obtimal_objects.add(tuple_id);
        }
        return isPareto;
    }

    public static boolean load_Customer(int tuple_id, int clusterNo) throws FileNotFoundException, IOException {
        boolean paretoInGroup = false;

        for (int i = 0; i < total_group; i++) {
            int d = 0;

            for (; d < total_dimension; d++) {
                if (incomparableSet[d][clusterNo][i][tuples.get(tuple_id).measure_values[d]] == true) {
                    break;
                }
            }

            if (d == total_dimension) {
                continue;
            }

            boolean isPareto = true;

            if (groups[clusterNo][i].pareto_obtimal_objects.isEmpty() == false) {
                for (int j = 0; j < groups[clusterNo][i].pareto_obtimal_objects.size(); j++) {
                    boolean isIncomparble = false;
                    int dominate = 0;
                    int dominated = 0;
                    int dom_count = 0;

                    int id = groups[clusterNo][i].pareto_obtimal_objects.get(j);

                    for (int k = 0; k < total_dimension; k++) {
                        int dom = comparison(tuples.get(tuple_id).measure_values[k], tuples.get(id).measure_values[k], matrix[k][clusterNo][i]);

                        if (dom == 1) {
                            dom_count++;
                        } else if (dom == -1) {
                            isIncomparble = true;
                            break;
                        } else if (dom == 2) {
                            dominate++;
                        } else if (dom == -2) {
                            dominated++;
                        }
                        if (dominate > 0 && dominated > 0) {
                            isIncomparble = true;
                            break;
                        }
                    }
                    /*Identical with an existing Pareto-optimal object. No more computation is needed. 
                     It is also a Pareto-optimal object and not going to dominate any existing 
                     Pareto-optimal object. If it is not the case, 
                     then the identical one is not supposed to be Pareto-optimal.*/
                    if (dom_count == total_dimension) {
                        break;
                    }
                    if (isIncomparble == false && dominated > 0) {
                        isPareto = false;
                        break;
                    }
                    if (dominate > 0 && dominated == 0) {
                        groups[clusterNo][i].pareto_obtimal_objects.removeAt(j);
                        j--;
                    }
                }
            }

            if (isPareto) {
                groups[clusterNo][i].pareto_obtimal_objects.add(tuple_id);
                paretoInGroup = true;
            }
        }

        return paretoInGroup;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        /*
        args[0]=users per cluster
        args[1]=total dimension
        args[2]=dataset
        args[3]=user profiles
        args[4]=objects
         */
        total_dimension = Integer.parseInt(args[1]);
        
        load_Data(args[4]);
        
        File data_groupNo = new File(args[0]);
        BufferedReader readerGroupNo = new BufferedReader(new FileReader(data_groupNo));
        String lineGN = readerGroupNo.readLine();
        StringTokenizer tokenizerGN = new StringTokenizer(lineGN, " ");
        total_cluster = tokenizerGN.countTokens();
        int groupNo[] = new int[total_cluster];

        for (int j = 0; j < total_cluster; j++) {
            groupNo[j] = Integer.parseInt(tokenizerGN.nextToken());
        }
        readerGroupNo.close();

        dimension_no = new int[total_dimension];

        if (args[2].compareTo("1") == 0) {
            switch (total_dimension) {
                case 4:
                    dimension_no[3] = 200;
                case 3:
                    dimension_no[2] = 200;
                case 2:
                    dimension_no[1] = 25;
                case 1:
                    dimension_no[0] = 200;
            }
        } else {
            switch (total_dimension) {
                case 4:
                    dimension_no[3] = 200;
                case 3:
                    dimension_no[2] = 200;
                case 2:
                    dimension_no[1] = 200;
                case 1:
                    dimension_no[0] = 9;
            }
        }

        groups = new Group[total_cluster][];
        cluster = new Group[total_cluster];
        matrix = new boolean[total_dimension][total_cluster][][][];
        incomparableSet = new boolean[total_dimension][total_cluster][][];
        matrix_cluster = new boolean[total_dimension][total_cluster][][];
        incomparableSetCluster = new boolean[total_dimension][total_cluster][];

        for (int k = 0; k < total_cluster; k++) {
            total_group = groupNo[k];
            cluster[k] = new Group();
            groups[k] = new Group[total_group];

            for (int d = 0; d < total_dimension; d++) {
                matrix[d][k] = new boolean[total_group][dimension_no[d]][dimension_no[d]];
                matrix_cluster[d][k] = new boolean[dimension_no[d]][dimension_no[d]];
                incomparableSet[d][k] = new boolean[total_group][dimension_no[d]];
                incomparableSetCluster[d][k] = new boolean[dimension_no[d]];

                File data_fileCluster = new File(args[3] + k + "\\user_common.txt");
                BufferedReader readerCluster = new BufferedReader(new FileReader(data_fileCluster));
                int r = 0;
                StringTokenizer tokenizer;
                String line;

                while ((line = readerCluster.readLine()) != null) {
                    tokenizer = new StringTokenizer(line, " ");

                    for (int c = 0; c < dimension_no[d]; c++) {
                        matrix_cluster[d][k][r][c] = Byte.parseByte(tokenizer.nextToken()) == 1 ? true : false;
                        incomparableSetCluster[d][k][r] = incomparableSetCluster[d][k][c] = true;

                    }

                    r++;

                    if (r >= dimension_no[d]) {
                        break;
                    }
                }
                readerCluster.close();
            }

            for (int i = 0; i < total_group; i++) {
                groups[k][i] = new Group();

                for (int d = 0; d < total_dimension; d++) {
                    File data_file = new File(args[3] + k + "\\user" + i + ".txt");
                    BufferedReader reader = new BufferedReader(new FileReader(data_file));
                    int r = 0;
                    StringTokenizer tokenizer;
                    String line;

                    while ((line = reader.readLine()) != null) {
                        tokenizer = new StringTokenizer(line, " ");

                        for (int c = 0; c < dimension_no[d]; c++) {
                            matrix[d][k][i][r][c] = Byte.parseByte(tokenizer.nextToken()) == 1 ? true : false;
                            if (matrix[d][k][i][r][c] && matrix_cluster[0][k][r][c]) {
                                incomparableSet[d][k][i][r] = incomparableSet[d][k][i][c] = true;
                            }
                        }

                        r++;

                        if (r >= dimension_no[d]) {
                            break;
                        }
                    }
                    reader.close();
                }
            }
        }

        for (int i = 0; i < tuples.size(); i++) {
            for (int k = 0; k < total_cluster; k++) {
                start_time = System.currentTimeMillis();

                load_Customer(i, k);

                single_tuple_time = System.currentTimeMillis() - start_time;
                cumulative_time += single_tuple_time;
            }
            if (i % 10000 == 0) {
                System.out.print(i + "," + cumulative_time);

                int pareto_here = 0;

                for (int m = 0; m < total_cluster; m++) {
                    for (int n = 0; n < groups[m].length; n++) {
                        pareto_here += groups[m][n].pareto_obtimal_objects.size();
                    }
                }

                System.out.println("," + pareto_here);
            }
        }

        System.out.println("#############################");

        for (int m = 0; m < total_cluster; m++) {
            for (int n = 0; n < groups[m].length; n++) {
                for (int p = 0; p < groups[m][n].pareto_obtimal_objects.size(); p++) {
                    System.out.println(m + "," + n + "," + groups[m][n].pareto_obtimal_objects.get(p));
                }
            }
        }
    }
}
