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
    	new Main("daniel_challenge", 3, 3, 2, "sefaria_index").Compute();
//    	new Main("shimoni", 4, 3, 2, "sefaria_index").Compute();
//        new Main("synthetic/utf8/", 5, 2, 2, "index").Compute();
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
    	int i = 0;
        for (FileGniza file : gnizaReader.readFiles()) {
            ComputeFileForDaniel(file);
//            if (++i == 20) {
//            	break;
//            }
        }
    }

    private void ComputeFile(FileGniza file) throws IOException
    {
        System.out.println("**** " + file.getName() + "****");
        String[] fileLines = file.getLines();
		List<SearchResult> result = searcher.Search(fileLines);
        		
        int maxResultsToDisplay = 5;
        int yalkutResult = 0;
        for (SearchResult searchResult : result) {
            ReferenceDetail referenceDetail = searchResult.getReferenceDetail();
            if (referenceDetail.getName().indexOf("Shimoni") == -1) {
            	System.out.println("\t" + referenceDetail.getName() + "=>" + referenceDetail.getTypeBook() + " : " + (int)searchResult.getPropability() + "/" + yalkutResult);
            	if (--maxResultsToDisplay == 0) {
            		break;
            	}
            }
            else {
            	if (yalkutResult == 0)
            		yalkutResult = (int)searchResult.getPropability();
            }
        }
    }


	private void ComputeFileForDaniel(FileGniza file) throws IOException
	    {
	        System.out.print(file.getName());
	        String[] fileLines = file.getLines();
			List<SearchResult> result = searcher.Search(fileLines);
	        int numberOfWords = 0;
	        for (String line: fileLines) {
	        	numberOfWords += line.split(" ").length;
	        }
	        
	        if (result.isEmpty()) {
	        	System.out.println("\t\t");
	        }
	        else {
	        	SearchResult searchResult = result.get(0);
	            ReferenceDetail referenceDetail = searchResult.getReferenceDetail();
				System.out.println("\t" + referenceDetail.getName() + "\t"
						+ (int) searchResult.getPropability() + "/" + numberOfWords);
	        }
	    }
}

