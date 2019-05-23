package gniza.beans;

import java.util.List;

public interface TextSearcher
{
    List<SearchResult> Search(String text);
    List<SearchResult> Search(String[] lines);
}
