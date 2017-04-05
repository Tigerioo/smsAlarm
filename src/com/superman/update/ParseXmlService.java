package com.superman.update;

import java.io.InputStream;
import java.util.HashMap;

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

	public HashMap<String, String> parseXml(InputStream inStream)
			throws Exception {
		HashMap<String, String> hashMap = new HashMap<String, String>();

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
			// 遍历子节点
			Node childNode = (Node) childNodes.item(j);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) childNode;
				Node node = childElement.getFirstChild();
				// 版本号
				if ("version".equals(childElement.getNodeName())) {
					hashMap.put("version", node == null ? "" : node.getNodeValue());
				}
				// 软件名称
				else if (("name".equals(childElement.getNodeName()))) {
					hashMap.put("name", node == null ? "" : node.getNodeValue());
				}
				// 下载地址
				else if (("url".equals(childElement.getNodeName()))) {
					hashMap.put("url", node == null ? "" : node.getNodeValue());
				}
				//更新说明
				else if (("content".equals(childElement.getNodeName()))) {
					hashMap.put("content", node == null ? "" : node.getNodeValue());
				}
			}
		}
		return hashMap;
	}
}
