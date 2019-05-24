package gniza;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

import com.sun.net.httpserver.*;

import gniza.beans.*;
import gniza.logic.*;

public class GennizaServer {

	private static GennizaServer instance = null;

	private static Logger logger =
			Logger.getLogger(GennizaServer.class.getPackage().getName());
	
	private static final int PORT = 9999;

	private static final int slop = 2;

	private static final int maxEdits = 2;

	private static final int groupWordLength = 5;

	private TextSearcher searcher;

	private WordsSearcher wordsSearcher;
	
	public static void main(String[] args) {
		GennizaServer.instance().run();
	}

	public static GennizaServer instance() {
		if (instance != null) {
			return instance;
		}
		synchronized (logger) {
			if (instance != null) {
				return instance;
			}
			instance = new GennizaServer();
			return instance();
		}
	}

	public GennizaServer() {
        //wordsSearcher = new FuzzySearch(slop, maxEdits);
        //searcher = new TextSearcherImpl(wordsSearcher, groupWordLength);
	}

	/**
	 * 
	 */
	private void run() {
		logger.info("Starting HTTP server on port :" + PORT);
		HttpServer server = null;
		try {
			server = HttpServer.create(new InetSocketAddress(PORT), 0);
		} catch (IOException e) {
			logger.warning("Error starting HTTP Server: " + e);
			return;
		}
		server.createContext("/identify", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();		
		logger.info("Successfully started HTTP server on port :" + PORT);
	}

	class MyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "Ok";
			
			// disable cache
			t.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
			t.getResponseHeaders().set("Pragma", "no-cache");
			t.getResponseHeaders().set("Expires", "0");
			
			t.getResponseHeaders().add(
					"Access-Control-Allow-Origin", "*");

			if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
				// Send permissive CORS header if this is OPTIONS request
				t.getResponseHeaders().add(
						"Access-Control-Allow-Methods", "GET, OPTIONS");
				t.getResponseHeaders().add(
						"Access-Control-Allow-Headers",
						"Content-Type,Authorization");
				// return response with empty body
				t.sendResponseHeaders(204, -1);
				return;
			}
			

			InputStream is = t.getRequestBody();
			java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
			String inputString = scanner.hasNext() ? scanner.next() : "";

			// else - not OPTIONS request				
			t.sendResponseHeaders(200, response.length());				

			OutputStream os = t.getResponseBody();
			OutputStreamWriter responseWriter = new OutputStreamWriter(os, "utf-8");
			List<SearchResult> result = null;
			
			try {
				wordsSearcher = new FuzzySearch(slop, maxEdits, "index");
		        searcher = new TextSearcherImpl(wordsSearcher, groupWordLength);
				result = searcher.Search(inputString);
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
	        for (SearchResult searchResult : result) {
	            ReferenceDetail referenceDetail = searchResult.getReferenceDetail();
	            responseWriter.write("\t"+referenceDetail.getName() + "=>" + referenceDetail.getTypeBook() + " : " + searchResult.getPropability());
	            //System.out.println("\t"+referenceDetail.getName() + "=>" + referenceDetail.getTypeBook() + " : " + searchResult.getPropability());
	        }
					
	        responseWriter.close();
		}
	}

}
