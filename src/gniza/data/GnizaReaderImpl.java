package gniza.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GnizaReaderImpl implements gniza.beans.GnizaReader
{
    private final String dir;

    public GnizaReaderImpl(String dir)
    {
        this.dir = dir;
    }

    @Override
    public List<String[]> readFiles() throws IOException
    {
        List<String[]> result = new ArrayList<>();
        for (File file : new File(dir).listFiles()) {
            result.add(ReadFile(file));
        }
        return result;
    }


    private String[] ReadFile(String path) throws IOException
    {
        File file = new File(path);
        return ReadFile(file);
    }

    private String[] ReadFile(File file) throws IOException
    {
        List<String> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file));) {
            String st;
            while ((st = br.readLine()) != null)
                result.add(st);
        }
        return result.toArray(new String[result.size()]);
    }

}
