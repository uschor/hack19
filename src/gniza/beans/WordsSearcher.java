package gniza.beans;

import java.io.IOException;
import java.util.List;

public interface WordsSearcher
{
    List<SearchResult> Search(int lengthText,String... words) throws IOException;
}
