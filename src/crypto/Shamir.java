package crypto;

import com.tiemens.secretshare.main.cli.MainCombine;
import com.tiemens.secretshare.main.cli.MainSplit;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.min;
import static java.util.Arrays.copyOfRange;

/**
 * Created by hooda on 2/3/2015.
 * Contains methods to encrypt files or strings.
 */
public class Shamir {

    //The encoding that will be used when splitting and combining files.
    static String encoding = "ISO-8859-1";
    //The number of bytes per piece (except maybe the last one)!
    static int pieceSize = 128;

    public static ArrayList<String> shamirSplit(String inputString, int numPieces, int minPieces) {
        return shamirSplit(inputString, numPieces, minPieces, 0);
    }

    public static ArrayList<String> shamirSplit(String inputString, int numPieces, int minPieces, int mode) {

        String type = "-sS";
        if (mode == 1) {
            type = "-sN";
        }

        ArrayList<String> parts = new ArrayList<>(numPieces);
        String[] splitArgs = {"-n", Integer.toString(numPieces), "-k", Integer.toString(minPieces), type, inputString, "-primeNone"};
        MainSplit.SplitInput splitInput = MainSplit.SplitInput.parse(splitArgs);
        MainSplit.SplitOutput splitOutput = splitInput.output();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        splitOutput.print(ps);
        String content = baos.toString(); // e.g. ISO-8859-1
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line;
        int i = 0;
        try {
            while ((line = reader.readLine()) != null && i < numPieces) {
                if (line.startsWith("Share (x")) {
                    i++;
                    parts.add(line.trim());
                }
            }
        } catch (Exception e) {
            //TODO Catch
        }
        return parts;
    }

    //Decrypt array of strings to return a string.
    public static String shamirCombine(ArrayList<String> parts, ArrayList<String> flags, int k) {
        ArrayList<String> args = new ArrayList<>();
        args.add("-primeNone");
        args.add("-k");
        args.add(Integer.toString(k));
//        for (int i = 0; i < flags.size(); i++) {
//            args.add(flags.get(i));
//            args.add(parts.get(i));
//        }

        for (int i = 0; i < k; i++) {
            String part = parts.get(i);
            String partSecret = part.split("=")[1].trim();
            String partNum = part.split("=")[0].split(":")[1].split("\\)")[0].trim();
            args.add("-s".concat(partNum));
            args.add(partSecret);
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        String[] combineArgs = args.toArray(new String[args.size()]);
        MainCombine.CombineInput combineInput = MainCombine.CombineInput.parse(combineArgs, null, ps);
        MainCombine.CombineOutput combineOutput = combineInput.output();
        combineOutput.print(ps);
        String content = baos.toString(); // e.g. ISO-8859-1
        Pattern pattern = Pattern.compile("secret.string = '");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return (content.substring(matcher.end(), content.length() - 3));
        } else {
            return "";
        }
    }

    //Returns the Integer that the decryption represents, but in string format.
    public static String shamirCombineInt(ArrayList<String> parts, ArrayList<Integer> partNums, ArrayList<String> flags, int k) {
        ArrayList<String> args = new ArrayList<>();
        args.add("-primeNone");
        args.add("-k");
        args.add(Integer.toString(k));

        for (int i = 0; i < k; i++) {
            String partSecret = parts.get(i);
            String partNum = partNums.get(i).toString();
            args.add("-s".concat(partNum));
            args.add(partSecret);
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        String[] combineArgs = args.toArray(new String[args.size()]);
        MainCombine.CombineInput combineInput = MainCombine.CombineInput.parse(combineArgs, null, ps);
        MainCombine.CombineOutput combineOutput = combineInput.output();
        combineOutput.print(ps);
        String content = baos.toString(); // e.g. ISO-8859-1
        Pattern pattern = Pattern.compile("secret.number = '");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            int i = matcher.end();
            char c = content.charAt(matcher.end());
            while (c != '\'') {
                i++;
                c = content.charAt(i);
            }
            return (content.substring(matcher.end(), i));
        } else {
            return "";
        }
    }

    /**
     * Splits the given file into numPieces, of which at least minPieces are needed to recover the original.
     *
     * @param filePath  Path to the file to be encrypted.
     * @param numPieces Number of files to split into.
     * @param minPieces Minimum splitted files needed to recover original.
     * @return
     * @throws IOException
     */
    public static ArrayList<String> fileSplit(String filePath, int numPieces, int minPieces) throws IOException {

        long startTime = System.currentTimeMillis();
        File file = new File(filePath);

        //Create files to which encrypted pieces will b written.
        ArrayList<String> splitNames = new ArrayList<>();
        ArrayList<FileOutputStream> splitFiles = new ArrayList<>(numPieces);
        for (int i = 1; i <= numPieces; i++) {
            String name = file.getName();
            String suffix = ".".concat(Integer.toString(i));
            File temp = File.createTempFile(name, suffix);
            temp.deleteOnExit();
            splitNames.add(temp.getAbsolutePath().toString());
            splitFiles.add(new FileOutputStream(temp.getAbsolutePath().toString()));
        }

        //Get the file as a byte array.
        byte[] fileAsBytes = Files.readAllBytes(Paths.get(filePath));
        System.out.println("File had ".concat(Integer.toString(fileAsBytes.length)));

        //Do the encryption.
        for (int i = 0; i < fileAsBytes.length; ) {

            //We want to partition the byte array into pieces of length 4/8/16 whatever, but if length is not multiple (eg there are 15 bytes)
            //then the last piece should be shorter. j takes care of that.
            int j = min(fileAsBytes.length - i, Shamir.pieceSize);
            byte[] piece = copyOfRange(fileAsBytes, i, i + j);
            i = i + j;

            Shamir.encryptAndWrite(piece, numPieces, minPieces, splitFiles);
        }


        for (FileOutputStream f : splitFiles) {
            f.close();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Encryption took " + (endTime - startTime) / 1000.0 + " seconds");


        //TESTING CODE. TODO remove
//        startTime = System.currentTimeMillis();
//
//
//        System.out.println("\n\ntesting the decryption\n\n");
//        ArrayList<String> files = new ArrayList<>();
//        files.add("E://dummy.txt.1");
//        files.add("E://dummy.txt.2");
//        files.add("E://dummy.txt.3");
//        Shamir.fileCombine(files, minPieces);
//
//
//        endTime = System.currentTimeMillis();
//
//        System.out.println("Decryption took " + (endTime - startTime) / 1000.0 + " seconds");

        return splitNames;
    }


    /**
     * Okay, this is a bit hacky. We want to take a piece of a file, encrypt/split it, and write the splits
     * to the given FileOutPutStreams array. We want to treat the piece as an integer (treating it as string => large space overhead).
     * This is tricky because of two reasons:
     * 1. If all the bytes are zero, out piece will be zero, and we get an exception! It cannot be encrypted.
     * 2. If the piece has any zero bytes at start, they get lost in the encrypt/decrypt process.
     * 3. We cannot predict the length of the encrypted result. A 128 byte piece, when encrypted, can be 128, or 129 or whatever bytes.
     *
     * To fix this, we use prefixing and size byte.
     * We prefix each piece with a one byte - 00000001. This means our piece will never have zero bytes at start. Takes care of 1 and 2.
     * And, when writing the encrypted data to files, we prefix each with one byte containing its size.
     *
     * Then, when reading, here's what we do - we have N files. From each, we read the first byte. That will give us sizes n1,n2..nN.
     * From each file, we then read the corresponding number of bytes n1 bytes from 1.. nN bytes from N, and feed them to shamir decryptor.
     * Finally, we convert the recovered number to byte array, and discard the first one - we inserted it ourselves.
     *
     * @param piece Byte array to be encrypted and written.
     * @param numPieces n
     * @param minPieces k
     * @param files n FileOutPutStreams
     * @throws IOException
     */
    public static void encryptAndWrite(byte[] piece, int numPieces, int minPieces, ArrayList<FileOutputStream> files) throws IOException {

//        printByteArray(piece);

        //Prefixing a new 1 at the start of piece == Add to Multiply by 2^(no. of bytes*8).
        //For example, we had a two byte number, multiply it by 2^16.
        BigInteger pieceAsInt = new BigInteger(1, piece);
        BigInteger toAdd = (new BigInteger("2")).pow(piece.length * 8);
        pieceAsInt = pieceAsInt.add(toAdd);
        assert (pieceAsInt.toByteArray().length == piece.length + 1);

        //Split the integer.
        ArrayList<String> pieceSplit = Shamir.shamirSplit(pieceAsInt.toString(), numPieces, minPieces, 1);

        //Write to file.
        for (int i = 0; i < pieceSplit.size(); i++) {
            String secret = pieceSplit.get(i).split("=")[1].trim();
            byte[] toWrite = (new BigInteger(secret)).toByteArray();
            files.get(i).write((byte) toWrite.length);
//                files.get(i).write(flag);
            files.get(i).write(toWrite);
        }

    }

    public static void writeBytesToFiles(ArrayList<String> shamirOutput, ArrayList<FileOutputStream> files) throws IOException {
        for (int i = 0; i < shamirOutput.size(); i++) {
            String partSecret = shamirOutput.get(i).split("=")[1].trim();
//            System.out.println(shamirOutput.get(i));

            byte[] toWrite = (new BigInteger(partSecret)).toByteArray();
            assert (toWrite.length <= 255);
//            System.out.println(toWrite.length);
            System.out.println(toWrite.length);
            files.get(i).write((byte) (toWrite.length));
            files.get(i).write(toWrite);
        }
    }


    public static void fileCombine(ArrayList<String> files, int k) throws IOException {

        //Create input streams, and part numbers (needed when decrypting)
        ArrayList<FileInputStream> fileStreams = new ArrayList<>(files.size());
        ArrayList<Integer> partNums = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); i++) {
            fileStreams.add(i, new FileInputStream(files.get(i)));
            partNums.add(i, Integer.parseInt(files.get(i).substring(files.get(i).lastIndexOf(".") + 1, files.get(i).length())));
        }

        ArrayList<ArrayList<BigInteger>> filesAsInts = new ArrayList<>();
        for (int i = 0; i < fileStreams.size(); i++) {

            long size = fileStreams.get(i).getChannel().size();
            ArrayList<BigInteger> temp = new ArrayList<BigInteger>((int) size/128);
            for (int j = 0; j < size; ) {

                //Need to bitmask because java stores integers as two's complement.
                //If we convert i>128 to a byte and back, we'll end up with negative value without this.
                int bytesToRead = (int) (fileStreams.get(i).read() & 0xFF);
                j ++;
                byte[] intBytes = new byte[bytesToRead];
                fileStreams.get(i).read(intBytes);
                BigInteger bigInteger = new BigInteger(1, intBytes);
//                System.out.println("Read : ".concat(bigInteger.toString()));
                j += bytesToRead;
                temp.add(bigInteger);
            }
            filesAsInts.add(i, temp);
        }

        ArrayList<BigInteger> decryptedInts = new ArrayList<>(filesAsInts.get(0).size());
        for (int i = 0; i < filesAsInts.get(0).size(); i++) {
//            System.out.println(i);
            ArrayList<String> intsAsStrings = new ArrayList<>();
            for (int j = 0; j < filesAsInts.size(); j++) {
                intsAsStrings.add(filesAsInts.get(j).get(i).toString());
            }
            String decrypted = Shamir.shamirCombineInt(intsAsStrings, partNums, null, k);
            decryptedInts.add(i,new BigInteger(decrypted));
        }

        FileOutputStream fileOutputStream = new FileOutputStream(files.get(0).substring(0, files.get(0).length() - 2));
        for (int i = 0; i < decryptedInts.size(); i++) {
//            System.out.println(decryptedInts.get(i));
            byte[] intBytes = decryptedInts.get(i).toByteArray();
            byte[] toWrite = copyOfRange(intBytes, 1, intBytes.length);
//            printByteArray(toWrite);

            fileOutputStream.write(toWrite);
        }
        fileOutputStream.close();
        System.out.println("File decrypted!");

        for(FileInputStream f : fileStreams){
            f.close();
        }
    }

    public static void printByteArray(byte[] bytes){
        for (byte b : bytes) {
            System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1));
            System.out.print(" ");
        }
        System.out.println("");
    }
}

