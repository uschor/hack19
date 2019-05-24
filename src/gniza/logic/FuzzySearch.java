package gniza.logic;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.*;
import org.apache.lucene.store.FSDirectory;

import gniza.beans.*;

public class FuzzySearch implements WordsSearcher
{

    static final String FTD = "contents";
    static final String PATH_PROPERTY = "path";
    private IndexSearcher searcher = null;

    private int slop;

    private int maxEdits;

    public FuzzySearch() throws IOException
    {
        this(2, 2, "index");
    }

    /**
     * @param slop
     * @param maxEdits
     * @param indexDir
     */
    public FuzzySearch(int slop, int maxEdits, String indexDir) throws IOException
    {
        this.slop = slop;
        this.maxEdits = maxEdits;

        IndexReader reader;
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        searcher = new IndexSearcher(reader);
    }

    @Override
    public List<SearchResult> Search(int lengthText, String... words) throws IOException
    {
        Query query = null;
        if (words.length == 1) {
            query = new FuzzyQuery(new Term(FTD, words[0]), maxEdits);
        } else {
            SpanQuery[] clauses = new SpanQuery[words.length];
            for (int i = 0; i < words.length; i++) {
                clauses[i] = new SpanMultiTermQueryWrapper<MultiTermQuery>(
                        new FuzzyQuery(new Term(FTD, words[i]), maxEdits));
            }
            query = new SpanNearQuery(clauses, slop, true);
        }

        TopDocs searchResults;
        try {
            searchResults = searcher.search(query, 10);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        ScoreDoc[] hits = searchResults.scoreDocs;
        List<SearchResult> results = new ArrayList<SearchResult>();
        for (int i = 0; i < hits.length; i++) {
            ReferenceDetail book = new ReferenceDetail();
            Document doc;
            doc = searcher.doc(hits[i].doc);
            String path = doc.get(PATH_PROPERTY);
            book.setName(new File(path).getName().replace(".txt",""));
            results.add(new SearchResult(1.0, book));
        }
        return results;
    }

}
