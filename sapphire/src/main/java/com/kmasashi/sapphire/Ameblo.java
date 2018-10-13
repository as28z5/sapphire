package com.kmasashi.sapphire;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Ameblo {

	private final String BASE_URL = "https://ameblo.jp/";
//	private final String BLOG = "jj-haru";
//	private final String BLOG_URL = BASE_URL + BLOG + "/";
	private final String BLOG_URL;

	public Ameblo(String blog) {
		this.BLOG_URL = BASE_URL + blog + "/";
	}

	/**
	 * ブログタイトル取得
	 * @return
	 */
	public String getBlogTitle() {

		// Docuemnt オブジェクト
		Document document = getDocument(BLOG_URL);

		String result = document.select("title").text();
		System.out.println("Title:" + result);

		return result;
	}

	/**
	 * 月ごとの記事カウントリスト 取得
	 * @param year
	 * @return
	 */
	public Map<String, Integer> getMonthlyArticleCountList(String year) {

		// Docuemnt オブジェクト
		Document document = getDocument(BLOG_URL + "archive-" + year + "01.html");

		Elements listContentsArea = document.select(".listContentsArea");

		String[] listContentsAreaArray = listContentsArea.text().split(" ");

		Map<String, Integer> map = new TreeMap<>();

		for (String listContents : listContentsAreaArray) {

			if (listContents.matches("\\d+月\\(\\d+\\)")) {

				Pattern p = Pattern.compile("(\\d+)月\\((\\d+)\\)");
				Matcher m = p.matcher(listContents);
				if (m.find()){
					System.out.println(m.group(1) + " " + m.group(2));
					// 0 件以上を取り込む
					if (0 < Integer.valueOf(m.group(2))) {
						if (10 > Integer.valueOf(m.group(1))) {
							map.put(year + "0" + m.group(1), Integer.valueOf(m.group(2)));
						} else {
							map.put(year + m.group(1), Integer.valueOf(m.group(2)));
						}
						
					}
				}
			}
			
		}

		return map;
	}

	/**
	 * 月別記事一覧 取得
	 */
	public Map<String, List<String>> getMonthlyArticleList(Map<String, Integer> monthlyArticleCount) {

		Map<String, List<String>> map = new TreeMap<>();

		for(Map.Entry<String, Integer> entry : monthlyArticleCount.entrySet()) {

			// Docuemnt オブジェクト
			Document document = getDocument(BLOG_URL + "archive-" + entry.getKey() + ".html");

			Elements contentTitle = document.select(".contentTitle");
			System.out.println(contentTitle.html());
			
			Elements contentTitleArea = document.select(".contentTitleArea");
//			System.out.println(contentTitleArea.text());
			System.out.println(contentTitleArea.html());

			String[] contentTitleAreaArray = contentTitleArea.html().split("\\n");

			List<String> htmlList = new ArrayList<>();

			for (String contentTitleAreaHtml : contentTitleAreaArray) {
				System.out.println(contentTitleAreaHtml);
				htmlList.add(contentTitleAreaHtml);
			}

			map.put(entry.getKey(), htmlList);
		}

		return map;
	}

//	/**
//	 * 月別記事一覧ループ
//	 * @param monthlyArticleList
//	 */
//	public List<Map<String, String>> loopMonthlyArticleList(List<Map<String, List<String>>> monthlyArticleList) {
//
//		// 出力ファイル
//		File outFile = new File("text.txt");
//
//		// 出力ファイル削除
//		if (outFile.exists()) {
//			outFile.delete();
//		}
//
//		List<Map<String, String>> list = new ArrayList<>();
//
//		for (Map<String, List<String>> monthlyArticle : monthlyArticleList) {
//			for (Map.Entry<String, List<String>> map : monthlyArticle.entrySet()) {
//				System.out.println(map.getKey());
//				List<String> articleList = map.getValue();
//				Collections.reverse(articleList);
//
//				for (String article : articleList) {
//					// 記事取得
//					list.add(getArticle(article, outFile));
//				}
//			}
//		}
//
//		return list;
//	}

	/**
	 * 月別記事一覧ループ
	 * @param monthlyArticleList
	 */
	public List<Map<String, String>> loopMonthlyArticleList(Map<String, List<String>> monthlyArticleList) {

		// 出力ファイル
		File outFile = new File("text.txt");

		// 出力ファイル削除
		if (outFile.exists()) {
			outFile.delete();
		}

		List<Map<String, String>> list = new ArrayList<>();

		for (Map.Entry<String, List<String>> map : monthlyArticleList.entrySet()) {
			System.out.println(map.getKey());
			List<String> articleList = map.getValue();
			Collections.reverse(articleList);

			for (String article : articleList) {
				// 記事取得
				try {
					list.add(getArticle(article, outFile));
				} catch (ArticleException e) {
					continue;
				}
			}
		}

		return list;
	}

	/**
	 * Docuemnt オブジェクト取得
	 * @param url
	 * @return
	 */
	private Document getDocument(String url) {
		try {
			return Jsoup.connect(url).get();
		} catch (IOException e) {
			throw new RuntimeException(url, e);
		}
	}

	/**
	 * 記事取得
	 * @param article
	 * @param outFile
	 * @return
	 */
	private Map<String, String> getArticle(String article, File outFile) {

		Pattern p = Pattern.compile("<h1><a class=\"contentTitle\" href=\"/jj-haru/(.*\\.html)\">(.*)</a></h1>");
		Matcher m = p.matcher(article);

		if (!m.find()) {
			throw new ArticleException("記事取得失敗:" + article);
		}

		String url = m.group(1);
		String title = m.group(2);
		System.out.println(url + " " + title);

		// Document オブジェクト
		Document document = getDocument(BLOG_URL + url);

		String srticleTitle = document.select(".skinArticleTitle").text();
		System.out.println("Title:" + srticleTitle);

		String articleTime = document.select(".articleTime").text();
		System.out.println("Time:" + articleTime);

		String articleTheme = document.select(".articleTheme").text();
		System.out.println("Theme:" + articleTheme);

		String articleText = document.select(".articleText").text();
//			System.out.println(articleText.html());
		if (50 < articleText.length()) {
			System.out.println(articleText.substring(0, 50) + "...");
		} else {
			System.out.println(articleText);
		}

		// 出力
		outArticleText(outFile, title, articleTime, articleTheme, articleText);

		// Map作成
		Map<String, String> map = new HashMap<>();
		map.put("title", title);
		map.put("time", articleTime);
		map.put("theme", articleTheme);
		map.put("article", document.select(".articleText").html());

		return map;
	}

	/**
	 * テキスト出力
	 * @param file
	 * @param title
	 * @param time
	 * @param theme
	 * @param article
	 */
	private void outArticleText(File file, String title, String time, String theme, String article) {

		try (FileWriter filewriter = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(filewriter);
				PrintWriter pw = new PrintWriter(bw);) {

			pw.println("title:" + title);
			pw.println("time:" + time);
			pw.println("theme:" + theme);
			pw.println("article:" + article);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
