package compressione;

public class Mismatch{
    private ArrayList<Byte> ref;
    private ArrayList<Byte> tar;
    private short[] delta;
    private long position;

    public Mismatch(ArrayList<Byte> r, ArrayList<Byte> t){
        this.ref=r;
        this.tar=t;
        this.delta = new short[r.length];
        for(int i = 0; i<r.length; i++){
            System.out.println("Ricordati di riscrivere la class Mismatch che attualmente non va bene!");
            //this.delta[i] = t[i] - r[i];
        }
    }

    public Mismatch(long pos, ArrayList<Byte> r, ArrayList<Byte> t){
        this.ref=r;
        this.tar=t;
        this.position=pos;
        this.delta = new short[r.length];
        for(int i = 0; i<r.length; i++){
            System.out.println("Ricordati di riscrivere la class Mismatch che attualmente non va bene!");
            //this.delta[i] = t[i] - r[i];
        }
    }

    public ArrayList<Byte> getRef(){
        return this.ref;
    }

    public ArrayList<Byte> getTar(){
        return this.tar;
    }

    public short[] getDelta(){
        return this.delta;
    }

    public Boolean equals(Mismatch mm){
        if(this.ref.equals(mm.ref) && this.tar.equals(mm.tar)) 
            return true;

        else return false;
    }

    public Boolean hasSameDelta(Mismatch mm){
        if(this.delta.equals(mm.delta))
            return true;

        else return false;
    }

    //potrebbe non servire
    public Boolean hasSameRef(byte[] r){
        if(this.ref.equals(r))
            return true;

        else return false;
    }

    public static String patternToString(ArrayList<Byte> pattern){
        String out="";
        for(Byte b:pattern){
            out+=(char) b.byteValue();
        }
        return out;
    }
}
