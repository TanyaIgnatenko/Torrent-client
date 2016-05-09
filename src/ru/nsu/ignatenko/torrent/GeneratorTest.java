package ru.nsu.ignatenko.torrent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.SortedSet;
import java.io.File;
import java.security.MessageDigest;
import java.io.InputStream;

import com.googlecode.jbencode.composite.DictionaryType;
import com.googlecode.jbencode.composite.EntryType;
import com.googlecode.jbencode.primitive.IntegerType;
import com.googlecode.jbencode.primitive.StringType;


public class
GeneratorTest
{
    public static void main(String[] args)
    {
        GeneratorTest generator = new GeneratorTest();
        generator.createMetaFile("file.txt");
    }


    public void createMetaFile(String filename)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        DictionaryType root = new DictionaryType() {
            @Override
            protected void populate(SortedSet<EntryType<?>> entries) {
                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("announce "),
                        new LiteralStringType("URL of tracker ")));
                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("announce-list "),
                        new LiteralStringType("URL of backup tracker")));
                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("created by "),
                        new LiteralStringType("MyTorrent 1.0 ")));
                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("info "),
                        new DictionaryType()
                        {
                            @Override
                            protected void populate(SortedSet<EntryType<?>> entries)
                            {
                                ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                                String path = classLoader.getResource(filename).getPath();

                                int sizeOfPiece = 256;
                                int sizeOfFile = (int) new File(path).length();
                                int countOfPieces = sizeOfFile/sizeOfPiece + 1;

                                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("length "),
                                        new IntegerType(sizeOfFile)));
                                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("name "),
                                        new LiteralStringType(filename + " ")));
                                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("piece length "),
                                        new IntegerType(sizeOfPiece)));

                                MessageDigest sha1 = null;
                                InputStream file = null;
                                try
                                {
                                    file = classLoader.getResourceAsStream(filename);
                                    sha1 = MessageDigest.getInstance("SHA1");

                                    byte[] data = new byte[sizeOfPiece];
                                    byte[] hashValues = new byte[20 * countOfPieces];
                                    
                                    int count;
                                    for (int i = 0; i < countOfPieces; ++i)
                                    {
                                        if ((count = file.read(data, i * sizeOfPiece, sizeOfPiece)) < sizeOfPiece)
                                        {
                                            for (int j = 0; j < sizeOfPiece - count; ++j)
                                            {
                                                data[count + j] = 0;
                                            }
                                        }
                                        byte[] sha1Hash = sha1.digest(data);
                                        System.arraycopy(sha1Hash, 0, hashValues, i * 20, sha1Hash.length);
                                    }
                                    entries.add(new EntryType<LiteralStringType>(new LiteralStringType("pieces "),
                                            new LiteralStringType(new String(hashValues))));
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }

                        }
                ));
            }
        };

        try
        {
            root.write(os);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println(os.toString());
    }

    private static class LiteralStringType extends StringType implements Comparable<LiteralStringType> {
        private final String value;

        public LiteralStringType(String value) {
            this.value = value;
        }

        @Override
        protected long getLength() {
            return value.length();
        }

        @Override
        protected void writeValue(OutputStream os) throws IOException {
            os.write(value.getBytes("US-ASCII"));
        }

        public int compareTo(LiteralStringType o) {
            return o.value.compareTo(value);
        }
    }
}

