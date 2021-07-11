package gniza;

public class SearchRequest {
	
	private static final String DEFAULT_CORPUS_DIR = "sefaria";

	private final String corpus;

	private final int editDistance;

	private final int wordSpan;

	private final int windowSize;

	private final String quotesCorpus;

	private final int quotesEditDistance;

	private final int quotesWordSpan;

	private final int quotesWindowSize;

	private String text;

	public SearchRequest(String corpus, int editDistance, int wordSpan, int windowSize, String quotesCorpus,
			int quotesEditDistance, int quotesWordSpan, int quotesWindowSize, String text) {
		this.corpus = corpus;
		this.editDistance = editDistance;
		this.wordSpan = wordSpan;
		this.windowSize = windowSize;
		this.quotesCorpus = quotesCorpus;
		this.quotesEditDistance = quotesEditDistance;
		this.quotesWordSpan = quotesWordSpan;
		this.quotesWindowSize = quotesWindowSize;
		this.text = text;
	}

	/**
	 * @return the corpus
	 */
	public String getCorpus() {
		if (corpus != null)
			return corpus;
		return DEFAULT_CORPUS_DIR;
	}

	/**
	 * @return the editDistance
	 */
	public int getEditDistance() {
		return editDistance;
	}

	/**
	 * @return the wordSpan
	 */
	public int getWordSpan() {
		return wordSpan;
	}

	/**
	 * @return the windowSize
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * @return the quotesCorpus
	 */
	public String getQuotesCorpus() {
		return quotesCorpus;
	}

	/**
	 * @return the quotesEditDistance
	 */
	public int getQuotesEditDistance() {
		return quotesEditDistance;
	}

	/**
	 * @return the quotesWordSpan
	 */
	public int getQuotesWordSpan() {
		return quotesWordSpan;
	}

	/**
	 * @return the quotesWindowSize
	 */
	public int getQuotesWindowSize() {
		return quotesWindowSize;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}