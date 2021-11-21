package gniza;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.*;

import org.apache.commons.cli.*;
import org.apache.lucene.index.IndexNotFoundException;

import gniza.beans.*;
import gniza.logic.FuzzySearch;

/**
 * Map a long text into paragraphs in a corpus (Lucene search index).
 * 
 */
public class MapText {
	private static final int SEARCH_WINDOW_SIZE = 3;
	private static final int MAX_EDITS = 2;
	private static final int SLOP = 2;
	private static final int SEARCH_HISTORY = 15;

	private static final int PRIMARY_MAX_EDITS = 2;
	private static final int PRIMARY_SLOP = 0;
	private static final String PRIMARY_INDEX_DIR = "index" + File.separatorChar + "bible";

	public static void main(String[] args) throws IOException {
		Options options = buildCommandOptions();

		// Parse the command line arguments
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelpAndExit(options);
		}
		if (cmd.hasOption("help")) {
			printHelpAndExit(options);
		}
		boolean bias = cmd.hasOption("bias");
		String inputFile = cmd.getOptionValue("input");
		String outputFile = cmd.getOptionValue("output");
		PrintStream output = new PrintStream(outputFile);

		String indexDir = cmd.getOptionValue("index");

		int slop = SLOP;
		try {
			slop = Integer.parseInt(cmd.getOptionValue("slop"));
		} catch (NumberFormatException e) {
		}

		int maxEdits = MAX_EDITS;
		try {
			maxEdits = Integer.parseInt(cmd.getOptionValue("max_edits"));
		} catch (NumberFormatException e) {
		}

		String primaryIndexDir = cmd.getOptionValue("primary", PRIMARY_INDEX_DIR);

		int primarySlop = PRIMARY_SLOP;
		try {
			primarySlop = Integer.parseInt(cmd.getOptionValue("primary_slop"));
		} catch (NumberFormatException e) {
		}

		int primaryMaxEdits = PRIMARY_MAX_EDITS;
		try {
			primaryMaxEdits = Integer.parseInt(cmd.getOptionValue("primary_max_edits"));
		} catch (NumberFormatException e) {
		}

		boolean groupWorks = cmd.hasOption("works");
		String[] works = null;
		if (groupWorks) {
			try (Stream<String> lines = Files.lines(Paths.get(cmd.getOptionValue("works")))) {
				works = lines.collect(Collectors.toList()).toArray(new String[] {});
			}

			// Print the CSV header
			for (String work : works) {
				output.print(work);
				output.print('\t');
			}
			output.println("text");

		}

		WordsSearcher wordsSearcher = null;
		WordsSearcher primaryWordsSearcher = null;
		try {
			wordsSearcher = new FuzzySearch(slop, maxEdits, indexDir);
			primaryWordsSearcher = new FuzzySearch(primarySlop, primaryMaxEdits, primaryIndexDir);
		} catch (IndexNotFoundException e) {
			System.err.println("Index not found");
			System.exit(1);
		}

		List<String> wordWindow = new MaxSizedList<String>(SEARCH_WINDOW_SIZE);
		List<String> resultsHistory = new MaxSizedList<String>(SEARCH_HISTORY);
		List<String> secondResultsHistory = new MaxSizedList<String>(SEARCH_HISTORY);

		Pattern excludePattern = Pattern.compile("\\[.*?\\]|\\p{Punct}|'|˙");

		long start = System.currentTimeMillis();
		String lastResult = "";
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile));) {
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
						List<SearchResult> excludedSearchResults = primaryWordsSearcher
								.Search(wordWindow.toArray(new String[0]));

						// if not found in excluded corpus, search in included one
						if (excludedSearchResults.isEmpty()) {
							List<SearchResult> searchResults = wordsSearcher.Search(wordWindow.toArray(new String[0]));
							addSearchResultToHistory(resultsHistory, searchResults, 0);
							addSearchResultToHistory(secondResultsHistory, searchResults, 1);
						} else {
							excludedResult = excludedSearchResults.get(0).getReferenceDetail().getName();
						}
					}
				}
				++lineNum;
				if (lineNum % 10 == 0) {
					System.out.print('\r');
					System.out.print("Processed: " + lineNum);
					System.out.flush();
				}
				if (!resultsHistory.isEmpty()) {
					List<String> biasedResultHistory = new ArrayList<String>(resultsHistory);
					biasedResultHistory.addAll(secondResultsHistory);
					if (bias)
						biasedResultHistory.add(lastResult);

					if (groupWorks) {
						int[] stats = new int[works.length];
						for (int i = 0; i < works.length; ++i) {
							for (String result : resultsHistory) {
								if (result.startsWith(works[i])) {
									stats[i]++;
								}
							}
						}
						printGroupedLineResult(output, line, stats);
					} else {
						// Not grouping works
						ScoredResult lineResult = getBestMatch(biasedResultHistory);
						lastResult = lineResult.id;
						printLineResult(output,
								new LineResult(line, lineResult, excludedResult,
										resultsHistory.toArray(new String[] {}),
										secondResultsHistory.toArray(new String[] {})));
					}
				} else {
					if (groupWorks) {
						int[] stats = new int[works.length];
						printGroupedLineResult(output, line, stats);
					} else {
						printLineResult(output, new LineResult(line, new ScoredResult("", 0), excludedResult,
								new String[] {}, new String[] {}));
					}
				}
			}
		}

		output.close();
		System.err.println("\nTook " + (System.currentTimeMillis() - start));

	}

	/**
	 * Print the text line results, organized by groups
	 * @param output
	 * @param line
	 * @param stats
	 */
	private static void printGroupedLineResult(PrintStream output, String line, int[] stats) {
		// Print the CSV line
		for (int stat : stats) {
			output.print("" + stat);
			output.print('\t');
		}
		output.println(line);
	}

	/**
	 * @return
	 */
	private static Options buildCommandOptions() {
		Options options = new Options();
		options.addOption(new Option("help", "print this message"));
		options.addOption(Option.builder("output").argName("file").hasArg().desc("output file name - required")
				.required(true).build());

		options.addOption(Option.builder("input").argName("file").hasArg().desc("input file name - required")
				.required(true).build());

		options.addOption(Option.builder("index").argName("index_dir").hasArg()
				.desc("the directory which contains the search index - required").required(true).build());

		options.addOption(
				Option.builder("max_edits").argName("num").hasArg().desc("maximal edit distance. Default=2").build());

		options.addOption(Option.builder("slop").argName("num").hasArg().desc("maximal word span. Default=2").build());

		options.addOption(Option.builder("primary").argName("index_dir").hasArg()
				.desc("the directory which contains the primary (quotations) search index. Default=bible_index")
				.required(false).build());

		options.addOption(Option.builder("primary_max_edits").argName("num").hasArg()
				.desc("primary index maximal edit distance. Default=2").build());

		options.addOption(Option.builder("primary_slop").argName("num").hasArg()
				.desc("primary index maximal word span. Default=0").build());

		options.addOption(Option.builder("works").argName("file").hasArg()
				.desc("a file with works list (line per work) to group the works by").build());

		options.addOption(
				new Option("bias", "bias for continuity of identifications, by feedback-ing the last result"));
		return options;
	}

	/**
	 * @param output
	 * @param lineResult
	 */
	private static void printLineResult(PrintStream output, LineResult lineResult) {
		output.print(lineResult.text + "\t"
				+ (lineResult.result.id.isEmpty() ? "\t" : lineResult.result.id + "\t" + lineResult.result.count) + "\t"
				+ lineResult.excludedResult + "\t");
		for (String res : lineResult.firstResultsHistory) {
			output.print(res + ":");
		}
		output.print("\t");
		for (String res : lineResult.secondResultsHistory) {
			output.print(res + ":");
		}
		output.println();
	}

	/**
	 * @param options
	 */
	private static void printHelpAndExit(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(null);
		formatter.printHelp("MapText", options);
		System.exit(0);
	}

	private static ScoredResult getBestMatch(List<String> resultsHistory) {
		try {
			Entry<String, Long> maxSearchResult = resultsHistory.stream().filter(str -> !str.isEmpty())
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream()
					.max(Comparator.comparing(Entry::getValue)).get();
			if (maxSearchResult != null) {
				return new ScoredResult(maxSearchResult.getKey(), maxSearchResult.getValue());
			}
		} catch (NoSuchElementException e) {
		}
		return new ScoredResult("", 0);
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
		} else {
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

	private static class ScoredResult {
		String id;
		long count;

		public ScoredResult(String id, long count) {
			this.id = id;
			this.count = count;
		}
	}

	private static class LineResult {
		String text;
		ScoredResult result;
		String excludedResult;
		String[] firstResultsHistory;
		String[] secondResultsHistory;

		public LineResult(String text, ScoredResult result, String excludedResult, String[] firstResultsHistory,
				String[] secondResultsHistory) {
			this.text = text;
			this.result = result;
			this.excludedResult = excludedResult;
			this.firstResultsHistory = firstResultsHistory;
			this.secondResultsHistory = secondResultsHistory;
		}

	}
}
