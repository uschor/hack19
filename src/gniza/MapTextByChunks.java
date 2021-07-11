package gniza;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;
import java.util.stream.*;

import gniza.beans.*;
import gniza.logic.FuzzySearch;

public class MapTextByChunks {
	private static final int SEARCH_WINDOW_SIZE = 3;
	private static final int MAX_EDITS = 2;
	private static final int SLOP = 2;
	private static final int SEARCH_HISTORY_LINES = 3;
//	private static final String INDEX_DIR = "london_index";
	private static final String INDEX_DIR = "sefaria_primary_index";
//	private static final String INDEX_DIR = "sefaria_index";
//	private static final String INDEX_DIR = "joint_index";
//	private static final String INDEX_DIR = "tanhuma_index";
	
	private static final int EXCLUDED_MAX_EDITS = 2;
	private static final int EXCLUDED_SLOP = 0;
	private static final String EXCLUDE_INDEX_DIR = "bible_index";
	//private static final String EXCLUDE_INDEX_DIR = "tanhuma_index";
	private static final Map<String, String> works = Stream.of(new String[][] {
		{ "bavli.Arakhin", "bavli"},
		{ "bavli.Avodah_Zarah", "bavli"},
		{ "bavli.Bava_Batra", "bavli"},
		{ "bavli.Bava_Kamma", "bavli"},
		{ "bavli.Bava_Metzia", "bavli"},
		{ "bavli.Beitzah", "bavli"},
		{ "bavli.Bekhorot", "bavli"},
		{ "bavli.Berakhot", "bavli"},
		{ "bavli.Chagigah", "bavli"},
		{ "bavli.Chullin", "bavli"},
		{ "bavli.Eruvin", "bavli"},
		{ "bavli.Gittin", "bavli"},
		{ "bavli.Horayot", "bavli"},
		{ "bavli.Keritot", "bavli"},
		{ "bavli.Ketubot", "bavli"},
		{ "bavli.Kiddushin", "bavli"},
		{ "bavli.Makkot", "bavli"},
		{ "bavli.Megillah", "bavli"},
		{ "bavli.Menachot", "bavli"},
		{ "bavli.Moed_Katan", "bavli"},
		{ "bavli.Nazir", "bavli"},
		{ "bavli.Nedarim", "bavli"},
		{ "bavli.Niddah", "bavli"},
		{ "bavli.Pesachim", "bavli"},
		{ "bavli.Rosh_Hashanah", "bavli"},
		{ "bavli.Sanhedrin", "bavli"},
		{ "bavli.Shabbat", "bavli"},
		{ "bavli.Shevuot", "bavli"},
		{ "bavli.Sotah", "bavli"},
		{ "bavli.Sukkah", "bavli"},
		{ "bavli.Taanit", "bavli"},
		{ "bavli.Tamid", "bavli"},
		{ "bavli.Temurah", "bavli"},
		{ "bavli.Yevamot", "bavli"},
		{ "bavli.Yoma", "bavli"},
		{ "bavli.Zevachim", "bavli"},
		{ "midrash.Bemidbar_Rabbah", "midrash_amoraic"},
		{ "midrash.Bereishit_Rabbah", "midrash_amoraic"},
		{ "midrash.Derech_Eretz_Zuta", "midrash_amoraic"},
		{ "midrash.Devarim_Rabbah", "midrash_amoraic"},
		{ "midrash.Eichah_Rabbah", "midrash_amoraic"},
		{ "midrash.Esther_Rabbah", "midrash_amoraic"},
		{ "midrash.Kohelet_Rabbah", "midrash_amoraic"},
		{ "midrash.Mekhilta_DeRabbi_Shimon_Bar_Yochai", "midrash_tannaitic"},
		{ "midrash.Midrash_Aggadah", "midrash_amoraic"},
		{ "midrash.Midrash_Mishlei", "midrash_amoraic"},
		{ "midrash.Midrash_Tehillim", "midrash_amoraic"},
		{ "midrash.Pesikta_D", "midrash_amoraic"},
		{ "midrash.Pesikta_Rabbati", "midrash_amoraic"},
		{ "midrash.Pirkei_DeRabbi_Eliezer", "midrash_amoraic"},
		{ "midrash.Ruth_Rabbah", "midrash_amoraic"},
		{ "midrash.Seder_Olam_Rabbah", "midrash_amoraic"},
		{ "midrash.Shemot_Rabbah", "midrash_amoraic"},
		{ "midrash.Sifrei_Bamidbar", "midrash_tannaitic"},
		{ "midrash.Sifrei_Devarim", "midrash_tannaitic"},
		{ "midrash.Tanna_Debei_Eliyahu_Rabbah", "midrash_amoraic"},
		{ "midrash.Vayikra_Rabbah", "midrash_amoraic"},
		{ "minor_tractate.Avot_D", "midrash_amoraic"},
		{ "minor_tractate.Tractate_Derekh_Eretz_Rabbah", "minor_tractate"},
		{ "minor_tractate.Tractate_Derekh_Eretz_Zuta", "minor_tractate"},
		{ "minor_tractate.Tractate_Kallah", "minor_tractate"},
		{ "minor_tractate.Tractate_Kallah_Rabbati", "minor_tractate"},
		{ "mishnah.Beitzah", "mishnah"},
		{ "mishnah.Berakhot", "mishnah"},
		{ "mishnah.Chagigah", "mishnah"},
		{ "mishnah.Chullin", "mishnah"},
		{ "mishnah.Eduyot", "mishnah"},
		{ "mishnah.Kelim", "mishnah"},
		{ "mishnah.Keritot", "mishnah"},
		{ "mishnah.Kiddushin", "mishnah"},
		{ "mishnah.Makkot", "mishnah"},
		{ "mishnah.Meilah", "mishnah"},
		{ "mishnah.Mikvaot", "mishnah"},
		{ "mishnah.Nazir", "mishnah"},
		{ "mishnah.Negaim", "mishnah"},
		{ "mishnah.Niddah", "mishnah"},
		{ "mishnah.Oholot", "mishnah"},
		{ "mishnah.Peah", "mishnah"},
		{ "mishnah.Pirkei_Avot", "mishnah"},
		{ "mishnah.Rosh_Hashanah", "mishnah"},
		{ "mishnah.Sanhedrin", "mishnah"},
		{ "mishnah.Shabbat", "mishnah"},
		{ "mishnah.Shevuot", "mishnah"},
		{ "mishnah.Yadayim", "mishnah"},
		{ "mishnah.Yoma", "mishnah"},
		{ "tosefta.Avodah_Zarah", "tosefta"},
		{ "tosefta.Bava_Metzia", "tosefta"},
		{ "tosefta.Bekhorot", "tosefta"},
		{ "tosefta.Berakhot", "tosefta"},
		{ "tosefta.Bikkurim", "tosefta"},
		{ "tosefta.Demai", "tosefta"},
		{ "tosefta.Ketubot", "tosefta"},
		{ "tosefta.Kiddushin", "tosefta"},
		{ "tosefta.Peah", "tosefta"},
		{ "tosefta.Sanhedrin", "tosefta"},
		{ "tosefta.Sheviit", "tosefta"},
		{ "tosefta.Shevuot", "tosefta"},
		{ "tosefta.Sotah", "tosefta"},
		{ "yerushalmi.Avodah_Zarah", "yerushalmi"},
		{ "yerushalmi.Bava_Batra", "yerushalmi"},
		{ "yerushalmi.Bava_Kamma", "yerushalmi"},
		{ "yerushalmi.Bava_Metsia", "yerushalmi"},
		{ "yerushalmi.Berakhot", "yerushalmi"},
		{ "yerushalmi.Bikkurim", "yerushalmi"},
		{ "yerushalmi.Chagigah", "yerushalmi"},
		{ "yerushalmi.Demai", "yerushalmi"},
		{ "yerushalmi.Eiruvin", "yerushalmi"},
		{ "yerushalmi.Gittin", "yerushalmi"},
		{ "yerushalmi.Horayot", "yerushalmi"},
		{ "yerushalmi.Ketubot", "yerushalmi"},
		{ "yerushalmi.Kiddushin", "yerushalmi"},
		{ "yerushalmi.Makkot", "yerushalmi"},
		{ "yerushalmi.Megillah", "yerushalmi"},
		{ "yerushalmi.Mikvaot", "yerushalmi"},
		{ "yerushalmi.Moed_Kattan", "yerushalmi"},
		{ "yerushalmi.Nazir", "yerushalmi"},
		{ "yerushalmi.Nedarim", "yerushalmi"},
		{ "yerushalmi.Niddah", "yerushalmi"},
		{ "yerushalmi.Peah", "yerushalmi"},
		{ "yerushalmi.Pesachim", "yerushalmi"},
		{ "yerushalmi.Rosh_Hashanah", "yerushalmi"},
		{ "yerushalmi.Sanhedrin", "yerushalmi"},
		{ "yerushalmi.Shabbat", "yerushalmi"},
		{ "yerushalmi.Shekalim", "yerushalmi"},
		{ "yerushalmi.Shevuot", "yerushalmi"},
		{ "yerushalmi.Sotah", "yerushalmi"},
		{ "yerushalmi.Sukkah", "yerushalmi"},
		{ "yerushalmi.Ta", "yerushalmi"},
		{ "yerushalmi.Terumot", "yerushalmi"},
		{ "yerushalmi.Yevamot", "yerushalmi"},
		{ "yerushalmi.Yoma", "yerushalmi"},
		}).collect(Collectors.toMap(data -> data[0], data -> data[1]));

	public static void main(String[] args) throws IOException {

		Pattern excludePattern = Pattern.compile("\\[.*?\\]|\\p{Punct}|'|˙");
		
//		String file = "input_texts/london_sample.txt";
		String file = "input_texts/london_or_1389.txt";

		Pattern pattern = Pattern.compile("(^\\w+\\.\\w+)\\..*");
		
		// get the works array and print CSV header
		String[] worksArray = new HashSet<String>(works.values()).toArray(new String[0]);
		System.out.print("line_text");
		for (String work: worksArray) {
			System.out.print("\t" + work);
		}
		System.out.println();

		try (BufferedReader br = new BufferedReader(new FileReader(file));) {
			String line;
			List<String> linesWindow = new MaxSizedList<String>(SEARCH_HISTORY_LINES);
			while ((line = br.readLine()) != null) {
				line = excludePattern.matcher(line).replaceAll("").trim();
				String result1 = "";
				int count1 = 0;
				String result2 = "";
				int count2 = 0;
				String workFound = null;

				if (!line.isEmpty()) {
					linesWindow.add(line);
					SearchRequest request = new SearchRequest(INDEX_DIR, MAX_EDITS, SLOP, SEARCH_WINDOW_SIZE, EXCLUDE_INDEX_DIR,
							EXCLUDED_MAX_EDITS, EXCLUDED_SLOP, SEARCH_WINDOW_SIZE, String.join(" ", linesWindow));
					try {
						SearchResponse response = search(request);
						SearchResultItem[] results = response.getResults();
						if (results.length > 0) {
							SearchResultItem topResult = results[0];
							result1 = topResult.getDocument();
							count1 = topResult.getCount();
							
							// look for the work
							if (!result1.isEmpty()) {
								Matcher matcher = pattern.matcher(result1);
								if (matcher.matches()) {
									workFound = works.get(matcher.group(1));
									result1 = workFound == null ? "unknown:" + matcher.group(1) : workFound;
								}
							}
						}
						if (results.length > 1) {
							SearchResultItem topResult = results[1];
							result2 = topResult.getDocument();
							count2 = topResult.getCount();
						}
					} catch (IOException e) {
						System.err.println("Error occurred: " + e);
					}
				}

				// Print by works (all non-found get zero)
				System.out.print(line);
				for (String work: worksArray) {
					if (work.equals(workFound)) {
						System.out.print("\t" + count1);
					}
					else {
						System.out.print("\t0");										
					}
				}
				System.out.println();

//				System.out.println(line + "\t" + result1 + "\t" + count1 + "\t" + result2 + "\t" + count2);
			}
		}


	}

	private static SearchResponse search(SearchRequest request) throws IOException {
		String[] searchWords = request.getText().split("\\s+");
		int wc = searchWords.length;
		String corpusDir = request.getCorpus();
		Map<String, DocResults> resultsHistory = new HashMap<String, DocResults>();
		WordsSearcher wordsSearcher = new FuzzySearch(request.getWordSpan(), request.getEditDistance(), corpusDir);
		List<SearchResult> searchResults = null;
		for (int i = 0; i <= wc - request.getWindowSize(); i++) {
			String[] window = Arrays.copyOfRange(searchWords, i, i + request.getWindowSize());
			searchResults = wordsSearcher.Search(window);
			// add the first two results to the history
			addSearchResultToHistory(resultsHistory, searchResults, 0, i, request.getWindowSize(), wc);
			addSearchResultToHistory(resultsHistory, searchResults, 1, i, request.getWindowSize(), wc);
//
//			System.out.println("Found " + searchResults.size());
//			if (!searchResults.isEmpty())
//				System.out.println("Result " + searchResults.get(0).getReferenceDetail().getName());
		}

		return new SearchResponse(sortResults(request, resultsHistory), searchWords);
	}


	private static void addSearchResultToHistory(Map<String, DocResults> resultsHistory,
			List<SearchResult> searchResults, int index, int start, int windowSize, int totalWords) {
		if (searchResults.size() > index) {
			String name = searchResults.get(index).getReferenceDetail().getName();
			DocResults value = resultsHistory.get(name);
			if (value == null) {
				value = new DocResults(totalWords);
			}
			value.matchCount++;
			value.markMatch(start, windowSize);

			resultsHistory.put(name, value);
		}
	}

	public static SearchResultItem[] sortResults(SearchRequest request, Map<String, DocResults> map) {
		List<Entry<String, DocResults>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue(Comparator.reverseOrder()));

		// limit to max 10 results
		// TODO parameterize
		int resultsNum = Math.min(list.size(), 10);

		SearchResultItem[] results = new SearchResultItem[resultsNum];
		int i = 0;
		for (Entry<String, DocResults> entry : list) {
			String document = entry.getKey();
			String documentContent = "";//getDocumentContent(request, document);
			results[i++] = new SearchResultItem(entry.getValue().matchCount, document, entry.getValue().words, documentContent);
			if (i == resultsNum) {
				break;
			}
		}
		return results;
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
	private static class DocResults implements Comparable<DocResults> {

		DocResults(int wordCount) {
			matchCount = 0;
			words = new boolean[wordCount];
		}

		void markMatch(int start, int count) {
			for (int i = start; i < start + count; i++) {
				words[i] = true;
			}
		}

		boolean[] words;

		int matchCount;

		@Override
		public int compareTo(DocResults o) {
			return this.matchCount - o.matchCount;
		}
	}

}
