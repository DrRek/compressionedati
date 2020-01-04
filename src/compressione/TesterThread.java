package compressione;

import SevenZip.Encoder;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.*;

public class TesterThread {
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

        int[] insiemeDiC        = new int[]{ 128, 64, 64 };
        int[] insiemeDiMMLEN    = new int[]{  8,   8, 16 };
        int threadN = 12;

        RunnableThreadTester[] listThread = new RunnableThreadTester[threadN];

        for(int i = 0; i<threadN; i++){
            RunnableThreadTester asd = new RunnableThreadTester(insiemeDiC, insiemeDiMMLEN, i, threadN);
            asd.start();
            listThread[i] = asd;
        }
    }

    private static class RunnableThreadTester  extends Thread{

        private final int id;
        private final int[] mmlen;
        private final int[] c;
        private final int maxNumberOfThread;

        RunnableThreadTester(int[] c, int[] mmlen, int id, int maxNumberOfThread){
            this.id = id;
            this.mmlen = mmlen;
            this.c = c;
            this.maxNumberOfThread = maxNumberOfThread;
        }

        @Override
        public void run() {
            String basePath = "test_files/";
//            String[] set = iterateOverTestFiles(basePath);
            String[] set = {"Internet Explorer Web Browser", "Notepad++", "Opera", "Putty", "FileZilla", "MINGW gcc", "Mozilla Firefox", "AdobeReader", "VLC", "AVG antivirus"};

            for(int currentFileIndex = id; currentFileIndex < set.length; currentFileIndex = currentFileIndex + maxNumberOfThread){
                String dir = set[currentFileIndex];
                String testDir = basePath+dir;

                String filenameReference = testDir+"/input.reference";
                String filenameTarget = testDir+"/input.target";
                String filenameCompressed = testDir+"/output.compressed";
                String filenameDecompressed = testDir+"/output.decompressed";
                String filename7z = testDir + "/output.7z";
                String results = testDir + "/results.txt";

                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(results, true));

                    writer.newLine();
                    writer.newLine();
                    writer.write("\tIniziato test globale del file alle: "+printDate());
                    writer.newLine();
                    writer.newLine();
                    writer.flush();

                    for(int i = 0; i<c.length; i++) {
                        int cS = c[i];
                        int mmlenS = mmlen[i];

                        NewCompressor compressor = new NewCompressor(cS, mmlenS, filenameReference, filenameTarget, filenameCompressed);
                        if (!compressor.run()) {
                            System.out.println("Error while compressing");
                            System.exit(1);
                        }

                        Encoder.main(new String[]{filenameCompressed, filename7z});

//                        NewDecompressor decompressor = new NewDecompressor(cS, mmlenS, filenameReference, filenameCompressed, filenameDecompressed);
//                        if (!decompressor.run()) {
//                            System.out.println("Error while decompressing");
//                            System.exit(1);
//                        }

//                        BufferedReader pre = new BufferedReader(new FileReader(new File(filenameTarget)));
//                        BufferedReader post = new BufferedReader(new FileReader(new File(filenameDecompressed)));
//                        boolean equals = true;
//                        String line1, line2;
//                        while (((line1 = pre.readLine()) != null) && ((line2 = post.readLine()) != null)) {
//                            if (!line1.equals(line2)) {
//                                equals = false;
//                                break;
//                            }
//                        }

//                        if (equals) {
                            writer.write("test with c = "+cS+" and mmlen = "+ mmlenS+"\n");
                            String out = getFormattedFileSize(filenameReference);
                            writer.write("reference " + out);
                            out = getFormattedFileSize(filenameTarget);
                            writer.write("target " + out);
                            out = getFormattedFileSize(filenameCompressed);
                            writer.write("compressed " + out);
                            out = getFormattedFileSize(filename7z);
                            writer.write("7z " + out);
                            writer.write("finito alle: "+printDate());

//                        } else {
//                            writer.write("ERROR FOR THIS FILE");
//                            System.err.println("\nFiles are NOT identical");
//                        }
                        writer.newLine();
                        writer.newLine();
                        writer.flush();

                        System.out.println("Thread "+id+" : finito "+dir+" per c="+cS+" e mmlen="+mmlenS);
                    }

                    System.out.println("Thread "+id+" ha terminato");
                    writer.write("\tFinito test globale del file alle: "+printDate());
                    writer.newLine();
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        private static String printDate() throws IOException {
            String format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
            return format1;
        }
    }
}
