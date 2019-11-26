package compressione;

import java.util.*;
import java.io.*;

public class Tester {
    public static void main(String args[]) throws FileNotFoundException{

        //Number of bytes that will correspond to length of the blocks pointed by the dict.
        int c = 4;
        if(args.length > 0){
            c = Integer.parseInt(args[0]);
        }

        String filenameReference = "test_files/test.reference";
        String filenameTarget = "test_files/test.target";
        String filenameCompressed = "test_files/test.compressed";
        String filenameDecompressed = "test_files/test.decompressed";

        Compressor compressor = new Compressor(c, filenameReference, filenameTarget, filenameCompressed);

        Decompressor decompressor = new Decompressor(c, filenameReference, filenameCompressed, filenameDecompressed);

        if(!compressor.run()){
            System.out.println("Error while compressing");
            System.exit(1);
        }
        if(!decompressor.run()){
            System.out.println("Error while decompressing");
            System.exit(1);
        }
    }
}
