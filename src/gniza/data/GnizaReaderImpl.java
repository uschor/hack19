package gniza.data;

import gniza.beans.FileGniza;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GnizaReaderImpl implements gniza.beans.GnizaReader
{
    private final String dir;

    public GnizaReaderImpl(String dir)
    {
        this.dir = dir;
    }

    @Override
    public List<FileGniza> readFiles() throws IOException
    {
        List<FileGniza> result = new ArrayList<>();
        for (File file : new File(dir).listFiles()) {
            String[] lines = ReadFile(file);
            if (lines.length > 0)
                result.add(new FileGniza(lines, file.getName()));
        }
        result.sort(Comparator.comparing(FileGniza::getName));
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
            String line;
            while ((line = br.readLine()) != null)
                result.add(line);
        }
        return result.toArray(new String[result.size()]);
    }

}
