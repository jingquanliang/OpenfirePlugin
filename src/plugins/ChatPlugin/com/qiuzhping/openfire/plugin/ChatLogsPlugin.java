package com.qiuzhping.openfire.plugin;

import java.io.File;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;

/**
 * <Description functions in a word>
 * 记录聊天信息插件
 * <Detail description>
 * 
 * @author Peter.Qiu
 * @version  [Version NO, 2016年12月13日]
 * @see  [Related classes/methods]
 * @since  [product/module version]
 */
public class ChatLogsPlugin implements  Plugin {
	
	private InterceptorManager interceptorManager = InterceptorManager
			.getInstance();
	private ChatLogsInterceptor chatLogsInterceptor;

	@Override
	public void destroyPlugin() {
		interceptorManager.removeInterceptor(chatLogsInterceptor);
		System.out.println("销毁聊天记录插件成功！");
	}

	@Override
	public void initializePlugin(PluginManager pluginManager,
			File pluginDirectory) {
		chatLogsInterceptor = new ChatLogsInterceptor();
		interceptorManager.addInterceptor(chatLogsInterceptor);
		System.out.println("安装聊天记录插件成功！");
	}
}
