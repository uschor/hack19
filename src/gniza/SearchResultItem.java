package gniza;

public class SearchResultItem {
	
	public SearchResultItem(int count, String document, boolean[] words, String documentContent) {
		this.count = count;
		this.document = document;
		this.words = words;
		this.documentContent = documentContent;
	}

	private final int count;
	
	private final String document;
	
	private final boolean[] words;
	
	private final String documentContent;

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return the document
	 */
	public String getDocument() {
		return document;
	}

	/**
	 * @return the words
	 */
	public boolean[] getWords() {
		return words;
	}
	
	/**
	 * @return the document
	 */
	public String getDocumentContent() {
		return documentContent;
	}
	
}