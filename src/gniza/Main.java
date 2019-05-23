package gniza;

import gniza.beans.GnizaReader;
import gniza.beans.TextSearcher;
import gniza.beans.WordsSearcher;
import gniza.data.GnizaReaderImpl;
import gniza.logic.TextSearcherImpl;

import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        new Main("Easy_Hebrew", 5).Compute();
    }

    private final GnizaReader gnizaReader;
    private final TextSearcher searcher;

    public Main(String dir, int groupWordLength)
    {
        gnizaReader = new GnizaReaderImpl(dir);
        WordsSearcher wordsSearcher = null;
        searcher = new TextSearcherImpl(wordsSearcher, groupWordLength);
    }


    private void Compute() throws IOException
    {
        for (String[] file : gnizaReader.readFiles()) {
            ComputeFile(file);
        }
    }

    private void ComputeFile(String[] lines) throws IOException
    {
        searcher.Search(lines);
    }
}

