package gniza;

public class SearchResponse {
        
    private final SearchResultItem[] results;
    
    private final String[] words;
        
	public SearchResponse(SearchResultItem[] results, String[] words) {
		this.results = results;
		this.words = words;
	}

	public SearchResultItem[] getResults() {
		return results;
	}
	
	public String[] getWords() {
		return words;
	}
}