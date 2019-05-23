package org.proj929.bps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.FSDirectory;

/**
 * Servlet implementation class BibleLocator
 */
//@WebServlet(description = "Locate a chapter in the bible, according to an image of a page", urlPatterns = {
//		"/bibleLocator" }, initParams = {
//				@WebInitParam(name = "ocrDir", value = "C:/Progra~2/Tesseract-OCR", description = "directory of Tesseract OCR tool"),
//				@WebInitParam(name = "ocrCmd", value = "tesseract.exe", description = "Tesseract OCR command"),
//@WebInitParam(name = "ocrConfig", value = "", description = "Tesseract OCR config file"),
//				@WebInitParam(name = "workDir", value = "d:/temp/ocr", description = "Work directory for uploaded files"),
//				@WebInitParam(name = "luceneIndexDir", value = "C:/Users/Uriels/workspace/lucene_test/index", description = "Lucene index directory"),
//				@WebInitParam(name = "imageCleaner", value = "", description = "Image cleaner command (null if none)") })
 @WebServlet(description = "Locate a chapter in the bible, according to an image of a page", urlPatterns = {
 "/bibleLocator" }, initParams = { 
		 @WebInitParam(name = "ocrDir", value = "/usr/bin", description = "directory of Tesseract OCR tool"),
		 @WebInitParam(name = "ocrCmd", value = "tesseract", description = "Tesseract OCR command"),
		 @WebInitParam(name = "ocrConfig", value = "bible", description = "Tesseract OCR config file"),
 @WebInitParam(name = "workDir", value = "/opt/bps", description = "Work directory for uploaded files"),
 @WebInitParam(name = "luceneIndexDir", value = "/opt/bps/index", description = "Lucene index directory"),
 @WebInitParam(name = "imageCleaner", value = "/usr/local/bin/textcleaner", description = "Image cleaner command (null if none)") })
@MultipartConfig
public class BibleLocator extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private OcrTools ocrTools;

	private IndexSearcher searcher;

	private String imageCleaner;

	private final static Logger log = Logger.getLogger(BibleLocator.class.getName());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public BibleLocator() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ocrTools = new OcrTools(getInitParameter("ocrCmd"), getInitParameter("ocrDir"), getInitParameter("ocrConfig"));
		imageCleaner = getInitParameter("imageCleaner");

		// Initialize Lucene
		IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get(getInitParameter("luceneIndexDir"))));
			searcher = new IndexSearcher(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		boolean getNumber = Boolean.parseBoolean(request.getParameter("getNumber"));
		
		// Save the image file
		Part filePart = request.getPart("webcam"); // Retrieves <input
													// type="file"
													// name="webcam">
		InputStream fileContent = filePart.getInputStream();
		File file = File.createTempFile("ocr-", ".jpg", new File(getInitParameter("workDir")));
		Files.copy(fileContent, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

		// Run OCR
		String fileName = file.getAbsolutePath();

		JsonObject jsonResult = getNumber ? getNumberFromImageFile(fileName) : getChapterFromImageFile(fileName);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JsonWriter jsonWriter = Json.createWriter(response.getWriter());
		jsonWriter.writeObject(jsonResult);

		// write response to file
		Files.write(Paths.get(fileName + ".res.txt"), jsonResult.toString().getBytes());
		
		// response.getWriter().write("{\"chapter\": " + chapter + "}");
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private JsonObject getChapterFromImageFile(String fileName) throws IOException {
		JsonObjectBuilder jsonResultBuilder = Json.createObjectBuilder();
		JsonObjectBuilder statsJson = Json.createObjectBuilder();
		String cleanImage = null;
		ChapterLocationResult chapterInfo = locateChapterFromImage(fileName);
		statsJson.add("OCR #1 duration", chapterInfo.ocrDuration);
		statsJson.add("Search #1 duration", chapterInfo.searchDuration);
		String chapter = chapterInfo.chapterId;
		if (chapter == null && imageCleaner != null && !imageCleaner.isEmpty()) {
			log.info("Chapter not found. Trying to clean image");
			// try to clean the image
			try {
				cleanImage = ocrTools.cleanImage(fileName, imageCleaner);
				chapterInfo = locateChapterFromImage(cleanImage);
				statsJson.add("OCR #2 duration", chapterInfo.ocrDuration);
				statsJson.add("Search #2 duration", chapterInfo.searchDuration);
				chapter = chapterInfo.chapterId;
				if (chapter != null) {
					log.info("Chapter found after cleaning");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (chapter == null) {
			jsonResultBuilder.addNull("chapter");
		} else {
			jsonResultBuilder.add("chapter", chapter);
		}
		jsonResultBuilder.add("stats", statsJson);
		jsonResultBuilder.add("OCR #1", new String(Files.readAllBytes(Paths.get(fileName + ".txt"))));
		if (cleanImage != null) {
			jsonResultBuilder.add("OCR #2", new String(Files.readAllBytes(Paths.get(cleanImage + ".txt"))));
		}
		log.info("Chapter is " + chapter);
		JsonObject jsonResult = jsonResultBuilder.build();
		return jsonResult;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private JsonObject getNumberFromImageFile(String fileName) throws IOException {
		JsonObjectBuilder jsonResultBuilder = Json.createObjectBuilder();
		JsonObjectBuilder statsJson = Json.createObjectBuilder();
		String cleanImage = null;
		NumberOcrResult ocrNumberResult = ocrNumberInImage(fileName);
		statsJson.add("OCR #1 duration", ocrNumberResult.ocrDuration);
		String ocrNumber = ocrNumberResult.ocrNumber;
		if (ocrNumber == null && imageCleaner != null && !imageCleaner.isEmpty()) {
			log.info("Number not found. Trying to clean image");
			// try to clean the image
			try {
				cleanImage = ocrTools.cleanImage(fileName, imageCleaner);
				ocrNumberResult = ocrNumberInImage(cleanImage);
				statsJson.add("OCR #2 duration", ocrNumberResult.ocrDuration);
				ocrNumber = ocrNumberResult.ocrNumber;
				if (ocrNumber != null) {
					log.info("Number found after cleaning");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (ocrNumber == null) {
			jsonResultBuilder.addNull("ocrNumber");
		} else {
			jsonResultBuilder.add("ocrNumber", ocrNumber);
		}
		jsonResultBuilder.add("stats", statsJson);
		jsonResultBuilder.add("OCR #1", new String(Files.readAllBytes(Paths.get(fileName + ".txt"))));
		if (cleanImage != null) {
			jsonResultBuilder.add("OCR #2", new String(Files.readAllBytes(Paths.get(cleanImage + ".txt"))));
		}
		log.info("Number is " + ocrNumber);
		JsonObject jsonResult = jsonResultBuilder.build();
		return jsonResult;
	}

	/**
	 * @param fileName
	 * @param
	 * @return
	 * @throws IOException
	 */
	protected NumberOcrResult ocrNumberInImage(String fileName) throws IOException {
		long ocrDuration = 0;
		try {
			Date start = new Date();
			ocrTools.runOCR(fileName, true);
			ocrDuration = new Date().getTime() - start.getTime();
		} catch (InterruptedException e) {
			// TODO return error
		}

		// Look for digits in the output file
		String textFileName = fileName + ".txt";
		BufferedReader reader = null;
		String ocrNumber = "";
		try {
			reader = new BufferedReader(new FileReader(textFileName));
			String line = null;
			// loop over the lines in the OCR file, and return the last appearance of digits
			while ((line = reader.readLine()) != null) {
				String digits = line.replaceAll("[^0-9]", "");
				if (! digits.isEmpty()) {
					ocrNumber = digits;
				}
			}			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
				
		return new NumberOcrResult(ocrNumber, ocrDuration);
	}


	/**
	 * @param fileName
	 * @param
	 * @return
	 * @throws IOException
	 */
	protected ChapterLocationResult locateChapterFromImage(String fileName) throws IOException {
		long ocrDuration = 0, searchDuration = 0;
		try {
			Date start = new Date();
			ocrTools.runOCR(fileName);
			ocrDuration = new Date().getTime() - start.getTime();
		} catch (InterruptedException e) {
			// TODO return error
		}

		// Run text search
		Date start = new Date();
		String chapter = locateChapterFromText(fileName + ".txt");
		searchDuration = new Date().getTime() - start.getTime();
		return new ChapterLocationResult(chapter, ocrDuration, searchDuration);
	}

	public String locateChapterFromText(String textFileName) {
		HashMap<String, Double> chapterScores = new HashMap<String, Double>();

		int numOfLines = 0;
		BufferedReader reader = null;
		try {
			// FileInputStream is = new FileInputStream(textFileName);
			// InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			reader = new BufferedReader(new FileReader(textFileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0) {
					continue;
				}
				String[] words = line.split("\\s+");
				if (words.length < 4) {
					continue;
				}
				List<String> chapters = getChapters(Arrays.copyOfRange(words, 1, 4));
				int resultPosition = 0;
				for (String chapter : chapters) {
					double addedScore = Math.pow(0.9, resultPosition);
					++resultPosition;
					
					Double score = chapterScores.get(chapter);
					if (score == null) {
						chapterScores.put(chapter, addedScore);
					} else {
						chapterScores.put(chapter, score + addedScore);
					}
				}

				// 10 lines is more than enough
				if (++numOfLines == 10) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}

		// now, iterate the chapters, and chose the one with the highest score
		if (chapterScores.isEmpty()) {
			return null;
		}

		Map.Entry<String, Double> maxChapter = null;

		for (Map.Entry<String, Double> entry : chapterScores.entrySet()) {
			if (maxChapter == null || entry.getValue().compareTo(maxChapter.getValue()) > 0) {
				maxChapter = entry;
			}
		}
		return maxChapter.getKey();
	}

	public List<String> getChapters(String[] words) {
		List<String> chapters = new ArrayList<String>();
		if (searcher == null) {
			return chapters;
		}

		if (words == null || words.length == 0) {
			return chapters;
		}

		// Run query on the words (fuzzy, adjacent).
		Query query = null;
		if (words.length == 1) {
			query = new FuzzyQuery(new Term("contents", words[0]));
		} else {
			SpanQuery[] clauses = new SpanQuery[words.length];
			for (int i = 0; i < words.length; i++) {
				clauses[i] = new SpanMultiTermQueryWrapper<MultiTermQuery>(
						new FuzzyQuery(new Term("contents", words[i])));
			}
			query = new SpanNearQuery(clauses, 0, true);
		}
		log.info("Searching for: " + query.toString("contents"));

		StringBuilder resultsString = new StringBuilder("\tChapters: ");
		try {
			// TODO parameterize
			TopDocs results = searcher.search(query, 20);
			ScoreDoc[] hits = results.scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("path");
				if (path != null && !path.isEmpty()) {
					// Get the base name. path separator can be UNIX or DOS
					// style (/ or \)
					String[] pathTokens = path.split("\\\\|/");
					if (pathTokens.length > 0) {
						String chapter = pathTokens[pathTokens.length - 1].split("\\.")[0];
						resultsString.append(chapter).append(", ");
						chapters.add(chapter);
					}
				}
			}
			log.info(resultsString.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return chapters;
	}

	private class ChapterLocationResult {
		String chapterId;
		long ocrDuration;
		long searchDuration;

		public ChapterLocationResult(String chapterId, long ocrDuration, long searchDuration) {
			super();
			this.chapterId = chapterId;
			this.ocrDuration = ocrDuration;
			this.searchDuration = searchDuration;
		}
	}

	private class NumberOcrResult {
		String ocrNumber;
		long ocrDuration;

		public NumberOcrResult(String ocrNumber, long ocrDuration) {
			this.ocrNumber = ocrNumber;
			this.ocrDuration = ocrDuration;
		}
	}

 }
