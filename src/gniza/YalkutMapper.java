package gniza;

import gniza.beans.*;
import gniza.data.GnizaReaderImpl;
import gniza.logic.FuzzySearch;
import gniza.logic.TextSearcherImpl;

import java.io.IOException;
import java.util.List;

public class YalkutMapper
{
    public static void main(String[] args) throws IOException
    {
    	new YalkutMapper("Bemidbar_Rabbah", 4, 3, 2, "sefaria_index").Compute();
//    	new YalkutMapper("Shimoni", 4, 3, 2, "sefaria_index").Compute();
//        new Main("synthetic/utf8/", 5, 2, 2, "index").Compute();
    }

    private final GnizaReader gnizaReader;
    private final TextSearcher searcher;
    private String dir;

    public YalkutMapper(String dir, int groupWordLength, int slop, int maxEdits, String indexDir) throws IOException
    {
    	this.dir = dir;
        gnizaReader = new GnizaReaderImpl(dir);
        WordsSearcher wordsSearcher = new FuzzySearch(slop, maxEdits, indexDir);
        searcher = new TextSearcherImpl(wordsSearcher, groupWordLength);
    }


    private void Compute() throws IOException
    {
    	int i = 0;
        for (FileGniza file : gnizaReader.readFiles()) {
            ComputeFile(file);
            if (++i == 40) {
            	break;
            }
        }
    }

    private void ComputeFile(FileGniza file) throws IOException
    {
        System.out.print(file.getName() + "\t");
        List<SearchResult> result = searcher.Search(file.getLines());
        int maxResultsToDisplay = 2;
        int selfScore = 0;
        for (SearchResult searchResult : result) {
            ReferenceDetail referenceDetail = searchResult.getReferenceDetail();
            if (referenceDetail.getName().indexOf(dir) == -1) {
            	System.out.print("\t" + referenceDetail.getName() + "\t" + (int)searchResult.getPropability() + "/" + selfScore);
            	if (--maxResultsToDisplay == 0) {
            		break;
            	}
            }
            else {
            	if (selfScore == 0)
            		selfScore = (int)searchResult.getPropability();
            }
        }
        System.out.println();
    }
}

