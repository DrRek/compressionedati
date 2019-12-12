package compressione;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewDecompressor {
    private final FileInputStream compressedFile;
    private final Dictionary dict;
    private final FileOutputStream decompressedFile;
    private final String decompressedPath;
    private final String referencePath;
    private final int c;
    private final MMTable[] mismatchTables;
    private final int mmlen;
    private byte command;

    NewDecompressor(int c, int mmlen, String referencePath, String compressedPath, String decompressedPath) throws IOException {
        this.compressedFile=new FileInputStream(new File(compressedPath));
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
        DecoderMatch currentMatch = null;
        command = (byte) compressedFile.read();
        do {
            if(command == 's'){
                int setLen = readIntegerAndUpdateCommand();
                byte[] setBuff = readByteArrayAndUpdateCommand(setLen);
                writeAndAddToDict(setBuff);
            } else if(command == 'c'){
                //This is just to give a reason to each reference
                int dictMapIndex = readIntegerAndUpdateCommand();
                int dictListIndex = readIntegerAndUpdateCommand();
                int matchLen;
                if(command == ',')
                    matchLen = readIntegerAndUpdateCommand();
                else {
                    matchLen = dictListIndex;
                    dictListIndex = 0;
                }

                BlockPointer ptr = dict.getPtrFromParamaters(dictMapIndex, dictListIndex);
                currentMatch = new DecoderMatch(ptr, matchLen);

                if(command != 'm')
                    writeAndAddToDict(currentMatch);

            } else if(command == 'm') {
                int offset = readIntegerAndUpdateCommand();
                int len;
                Mismatch mm;
                if(command == ',') {
                    len = readIntegerAndUpdateCommand();
                    MMTable currentTable = mismatchTables[len - 1];

                    BlockPointer ptr = currentMatch.getPtr();
                    FileInputStream input = getFilePointer(ptr);
                    input.skip(offset+c);
                    byte[] ref = new byte[len];
                    int read = input.read(ref);
                    input.close();
                    if(read != len){
                        System.out.println("Error while reading source "+len+ " "+read);
                    }

                    if(command != ',') {
                        //mi trovo nella situazione 1
                        mm = currentTable.getMismatchFromRefAndUpdate(ref);
                        mm.setOffset(offset);
                    } else {
                        int rowIndex = readIntegerAndUpdateCommand();
                        mm = currentTable.getMismatchFromRefAndUpdate(ref, rowIndex);
                        mm.setOffset(offset);
                    }
                } else {
                    //mi trovo nella situazione 3
                    List<Short> delta = readShortListAndUpdateCommand();
                    len = delta.size();

                    BlockPointer ptr = currentMatch.getPtr();
                    FileInputStream input = getFilePointer(ptr);
                    input.skip(offset + c);
                    byte[] ref = new byte[len];
                    int read = input.read(ref);
                    input.close();
                    if (read != len) {
                        System.out.println("Error while reading source " + len + " " + read);
                    }

                    MMTable currentTable = mismatchTables[len - 1];
                    mm = new Mismatch(ref, delta, offset);
                    currentTable.addEntry(mm);
                }
                currentMatch.addMismatch(mm);

                if(command != 'm')
                    writeAndAddToDict(currentMatch);
            } else {
                System.out.println("C'è qualche probleema di aggiornamento command: " + command);
            }
        } while (command != (byte)-1);

        this.closeHandles();
        return true;
    }

    private List<Short> readShortListAndUpdateCommand() throws IOException {
        List<Short> res = new ArrayList<>();
        StringBuilder temp = new StringBuilder();
        boolean firstTime = true;
        do {
            if(((char)command == '+' || (char)command == '-') && !firstTime) { // 45 e 43 foss + e -
                res.add(Short.parseShort(temp.toString()));
                temp = new StringBuilder((char)command + "");
            } else {
                firstTime = false;
                temp.append((char)command);
            }
        }while(
                (command = (byte)compressedFile.read()) != (byte)-1 && ((char)command >= '0' && (char)command <= '9' || (char)command == '+' || (char)command == '-')
        );
        res.add(Short.parseShort(temp.toString()));
        return res;
    }

    private FileInputStream getFilePointer(BlockPointer ptr) throws IOException {
        FileInputStream input;
        if(ptr.isTarget())
            input = new FileInputStream(new File(decompressedPath));
        else
            input = new FileInputStream(new File(referencePath));
        input.skip(ptr.getOffset());
        return input;
    }

    private void writeAndAddToDict(BlockPointer ptr, short[] toSub) throws IOException {
        FileInputStream input = getFilePointer(ptr);
        for(int i = 0; i < this.c; i++){
            writeAndAddToDict((byte)input.read());
        }
        for(short deltaItem : toSub){
            byte refByte = (byte)input.read();
            writeAndAddToDict((byte)(refByte - deltaItem));
        }
        input.close();
    }

    private void writeAndAddToDict(DecoderMatch match) throws IOException {
        short[] toSub = new short[match.getMatchLength()];
        for(Mismatch mm : match.getMismatches()){
            int index = mm.getOffset();
            for(short deltaItem : mm.getDelta()){
                toSub[index] = deltaItem;
                index++;
            }
        }

        writeAndAddToDict(match.getPtr(), toSub);
    }

    private void writeAndAddToDict(byte b) throws IOException {
        dict.addString(b);
        decompressedFile.write(b);
        decompressedFile.flush();
    }

    private void writeAndAddToDict(byte[] a) throws IOException {
        for(byte b : a) writeAndAddToDict(b);
    }

    private byte[] readByteArrayAndUpdateCommand(int len) throws IOException {
        byte[] buf = new byte[len];
        int read = compressedFile.read(buf);
        if(read != len)
            System.out.println("Error in readByteArrayAndUpdateCommand "+len+" "+read);
        command = (byte)compressedFile.read();
        return buf;
    }

    private int readIntegerAndUpdateCommand() throws IOException {
        StringBuilder res = new StringBuilder();
        while(
                (command = (byte) compressedFile.read()) != (byte)-1 && command >= '0' && command <= '9'
        ){
            res.append((char)command);
        }
        return Integer.parseInt(res.toString());
    }

    private void closeHandles() throws IOException {
        compressedFile.close();
        decompressedFile.close();
    }
}
