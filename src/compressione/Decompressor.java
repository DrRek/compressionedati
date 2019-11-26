package compressione;

public class Deompressor {
    private Dictionary dict;
    private File ref, tar, out;

    public Compressor(String ref, String tar, String out){
        this.dict = new Dictionary(ref);
        this.tar = new File(tar);
        this.out = new File(out);
    }

    public boolean run(){
        return true;
    }
}
