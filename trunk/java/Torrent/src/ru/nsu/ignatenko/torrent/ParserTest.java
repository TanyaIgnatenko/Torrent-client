package ru.nsu.ignatenko.torrent;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.googlecode.jbencode.Parser;
import com.googlecode.jbencode.Value;
import com.googlecode.jbencode.composite.DictionaryValue;
import com.googlecode.jbencode.composite.EntryValue;
import com.googlecode.jbencode.composite.ListValue;
import com.googlecode.jbencode.primitive.IntegerValue;
import com.googlecode.jbencode.primitive.StringValue;

public class ParserTest
{
    public static void main(String[] args) throws IOException
    {
        ByteArrayInputStream is = new ByteArrayInputStream
                ("d9:announce 15:URL of tracker 14:announce-list 21:URL of backup tracker11:created by 14:MyTorrent 1.0 5:info d7:length i12e5:name 9:file.txt 13:piece length i256e7:pieces 20:??\u000Em?\u0018?????!\u001A??\u001A?\u001D??ee\n".getBytes());


    }
}
