package compressione;

import java.util.*;
import java.io.*;

public class NewTester {
    public static void main(String args[]) throws FileNotFoundException, IOException{

        //Number of bytes that will correspond to length of the blocks pointed by the dict.
        int c = 4;
        String name = "test";
        if(args.length > 1){
            c = Integer.parseInt(args[0]);
            name = args[1];
        }


        int mmlen = 4;

        String filenameReference = "../references/Compressor.java";
        String filenameTarget = "Compressor.java";
        String filenameCompressed = "test_files/patch.txt";
        String filenameDecompressed = "test_files/test.decompressed";

        System.out.println("Starting compression");
        System.out.println("\ttarget file :\t" + filenameTarget);
        System.out.println("\treference file :\t" + filenameReference);
        System.out.println("\toutput file :\t" + filenameCompressed);
        Compressor compressor = new Compressor(c, mmlen, filenameReference, filenameTarget, filenameCompressed);
        if(!compressor.run()){
            System.out.println("Error while compressing");
            System.exit(1);
        }
        System.out.println("\nFinished compressing");
//        System.out.println("Dictionary:\n"+compressor.getDictionary());

        System.out.println("starting decompression");
        Decompressor decompressor = new Decompressor(c, mmlen, filenameReference, filenameCompressed, filenameDecompressed);
        if(!decompressor.run()){
            System.out.println("Error while decompressing");
            System.exit(1);
        }
    }
}
