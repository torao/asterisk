/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package io.asterisque;

import java.util.Optional;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Close
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * パイプのクローズを示すメッセージです。
 * {@code result} もしくは {@code abort} のどちらかが有効な値を持ちます。
 *
 * @author Takami Torao
 */
public final class Close extends Message {

	// ==============================================================================================
	// 結果
	// ==============================================================================================
	/**
	 * 正常に終了した結果です。
	 */
	public final Optional<Object> result;

	// ==============================================================================================
	// 中断
	// ==============================================================================================
	/**
	 * 中断によって処理が終了したことを示す値です。
	 */
	public final Optional<Abort> abort;

	// ==============================================================================================
	// コンストラクタ
	// ==============================================================================================
	/**
	 * Open メッセージを構築します。
	 */
	public Close(short pipeId, Object result){
		super(pipeId);
		this.result = Optional.of(result);
		this.abort = Optional.empty();
	}

	// ==============================================================================================
	// コンストラクタ
	// ==============================================================================================
	/**
	 * Open メッセージを構築します。
	 */
	public Close(short pipeId, Abort abort){
		super(pipeId);
		this.result = Optional.empty();
		this.abort = Optional.of(abort);
		assert(abort != null);
	}

	// ==============================================================================================
	// インスタンスの文字列化
	// ==============================================================================================
	/**
	 * このインスタンスを文字列化します。
	 */
	@Override
	public String toString(){
		if(result.isPresent()){
			return "Close(" + pipeId + "," + Debug.toString(result.get()) + ")";
		} else {
			return "Close(" + pipeId + "," + abort.get() + ")";
		}
	}

	// ==============================================================================================
	// 例外による終了
	// ==============================================================================================
	/**
	 * 予期しない状態によってパイプを終了するときのクローズメッセージを作成します。
	 */
	public static Close unexpectedError(short pipeId, String msg){
		return new Close(pipeId, new Abort(Abort.Unexpected, msg));
	}

}
