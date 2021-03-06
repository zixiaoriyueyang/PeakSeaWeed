package com.peakcentre.web.ajax;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.peakcentre.web.dao.TestResultTemplateDao;
import com.peakcentre.web.dao.UserinfoDao;
import com.peakcentre.web.entity.Userinfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
/**
 * Servlet implementation class AjaxGetTestResultTemplate
 */
@WebServlet("/jsp/AjaxGetTestResultTemplate")
public class AjaxGetTestResultTemplate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AjaxGetTestResultTemplate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(request.getSession(false) != null && request.getSession(false).getAttribute("id") != null) {
			response.sendRedirect("dashboard.jsp");
		} else {
			response.sendRedirect(request.getContextPath() + "/index.jsp");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		UserinfoDao uidao = new UserinfoDao();
		TestResultTemplateDao tdao = new TestResultTemplateDao();
		Userinfo ui = new Userinfo();
		ArrayList<Userinfo> list = new ArrayList<Userinfo>();
		Locale locale = (Locale) request.getSession(true)
				.getAttribute("locale");
		ResourceBundle resb = ResourceBundle.getBundle("peakcentre", locale);

		String fname = request.getParameter("fname");
		String lname = request.getParameter("lname");
		int i = Integer.parseInt(request.getParameter("userlist"));

		list = uidao.getUserinfo(fname, lname);
		ui = list.get(i);

		String username = ui.getUsername();
		String templateName = request.getParameter("templateName");
		int tempId = tdao.getTempIdByName(templateName);
		String date = request.getParameter("date");
		String tempPath = tdao.getTempPathByName(templateName);

		//read template xml file
		File f = new File("/Users/sunmingyang/Documents/temp/" + tempPath);
		String html = "<input type='hidden' name='username' value='" + username
				+ "'>";
		html += "<input type='hidden' name='tempId' value='" + tempId + "'>";
		html += "<input type='hidden' name='templateName' value='"
				+ templateName + "'>";
		html += "<input type='hidden' name='date' value='" + date + "'>";

		Element element = null;
		int graphNumber = 1;
		int tableSequence = 1;

		DocumentBuilder db = null;
		DocumentBuilderFactory dbf = null;

		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();

			Document dt = db.parse(f);
			element = dt.getDocumentElement();

			NodeList childNodes = element.getChildNodes();
			for (int n = 0; n < childNodes.getLength(); n++) {
				Node node1 = childNodes.item(n);
				//print textarea
				if ("textarea".equals(node1.getNodeName())) {
					String textarea = node1.getTextContent();
					html += "<textarea readonly class='display' rows='6'>";
					html += textarea;
					html += "</textarea>";
					html += "<br><br>";
				//print table
				} else if ("table".equals(node1.getNodeName())) {
					int row = 0;
					int column = 0;
					int numberInTotal = 0;
					String tableName = "tableName";
					ArrayList<String> th = new ArrayList<String>();

					NodeList nodeDetail = node1.getChildNodes();
					for (int j = 0; j < nodeDetail.getLength(); j++) {
						Node detail = nodeDetail.item(j);
						if ("row".equals(detail.getNodeName())) {
							row = Integer.parseInt(detail.getTextContent());
						} else if ("column".equals(detail.getNodeName())) {
							column = Integer.parseInt(detail.getTextContent());
						} else if ("numberInTotal".equals(detail.getNodeName())) {
							numberInTotal = Integer.parseInt(detail
									.getTextContent());
						} else if ("th".equals(detail.getNodeName())) {
							th.add(detail.getTextContent());
						} else if ("tableName".equals(detail.getNodeName())) {
							tableName = detail.getTextContent();
						}
					}
					HttpSession session = request.getSession(true);
					session.setAttribute("ths" + tableSequence, th);
					html += "<ul class=\"table-toolbar\">";
					html += "<li>" + tableName + "</li>";
					html += "<li class='addarow' style='float:right' ><a><img src='../image/icons/basic/plus.png' />Add Row</a></li></ul>";
					html += "<table class='display' id='myDataTable"
							+ numberInTotal + "'>";
					html += "<thead>" + "<tr><th></th>";
					for (int c = 0; c < column; c++) {
						html += "<th>" + th.get(c) + "</th>";

					}
					html += "</tr>" + "</thead>" + "<tbody>";
					for (int r = 0; r < row; r++) {
						html += "<tr>"+"<td width=\"5%\"><button class=\"removebutton\"><img src=\"../image/icons/basic/delete.png\" /></button></td>";
						for (int c = 0; c < column; c++) {
							html += "<td><input required size='8' type='text' id='"
									+ th.get(c)
									+ "_"
									+ r
									+ "' name='table"
									+ tableSequence
									+ "row"
									+ r
									+ "column"
									+ c
									+ "'></td>";
						}
						html += "</tr>";
					}
					html += "</tbody>";
					html += "</table>";
					html += "<input type='hidden' name='table" + tableSequence
							+ "Row' value='" + row + "'>";
					html += "<input type='hidden' name='table" + tableSequence
							+ "Column' value='" + column + "'>";

					html += "<br><br>";
					tableSequence++;
				//print graph
				} else if ("img".equals(node1.getNodeName())) {
					int tableNumber = 1;
					String imgHTML = "";
					String graphName = "graphName";
					
					String X = "";
					String Y = "";

					NodeList nodeDetail = node1.getChildNodes();

					for (int j = 0; j < nodeDetail.getLength(); j++) {
						Node detail = nodeDetail.item(j);
						if ("col1".equals(detail.getNodeName())) {
							imgHTML += "<input type='hidden' id='col1_"
									+ tableNumber + "' value='"
									+ detail.getTextContent() + "'>";
							X = detail.getTextContent();
						} else if ("col2".equals(detail.getNodeName())) {
							imgHTML += "<input type='hidden' id='col2_"
									+ tableNumber + "' value='"
									+ detail.getTextContent() + "'>";
							Y = detail.getTextContent();
						} else if ("tableNumber".equals(detail.getNodeName())) {
							tableNumber = Integer.parseInt(detail
									.getTextContent());
						} else if ("graphName".equals(detail.getNodeName())) {
							graphName = detail.getTextContent();
						}
					}
					html += "<p>" + graphName + "</p>";
					html += "<br>";
					html += "<div id='flot-lines" + tableNumber + "'>";
					html += "</div>";
					html += imgHTML;
					html += "<input type='button' id='generateButton"
							+ tableNumber
							+ "' class='small button green' value='generate graph' onclick='generateGraph("
							+ tableNumber + ",\"" +X +"\", \"" + Y +"\" )'/>";
					html += "<br><br>";
					graphNumber++;
				}

			}
			html += "<input type='hidden' name='totalTable' value='"
					+ (tableSequence - 1) + "'>";

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		//send html code to the front end
		String htmlJson = new Gson().toJson(html); 
		response.setContentType("application/json"); 
		response.setCharacterEncoding("utf-8"); 
		response.getWriter().write(htmlJson);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
/*================WithOut Add Row And Remove Function==========================
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		UserinfoDao uidao = new UserinfoDao();
		TestResultTemplateDao tdao = new TestResultTemplateDao();
		Userinfo ui = new Userinfo();
		ArrayList<Userinfo> list = new ArrayList<Userinfo>();
		Locale locale = (Locale) request.getSession(true)
				.getAttribute("locale");
		ResourceBundle resb = ResourceBundle.getBundle("peakcentre", locale);

		String fname = request.getParameter("fname");
		String lname = request.getParameter("lname");
		int i = Integer.parseInt(request.getParameter("userlist"));

		list = uidao.getUserinfo(fname, lname);
		ui = list.get(i);

		String username = ui.getUsername();
		String templateName = request.getParameter("templateName");
		int tempId = tdao.getTempIdByName(templateName);
		String date = request.getParameter("date");
		String tempPath = tdao.getTempPathByName(templateName);

		//read template xml file
		File f = new File("/Users/sunmingyang/Documents/temp/" + tempPath);
		String html = "<input type='hidden' name='username' value='" + username
				+ "'>";
		html += "<input type='hidden' name='tempId' value='" + tempId + "'>";
		html += "<input type='hidden' name='templateName' value='"
				+ templateName + "'>";
		html += "<input type='hidden' name='date' value='" + date + "'>";

		Element element = null;
		int graphNumber = 1;
		int tableSequence = 1;

		DocumentBuilder db = null;
		DocumentBuilderFactory dbf = null;

		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();

			Document dt = db.parse(f);
			element = dt.getDocumentElement();

			NodeList childNodes = element.getChildNodes();
			for (int n = 0; n < childNodes.getLength(); n++) {
				Node node1 = childNodes.item(n);
				//print textarea
				if ("textarea".equals(node1.getNodeName())) {
					String textarea = node1.getTextContent();
					html += "<textarea readonly class='display' rows='6'>";
					html += textarea;
					html += "</textarea>";
					html += "<br><br>";
				//print table
				} else if ("table".equals(node1.getNodeName())) {
					int row = 0;
					int column = 0;
					int numberInTotal = 0;
					String tableName = "tableName";
					ArrayList<String> th = new ArrayList<String>();

					NodeList nodeDetail = node1.getChildNodes();
					for (int j = 0; j < nodeDetail.getLength(); j++) {
						Node detail = nodeDetail.item(j);
						if ("row".equals(detail.getNodeName())) {
							row = Integer.parseInt(detail.getTextContent());
						} else if ("column".equals(detail.getNodeName())) {
							column = Integer.parseInt(detail.getTextContent());
						} else if ("numberInTotal".equals(detail.getNodeName())) {
							numberInTotal = Integer.parseInt(detail
									.getTextContent());
						} else if ("th".equals(detail.getNodeName())) {
							th.add(detail.getTextContent());
						} else if ("tableName".equals(detail.getNodeName())) {
							tableName = detail.getTextContent();
						}
					}
					HttpSession session = request.getSession(true);
					session.setAttribute("ths" + tableSequence, th);

					html += "<p>" + tableName + "</p>";
					html += "<table class='display' id='myDataTable"
							+ numberInTotal + "'>";
					html += "<thead>" + "<tr>";
					for (int c = 0; c < column; c++) {
						html += "<th>" + th.get(c) + "</th>";

					}
					html += "</tr>" + "</thead>" + "<tbody>";
					for (int r = 0; r < row; r++) {
						html += "<tr>";
						for (int c = 0; c < column; c++) {
							html += "<td><input required size='8' type='text' id='"
									+ th.get(c)
									+ "_"
									+ r
									+ "' name='table"
									+ tableSequence
									+ "row"
									+ r
									+ "column"
									+ c
									+ "'></td>";
						}
						html += "</tr>";
					}
					html += "</tbody>";
					html += "</table>";
					html += "<input type='hidden' name='table" + tableSequence
							+ "Row' value='" + row + "'>";
					html += "<input type='hidden' name='table" + tableSequence
							+ "Column' value='" + column + "'>";

					html += "<br><br>";
					tableSequence++;
				//print graph
				} else if ("img".equals(node1.getNodeName())) {
					int tableNumber = 1;
					String imgHTML = "";
					String graphName = "graphName";

					NodeList nodeDetail = node1.getChildNodes();

					for (int j = 0; j < nodeDetail.getLength(); j++) {
						Node detail = nodeDetail.item(j);
						if ("col1".equals(detail.getNodeName())) {
							imgHTML += "<input type='hidden' id='col1_"
									+ tableNumber + "' value='"
									+ detail.getTextContent() + "'>";
						} else if ("col2".equals(detail.getNodeName())) {
							imgHTML += "<input type='hidden' id='col2_"
									+ tableNumber + "' value='"
									+ detail.getTextContent() + "'>";
						} else if ("tableNumber".equals(detail.getNodeName())) {
							tableNumber = Integer.parseInt(detail
									.getTextContent());
						} else if ("graphName".equals(detail.getNodeName())) {
							graphName = detail.getTextContent();
						}
					}
					html += "<p>" + graphName + "</p>";
					html += "<br>";
					html += "<div id='flot-lines" + tableNumber + "'>";
					html += "</div>";
					html += imgHTML;
					html += "<input type='button' id='generateButton"
							+ tableNumber
							+ "' class='small button green' value='generate graph' onclick='generateGraph("
							+ tableNumber + ")'/>";
					html += "<br><br>";
					graphNumber++;
				}

			}
			html += "<input type='hidden' name='totalTable' value='"
					+ (tableSequence - 1) + "'>";

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		//send html code to the front end
		String htmlJson = new Gson().toJson(html); 
		response.setContentType("application/json"); 
		response.setCharacterEncoding("utf-8"); 
		response.getWriter().write(htmlJson);
	}
================WithOut Add Row And Remove Function==========================*/
}
