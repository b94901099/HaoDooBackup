package getAllBookNameAuthorUrl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Martial {

	public static void main(String[] args) {
		Martial martial = new Martial();
		martial.run();
	}

	private void run() {
		List<Book> allBooks = new ArrayList<>(); 
		try {
			for (int i = 1; i <= 10; i++) {
				allBooks.addAll(sendGet(i));
			}
			File file = new File("/Users/chen/Documents/haodoo_book_list/martial.tsv");
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (Book book: allBooks) {
				printWriter.println(book);
			}
			printWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final String USER_AGENT = "Mozilla/5.0";

	private List<Book> sendGet(int pageIndex) throws Exception {
		String url = "http://www.haodoo.net/?M=hd&P=martial-" + pageIndex;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		String currentAuthor = "";
		boolean readContent = false;
		List<Book> books = new ArrayList<>();
		
		Pattern patternAuthor = Pattern.compile("<font color=\"CC0000\">([\u4E00-\u9FA5]+)</font>");
		Pattern patternUrl = Pattern.compile("href=\"(.+)\"");
		Pattern patternBookname = Pattern.compile("[【《]([\u4E00-\u9FA5．：]+)[】》]");
		Pattern patternComment = Pattern.compile("</a>([\u4E00-\u9FA5 0-9a-zA-Z/\\(\\)、,]+)<br>");
		
		Book currentBook = null;
		
		while ((line = in.readLine()) != null) {
			if (line.equals("<div class=\"a03\">")) {
				readContent = true;
			} else if (readContent && line.equals("</div>")) {
				readContent = false;
			} else if (readContent) {
				Matcher matcher = patternAuthor.matcher(line);
				if (matcher.find()) {
					currentAuthor = matcher.group(1);
				}
				matcher = patternUrl.matcher(line);
				if (matcher.find()) {
					if (currentBook == null) {
						currentBook = new Book();
					}
					currentBook.url = matcher.group(1);
				}
				matcher = patternBookname.matcher(line);
				if (matcher.find()) {
					if (currentBook == null) {
						currentBook = new Book();
					}
					currentBook.name = matcher.group(1);
				}
				matcher = patternComment.matcher(line);
				if (matcher.find()) {
					if (currentBook == null) {
						currentBook = new Book();
					}
					currentBook.comment = matcher.group(1);
				}
				
				if (currentBook == null) {
					System.out.println("No book created for line: " + line);
				}
				
				if (currentBook != null) {
					currentBook.author = currentAuthor;
					books.add(currentBook);
					currentBook = null;
				}
			}
		}

		in.close();
		return books;
	}

	class Book {
		String author;
		String name;
		String url;
		String comment;
		
		@Override
		public String toString() {
			return author + "\t" + name + "\t" + url + "\t" + comment;
		}
	}
}