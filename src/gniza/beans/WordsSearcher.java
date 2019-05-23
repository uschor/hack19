package gniza.beans;

import java.util.List;

public interface WordsSearcher
{
    List<SearchResult> Search(String... words);
}
