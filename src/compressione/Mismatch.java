package compressione;

public class Mismatch{
    private byte[] ref;
    private byte[] tar;
    private short[] delta;

    public Mismatch(byte[] r, byte[] t){
        this.ref=r;
        this.tar=t;
        this.delta = new short[r.length];
        for(int i = 0; i<r.length; i++){
            this.delta[i] = t[i] - r[i];
        }
    }

    public byte[] getRef(){
        return this.ref;
    }

    public byte[] getTar(){
        return this.tar;
    }

    public byte[] getDelta(){
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
}
