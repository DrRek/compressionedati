package compressione;

public class Tester {
    public static void main(String args[]){

        //Number of bytes that will correspond to length of the blocks pointed by the dict.
        int c = 4;
        if(args.length > 0){
            c = Integer.parseInt(args[0]);
        }

        String filename = "test_files/test";
        Dictionary dict = new Dictionary(c);
        dict.addFile(filename);

        System.out.println("dizionario\n"+dict);
    }
}
