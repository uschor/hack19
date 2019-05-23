package gniza.logic;

import java.io.IOException;
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

    private IndexSearcher searcher = null;

    private int slop;

    private int maxEdits;

    public FuzzySearch()
    {
        this(2, 2);
    }

    /**
     * @param slop
     * @param maxEdits
     */
    public FuzzySearch(int slop, int maxEdits)
    {
        this.slop = slop;
        this.maxEdits = maxEdits;

        // TODO parameterize
        String index = "index";
        IndexReader reader;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SearchResult> Search(int lengthText, String... words)
    {
        Query query = null;
        if (words.length == 1) {
            query = new FuzzyQuery(new Term("contents", words[0]), maxEdits);
        } else {
            SpanQuery[] clauses = new SpanQuery[words.length];
            for (int i = 0; i < words.length; i++) {
                clauses[i] = new SpanMultiTermQueryWrapper<MultiTermQuery>(
                        new FuzzyQuery(new Term("contents", words[i]), maxEdits));
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
            try {
                doc = searcher.doc(hits[i].doc);
                String path = doc.get("path");
                book.setName(path);
                results.add(new SearchResult(1.0, book));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return results;
    }

}
