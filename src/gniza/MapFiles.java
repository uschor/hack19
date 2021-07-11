package gniza;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import gniza.beans.*;
import gniza.logic.FuzzySearch;

public class MapFiles {
	private static final int SEARCH_WINDOW_SIZE = 3;
	private static final int MAX_EDITS = 2;
	private static final int SLOP = 2;
	
//	private static final String INDEX_DIR = "sefaria_index";
//	private static final String FILE_PREFIX = "midrash.Midrash_Tanchuma.Re";
//	private static final String DIR = "/Users/urischor/Documents/Documents/PhD/code/Sefaria-extract/json/sefaria_paragraphs/";
	private static final String INDEX_DIR = "eliezer_index";
	private static final String FILE_PREFIX = "Magnificant";
	private static final String DIR = "/Users/urischor/Documents/proj/java/hack19/eliezer_paragraphs/";
	private static final double THRESHOLD = 0.1;
	private static final double MIN_WORDS = 10;

	private static final String[] CLASSES = {
			"Brescia", "Halfan", "Magnificant", "Munic_448"
	};
//	private static final String[] CLASSES = {
//			"midrash.Midrash_Tanchuma_Buber",
//			"midrash.Yalkut_Shimoni_on_Torah",
//			"midrash.Bemidbar_Rabbah",
//			"midrash.Yalkut_Shimoni_on_Nach",
//			"midrash.Shemot_Rabbah",
//			"midrash.Midrash_Tanchuma.",
//			"midrash.Pesikta_Rabbati",
//			"midrash.Vayikra_Rabbah",
//			"midrash.Pesikta_D'Rav_Kahanna",
//			"midrash.Mekhilta_DeRabbi_Shimon_Bar_Yochai",
//			"bavli.Sanhedrin",
//			"midrash.Midrash_Aggadah",
//			"midrash.Bereishit_Rabbah",
//			"bavli.Shabbat",
//			"mishnah.Menachot",
//			"midrash.Sifrei_Bamidbar",
//			"bavli.Moed_Katan",
//			"bavli.Menachot",
//			"yerushalmi.Shabbat",
//			"yerushalmi.Megillah",
//			"yerushalmi.Gittin",
//			"mishnah.Rosh_Hashanah",
//			"mishnah.Gittin",
//			"midrash.Sifrei_Devarim",
//			"midrash.Seder_Olam_Rabbah",
//			"midrash.Ruth_Rabbah",
//			"midrash.Midrash_Tehillim",
//			"midrash.Midrash_Mishlei",
//			"midrash.Esther_Rabbah",
//			"midrash.Ein_Yaakov",
//			"midrash.Devarim_Rabbah",
//			"bavli.Sotah",
//			"bavli.Rosh_Hashanah",
//			"bavli.Pesachim",
//			"bavli.Megillah",
//			"bavli.Bava_Kamma",
//			"bavli.Bava_Batra",		
//	};
	
	private static Map<String, Integer> nodes = new HashMap<String, Integer>();
	private static int nodeIdCounter = 0;

	public static void main(String[] args) throws IOException {
		WordsSearcher wordsSearcher = new FuzzySearch(SLOP, MAX_EDITS, INDEX_DIR);

		List<String> wordWindow = new MaxSizedList<String>(SEARCH_WINDOW_SIZE);

		// Print CSV header
		System.out.println("DocName" + '\t' + "MatchingDocName" + '\t' + "MatchPercentage" + '\t' + "Doc" + '\t' + "MatchingDoc");
		for (File file : listFiles(DIR, FILE_PREFIX)) {
			String fileName = file.getName();
			int fileNameId = getNodeId(fileName);
			List<String> resultsHistory = new ArrayList<String>();
			int words = 0;
			
			try (Scanner scanner = new Scanner(new FileReader(file));) {
				while (scanner.hasNext()) {
					++words;
					String word = scanner.next();
					wordWindow.add(word);
					if (wordWindow.size() == SEARCH_WINDOW_SIZE) {
						List<SearchResult> searchResults = wordsSearcher.Search(wordWindow.toArray(new String[0]));
						addSearchResultToHistory(resultsHistory, searchResults, 0, fileName);
						addSearchResultToHistory(resultsHistory, searchResults, 1, fileName);
						addSearchResultToHistory(resultsHistory, searchResults, 2, fileName);
					}
				}
				if (words < MIN_WORDS)
					continue;
				for (Map.Entry<String, Long> entry: getMatches(resultsHistory, Math.round(THRESHOLD * words)).entrySet()) {					
					String matchingParagraph = entry.getKey();
					String docText = readFile(DIR + File.separator + fileName);
					docText = (docText == null) ? "" : docText.trim().replaceAll("\\t", " ");
					String matchingDocText = readFile(DIR + File.separator + matchingParagraph);
					matchingDocText = (matchingDocText == null) ? "" : matchingDocText.trim().replaceAll("\\t", " ");
					System.out.println("" + fileName + '\t' + matchingParagraph + '\t' + Math.round(entry.getValue() * 100 / words) + '\t' + docText  + '\t' + matchingDocText);
					// Gephie output
//					int matchId = getNodeId(matchingParagraph);
//					System.out.println("" + fileNameId + '\t' + matchId + '\t' + Math.round(entry.getValue() * 100 / words));
				}
			}
		}
	}

	private static List<File> listFiles(String dir, String prefix) {
		return Stream.of(new File(dir).listFiles()).filter(file -> file.getName().startsWith(prefix))
				.sorted(Comparator.comparing(file -> file.getName()))
				.collect(Collectors.toList());
	}

	private static Map<String, Long> getMatches(List<String> resultsHistory, long threshold) {
		return resultsHistory.stream().filter(str -> !str.isEmpty())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream()
				.filter(e -> e.getValue() > threshold).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}

	private static int getNodeId(String docName) {
		Integer nodeId = nodes.get(docName);
		if (nodeId == null) {
			nodeId = nodeIdCounter;
			nodeIdCounter++;
			nodes.put(docName, nodeId);
			// find the class
			for (int classId = 0; classId < CLASSES.length; ++classId) {
				if (docName.startsWith(CLASSES[classId])) {
					// Gephie output
//					System.out.println(">" + nodeId + '\t' + docName + '\t' + classId + '\t' + CLASSES[classId]);
					break;
				}
			}
		}
		return nodeId;
	}

	/**
	 * @param resultsHistory
	 * @param searchResults
	 * @param index
	 */
	private static void addSearchResultToHistory(List<String> resultsHistory, List<SearchResult> searchResults,
			int index, String excludedName) {
		if (searchResults.size() > index) {
			String result = searchResults.get(index).getReferenceDetail().getName();
			if (! excludedName.equals(result)) {
				resultsHistory.add(result);
			}
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
	
	private static String readFile(String fileName) {
        try {
			return new String(Files.readAllBytes(Paths.get(fileName)));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
