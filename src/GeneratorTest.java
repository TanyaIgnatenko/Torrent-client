package ru.nsu.ignatenko.torrent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.SortedSet;
import java.io.File;
import java.security.MessageDigest;
import java.io.FileInputStream;

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
                            int sizeOfFile = (int) new File(filename).length();
                            int sizeOfPiece = 256;
                            @Override
                            protected void populate(SortedSet<EntryType<?>> entries)
                            {
                                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("length"),
                                        new IntegerType(sizeOfFile)));
                                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("name "),
                                        new LiteralStringType(filename + " ")));
                                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("piece length "),
                                        new IntegerType(sizeOfPiece)));
                                entries.add(new EntryType<LiteralStringType>(new LiteralStringType("pieces "),
                                        new LiteralStringType("hashValue ")));

                                MessageDigest sha1 = null;
                                FileInputStream file = null;
                                try
                                {
                                    sha1 = MessageDigest.getInstance("SHA1");
                                    file = new FileInputStream(filename);
                                    int countOfPieces = sizeOfFile/sizeOfPiece;

                                    byte[] data = new byte[sizeOfPiece];
                                    byte[] hashBytes = new byte[20 * countOfPieces];
                                    int count = 0;
                                    for (int i = 0; i < countOfPieces; ++i)
                                    {
                                        for (int j = 0; j < sizeOfPiece; ++j)
                                        {
                                            if ((count = file.read(data, i * sizeOfPiece, sizeOfPiece)) < sizeOfPiece)
                                            {
                                                for (int k = 0; k < sizeOfPiece - count; ++k)
                                                {
                                                    data[count + k] = 0;
                                                }
                                            }
                                            System.arraycopy(sha1.digest(data), 0, hashBytes, i*20, sha1.digest(data).length);
                                        }

                                    }
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

        System.out.println(new String(os.toByteArray()));
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

