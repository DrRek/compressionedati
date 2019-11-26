package compressione;

import java.util.*;
import java.io.*;

public class Compressor {
    private Dictionary dict;
    private RandomAccessFile tar;
    private File out;
    private int c;
    private String bufferToSetEncode;

    public Compressor(int c, String ref, String tar, String out) throws FileNotFoundException{
        this.dict = new Dictionary(c, ref);
        this.tar = new RandomAccessFile(new File(tar), "r");
        this.out = new File(out);
        this.c = c;
        this.bufferToSetEncode = "";
    }

    public boolean run(){
        long currentPos = 0;
        String currentBlock;

        while((currentBlock = readFromPos(currentPos)) != null){

            //If this is true the last block is less than the minimum block, so we'll just encode it
            if(currentBlock.length() < this.c){
                encodeSet(currentBlock);
            }

            List<BlockPointer> possibleMatches = this.dict.getPointersForBlock(currentBlock);
            Match bestMatch = getBestMatch(possibleMatches);
            if(bestMatch == null){
                this.bufferToSetEncode += currentBlock.charAt(0);
                ++currentPos;
            } else {
                encodeSetFromBuffer();
                encodeMatch(bestMatch);

                currentPos += bestMatch.getMatchLength();
            }
        }

        return true;
    }

    //Dovrà automaticamente fare l'encoding anche dei missmatch
    public Match getBestMatch(List<BlockPointer> list){
        if(list == null)
            return null;

        System.out.println("getBestMatch() still to implement");
        return null;
    }

    public void encodeMatch(Match m){

        System.out.println("encodeMatch() still to implement");
    }

    public void encodeSet(String s){

        System.out.println("encodeSet() still to implement");
    }

    public void encodeSetFromBuffer(){
        encodeSet(this.bufferToSetEncode);
        this.bufferToSetEncode = "";
    }

    public String readFromPos(long pos){
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
