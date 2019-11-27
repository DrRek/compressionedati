package compressione;

import java.util.*;
import java.io.*;

public class Compressor {
    private Dictionary dict;
    private RandomAccessFile tar;
    private File out;
    private int c, mmlen;
    private String bufferToSetEncode, refName, tarName;
    private MMTable[] mismatchTables;
    private Object writeLock;
    private FileWriter out_writer;

    public Compressor(int c, int mmlen, String ref, String tar, String out) throws FileNotFoundException{
        this.dict = new Dictionary(c, ref);
        this.tar = new RandomAccessFile(new File(tar), "r");
        this.out = new File(out);
        this.out_writer=new FileWriter(out);
        this.c = c;
        this.bufferToSetEncode = "";
        this.mmlen = mmlen;
        this.refName = ref;
        this.tarName = tar;
        this.writeLock=new Object();

        this.mismatchTables=new MMTable[this.mmlen];
        for(int i=0; i<this.mmlen; i++){
            this.mismatchTables[i]=new MMTable();
        }
    }

    public boolean run() throws FileNotFoundException{
        long currentPos = 0;
        String currentBlock;

        while((currentBlock = readFromPos(currentPos)) != null){

            //If this is true the last block is less than the minimum block, so we'll just encode it
            if(currentBlock.length() < this.c){
                encodeSet(currentBlock);
            }

            List<BlockPointer> possibleMatches = this.dict.getPointersForBlock(currentBlock);
            Match bestMatch = getBestMatch(currentPos, possibleMatches);
            if(bestMatch == null){
                this.bufferToSetEncode += currentBlock.charAt(0);
                ++currentPos;
            } else {
                encodeSetFromBuffer();
                encodeMatch(bestMatch, currentPos);

                currentPos += bestMatch.getMatchLength();
            }
        }

        return true;
    }

    //Dovrà automaticamente fare l'encoding anche dei missmatch
    private Match getBestMatch(long pos, List<BlockPointer> list) throws FileNotFoundException{
        if(list == null || list.size() < 1)
            return null;

        Match optimalMatch = null, tempMatch;
        for(BlockPointer ptr : list){
            tempMatch = getMatch(pos, ptr);

            if(optimalMatch != null && tempMatch != null && optimalMatch.getCost() > tempMatch.getCost())
                optimalMatch = tempMatch;
        }
        return optimalMatch;
    }

    
    private Match getMatch(long pos, BlockPointer ptr) throws FileNotFoundException{
        RandomAccessFile source, destination = new RandomAccessFile(new File(this.tarName), "r");
        if(ptr.isReference()){
            source = new RandomAccessFile(new File(this.refName), "r");
        } else {
            source = new RandomAccessFile(new File(this.tarName), "r");
        }

        boolean cont = true;
        int iterator = 0;
        Match resultMatch = new Match();
        List<Byte> currentSourceMissmatch = new ArrayList<Byte>();
        List<Byte> currentDestinationMissmatch = new ArrayList<Byte>();
        try {

            source.seek(pos+this.c);
            destination.seek(pos+this.c);

            do{
                byte sb = source.readByte(); //catch EOF and IOEx
                byte tb = destination.readByte();

                if(sb == tb){
                    //se i caratteri sono uguali
                    if(currentSourceMissmatch.size() > 0){
                        //TODO: E' terminato il missmatch di prima e devo aggiungerlo al match

                        //bisogna decidere se passare il puntatore assoluto nel file (pos+iterator*8) o lasciarlo relativo rispetto alla posizione di inizio match
                        resultMatch.addMissmatch(iterator*8, currentSourceMissmatch, currentDestinationMissmatch);
                        System.out.println("missmatch : "+currentSourceMissmatch+" "+currentDestinationMissmatch);

                        //Riinizializzo i missmatch
                        currentSourceMissmatch = new ArrayList<Byte>();
                        currentDestinationMissmatch = new ArrayList<Byte>();
                    }
                    resultMatch.increaseLen();
                } else{
                    //se i caratteri sono diversi
                    currentSourceMissmatch.add(sb);
                    currentDestinationMissmatch.add(tb);

                    if(currentSourceMissmatch.size() > this.mmlen){
                        cont = false;
                    }
                }
                ++iterator;
            } while(cont);
        } catch (EOFException eofe){
            return resultMatch;
        } catch (IOException ioe){
            System.out.println("getMatch of Compressor has thrown IOException");
            ioe.printStackTrace();
            return resultMatch;
        }

        return resultMatch;
    }

    private void encodeMatch(Match m, long position){
        String enc="c,"+m.getMatchLength()+","+position+"\n";
        Mismatch foundMismatch;
        int index;
        for(Mismatch mm: m.getMismatches()){
            //se il mismatch è contenuto nella cache
            if((index=mismatchTables[mm.getRef().length-1].find(mm))>=0){
                //se esiste un'unica entry nella cache per mm.ref
                if(mismatchTables[mm.getRef().length-1].hasUnique(mm.getRef())){
                    //codifico solamente la entry ref
                    enc+="m,"+mm.getRef+"\n";
                } //altrimenti devo aggiungere alla codifica anche l'indice del mismatch
                else enc+="m,"+mm.getRef()+","+index+"\n";                
            } 
            //se ce n'è uno che ha lo stesso delta codifico solo l'indice
            else if((index=mismatchTables[mm.getRef().length-1].findSameDelta(mm))>=0){
                enc+="m,"+index+"\n";
                this.storeMismatch(mm);
            } 
            //altrimenti devo codificare tutto 
            else{
                enc+="m,"+Mismatch.patternToString(mm.getRef())+","+Mismatch.patternToString(mm.getTar())+"\n";
                this.storeMismatch(mm);
            }
        }
        try{
            synchronized(writeLock){
                out_writer.append(enc);
            }
        } catch(IOException e){
            System.out.println("IOException in Compressor.encodeMatch()");
        }
    }

    private void encodeSet(String s){
        try{
            synchronized(writeLock){
                out_writer.append("s,"+s.length()+","+s+"\n");
            }
        } catch(IOException e){
            System.out.println("IOException in Compressor.encodeSet()");
        }
    }

    private void encodeSetFromBuffer() {
        if (!this.bufferToSetEncode.equals("")) {
            encodeSet(this.bufferToSetEncode);
            this.bufferToSetEncode = "";
        }
    }

    private void storeMismatch(Mismatch mm){
        int len=mm.getRef().length;

        //len-1 perchè non ci saranno mismatch di lunghezza 0 
        if(mismatchTables[len-1].find(mm)<0){
            mismatchTables[len-1].push(mm);
        }
    }

    private String readFromPos(long pos){
        byte[] b = new byte[this.c];

        try{
            this.tar.seek(pos);
        } catch (IOException ioe){
            System.out.println("Error in readFromPos while seeking for position, maybe this means that i've reached the end of the file.");
            ioe.printStackTrace();
            return null;
        }

        try{
            int readBytes = this.tar.read(b);
            if(readBytes < 0){
                //EOF reached
                return null;
            }

            return new String(Arrays.copyOfRange(b, 0, readBytes));
        } catch (IOException ioe){
            System.out.println("Error in readFromPos while reading for position, THIS NEEDS TO BE DEBUGED.");
            ioe.printStackTrace();
            return null;
        }
    }
}
