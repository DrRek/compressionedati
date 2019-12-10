package compressione;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.io.*;

class Decompressor {
    private Dictionary dict;
    private RandomAccessFile referenceFile;
    private FileInputStream compressdFile;
    private FileOutputStream decompressedFile;
    private int c, mmlen;
    private MMTable[] mismatchTables;
    private char command;
    private byte[] matchBuffer;
    private String referencePath, decompressedPath;

    Decompressor(int c, int mmlen, String referencePath, String compressedPath, String decompressedPath) throws IOException {
        this.referenceFile = new RandomAccessFile(new File(referencePath), "r");
        this.compressdFile=new FileInputStream(new File(compressedPath));
        this.decompressedFile=new FileOutputStream(new File(decompressedPath));
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
                    byte[] setString = nextString(setLen);
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
                    int offset = nextNumber();
                    int len;
                    Mismatch mm;
                    if(command == ',') {
                        len = (int) nextNumber();
                        MMTable currentTable = mismatchTables[len - 1];
                        if(command != ',') {
                            //mi trovo nella situazione 1
                            int realOffset = (int)offset+c;;
                            byte[] ref = Arrays.copyOfRange(matchBuffer, realOffset, realOffset+len);
                            mm = currentTable.getMismatchFromRefAndUpdate(ref);
                            mm.setOffset(offset);
                        } else {
                            //mi trovo nella situazione 2
                            int rowIndex = (int) nextNumber();
                            int realOffset = (int) offset+c;
                            byte[] ref = Arrays.copyOfRange(matchBuffer, realOffset, realOffset+len);
                            mm = currentTable.getMismatchFromRefAndUpdate(ref, rowIndex);
                            mm.setOffset(offset);
                        }
                    } else {
                        //mi trovo nella situazione 3
                        List<Short> delta = nextByteList();
                        int realOffset = offset+c;
                        len = delta.size();
                        byte[] ref = Arrays.copyOfRange(matchBuffer, (int)realOffset, (int)realOffset+len);
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
        for(Short s : mm.getDelta()){
            matchBuffer[realOffeset] = (byte)(matchBuffer[realOffeset] - s);
            realOffeset++;
        }
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
        matchBuffer = buffer;
        tempFileSource.close();
    }

    private List<Short> nextByteList() throws IOException {
        List<Short> res = new ArrayList<>();
        StringBuilder temp = new StringBuilder(command + "");
        while(
                (command = (char) compressdFile.read()) != (char)-1 && (command >= '0' && command <= '9' || command == '+' || command == '-')
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

    private byte[] nextString(int len) throws IOException {
        byte[] buf = new byte[len];
        compressdFile.read(buf);
        return buf;
    }

    private int nextNumber() throws IOException {
        StringBuilder res = new StringBuilder();
        while(
                (command = (char) compressdFile.read()) != (char)-1 && command >= '0' && command <= '9'
        ){
            res.append(command);
        }
        return Integer.parseInt(res.toString());
    }

    private void writeMatchBuffer() throws IOException {
        write(matchBuffer);
    }

    private void write(byte[] m) throws IOException {
        decompressedFile.write(m);
        decompressedFile.flush();
        dict.addString(m);
    }

    private void write(List<Byte> m) throws IOException {
        Byte[] array = new Byte[m.size()];
        array = m.toArray(array);
        decompressedFile.write(ArrayUtils.toPrimitive(array));
        decompressedFile.flush();
        dict.addString(m);
    }

    Dictionary getDictionary(){
        return dict;
    }
}
