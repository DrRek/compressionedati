package compressione;

import SevenZip.Encoder;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;

public class Tester {
    private static BufferedWriter writer;

    public static String[] iterateOverTestFiles(String path){
        File file = new File(path);
        return file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
    }

    public static void main(String args[]) throws Exception {

         writer = new BufferedWriter(new FileWriter("resultsFile.txt", true));

        int[] insiemeDiC        = new int[]{ 1024, 1024, 512, 256, 256, 128};
        int[] insiemeDiMMLEN    = new int[]{   32,   16,  16,  16,   8,   8};

        for(int i = 0; i<insiemeDiC.length; i++){
            int c = insiemeDiC[i];
            int mmlen = insiemeDiMMLEN[i];
            writer.write("[STARTING TESTS FOR C="+c+" and MMLEN="+mmlen+"]\n");
            writer.flush();
            iniziaWajo(c, mmlen);
            System.out.println("Done "+(i+1)+"/"+insiemeDiC.length);
        }

        writer.newLine();
        writer.newLine();
        writer.newLine();
        writer.close();


    }

    private static void iniziaWajo(int c, int mmlen) throws Exception {
        String basePath = "test_files/";
        String[] testFilesSrc = iterateOverTestFiles(basePath);

//        testFilesSrc = new String[]{"Putty"};

//        String[] toRemove = {"test97", "test93"};
//        List<String> s1List = new ArrayList(Arrays.asList(toRemove));
//        for (String s : testFilesSrc) {
//            if (s1List.contains(s)) {
//                s1List.remove(s);
//            } else {
//                s1List.add(s);
//            }
//        }
//        testFilesSrc = new String[s1List.size()];
//        testFilesSrc = s1List.toArray(testFilesSrc);

        for(String dir : testFilesSrc){

            writer.write("\tTesting file:"+dir);
            writer.write("\tIniziato alle: ");
            printDate();
            writer.newLine();
            writer.flush();
            String testDir = basePath+dir;

            String filenameReference = testDir+"/input.reference";
            String filenameTarget = testDir+"/input.target";
            String filenameCompressed = testDir+"/output.compressed";
            String filenameDecompressed = testDir+"/output.decompressed";
            String filename7z = testDir + "/output.7z";

            System.out.println("\nStarting compression of " + testDir);
            NewCompressor compressor = new NewCompressor(c, mmlen, filenameReference, filenameTarget, filenameCompressed);
            if(!compressor.run()){
                System.out.println("Error while compressing");
                System.exit(1);
            }
            System.out.println("Finished compressing");

            Encoder.main(new String[]{filenameCompressed, filename7z});

            System.out.println("\nStarting decompression of " + testDir);
            NewDecompressor decompressor = new NewDecompressor(c, mmlen, filenameReference, filenameCompressed, filenameDecompressed);
            if(!decompressor.run()){
                System.out.println("Error while decompressing");
                System.exit(1);
            }
            System.out.println("Finished decompressing");

            BufferedReader pre = new BufferedReader(new FileReader(new File(filenameTarget)));
            BufferedReader post = new BufferedReader(new FileReader(new File(filenameDecompressed)));
            boolean equals=true;
            String line1, line2;
            while (((line1 = pre.readLine()) != null)&&((line2 = post.readLine()) != null))
            {
                if (!line1.equals(line2))
                {
                    equals=false;
                    break;
                }
            }

            if(equals){
                String out = getFormattedFileSize(filenameReference);
                writer.write("reference "+out);
                out = getFormattedFileSize(filenameTarget);
                writer.write("target "+out);
                out = getFormattedFileSize(filenameCompressed);
                writer.write("compressed "+out);
                out = getFormattedFileSize(filename7z);
                writer.write("7z "+out);
                writer.write("finito alle: ");
                printDate();

                System.out.println("\nFiles are identical!");
            } else {
                writer.write("ERROR FOR THIS FILE");
                System.err.println("\nFiles are NOT identical");
            }
            writer.newLine();
            writer.newLine();
            writer.flush();
        }
    }

    private static String getFormattedFileSize(String filenameReference) {
        File one = new File(filenameReference);
        return "size = "+getFileSizeBytes(one) +", "+getFileSizeKiloBytes(one)+", "+getFileSizeMegaBytes(one)+"\n";

    }

    private static String getFileSizeMegaBytes(File file) {
        return (double) file.length() / (1024 * 1024) + " mb";
    }

    private static String getFileSizeKiloBytes(File file) {
        return (double) file.length() / 1024 + "  kb";
    }

    private static String getFileSizeBytes(File file) {
        return file.length() + " bytes";
    }

    private static void printDate() throws IOException {
        String format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
        writer.write(format1);
    }
}
