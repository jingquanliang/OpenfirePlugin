/*
 * System Abbrev ：
 * system Name  :
 * Component No  ：
 * Component Name：
 * File name     ：ChatLogsDbManager.java
 * Author        ：Peter.Qiu
 * Date          ：2016年12月12日
 * Description   :  <description>
 */

/* Updation record 1：
 * Updation date        :  2016年12月12日
 * Updator          :  Peter.Qiu
 * Trace No:  <Trace No>
 * Updation No:  <Updation No>
 * Updation Content:  <List all contents of updation and all methods updated.>
 */
package com.qiuzhping.openfire.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.jivesoftware.database.DbConnectionManager;

import com.qiuzhping.openfire.plugin.entity.ChatLogs;

public class ChatLogsDbManager {

	private static final ChatLogsDbManager CHAT_LOGS_MANAGER = new ChatLogsDbManager();

	private ChatLogsDbManager() {

	}

	public static ChatLogsDbManager getInstance() {
		return CHAT_LOGS_MANAGER;
	}

	private static final String LOGS_INSERT = "INSERT INTO ofChatLogs(messageId, sessionJID, sender, receiver, createDate, length, content, detail, state,message_type) VALUES(?,?,?,?,?,?,?,?,?,?)";



	/** <Description functions in a word>
	 * 添加聊天记录信息
	 * <Detail description>
	 * @author Peter.Qiu
	 * @param logs
	 * @return [Parameters description]
	 * @return boolean [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public boolean add(ChatLogs logs) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			if (logs == null) {
				return false;
			}
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(LOGS_INSERT);
			int i = 1;
			pstmt.setLong(i++, logs.getMessageId());
			pstmt.setString(i++, logs.getSessionJID());
			pstmt.setString(i++, logs.getSender());
			pstmt.setString(i++, logs.getReceiver());
			pstmt.setTimestamp(i++, logs.getCreateDate());
			pstmt.setInt(i++, logs.getLength());
			pstmt.setString(i++, logs.getContent());
			pstmt.setString(i++, logs.getDetail());
			pstmt.setInt(i++, logs.getState());
			pstmt.setString(i++, logs.getMessageType() != null ? logs.getMessageType() : "0");
			return pstmt.execute();
		} catch (Exception e) {
			System.out.println("添加聊天记录信息出错" + e);
			return false;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}
}
