package compressione;

public class Compressor {
    private Dictionary dict;
    private File ref, tar, out;
    private int c;

    public Compressor(int c, String ref, String tar, String out){
        this.dict = new Dictionary(ref);
        this.tar = new File(tar);
        this.out = new File(out);
        this.c = c;
    }

    public boolean run(){
        byte[] b = new byte[this.c];
        RandomAccessFile tar = new RandomAccessFile(this.tar, "r");

        int readBytes = 0;
        while((readBytes = tar.read(b)) != -1){
            String currentBlock = new String(Arrays.copyOfRange(b, 0, readBytes));

            if(readBytes < c){
                //Probabilmente sono arrivato a leggere la fine del file
                //a questo punto mi conviene fare solo una set
            } else {
                //In questo caso readBytes == c, quindi posso cercare un
                //match e farne l'encoding
                List<BlockPointer> possibleMatches = this.dict.getPointerForBlock(currentBlock);
                Match getBestMatch = this.getBestMatch(possibleMatches)
                
                if(getBestMatch == null){
                    //In questo caso per un motivo o per un altro non sono
                    //riuscito a trovare un match, bisogna aggiungere il
                    //primo byte ad un buffer che verra codificato con una
                    //set prima del prossimo messaggio di match
                } else {
                    //In questo caso ho trovato un match, devo prima
                    //encodare tutte le set presenti nel buffer e poi devo
                    //fare l'encoding del match
                }
            }
        }
        return true;
    }
}
