package compressione;

import SevenZip.Encoder;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tester {

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

        int mmlen = 16;

        //Number of bytes that will correspond to length of the blocks pointed by the dict.
        int c = 512;
        if(args.length > 1){
            c = Integer.parseInt(args[0]);
        }

        String basePath = "test_files/";
        String[] testFilesSrc = iterateOverTestFiles(basePath);

        testFilesSrc = new String[]{"Putty"};

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
            String testDir = basePath+dir;

            String filenameReference = testDir+"/input.reference";
            String filenameTarget = testDir+"/input.target";
            String filenameCompressed = testDir+"/output.compressed";
            String filenameDecompressed = testDir+"/output.decompressed";

            System.out.println("\nStarting compression of " + testDir);
            NewCompressor compressor = new NewCompressor(c, mmlen, filenameReference, filenameTarget, filenameCompressed);
            if(!compressor.run()){
                System.out.println("Error while compressing");
                System.exit(1);
            }
            System.out.println("Finished compressing");

            Encoder.main(new String[]{filenameTarget, testDir + "/output.7z"});

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

            if(equals)
                System.out.println("\nFiles are identical!");
            else {
                System.exit(1);
                System.err.println("\nFiles are NOT identical");
            }
        }
    }
}
