package com.superman.smsalarm.backup;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * <p>
 * Title: com.superman.update.ParseXmlService.java
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2001-2013 Newland SoftWare Company
 * </p>
 * 
 * <p>
 * Company: Newland SoftWare Company
 * </p>
 * 
 * @author Lewis.Lynn
 * 
 * @version 1.0 CreateTime：2014-3-17 下午3:08:31
 */
public class ParseXmlService {
	
	public List<Map<String, String>> parseXml(InputStream inStream)
			throws Exception {
		List<Map<String, String>> reList = new ArrayList<Map<String, String>>();

		// 实例化一个文档构建器工厂
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// 通过文档构建器工厂获取一个文档构建器
		DocumentBuilder builder = factory.newDocumentBuilder();
		// 通过文档通过文档构建器构建一个文档实例
		Document document = builder.parse(inStream);
		// 获取XML文件根节点
		Element root = document.getDocumentElement();
		// 获得所有子节点
		NodeList childNodes = root.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			Map<String, String> map = new HashMap<String, String>();
			// 遍历子节点
			Node childNode = (Node) childNodes.item(j);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) childNode;// rcd
				
				NodeList rcdNodeList = childElement.getChildNodes();
				for (int i = 0; i < rcdNodeList.getLength(); i++) {
					Node rcdNode = rcdNodeList.item(i);
					map.put(rcdNode.getNodeName(), rcdNode.getFirstChild() == null ? "" : rcdNode.getFirstChild().getNodeValue());
				}
			}
			reList.add(map);
		}
		return reList;
	}
}
