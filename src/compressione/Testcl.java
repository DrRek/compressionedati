package compressione;

import java.io.*;
import java.util.Arrays;

public class Testcl {
    public static void main(String args[]) throws IOException {

        String filenameTarget = "test_files/test.target";
        String filenameDecompressed = "test_files/test.decompressed";

//        FileReader pre1 =new FileReader(new File(filenameTarget));
//        FileWriter post1 = new FileWriter(new File(filenameDecompressed));
//        char[] line = new char[10];
//        int readChars;
//        while ( (readChars = pre1.read(line)) > -1 )
//        {
//            post1.write(Arrays.copyOfRange(line, 0, readChars));
//        }

        BufferedReader pre1 = new BufferedReader(new FileReader(new File(filenameTarget)));
        FileWriter post1 = new FileWriter(new File(filenameDecompressed));
        String res;
        while( (res = pre1.readLine()) != null){
            post1.write(res);
        }

        pre1.close();
        post1.close();




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

        pre.close();
        post.close();

        if(equals)
            System.out.println("Files are identical!");
        else
            System.err.println("Files are NOT identical");
    }
}
