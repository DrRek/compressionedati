package SevenZip;

import java.io.*;

public class Tester{
    public static final int kEncode = 0;
	public static final int kDecode = 1;
	public static final int kBenchmak = 2;
	
	public static int Command = -1;
	public static int NumBenchmarkPasses = 10;
	
	public static int DictionarySize = 1 << 23;
	public static boolean DictionarySizeIsDefined = false;
	
	public static int Lc = 3;
	public static int Lp = 0;
	public static int Pb = 2;
	
	public static int Fb = 128;
	public static boolean FbIsDefined = false;
	
	public static boolean Eos = false;
	
	public static int Algorithm = 2;
	public static int MatchFinder = 1;

	public static void main(String[] args) throws Exception {
		String name = "test.compressed";

		if(args.length > 0){
			name = args[0]+".compressed";
		}
		
        //END OF STRING: DA PROVARE ENTRAMBI
		boolean eos = false;

        //Start test compression
		File inFile = new File("test_files/"+name);
		File outFile = new File("test_files/"+name+".7z");
                                                                                                 
	    BufferedInputStream inStream  = new BufferedInputStream(new FileInputStream(inFile));      
        BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFile)); 
 
        SevenZip.Compression.LZMA.Encoder encoder = new SevenZip.Compression.LZMA.Encoder();
 
        encoder.SetAlgorithm(Algorithm);
		encoder.SetDictionarySize(DictionarySize);
		encoder.SetNumFastBytes(Fb);
		encoder.SetMatchFinder(MatchFinder);
		encoder.SetLcLpPb(Lc, Lp, Pb);
		encoder.SetEndMarkerMode(eos);
		encoder.WriteCoderProperties(outStream);
		long fileSize;
		if (eos)
			fileSize = -1;
		else
			fileSize = inFile.length();
		for (int i = 0; i < 8; i++)
			outStream.write((int)(fileSize >>> (8 * i)) & 0xFF);
		encoder.Code(inStream, outStream, -1, -1, null);
		
        outStream.flush();
        outStream.close();
        inStream.close();


        //Start test decompression
        inFile = new File("test_files/"+name+".7z");
        outFile = new File("test_files/"+name+".7z.decompressed");

        inStream  = new BufferedInputStream(new FileInputStream(inFile));      
        outStream = new BufferedOutputStream(new FileOutputStream(outFile)); 

        int propertiesSize = 5;
		byte[] properties = new byte[propertiesSize];
		if (inStream.read(properties, 0, propertiesSize) != propertiesSize)
			throw new Exception("input .lzma file is too short");
		SevenZip.Compression.LZMA.Decoder decoder = new SevenZip.Compression.LZMA.Decoder();
		decoder.SetDecoderProperties(properties);
		long outSize = 0;
		for (int i = 0; i < 8; i++)
		{
			int v = inStream.read();
			if (v < 0)
				throw new Exception("Can't read stream size");
			outSize |= ((long)v) << (8 * i);
		}
		decoder.Code(inStream, outStream, outSize);
		outStream.flush();
		outStream.close();
		inStream.close();
		

        return;
	}
}
