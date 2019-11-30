package compressione;

import java.util.*;
import java.io.*;

public class Tester {
    public static void main(String args[]) throws FileNotFoundException, IOException{

        //Number of bytes that will correspond to length of the blocks pointed by the dict.
        int c = 4;
        if(args.length > 0){
            c = Integer.parseInt(args[0]);
        }

        int mmlen = 4;

        String filenameReference = "test_files/test.reference";
        String filenameTarget = "test_files/test.target";
        String filenameCompressed = "test_files/test.compressed";
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
        System.out.println("Dictionary:\n"+compressor.getDictionary());

        //System.out.println("starting decompression");
        //Decompressor decompressor = new Decompressor(c, filenameReference, filenameCompressed, filenameDecompressed);
        //if(!decompressor.run()){
        //    System.out.println("Error while decompressing");
        //    System.exit(1);
        //}
    }
}
