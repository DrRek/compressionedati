package compressione;

import java.io.*;
import java.util.Arrays;

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

    public static void main(String args[]) throws FileNotFoundException, IOException{

        int mmlen = 4;

        //Number of bytes that will correspond to length of the blocks pointed by the dict.
        int c = 4;
        if(args.length > 1){
            c = Integer.parseInt(args[0]);
        }

        String basePath = "test_files/";
        String[] testFilesSrc = iterateOverTestFiles(basePath);

        //testFilesSrc = new String[]{"test97"};

        for(String dir : testFilesSrc){
            String testDir = basePath+dir;

            String filenameReference = testDir+"/input.reference";
            String filenameTarget = testDir+"/input.target";
            String filenameCompressed = testDir+"/output.compressed";
            String filenameDecompressed = testDir+"/output.decompressed";

            System.out.println("\nStarting compression of " + testDir);
            Compressor compressor = new Compressor(c, mmlen, filenameReference, filenameTarget, filenameCompressed);
            if(!compressor.run()){
                System.out.println("Error while compressing");
                System.exit(1);
            }
            System.out.println("Finished compressing");
            //System.out.println("Dictionary:\n"+compressor.getDictionary());

            System.out.println("\nStarting decompression of " + testDir);
            Decompressor decompressor = new Decompressor(c, mmlen, filenameReference, filenameCompressed, filenameDecompressed);
            if(!decompressor.run()){
                System.out.println("Error while decompressing");
                System.exit(1);
            }
            System.out.println("Finished decompressing");
            //System.out.println("Dictionary:\n"+decompressor.getDictionary());

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
                System.out.println("Files are identical!");
            else {
                System.exit(1);
                System.err.println("Files are NOT identical");
            }
        }
    }
}
