package compressione;

import java.util.*;
import java.io.*;

public class Decompressor {
    private Dictionary dict;
    private RandomAccessFile referenceFile;
    private FileReader compressdFile;
    private FileWriter decompressedFile;
    private int c, mmlen;
    private MMTable[] mismatchTables;
    private char command;
    private String matchBuffer;

    public Decompressor(int c, int mmlen, String referencePath, String compressedPath, String decompressedPath) throws IOException {
        this.referenceFile = new RandomAccessFile(new File(referencePath), "r");
        this.compressdFile=new FileReader(new File(compressedPath));
        this.decompressedFile=new FileWriter(new File(decompressedPath));
        this.dict = new Dictionary(c, referencePath, decompressedPath);

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
                    int setLen = nextNumber();
                    String setString = nextString(setLen);
                    write(setString);
                    command = (char) compressdFile.read();
                    break;
                case 'c':
                    int dictMapIndex = nextNumber();
                    int dictListIndex = nextNumber();
                    int matchLen;
                    if(command == ',')
                        matchLen = nextNumber();
                    else {
                        matchLen = dictListIndex;
                        dictListIndex = 0;
                    }
                    BlockPointer ptr = dict.getPtrFromParamaters(dictMapIndex, dictListIndex);
                    matchBufferFromPtr(ptr, matchLen);
                    break;
                case 'm':
                    int offset = nextNumber();
                    Mismatch mm;
                    if(command == ',') {
                        int len = nextNumber();
                        MMTable currentTable = mismatchTables[len - 1];
                        if(command != ',') {
                            //mi trovo nella situazione 1
                            int realOffset = offset+c;
                            String ref = matchBuffer.substring(realOffset, realOffset+len);
                            mm = currentTable.getMismatchFromRef(ref);
                        } else {
                            //mi trovo nella situazione 2
                            int rowIndex = nextNumber();
                            int realOffset = offset+c;
                            String ref = matchBuffer.substring(realOffset, realOffset+len);
                            mm = currentTable.getMismatchFromRef(ref, rowIndex);

                        }
                    } else {
                        //mi trovo nella situazione 3
                        List<Short> delta = nextByteList();
                        int realOffset = offset+c;
                        String ref = matchBuffer.substring(realOffset, realOffset+delta.size());
                        MMTable currentTable = mismatchTables[delta.size() - 1];
                        mm = new Mismatch(ref, delta, offset);
                    }

                    updateMatchBufferFromMismatch(mm);
                    if(command != 'm')
                        writeMatchBuffer();
                    break;
                default:
                    System.out.println("Error while decompressing");
            }
            System.out.print(command);
        }
        return true;
    }

    private void write(String m) throws IOException {
        decompressedFile.write(m);
    }

    private List<Short> nextByteList() throws IOException {
        List<Short> res = new ArrayList<>();
        StringBuilder temp = new StringBuilder(command);
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

    private int nextNumber() throws IOException {
        StringBuilder res = new StringBuilder();
        while(
                (command = (char) compressdFile.read()) != (char)-1 && command != ','
        ){
            res.append(command);
        }
        return Integer.parseInt(res.toString());
    }}

}
