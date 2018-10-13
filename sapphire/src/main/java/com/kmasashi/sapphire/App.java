package com.kmasashi.sapphire;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class App {

	public static void main(String[] args) {

		// 引数チェック
		checkArgs(args);

		// ブログ(引数一つ目)
		String blog = args[0];

		// 年(引数二つ目から)
		List<String> years = new ArrayList<>();
		for (int i = 1; i < args.length; i++) {
			years.add(args[i]);
		}

		// アメブロ
		Ameblo ameblo = new Ameblo(blog);

		// ブログタイトル
		String bolgTitle = ameblo.getBlogTitle();

		for (String year : years) {
			// 月ごとの記事カウントマップ
			Map<String, Integer> monthlyArticleCountMap = ameblo.getMonthlyArticleCountList(year);
	
			// // 月別記事一覧
			Map<String, List<String>> monthlyArticleList = ameblo.getMonthlyArticleList(monthlyArticleCountMap);
	
			// 月別記事一覧ループ
			List<Map<String, String>> articleList = ameblo.loopMonthlyArticleList(monthlyArticleList);
	
			String fileName = year + "_" + new Date().getTime() + ".pdf";
	
			// PDF出力
			new PDF(bolgTitle, year, fileName).out(articleList);
		}
	}

	/**
	 * 引数チェック
	 * @param args
	 */
	private static void checkArgs(String[] args) {
		if (2 > args.length) {
			throw new IllegalArgumentException("引数不正");
		}
	}

}
