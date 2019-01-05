package com.kmasashi.sapphire;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class AmebloTemplete1 extends Ameblo {

	public AmebloTemplete1(String blog) {
		super(blog);
	}

	/**
	 * 記事取得
	 * @param article
	 * @param outFile
	 * @return
	 */
	@Override
	protected Map<String, String> getArticle(String article, File outFile) {

		Pattern p = Pattern.compile("<h1><a class=\"contentTitle\" href=\"/" + this.BLOG + "/(.*\\.html)\">(.*)</a></h1>");
		Matcher m = p.matcher(article);

		if (!m.find()) {
			throw new ArticleException("記事取得失敗:" + article);
		}

		String url = m.group(1);
		String title = m.group(2);
		System.out.println(url + " " + title);

		// Document オブジェクト
		Document document = getDocument(BLOG_URL + url);

		String articleTitle = document.select(".skinArticleTitle").text();
		System.out.println("Title:" + articleTitle);

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

		String articleHtml = document.select(".articleText").html();
		// &nbsp; を改行
		articleHtml = articleHtml.replace("&nbsp;", "<br>");

		// 出力
		outArticleText(outFile, articleTitle, articleTime, articleTheme, articleText);

		// Map作成
		Map<String, String> map = new HashMap<>();
		map.put("title", articleTitle);
		map.put("time", articleTime);
		map.put("theme", articleTheme);
		map.put("article", articleHtml);

		return map;
	}

	/**
	 * ブログタイトル取得
	 * @return
	 */
	@Override
	protected String getBlogTitle() {

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
	@Override
	protected Map<String, Integer> getMonthlyArticleCountList(String year) {

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
	@Override
	protected Map<String, List<String>> getMonthlyArticleList(Map<String, Integer> monthlyArticleCount) {

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
}
