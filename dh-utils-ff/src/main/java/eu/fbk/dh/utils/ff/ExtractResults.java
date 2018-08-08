package eu.fbk.dh.utils.ff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class ExtractResults {
    public static void main(String[] args) {
        Set<String> inputFiles = new HashSet<>();

        inputFiles.add("/Users/alessio/Google Drive/ff/clic/test2.vectors/res_feats.out");
        inputFiles.add("/Users/alessio/Google Drive/ff/clic/test2.vectors/res_feats7.out");
        inputFiles.add("/Users/alessio/Google Drive/ff/clic/test2.vectors/res_nosyn.out");

        try {
            for (String file : inputFiles) {
                File f = new File(file);
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line;

                int total = 0;
                int okStrAllFf = 0;
                int okStrMaj = 0;
                int deMauro4 = 0;
                int deMauro34 = 0;
                int strAllFf = 0;
                int strMaj = 0;
                int correctDM4 = 0;
                int correctDM34 = 0;
                int correctFF = 0;
                int correctMaj = 0;
                int totalFF = 0;
                int totalCO = 0;

                int Atotal = 0;
                int AstrAllFf = 0;
                int AstrMaj = 0;
                int AdeMauro4 = 0;
                int AdeMauro34 = 0;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\t");
                    if (parts.length < 5) {
                        continue;
                    }

                    String gold = parts[0];
                    Integer deMauro = Integer.parseInt(parts[1]);
                    Integer co = Integer.parseInt(parts[2]);
                    Integer ff = Integer.parseInt(parts[3]);

                    Atotal++;
                    if (deMauro >= 4) {
                        AdeMauro4++;
                    }
                    if (deMauro >= 3) {
                        AdeMauro34++;
                    }
                    if (ff > co) {
                        AstrMaj++;
                    }
                    if (co == 0) {
                        AstrAllFf++;
                    }

                    if (!gold.equals("no")) {
                        if (gold.equals("ff")) {
                            totalFF++;
                        }
                        if (gold.equals("co")) {
                            totalCO++;
                        }
                        total++;
                        if (deMauro >= 4) {
                            deMauro4++;
                            if (gold.equals("ff")) {
                                correctDM4++;
                            }
                        }
                        if (deMauro >= 3) {
                            deMauro34++;
                            if (gold.equals("ff")) {
                                correctDM34++;
                            }
                        }
                        if (ff > co) {
                            strMaj++;
                            if (gold.equals("ff")) {
                                okStrMaj++;
                                correctMaj++;
                            }
                        } else {
                            if (gold.equals("co")) {
                                okStrMaj++;
                            }
                        }
                        if (co == 0) {
                            strAllFf++;
                            if (gold.equals("ff")) {
                                okStrAllFf++;
                                correctFF++;
                            }
                        } else {
                            if (gold.equals("co")) {
                                okStrAllFf++;
                            }
                        }
                    }

                }

                System.out.println(String.format("No gold - %s %d %d %d %d %d", f.getName(), Atotal, AstrAllFf, AstrMaj, AdeMauro4, AdeMauro34));
                System.out.println(String.format("With gold - %s %d %d (%d) %d (%d) %d %d", f.getName(), total, strAllFf, okStrAllFf, strMaj, okStrMaj, deMauro4, deMauro34));
                System.out.println(String.format("%d %d - %d %d %d %d", totalFF, totalCO, correctFF, correctMaj, correctDM4, correctDM34));
                System.out.println();
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
