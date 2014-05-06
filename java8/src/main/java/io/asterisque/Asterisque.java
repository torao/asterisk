/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package io.asterisque;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Asterisque
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import org.slf4j.Logger;

import javax.net.ssl.SSLSession;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.concurrent.ThreadFactory;

/**
 * @author Takami Torao
 */
public final class Asterisque {

	// ==============================================================================================
	// コンストラクタ
	// ==============================================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 */
	private Asterisque() { }

	// ==============================================================================================
	// ID
	// ==============================================================================================
	/**
	 * ディレクトリ名や URI の一部に使用できる asterisque の識別子です。
	 */
	public static final String ID = "asterisque";


	// ==============================================================================================
	// UTF-8
	// ==============================================================================================
	/**
	 * UTF-8 を表す文字セットです。
	 */
	public static final Charset UTF8 = Charset.forName("UTF-8");

	// ==============================================================================================
	// スレッドグループ
	// ==============================================================================================
	/**
	 * asterisque が使用するスレッドの所属するグループです。
	 */
	public static final ThreadGroup threadGroup = new ThreadGroup(ID);

	// ==============================================================================================
	// スレッドファクトリの参照
	// ==============================================================================================
	/**
	 * 指定されたロールのためのスレッドファクトリを参照します。
	 */
	public static Thread newThread(String role, Runnable r){
		return new Thread(threadGroup, r, ID + "." + role);
	}

	// ==============================================================================================
	// スレッドファクトリの参照
	// ==============================================================================================
	/**
	 * 指定されたロールのためのスレッドファクトリを参照します。
	 */
	public static ThreadFactory newThreadFactory(String role){
		return r -> newThread(role, r);
	}

}
