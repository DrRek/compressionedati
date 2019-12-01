package compressione;

import java.util.*;
import java.io.*;

class Decompressor {
    private Dictionary dict;
    private RandomAccessFile referenceFile;
    private FileReader compressdFile;
    private FileWriter decompressedFile;
    private int c, mmlen;
    private MMTable[] mismatchTables;
    private char command;
    private String matchBuffer, referencePath, decompressedPath;

    Decompressor(int c, int mmlen, String referencePath, String compressedPath, String decompressedPath) throws IOException {
        this.referenceFile = new RandomAccessFile(new File(referencePath), "r");
        this.compressdFile=new FileReader(new File(compressedPath));
        this.decompressedFile=new FileWriter(new File(decompressedPath));
        this.dict = new Dictionary(c, referencePath, decompressedPath);

        this.referencePath = referencePath;
        this.decompressedPath = decompressedPath;

        this.c = c;
        this.mmlen = mmlen;

        this.mismatchTables=new MMTable[this.mmlen];
        for(int i=0; i<this.mmlen; i++){
            this.mismatchTables[i]=new MMTable();
        }
    }

    boolean run() throws IOException {
        command = (char) compressdFile.read();
        while (command != (char)-1){
            switch (command){
                case 's':
                    int setLen = (int)nextNumber();
                    String setString = nextString(setLen);
                    write(setString);
                    command = (char) compressdFile.read();
                    break;
                case 'c':
                    int dictMapIndex = (int)nextNumber();
                    int dictListIndex = (int)nextNumber();
                    int matchLen;
                    if(command == ',')
                        matchLen = (int)nextNumber();
                    else {
                        matchLen = dictListIndex;
                        dictListIndex = 0;
                    }
                    BlockPointer ptr = dict.getPtrFromParamaters(dictMapIndex, dictListIndex);
                    matchBufferFromPtr(ptr, matchLen);

                    if(command != 'm')
                        writeMatchBuffer();
                    break;
                case 'm':
                    long offset = nextNumber();
                    int len;
                    Mismatch mm;
                    if(command == ',') {
                        len = (int) nextNumber();
                        MMTable currentTable = mismatchTables[len - 1];
                        if(command != ',') {
                            //mi trovo nella situazione 1
                            int realOffset = (int)offset+c;
                            String ref = matchBuffer.substring(realOffset, realOffset+len);
                            mm = currentTable.getMismatchFromRefAndUpdate(ref);
                        } else {
                            //mi trovo nella situazione 2
                            int rowIndex = (int) nextNumber();
                            int realOffset = (int) offset+c;
                            String ref = matchBuffer.substring(realOffset, realOffset+len);
                            mm = currentTable.getMismatchFromRefAndUpdate(ref, rowIndex);

                        }
                    } else {
                        //mi trovo nella situazione 3
                        List<Short> delta = nextByteList();
                        long realOffset = offset+c;
                        len = delta.size();
                        String ref = matchBuffer.substring((int) realOffset, (int)realOffset+len);
                        MMTable currentTable = mismatchTables[len - 1];
                        mm = new Mismatch(ref, delta, offset);
                        currentTable.addEntry(mm);
                    }

                    updateMatchBufferFromMismatch(mm);
                    if(command != 'm')
                        writeMatchBuffer();
                    break;
                default:
                    System.out.println("Error while decompressing");
            }
        }

        this.closeHandles();
        return true;
    }

    private void closeHandles() throws IOException {
        referenceFile.close();
        compressdFile.close();
        decompressedFile.close();
    }

    private void updateMatchBufferFromMismatch(Mismatch mm) {
        int realOffeset = (int)mm.getOffset() + c;
        StringBuilder tempBuffer = new StringBuilder(matchBuffer);

        for(Short s : mm.getDelta()){
            char c = tempBuffer.charAt(realOffeset);
            tempBuffer.setCharAt(realOffeset, (char)(c - s));
            realOffeset++;
        }
        matchBuffer = tempBuffer.toString();
    }

    private void matchBufferFromPtr(BlockPointer ptr, int matchLen) throws IOException {
        RandomAccessFile tempFileSource;
        if(ptr.isTarget())
            tempFileSource = new RandomAccessFile(new File(decompressedPath), "r");
        else
            tempFileSource = new RandomAccessFile(new File(referencePath), "r");

        tempFileSource.seek(ptr.getOffset());
        byte[] buffer = new byte[matchLen + c];
        tempFileSource.read(buffer);
        matchBuffer = new String(buffer);
        tempFileSource.close();
    }

    private List<Short> nextByteList() throws IOException {
        List<Short> res = new ArrayList<>();
        StringBuilder temp = new StringBuilder(command + "");
        while(
                (command = (char) compressdFile.read()) != (char)-1 && command != ','
        ){
            if(command == '+' || command == '-') {
                res.add(Short.parseShort(temp.toString()));
                temp = new StringBuilder(command + "");
            } else {
                temp.append(command);
            }
        }
        res.add(Short.parseShort(temp.toString()));
        return res;
    }

    private String nextString(int len) throws IOException {
        char[] buf = new char[len];
        compressdFile.read(buf);
        return new String(buf);
    }

    private long nextNumber() throws IOException {
        StringBuilder res = new StringBuilder();
        while(
                (command = (char) compressdFile.read()) != (char)-1 && command >= '0' && command <= '9'
        ){
            res.append(command);
        }
        return Long.parseLong(res.toString());
    }

    private void writeMatchBuffer() throws IOException {
        write(matchBuffer);
    }

    private void write(String m) throws IOException {
        decompressedFile.write(m);
        decompressedFile.flush();
        dict.addString(m);
    }

    Dictionary getDictionary(){
        return dict;
    }
}
