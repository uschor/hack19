package gniza;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import gniza.beans.*;
import gniza.logic.FuzzySearch;

public class MapText {
	private static final int SEARCH_WINDOW_SIZE = 3;
	private static final int MAX_EDITS = 2;
	private static final int SLOP = 2;
	private static final int SEARCH_HISTORY = 15;
	private static final String INDEX_DIR = "london_index";
//	private static final String INDEX_DIR = "sefaria_index";
//	private static final String INDEX_DIR = "joint_index";
//	private static final String INDEX_DIR = "tanhuma_index";
	
	private static final int EXCLUDED_MAX_EDITS = 2;
	private static final int EXCLUDED_SLOP = 0;
	private static final String EXCLUDE_INDEX_DIR = "bible_index";
	//private static final String EXCLUDE_INDEX_DIR = "tanhuma_index";

	public static void main(String[] args) throws IOException {
		WordsSearcher wordsSearcher = new FuzzySearch(SLOP, EXCLUDED_MAX_EDITS, INDEX_DIR);
		WordsSearcher excludedWordsSearcher = new FuzzySearch(EXCLUDED_SLOP, MAX_EDITS, EXCLUDE_INDEX_DIR);

		List<String> wordWindow = new MaxSizedList<String>(SEARCH_WINDOW_SIZE);
		List<String> resultsHistory = new MaxSizedList<String>(SEARCH_HISTORY);
		List<String> secondResultsHistory = new MaxSizedList<String>(SEARCH_HISTORY);
		ArrayList<LineResult> lineResults = new ArrayList<LineResult>();

		Pattern excludePattern = Pattern.compile("\\[.*?\\]|\\p{Punct}|'|˙");
		
//		String file = "input_texts/tanhuma_buber.txt";
//		String file = "input_texts/geneva_146_aggregated.txt";
//		String file = "input_texts/geneva_146_pp6-44_ET_no_period.txt";
//		String file = "input_texts/london_sample.txt";
		String file = "input_texts/jlm.txt";
//		String file = "input_texts/london_or_1389.txt";
//		String file = "input_texts/parma3122_buber.txt";
//		String file = "geneva_146.txt";
//		String file = "vat44.txt";
//		String file = "vat44.beginning.txt";
		long start = System.currentTimeMillis();
		String lastResult = "";
		try (BufferedReader br = new BufferedReader(new FileReader(file));) {
			String line;
			int lineNum = 0;
			while ((line = br.readLine()) != null) {
				String excludedResult = "";
				line = excludePattern.matcher(line).replaceAll("");
				String[] words = line.split("\\s+");
				for (String word : words) {
					wordWindow.add(word);
					if (wordWindow.size() == SEARCH_WINDOW_SIZE) {
						// Search in the excluded corpus, and skip them 
						List<SearchResult> excludedSearchResults = excludedWordsSearcher.Search(wordWindow.toArray(new String[0]));

						// if not found in excluded corpus, search in included one
						if (excludedSearchResults.isEmpty()) {							
							List<SearchResult> searchResults = wordsSearcher.Search(wordWindow.toArray(new String[0]));
							addSearchResultToHistory(resultsHistory, searchResults, 0);
							addSearchResultToHistory(secondResultsHistory, searchResults, 1);
						}
						else {
							String excludedWords = wordWindow.stream().collect(Collectors.joining(" "));
							excludedResult = excludedSearchResults.get(0).getReferenceDetail().getName();
//							System.err.println(
//									excludedResult + "\t" + excludedWords);
						}
					}
				}
				++lineNum;
//				if (lineNum == 50) {
//					break;
//				}
				if (!resultsHistory.isEmpty()) {
					List<String> biasedResultHistory = new ArrayList<String>(resultsHistory);
					biasedResultHistory.addAll(secondResultsHistory);
					biasedResultHistory.add(lastResult);
					
					lastResult = getBestMatch(biasedResultHistory);
					lineResults.add(new LineResult(line, lastResult, excludedResult,
							resultsHistory.toArray(new String[] {}), secondResultsHistory.toArray(new String[] {})));
				}
				else {
					lineResults.add(new LineResult(line, "", excludedResult, new String[] {}, new String[] {}));
				}
//				System.err.println("" + lineNum);
			}
		}

		// Go over the results in reverse order, and fill in missing results
		ListIterator<LineResult> li = lineResults.listIterator(lineResults.size());
		String latestResult = "";
		while (li.hasPrevious()) {
			LineResult current = li.previous();
			if (current.result.isEmpty()) {
				current.result = latestResult;
			}
			latestResult = current.result;
		}
		// Now print the results
		for (LineResult lineResult : lineResults) {
			System.out.print(lineResult.text + "\t" + (lineResult.result.isEmpty() ? "?" : lineResult.result) + "\t"
					+ lineResult.excludedResult + "\t");
			for (String res : lineResult.firstResultsHistory) {
				System.out.print(res + ":");
			}
			System.out.print("\t");
			for (String res : lineResult.secondResultsHistory) {
				System.out.print(res + ":");
			}
			System.out.println();
		}
//		System.err.println("Took " + (System.currentTimeMillis() - start));

	}

	private static String getBestMatch(List<String> resultsHistory) {
		try {
			Entry<String, Long> maxSearchResult = resultsHistory.stream().filter(str -> !str.isEmpty())
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet()
					.stream().max(Comparator.comparing(Entry::getValue)).get();
			if (maxSearchResult != null) {
				return maxSearchResult.getKey();
			}
		}
		catch (NoSuchElementException e) {  			
		}
		return "";
	}
	
	/**
	 * @param resultsHistory
	 * @param searchResults
	 * @param index
	 */
	private static void addSearchResultToHistory(List<String> resultsHistory, List<SearchResult> searchResults,
			int index) {
		if (searchResults.size() > index) {
			resultsHistory.add(searchResults.get(index).getReferenceDetail().getName());
		}
		else {
			resultsHistory.add("");
		}
	}

	private static class MaxSizedList<T> extends LinkedList<T> {
		private static final long serialVersionUID = 1L;
		private int maxSize = 5;

		public MaxSizedList(int maxSize) {
			this.maxSize = maxSize;
		}

		@Override
		public boolean add(T e) {
			if (size() == maxSize) {
				removeFirst();
			}
			return super.add(e);
		}
	}
	
	private static class LineResult {
		String text;
		String result;
		String excludedResult;
		String[] firstResultsHistory;
		String[] secondResultsHistory;
		
		public LineResult(String text, String result, String excludedResult, String[] firstResultsHistory, String[] secondResultsHistory) {
			this.text = text;
			this.result = result;
			this.excludedResult = excludedResult;
			this.firstResultsHistory = firstResultsHistory;
			this.secondResultsHistory = secondResultsHistory;
		}
		
	}
}
