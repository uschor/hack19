package gniza.beans;

import java.io.IOException;
import java.util.List;

public interface TextSearcher
{
    List<SearchResult> Search(String text) throws IOException;
    List<SearchResult> Search(String[] lines) throws IOException;
}
