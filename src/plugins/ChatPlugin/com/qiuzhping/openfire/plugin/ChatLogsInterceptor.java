/*
* System Abbrev ：
* system Name  :
* Component No  ：
* Component Name：
* File name     ：ChatLogsInterceptor.java
* Author        ：Peter.Qiu
* Date          ：2016年12月14日
* Description   :  <description>
*/

/* Updation record 1：
 * Updation date        :  2016年12月14日
 * Updator          :  Peter.Qiu
 * Trace No:  <Trace No>
 * Updation No:  <Updation No>
 * Updation Content:  <List all contents of updation and all methods updated.>
 */
package com.qiuzhping.openfire.plugin;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.qiuzhping.openfire.plugin.entity.ChatLogs;
import com.qiuzhping.openfire.plugin.entity.ChatLogs.ChatLogsConstants;

/**
 * <Description functions in a word>
 * 
 * <Detail description>
 * 
 * @author Peter.Qiu
 * @version  [Version NO, 2016年12月14日]
 * @see  [Related classes/methods]
 * @since  [product/module version]
 */
public class ChatLogsInterceptor implements PacketInterceptor {

	public static final String MESSAGE_TYPE = "messageType";// 聊天记录类型
	public static final String BODY_CONTEXT = "bodyContext";// 聊天记录内容
	private ChatLogsDbManager logsManager = ChatLogsDbManager.getInstance();
	
	public ChatLogsInterceptor() {

	}
	/**
	 * 拦截消息核心方法，Packet就是拦截消息对象
	 * @param packet
	 * @param session
	 * @param incoming
	 * @param processed
	 * @throws PacketRejectedException
	 */
	@Override
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {
		if (session != null) {
			debug(packet, incoming, processed, session);
		}

		this.doAction(packet, incoming, processed, session);

	}

	/** <Description functions in a word>
	 * 执行保存/分析聊天记录动作
	 * <Detail description>
	 * @author Peter.Qiu
	 * @param packet
	 * @param incoming
	 * @param processed
	 * @param session [Parameters description]
	 * @return void [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	private void doAction(Packet packet, boolean incoming, boolean processed,
			Session session) {
		Packet copyPacket = packet.createCopy();
		if (packet instanceof Message && incoming==true && processed == false) {
			Message message = (Message) copyPacket;
			// 一对一聊天，单人模式
			if (message.getType() == Message.Type.chat) {
				logsManager.add(this.get(packet, incoming, session));
				// 群聊天，多人模式
			} else if (message.getType() == Message.Type.groupchat) {
				logsManager.add(this.get(packet, incoming, session));
				// 其他信息
			} else {				
				logsManager.add(this.get(packet, incoming, session));
			}
		} else if (packet instanceof IQ) {
			IQ iq = (IQ) copyPacket;
			if (iq.getType() == IQ.Type.set && iq.getChildElement() != null
					&& "session".equals(iq.getChildElement().getName())) {
				System.out.println("用户登录成功Start======================");
				System.out.println("用户登录成功:"+iq.toXML());
				System.out.println("用户登录成功End=======================");
			}
		} else if (packet instanceof Presence) {
			Presence presence = (Presence) copyPacket;
			if (presence.getType() == Presence.Type.unavailable) {
				System.out.println("用户退出服务器成功Start======================");
				System.out.println("用户退出服务器成功：" + presence.toXML());
				System.out.println("用户退出服务器成功End========================");
			}
		}
	}

	/** <Description functions in a word>
	 * 创建一个聊天记录实体对象，并设置相关数据
	 * <Detail description>
	 * @author Peter.Qiu
	 * @param packet		数据包
	 * @param incoming		如果为ture就表明是发送者
	 * @param session		当前用户session
	 * @return [Parameters description]
	 * @return ChatLogs [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	private ChatLogs get(Packet packet, boolean incoming, Session session) {
		//保留发送者信息.
		/*if (packet == null || !incoming) {
			return null;
		}*/
		Message message = (Message) packet;
		ChatLogs logs = new ChatLogs();
		JID jid = session.getAddress();
		logs.setSender(jid.getNode());
		JID recipient = message.getTo();
		logs.setReceiver(recipient.getNode());
		//自己发送的信息不用保存.
		if (logs.getSender() != null && logs.getReceiver() != null
				&& logs.getSender().equalsIgnoreCase(logs.getReceiver())) {
			return null;
		}
		logs.setContent(message.getBody());
		logs.setCreateDate(new Timestamp(new Date().getTime()));
		logs.setDetail(message.toXML());
		if(message.getBody()!=null && !message.getBody().equals(""))
			logs.setLength(message.getBody().length());
		else
			logs.setLength(0);
		logs.setState(0);
		logs.setSessionJID(jid.toString());
		// 生成主键id，利用序列生成器
		long messageID = SequenceManager.nextID(ChatLogsConstants.CHAT_LOGS);
		logs.setMessageId(messageID);
		//解析body.json
		//messageType: 聊天记录类型
		//bodyContext:聊天记录内容
		System.out.println("解析ParseBody聊天body Start=====================");
		System.out.println("message.getBody():" + message.getBody());
		Map<String, String> body = parseBody(message.getBody());
		if (body != null) {
			System.out.println("解析body.json");
			logs.setMessageType(body.get(MESSAGE_TYPE));
			logs.setContent(body.get(BODY_CONTEXT));
		}
		System.out.println("解析ParseBody聊天body End=====================");
		return logs;
	}

	/** <Description functions in a word>
	 * messageType: 聊天记录类型
	 * bodyContext: 聊天记录内容
	 * <Detail description>
	 * @author Peter.Qiu
	 * @param body
	 * @return [Parameters description]
	 * @return Map<String,String> [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	private Map<String, String> parseBody(String body) {
		if (body == null || body.length() < 10) {
			return null;
		}
		try {
			JSONObject json = JSONObject.fromObject(body);
			System.out.println("parseBody:"+ json);
			if (json != null) {
				Map<String, String> bodyMap = new HashMap<String, String>();
				String messageType = json.getString(MESSAGE_TYPE);
				if (messageType == null || Integer.parseInt(messageType) < 0) {
					return null;
				}
				bodyMap.put(MESSAGE_TYPE, json.getString(MESSAGE_TYPE));
				bodyMap.put(BODY_CONTEXT, json.getString(BODY_CONTEXT));
				return bodyMap;
			}
		} catch (Exception e) {
			System.out.println("parseBody转换JSON失败:"+ e);
			return null;
		}
		return null;
	}

	/** <Description functions in a word>
	 * 打印日志.
	 * <Detail description>
	 * @author Peter.Qiu
	 * @param packet
	 * @param incoming
	 * @param processed
	 * @param session [Parameters description]
	 * @return void [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	private void debug(Packet packet, boolean incoming, boolean processed,
			Session session) {
		long start = System.currentTimeMillis();
		System.out.println("###################debug start “"+start+"” ###################");
		String info = "[ packetID: " + packet.getID() + ", to: "
				+ packet.getTo() + ", from: " + packet.getFrom()
				+ ", incoming: " + incoming + ", processed: " + processed
				+ " ]";
		System.out.println("id:" + session.getStreamID() + ", address: "
				+ session.getAddress());
		System.out.println("info: " + info);
		System.out.println("xml: " + packet.toXML());
		long end = System.currentTimeMillis();
		System.out.println("###################debug end “"+end+"” #####################");
	}


	/** <Description functions in a word>
	 * 
	 * <Detail description>
	 * @author Peter.Qiu
	 * @param args [Parameters description]
	 * @return void [Return type description]
	 * @exception throws [Exception] [Exception description]
	 * @see [Related classes#Related methods#Related properties]
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
