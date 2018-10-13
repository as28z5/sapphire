package com.kmasashi.sapphire;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

public class PDF {

	/** ブログ名 */
	private final String blogName;
	/** 年 */
	private final String year;
	/** ファイル名 */
	private final String filename;

	/**
	 * コンストラクタ
	 * @param blogName
	 * @param year
	 * @param filename
	 */
	public PDF(String blogName, String year, String filename) {
		super();
		this.blogName = blogName;
		this.year = year;
		this.filename = filename;
	}

	/**
	 * 出力
	 * @param articleList
	 */
	public void out(List<Map<String, String>> articleList) {

		try (
				//出力先(アウトプットストリーム)の生成
				FileOutputStream fos = new FileOutputStream(filename);
				) {

			//文書オブジェクトを生成
//			Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
			Document doc = new Document(PageSize.A4);

			//アウトプットストリームをPDFWriterに設定
			PdfWriter pdfwriter = PdfWriter.getInstance(doc, fos);

			// フッター
			PDFHearderFooter headerFooter = new PDFHearderFooter();
			pdfwriter.setPageEvent(headerFooter);

			//フォントの設定
			Font font1 = new Font(BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H",BaseFont.NOT_EMBEDDED), 12, Font.BOLD);
			Font font2 = new Font(BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H",BaseFont.NOT_EMBEDDED), 24, Font.BOLD);

			//文章オブジェクト オープン
			doc.open();

			// 表示作成
			makeOpeningPage(doc, font2);

			// アンダーライン
			DottedLineSeparator separator = new DottedLineSeparator();
			separator.setPercentage(59500f / 523f);
			Chunk linebreak = new Chunk(separator);

			//PDF文章に文字列を追加
			if (null != articleList) {
				for (Map<String, String> articleMap: articleList) {
					out(doc, font1, articleMap);
	
					doc.add(linebreak);
				}
			}


			//文章オブジェクト クローズ
			doc.close();

			//PDFWriter クローズ
			pdfwriter.close();

		} catch (IOException | DocumentException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 表示作成
	 * @param doc
	 * @param font
	 * @throws DocumentException 
	 */
	private void makeOpeningPage(Document doc, Font font) throws DocumentException {

		doc.add(new Paragraph(blogName, font));
		doc.add(new Paragraph(year + "年", font));
		doc.newPage();
	}

	/**
	 * 出力
	 * @param doc
	 * @param font
	 * @param articleMap
	 * @throws DocumentException
	 */
	private void out(Document doc, Font font, Map<String, String> articleMap) throws DocumentException {

		doc.add(new Paragraph(articleMap.get("title"), font));
		doc.add(new Paragraph(articleMap.get("time") + " " + articleMap.get("theme"), font));

		String article = articleMap.get("article");
		// 改行コード削除
		String result = article.replace("\n\r", "").replace("\n", "").replace("\r", "");

		// タグの開始
		boolean tagStart = false;
		// ラグ読み込み
		boolean tagLoad = false;
		// タグ
		StringBuilder tag = null;
		// テキスト
		StringBuilder text = new StringBuilder();

		Paragraph paragraph = new Paragraph("", font);

		for(int i = 0; i < result.length(); i++) {
			char str = result.charAt(i);

			// タグの開始
			if ('<' == str) {
				tagStart = true;
			}

			// タグの終了
			if ('>' == str) {
				tagLoad = false;

				// タグの完成版
				String htmlTag = tag.append(str).toString();

				// タグリセット
				tag = null;

				if (null != text) {
					// テキスト出力
					paragraph.add(text.toString());
					// テキストをリセット
					text = null;
				}

				// タグの評価
				if (htmlTag.startsWith("<font ") || "</font>".equals(htmlTag)) {
					continue;
				}
				else if (htmlTag.startsWith("<span ") || "</span>".equals(htmlTag)) {
					continue;
				}
				// イメージ
				else if (htmlTag.startsWith("<img ")) {
					// src を取得
					int sPoint = htmlTag.indexOf(" src=\"");
					int ePoint = htmlTag.indexOf("\"", sPoint+6);
					// イメージURL
					String imageUrl = htmlTag.substring(sPoint + 6, ePoint);
					System.out.println("ImageURL:" + imageUrl);
					try {
						Image image = Image.getInstance(new URL(imageUrl));
						paragraph.add(new Chunk(image, 0, 0, true));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} 
				// 改行
				else if (htmlTag.startsWith("<br ") || "<br>".equals(htmlTag)) {
					paragraph.add("\n");

				}
				// リンク
				else if (htmlTag.contains(" href=\"")) {
					int sPoint = htmlTag.indexOf(" href=\"");
					int ePoint = htmlTag.indexOf("\"", sPoint+7);
					// リンクURL
					String linkUrl = htmlTag.substring(sPoint + 7, ePoint);
					paragraph.add(linkUrl);
				}
				else {
//					paragraph.add(htmlTag);
				}

				continue;
			}

			// タグ読込み開始
			if (tagStart) {
				tagLoad = true;
				tag = new StringBuilder();
				tagStart = false;
			}

			// タグ読込み中
			if (tagLoad) {
				tag.append(str);
				continue;
			}

			if (null == text) {
				text = new StringBuilder();
			}

			//テキスト
			text.append(str);
		}

		// 最後がテキストの場合
		if (null != text) {
			paragraph.add(text.toString());
		}
		
		doc.add(paragraph);
	}
}

