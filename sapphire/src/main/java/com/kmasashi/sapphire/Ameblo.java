package com.kmasashi.sapphire;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Ameblo {

	private final String BASE_URL = "https://ameblo.jp/";
	protected final String BLOG;
	protected final String BLOG_URL;

	public Ameblo(String blog) {
		this.BLOG = blog;
		this.BLOG_URL = BASE_URL + blog + "/";
	}

	/**
	 * 記事取得
	 * @param article
	 * @param outFile
	 * @return
	 */
	protected abstract Map<String, String> getArticle(String article, File outFile);

	/**
	 * ブログタイトル取得
	 * @return
	 */
	protected abstract String getBlogTitle();

	/**
	 * Docuemnt オブジェクト取得
	 * @param url
	 * @return
	 */
	protected Document getDocument(String url) {
		try {
			return Jsoup.connect(url).get();
		} catch (IOException e) {
			throw new RuntimeException(url, e);
		}
	}

	/**
	 * 月ごとの記事カウントリスト 取得
	 * @param year
	 * @return
	 */
	protected abstract Map<String, Integer> getMonthlyArticleCountList(String year);

	/**
	 * 月別記事一覧 取得
	 */
	protected abstract Map<String, List<String>> getMonthlyArticleList(Map<String, Integer> monthlyArticleCount);

	/**
	 * 月別記事一覧ループ
	 * @param monthlyArticleList
	 * @param fileName 
	 */
	protected List<Map<String, String>> loopMonthlyArticleList(Map<String, List<String>> monthlyArticleList, String fileName) {

		// 出力ファイル
		File outFile = new File(fileName);

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
	 * テキスト出力
	 * @param file
	 * @param title
	 * @param time
	 * @param theme
	 * @param article
	 */
	protected void outArticleText(File file, String title, String time, String theme, String article) {

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
