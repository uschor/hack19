package gniza;

import gniza.beans.*;
import gniza.data.GnizaReaderImpl;
import gniza.logic.FuzzySearch;
import gniza.logic.TextSearcherImpl;

import java.io.IOException;
import java.util.List;

public class Main
{
    public static void main(String[] args) throws IOException
    {
//        new Main("temp", 5, 2, 2).Compute();
        new Main("synthetic/utf8/", 5, 2, 2, "index").Compute();
    }

    private final GnizaReader gnizaReader;
    private final TextSearcher searcher;

    public Main(String dir, int groupWordLength, int slop, int maxEdits, String indexDir) throws IOException
    {
        gnizaReader = new GnizaReaderImpl(dir);
        WordsSearcher wordsSearcher = new FuzzySearch(slop, maxEdits, indexDir);
        searcher = new TextSearcherImpl(wordsSearcher, groupWordLength);
    }


    private void Compute() throws IOException
    {
        for (FileGniza file : gnizaReader.readFiles()) {
            ComputeFile(file);
        }
    }

    private void ComputeFile(FileGniza file)
    {
        System.out.println("**** " + file.getName() + "****");
        List<SearchResult> result = searcher.Search(file.getLines());
        result.sort((o1, o2) -> (int) Math.round(o2.getPropability() - o1.getPropability()));
        for (SearchResult searchResult : result) {
            ReferenceDetail referenceDetail = searchResult.getReferenceDetail();
            System.out.println("\t" + referenceDetail.getName() + "=>" + referenceDetail.getTypeBook() + " : " + searchResult.getPropability());

        }

    }
}

