package compressione;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.io.*;
import java.util.concurrent.ThreadPoolExecutor;

@SuppressWarnings("StringConcatenationInLoop")
class Compressor {
    private Dictionary dict;
    //there are cases in which the getMatch function requires to have two instance of two RandomAccessFile of the same file, instead of opening a new file for each getMatch invocation i've
    //preferred to add another copy of the obj to the method instance. That's a bit dirty, but eventually much more efficient.
    private RandomAccessFile referenceFile, targetFile, targetFileForGetMatch;
    private FileOutputStream compressedFileWriter;
    private int c, mmlen;
    private List<Byte> bufferToSetEncode;
    private MMTable[] mismatchTables;

    Compressor(int c, int mmlen, String referencePath, String targetPath, String compressedFile) throws IOException{
        this.dict = new Dictionary(c, referencePath, targetPath);

        this.referenceFile = new RandomAccessFile(new File(referencePath), "r");
        this.targetFile = new RandomAccessFile(new File(targetPath), "r");
        this.targetFileForGetMatch = new RandomAccessFile(new File(targetPath), "r");
        this.compressedFileWriter=new FileOutputStream(new File(compressedFile));

        this.c = c;
        this.mmlen = mmlen;
        this.bufferToSetEncode = new ArrayList<>();

        this.mismatchTables=new MMTable[this.mmlen];
        for(int i=0; i<this.mmlen; i++){
            this.mismatchTables[i]=new MMTable();
        }
    }

    boolean run() throws IOException {
        int currentPos = 0;
        Byte[] currentBlock;

        while(true){
            currentBlock = readFromPos(currentPos);
            //In this case i've reached the end of the file, since the read string is less then c i'll just set encode the ramainings
            if(currentBlock == null || currentBlock.length < this.c){
                if(currentBlock != null) {
                    List<Byte> list = Arrays.asList(currentBlock);
                    this.bufferToSetEncode.addAll(list);
                }
                encodeSetFromBuffer();
                break;
            }

            List<BlockPointer> possibleMatches = this.dict.getPointersForBlock(currentBlock);
            Match bestMatch = getBestMatch(currentPos, possibleMatches);
            if(bestMatch == null){
                //In this case i haven't found a match, so i'll just set this to be set encoded
                this.bufferToSetEncode.add(currentBlock[0]);
                ++currentPos;
            } else {
                encodeSetFromBuffer();
                encodeMatch(bestMatch);

                currentPos += bestMatch.getMatchLength()+c;
            }
        }

        this.closeHandles();
        return true;
    }

    private void closeHandles() throws IOException {
        referenceFile.close();
        targetFile.close();
        compressedFileWriter.close();
        targetFileForGetMatch.close();
    }

    private Match getBestMatch(int pos, List<BlockPointer> list) throws FileNotFoundException{
        if(list == null || list.size() < 1)
            return null;

        Match optimalMatch = null, tempMatch;
        for(BlockPointer ptr : list){
            tempMatch = getMatch(pos, ptr);

            if(optimalMatch == null || (tempMatch != null && optimalMatch.getCost() > tempMatch.getCost()))
                optimalMatch = tempMatch;
        }
        return optimalMatch;
    }

    
    private Match getMatch(int pos, BlockPointer ptr) throws FileNotFoundException{
        RandomAccessFile source, destination = targetFile;
        if(ptr.isReference()){
            source = referenceFile;
        } else {
            source = targetFileForGetMatch;
        }

        boolean cont = true;
        Match resultMatch = new Match(ptr.getDictMapIndex(), ptr.getDictListIndex());
        List<Byte> currentSourceMismatch = new ArrayList<Byte>();
        List<Byte> currentDestinationMismatch = new ArrayList<Byte>();
        try {

            source.seek(ptr.getOffset()+this.c);
            destination.seek(pos+this.c);

            //this will be used to remember missmatch location
            int missmatachStartOffset = 0, currentPosition = 0;

            do{
                byte sb = source.readByte(); //catch EOF and IOEx
                byte tb = destination.readByte();

                if(sb == tb){
                    //se i caratteri sono uguali
                    if(currentSourceMismatch.size() > 0){
                        resultMatch.addMissmatch(currentSourceMismatch, currentDestinationMismatch, missmatachStartOffset);

                        //Riinizializzo i missmatch dopo aver salvato l'ultimo
                        currentSourceMismatch = new ArrayList<>();
                        currentDestinationMismatch = new ArrayList<>();
                    }
                    resultMatch.increaseLen();
                } else {
                    //se sono all'inizio del mismatch
                    if(currentSourceMismatch.isEmpty())
                        missmatachStartOffset = currentPosition;

                    currentSourceMismatch.add(sb);
                    currentDestinationMismatch.add(tb);

                    if (currentSourceMismatch.size() > this.mmlen) {
                        cont = false;
                    }
                }

                ++currentPosition;
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

    /*
    Matches will be encoded as follow <c><dict_map_index><,><dic_list_index><,><match_length>
    example c1,10   from dict[1] pointer copy c+10 bytes
    unless the dictionary entry in the list is 0, in this case the encoded would be <c><dict_index><,><match_length>
    */
    private void encodeMatch(Match m) throws IOException {
        int dictListIndex = m.getDictListIndex();
        String enc = "c"+m.getDictMapIndex();
        if(dictListIndex != 0)
            enc += ","+m.getDictListIndex();
        enc += ","+m.getMatchLength();

        compressedFileWriter.write(enc.getBytes());
        for(Mismatch mm: m.getMismatches()){
            encodeMismatch(mm);
        }
        dict.addMatch(m);
    }

    /*
    There are 3 possible types of missmatch
    A missmatch will always be encoded with it's position and i't number of bytes
    if refs and tgts are equals for a row and no other row has same ref then sample message will be <m><offset><,><bytes_n>
    if entry with same delta is found then <m><offset><,><bytes_n><,><row_index>
    else <m><offset><+|-><d1><+|-><d2><+|-><d3>...
    */
    private void encodeMismatch(Mismatch mm) throws IOException {
        MMTable relevantTable = mismatchTables[mm.getRef().size() -1];
        int cacheLookupRes = relevantTable.searchAndUpdate(mm);
        String encodedMessage = "m"+mm.getOffset();
        if(cacheLookupRes == MMTable.PERFECT_HIT){
            encodedMessage += ","+mm.getRef().size();
            compressedFileWriter.write(encodedMessage.getBytes());
        }else if(cacheLookupRes == MMTable.NO_HIT){
            for(short i : mm.getDelta())
                if(i >= 0)
                    encodedMessage += "+"+i;
                else
                    encodedMessage += i;
            compressedFileWriter.write(encodedMessage.getBytes());
        } else {
            encodedMessage += ","+mm.getRef().size()+","+cacheLookupRes;
            compressedFileWriter.write(encodedMessage.getBytes());
        }
    }

    /*
    A set message will be composed as this: <s><message_len><,><message>
    */
    private void encodeSet(List<Byte> s) throws IOException {
        compressedFileWriter.write(("s"+s.size()+",").getBytes());
        Byte[] array = new Byte[s.size()];
        array = s.toArray(array);
        compressedFileWriter.write(ArrayUtils.toPrimitive(array));
    }

    private void encodeSetFromBuffer() throws IOException {
        if (this.bufferToSetEncode.size() != 0) {
            encodeSet(this.bufferToSetEncode);
            dict.addString(this.bufferToSetEncode);
            this.bufferToSetEncode = new ArrayList<>();
        }
    }

    private Byte[] readFromPos(int pos){
        byte[] a = new byte[this.c];
        Byte[] b = new Byte[this.c];

        try{
            this.targetFile.seek(pos);
        } catch (IOException ioe){
            System.out.println("Error in readFromPos while seeking for position, maybe this means that i've reached the end of the file.");
            ioe.printStackTrace();
            return null;
        }

        try{
            int readBytes = this.targetFile.read(a);
            for (int i=0;i<a.length;i++) {
                b[i] = a[i];
            }
            if(readBytes < 0){
                //EOF reached
                return null;
            }

            return b;
        } catch (IOException ioe){
            System.out.println("Error in readFromPos while reading for position, THIS NEEDS TO BE DEBUGED.");
            ioe.printStackTrace();
            return null;
        }
    }

    Dictionary getDictionary(){
        return dict;
    }
}
