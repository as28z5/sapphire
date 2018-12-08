package com.kmasashi.sapphire;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class App {

	public static void main(String[] args) {

		// 引数チェック
		checkArgs(args);

		// ブログ(引数1つ目)
		String blog = args[0];

		// テンプレート(引数2つ目)
		String template = args[1];

		// 年(引数3つ目から)
		List<String> years = new ArrayList<>();
		for (int i = 2; i < args.length; i++) {
			years.add(args[i]);
		}

		// アメブロ
		Ameblo ameblo;
		switch(template) {
		case "1":
			ameblo = new AmebloTemplete1(blog);
			break;

		case "2":
			ameblo = new AmebloTemplete2(blog);
			break;

		default:
			throw new IllegalArgumentException("テンプレート指定エラー");
		}

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
		if (3 > args.length) {
			throw new IllegalArgumentException("引数不正");
		}
	}

}
