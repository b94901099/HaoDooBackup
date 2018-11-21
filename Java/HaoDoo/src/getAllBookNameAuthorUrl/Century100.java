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

public class Century100 {

	public static void main(String[] args) {
		Century100 c = new Century100();
		List<Book> allBooks = new ArrayList<>();
		try {
			for (int i = 1; i <= 5 ; i++) {
				allBooks.addAll(c.sendGet(i));
			}
			
			System.out.println(allBooks.size());
			
			File file = new File("/Users/chen/Documents/haodoo_book_list/century100.tsv");
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

	public List<Book> sendGet(int pageIndex) throws Exception {
		URL url = new URL("http://www.haodoo.net/?M=hd&P=100-" + pageIndex);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		int status = con.getResponseCode();
		if (status == 200) {
			Pattern patternAuthor = Pattern.compile("<font color=\"CC0000\">\\d*\\.? ?([\u4E00-\u9FA5]+)</font>");
			Pattern patternBookname = Pattern.compile("[【《]([\\u4E00-\\u9FA5]+)[】》]");
			Pattern patternUrl = Pattern.compile("href=\"(.+)\"");
			Pattern patternComment = Pattern.compile("</a>([\u4E00-\u9FA5\\(\\)/0-9a-zA-Z. 、]+)<br>");
			boolean readContent = false;
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			List<Book> books = new ArrayList<>();
			
			while ((line = in.readLine()) != null) {
				if (line.equals("<div class=\"a03\">")) {
					readContent = true;
				} else if (line.equals("</div>")) {
					readContent = false;
				} else if (readContent && line.length() > 0) {
					Book book = new Book();
					Matcher matcher = patternAuthor.matcher(line);
					if (matcher.find()) {
						book.author = matcher.group(1);
					}
					matcher = patternBookname.matcher(line);
					if (matcher.find()) {
						book.name = matcher.group(1);
					}
					matcher = patternUrl.matcher(line);
					if (matcher.find()) {
						book.url = matcher.group(1);
					}
					matcher = patternComment.matcher(line);
					if (matcher.find()) {
						book.comment = matcher.group(1);
					}
					if (book.isCompleted()) {
						books.add(book);
					} else if (book.mightBeBook()){
						System.out.println("Incompleted book: " + book + " ; line: " + line);
					}
				}
			}
			in.close();
			
			return books;
		}
		return null;
	}

	class Book {
		String name;
		String author;
		String url;
		String comment;
		@Override
		public String toString() {
			return author + "\t" + name + "\t" + url + "\t" + comment;
		}
		
		public boolean isCompleted() {
			return name != null & author != null && url != null && comment != null;
		} 
		
		public boolean mightBeBook() {
			int count = 0;
			if (name == null) count++;
			if (author == null) count++;
			if (url == null) count++;
			if (comment == null) count++;
			return count < 2;
		}
	}
}
