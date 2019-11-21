package compressione;

public class Encoder{
    
    private final int MMlen=4;
    private final int C=4;

    private Dictionary dictionary;
    private String referenceFile;
    private String targetFile;
    private String patch;
    
    public Encoder(String r, String t){
        this.dictionary=new Dictionary(C);
        this.referenceFile=r;
        this.targetFile=t;
        this.patch=t+"_patch";
    }
    
    public void copy(){

    }

    public void set(){

    }


}