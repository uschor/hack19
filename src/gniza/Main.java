package gniza;

import gniza.beans.*;
import gniza.data.GnizaReaderImpl;
import gniza.logic.FuzzySearch;
import gniza.logic.TextSearcherImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        new Main("Easy_Hebrew", 5, 2, 2).Compute();
    }

    private final GnizaReader gnizaReader;
    private final TextSearcher searcher;

    public Main(String dir, int groupWordLength, int slop, int maxEdits)
    {
        gnizaReader = new GnizaReaderImpl(dir);
        WordsSearcher wordsSearcher = new FuzzySearch(slop, maxEdits);
        new WordsSearcher()
        {
            @Override
            public List<SearchResult> Search(int length, String... words)
            {
                List<SearchResult> result = new ArrayList<>();
                for (String word : words) {
                    for (int i = 0; i < 4; i++) {
                        result.add(new SearchResult(new Random().nextDouble(), new ReferenceDetail(TypeBook.Bavli, word)));
                    }
                }
                return result;
            }
        };
        searcher = new TextSearcherImpl(wordsSearcher, groupWordLength);
    }


    private void Compute() throws IOException
    {
        for (FileGniza file : gnizaReader.readFiles()) {
            ComputeFile(file);
        }
    }

    private void ComputeFile(FileGniza file) throws IOException
    {
        System.out.println(file.getName());
        List<SearchResult> result = searcher.Search(file.getLines());
        for (SearchResult searchResult : result) {
            ReferenceDetail referenceDetail = searchResult.getReferenceDetail();
            System.out.println(referenceDetail.getName() + "." + referenceDetail.getTypeBook() + "-" + searchResult.getPropability());

        }

    }
}

