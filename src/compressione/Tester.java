package compressione;

import java.io.*;

public class Tester {
    public static void main(String args[]) throws FileNotFoundException, IOException{

        //Number of bytes that will correspond to length of the blocks pointed by the dict.
        int c = 4;
        String name = "test";
        if(args.length > 1){
            c = Integer.parseInt(args[0]);
            name = args[1];
        }


        int mmlen = 4;

        String filenameReference = "test_files/"+name+".reference";
        String filenameTarget = "test_files/"+name+".target";
        String filenameCompressed = "test_files/"+name+".compressed";
        String filenameDecompressed = "test_files/"+name+".decompressed";

        System.out.println("Starting program");
        System.out.println("\ttarget file :\t" + filenameTarget);
        System.out.println("\treference file :\t" + filenameReference);
        System.out.println("\toutput file :\t" + filenameCompressed);

        System.out.println("\nStarting compression");
        Compressor compressor = new Compressor(c, mmlen, filenameReference, filenameTarget, filenameCompressed);
        if(!compressor.run()){
            System.out.println("Error while compressing");
            System.exit(1);
        }
        System.out.println("Finished compressing");
        System.out.println("Dictionary:\n"+compressor.getDictionary());

        System.out.println("\nStarting decompression");
        Decompressor decompressor = new Decompressor(c, mmlen, filenameReference, filenameCompressed, filenameDecompressed);
        if(!decompressor.run()){
            System.out.println("Error while decompressing");
            System.exit(1);
        }
        System.out.println("Finished decompressing");
        System.out.println("Dictionary:\n"+compressor.getDictionary());

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
        else
            System.err.println("Files are NOT identical");
    }
}
