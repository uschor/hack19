package gniza;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import gniza.beans.*;
import gniza.logic.FuzzySearch;

public class MapTextToWork {
	private static final int SEARCH_WINDOW_SIZE = 3;
	private static final int MAX_EDITS = 2;
	private static final int SLOP = 2;
	private static final int SEARCH_HISTORY = 100;
	private static final String INDEX_DIR = "sefaria_primary_index";//"sefaria_index";
	
	private static final int EXCLUDED_MAX_EDITS = 2;
	private static final int EXCLUDED_SLOP = 0;
	private static final String EXCLUDE_INDEX_DIR = "bible_index";
	private static final String[] WORKS = new String[] {
			"bavli",
			"midrash.Bereishit_Rabbah",
			"midrash.Shemot_Rabbah",
			"midrash.Vayikra_Rabbah",
			"midrash.Bemidbar_Rabbah",
			"midrash.Devarim_Rabbah",
			
			"midrash.Derech_Eretz_Zuta",
			"midrash.Eichah_Rabbah",
			"midrash.Esther_Rabbah",
			"midrash.Kohelet_Rabbah",
			"midrash.Midrash_Aggadah",
			"midrash.Midrash_Mishlei",
			"midrash.Midrash_Tehillim",
			"midrash.Pesikta_D",
			"midrash.Pesikta_Rabbati",
			"midrash.Pirkei_DeRabbi_Eliezer",
			"midrash.Ruth_Rabbah",
			"midrash.Seder_Olam_Rabbah",
			"midrash.Tanna_Debei_Eliyahu_Rabbah",
			
			"midrash.Mekhilta_DeRabbi_Shimon_Bar_Yochai",
			"midrash.Sifrei_Bamidbar",
			"midrash.Sifrei_Devarim",

			"minor_tractate",
			"mishnah",
			"tosefta",
			"yerushalmi",

//			"midrash.Bemidbar_Rabbah",
//			"midrash.Bereishit_Rabbah",
//			"midrash.Derech_Eretz_Zuta",
//			"midrash.Devarim_Rabbah",
//			"midrash.Eichah_Rabbah",
//			"midrash.Ein_Yaakov",
//			"midrash.Esther_Rabbah",
//			"midrash.Kohelet_Rabbah",
//			"midrash.Mekhilta_DeRabbi_Shimon_Bar_Yochai",
//			"midrash.Mekhilta_d'Rabbi_Yishmael",
//			"midrash.Midrash_Aggadah",
//			"midrash.Midrash_Lekach_Tov_on_Ruth",
//			"midrash.Midrash_Mishlei",
//			"midrash.Midrash_Tanchuma.",
//			"midrash.Midrash_Tanchuma_Buber",
//			"midrash.Midrash_Tehillim",
//			"midrash.Otzar_Midrashim",
//			"midrash.Pesikta_D'Rav_Kahanna",
//			"midrash.Pesikta_Rabbati",
//			"midrash.Pirkei_DeRabbi_Eliezer",
//			"midrash.Ruth_Rabbah",
//			"midrash.Seder_Olam_Rabbah",
//			"midrash.Sefer_HaYashar",
//			"midrash.Shemot_Rabbah",
//			"midrash.Shir_HaShirim_Rabbah",
//			"midrash.Sifra",
//			"midrash.Sifrei_Bamidbar",
//			"midrash.Sifrei_Devarim",
//			"midrash.Tanna_Debei_Eliyahu_Rabbah",
//			"midrash.Tanna_debei_Eliyahu_Zuta",
//			"midrash.Vayikra_Rabbah",
//			"midrash.Yalkut_Shimoni_on_Nach",
//			"midrash.Yalkut_Shimoni_on_Torah"
			
			"midrash.Midrash_Tanchuma.", 
			"midrash.Midrash_Tanchuma_Buber.", 
			"midrash.Pesikta_Rabbati.", 
			"midrash.Pesikta_D'Rav_Kahanna.", 
			"midrash.Yalkut_Shimoni_on_Torah.", 
			"midrash.Yalkut_Shimoni_on_Nach.",
			"bavli.",
			"yerushalmi."
			};

	public static void main(String[] args) throws IOException {
		WordsSearcher wordsSearcher = new FuzzySearch(SLOP, MAX_EDITS, INDEX_DIR);
		WordsSearcher excludedWordsSearcher = new FuzzySearch(EXCLUDED_SLOP, EXCLUDED_MAX_EDITS, EXCLUDE_INDEX_DIR);

		List<String> wordWindow = new MaxSizedList<String>(SEARCH_WINDOW_SIZE);
		List<String> resultsHistory = new ArrayList<String>();
		List<String> wordHistory = new ArrayList<String>();
				
//		String file = "input_texts/tanhuma_buber.txt";
		String file = "input_texts/london_or_1389.txt";
//		String file = "input_texts/geneva_146_at.txt";
//		String file = "input_texts/geneva_146_aggregated.txt";
//		String file = "input_texts/geneva_146_pp6-44_ET_no_period.txt";
//		String file = "input_texts/geneva_146_pp6-44_AT_no_period.txt";
//		String file = "input_texts/parma3122_buber.txt";
//		String file = "input_texts/parma3122.txt";
		
		// Print the CSV header
		for (String work: WORKS) {
			System.out.print(work);
			System.out.print('\t');
		}
		System.out.println("text");
		
		try (Scanner scanner = new Scanner(new FileReader(file));) {
			while (scanner.hasNext()) {
				String word = scanner.next();
				wordWindow.add(word);
				wordHistory.add(word);
				if (wordWindow.size() == SEARCH_WINDOW_SIZE) {
					// Search in the excluded corpus, and skip them 
					List<SearchResult> excludedSearchResults = excludedWordsSearcher.Search(wordWindow.toArray(new String[0]));

					// if not found in excluded corpus, search in included one
					if (excludedSearchResults.isEmpty()) {							
						List<SearchResult> searchResults = wordsSearcher.Search(wordWindow.toArray(new String[0]));
						addSearchResultToHistory(resultsHistory, searchResults, 0);
						addSearchResultToHistory(resultsHistory, searchResults, 1);
					}					
				}
				if (wordHistory.size() == SEARCH_HISTORY) {
					int[] stats = new int[WORKS.length];
					for (int i = 0; i < WORKS.length; ++i) {
						for (String result: resultsHistory) {
							if (result.startsWith(WORKS[i])) {
								stats[i]++;
							}
						}
					}
					// Print the CSV header
					for (int stat: stats) {
						System.out.print("" + stat);
						System.out.print('\t');
					}
					System.out.println(wordHistory.stream().collect(Collectors.joining(" ")));
					
					wordHistory.clear();
					resultsHistory.clear();
				}
			}
		}
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
}
