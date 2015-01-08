package com.adobe.wd;

import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * * This servlet is used to handle benefit table requests *
 */
@SlingServlet(resourceTypes = { "cq/Page" }, selectors = { "benefitsTable" }, extensions = { "html" }, methods = {
		"GET", "POST" })
public class BenefitTableServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 3169795937693969416L;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BenefitTableServlet.class);

	@Override
	public final void doGet(final SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {

		try {
			JSONArray jsonArray = null;
			jsonArray = getTableRowsFromService(request);
			LOGGER.info("Resultant JSON returned" + jsonArray);
			// change the response type to JSON
			response.setHeader("Content-Type", "application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(jsonArray);

		} catch (Exception exception) {
			LOGGER.error("Exception in BenefitsTableDataServlett: ", exception);
		}

	}

	/**
	 * Gets the table rows from service.
	 * 
	 * @param request
	 *            the request
	 * @return the table rows from service
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws JSONException
	 *             the JSON exception
	 */
	private JSONArray getTableRowsFromService(SlingHttpServletRequest request)
			throws RepositoryException, JSONException {

		JSONArray jsonArray = new JSONArray();
		Session session = request.getResourceResolver().adaptTo(Session.class);
		Node rootNode = session.getRootNode();

		Node benefitsTableNode = rootNode.getNode("content/global-aaa/aaa-site/home/membership/become-a-member/benefits-content-holder/jcr:content/benefitsTable");

		String stateCode = request.getParameter("stateCode");
		String mobileData = request.getParameter("mobileView");

		String[] filterType = new String[1];
		int global = 0;

		if (stateCode.contains("all")) {

			filterType = stateCode.split("-");
			if (filterType.length > 1) {
				stateCode = filterType[1];
				global = 1;
			} else {
				stateCode = "global";
				global = 0;
			}
		}

		Node stateNode = null;
		Node deviceNode = null;
		LOGGER.info("Requested stateCode is: " + stateCode);
		if (benefitsTableNode.hasNode(stateCode)) {
			stateNode = benefitsTableNode.getNode(stateCode);

			if (mobileData.equalsIgnoreCase("true")) {

				if (stateNode.hasNode("mobile")) {
					deviceNode = stateNode.getNode("mobile");
				}
			} else {
				if (stateNode.hasNode("desktop")) {
					deviceNode = stateNode.getNode("desktop");
				}
			}

			if (deviceNode != null) {

				NodeIterator stateRowIterator = deviceNode.getNodes();

				while (stateRowIterator.hasNext()) {

					Node rowNode = (Node) stateRowIterator.next();
					JSONArray stateArray = new JSONArray();

					String categoryName = "";
					String categoryDescription = "";
					String categoryToolTipTitle = "";
					String categoryToolTipText = "";
					String stateRow = "";

					if (rowNode.hasProperty("categoryName"))
						categoryName = rowNode.getProperty("categoryName")
								.getValue().getString();

					categoryName = categoryName.replaceAll("\"", "\'");

					if (!categoryName.equalsIgnoreCase("")) {

						if (rowNode.hasProperty("categoryDescription"))
							categoryDescription = rowNode
									.getProperty("categoryDescription")
									.getValue().getString();

						categoryDescription = categoryDescription.replaceAll(
								"\"", "\'");
						if (rowNode.hasProperty("categoryToolTipTitle"))
							categoryToolTipTitle = rowNode
									.getProperty("categoryToolTipTitle")
									.getValue().getString();

						categoryToolTipTitle = categoryToolTipTitle.replaceAll(
								"\"", "\'");

						if (rowNode.hasProperty("categoryToolTipText"))
							categoryToolTipText = rowNode
									.getProperty("categoryToolTipText")
									.getValue().getString();
						categoryToolTipText = categoryToolTipText.replaceAll(
								"\"", "\'");

						if (rowNode.hasProperty("stateRow"))
							stateRow = rowNode.getProperty("stateRow")
									.getValue().getString();

						if (!stateCode.equalsIgnoreCase("global")
								&& stateRow.equalsIgnoreCase("false")) {
							continue;
						}

						stateArray.put(categoryName);
						for (int count = 1; count <= 3; count++) {
							if (rowNode.hasProperty("col" + count + "_val")) {
								String value = rowNode
										.getProperty("col" + count + "_val")
										.getValue().getString();
								value = value.replaceAll("\"", "\'");
								stateArray.put(value);
							} else {
								stateArray.put("");
							}

						}

						int currentLength = stateArray.length();
						for (int counter = currentLength; counter < 4; counter++) {
							stateArray.put("");
						}
						stateArray.put(categoryDescription);
						stateArray.put(categoryToolTipTitle);
						stateArray.put(categoryToolTipText);

						stateArray.put(stateRow);
						jsonArray.put(stateArray);
					}
				}
			}
		}

		Node globalDeviceNode = null;
		if (global == 1) {
			Node globalNode = benefitsTableNode.getNode("global");

			if (mobileData.equalsIgnoreCase("true")) {

				if (globalNode.hasNode("mobile")) {
					globalDeviceNode = globalNode.getNode("mobile");
				}
			} else {

				if (globalNode.hasNode("desktop")) {
					globalDeviceNode = globalNode.getNode("desktop");
				}
			}
			if (globalDeviceNode != null) {
				NodeIterator globalRowIterator = globalDeviceNode.getNodes();

				while (globalRowIterator.hasNext()) {

					Node rowNode = (Node) globalRowIterator.next();
					JSONArray globalArray = new JSONArray();

					String categoryName = "";
					String categoryDescription = "";
					String categoryToolTipTitle = "";
					String categoryToolTipText = "";

					if (rowNode.hasProperty("categoryName"))
						categoryName = rowNode.getProperty("categoryName")
								.getValue().getString();

					categoryName = categoryName.replaceAll("\"", "\'");

					if (!categoryName.equalsIgnoreCase("")) {

						if (rowNode.hasProperty("categoryDescription"))
							categoryDescription = rowNode
									.getProperty("categoryDescription")
									.getValue().getString();
						categoryDescription = categoryDescription.replaceAll(
								"\"", "\'");

						if (rowNode.hasProperty("categoryToolTipTitle"))
							categoryToolTipTitle = rowNode
									.getProperty("categoryToolTipTitle")
									.getValue().getString();

						categoryToolTipTitle = categoryToolTipTitle.replaceAll(
								"\"", "\'");
						if (rowNode.hasProperty("categoryToolTipText"))
							categoryToolTipText = rowNode
									.getProperty("categoryToolTipText")
									.getValue().getString();

						categoryToolTipText = categoryToolTipText.replaceAll(
								"\"", "\'");
						boolean present = false;

						if (deviceNode != null) {
							NodeIterator newStateRowIterator = deviceNode
									.getNodes();
							while (newStateRowIterator.hasNext()) {

								Node newStateRowNode = (Node) newStateRowIterator
										.next();
								String newCategoryNameForState = newStateRowNode
										.getProperty("categoryName").getValue()
										.getString();

								newCategoryNameForState = newCategoryNameForState
										.replaceAll("\"", "\'");

								if (newCategoryNameForState
										.equalsIgnoreCase(categoryName)) {
									present = true;

								}
							}

							if (present) {
								continue;
							}
						}

						globalArray.put(categoryName);
						for (int count = 1; count <= 3; count++) {
							if (rowNode.hasProperty("col" + count + "_val")) {

								String value = rowNode
										.getProperty("col" + count + "_val")
										.getValue().getString();
								value = value.replaceAll("\"", "\'");
								globalArray.put(value);
							} else {
								globalArray.put("");
							}

						}

						int currentLength = globalArray.length();
						for (int counter = currentLength; counter < 4; counter++) {
							globalArray.put("");
						}

						globalArray.put(categoryDescription);
						globalArray.put(categoryToolTipTitle);
						globalArray.put(categoryToolTipText);
						globalArray.put("false");
						jsonArray.put(globalArray);

					}
				}
			}
		}
		return jsonArray;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache
	 *      .sling.api.SlingHttpServletRequest,
	 *      org.apache.sling.api.SlingHttpServletResponse)
	 */
	public final void doPost(final SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {

		LOGGER.info("inside POST call: " + request.getParameter("data"));

		String tableData = request.getParameter("data");
		String mobileView = request.getParameter("mobileView");

		JSONObject resultObject = new JSONObject();

		try {

			JSONObject jsonObject = new JSONObject(tableData);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			LOGGER.info("jsonArray " + jsonArray.toString());
			String stateCode = request.getParameter("stateCode");
			String[] filterType = new String[1];

			if (stateCode != null && !stateCode.equalsIgnoreCase("")) {
				if (stateCode.contains("all")) {

					filterType = stateCode.split("-");
					if (filterType.length > 1) {
						stateCode = filterType[1];
					} else {
						stateCode = "global";
					}
				}

				Session session = request.getResourceResolver().adaptTo(
						Session.class);
				Node rootNode = session.getRootNode();
				Node benefitsTableNode = rootNode
						.getNode("content/global-aaa/aaa-site/home/membership/become-a-member/benefits-content-holder/jcr:content/benefitsTable");
				Node stateNode = null;
				if (benefitsTableNode.hasNode(stateCode))
					stateNode = benefitsTableNode.getNode(stateCode);
				else {
					stateNode = benefitsTableNode.addNode(stateCode,
							"nt:unstructured");
				}
				benefitsTableNode.getSession().save();
				Node dataNode = null;
				Node newDataNode = null;

				if (mobileView.equalsIgnoreCase("true")
						&& stateNode.hasNode("mobile")) {
					dataNode = stateNode.getNode("mobile");
				} else if (stateNode.hasNode("desktop")) {
					dataNode = stateNode.getNode("desktop");
				}
				if (dataNode != null) {
					dataNode.remove();
					stateNode.getSession().save();
				}

				if (mobileView.equalsIgnoreCase("true")) {
					newDataNode = stateNode
							.addNode("mobile", "nt:unstructured");
				} else {
					newDataNode = stateNode.addNode("desktop",
							"nt:unstructured");
				}

				stateNode.getSession().save();

				for (int i = 0; i < jsonArray.length(); i++) {

					JSONArray array = (JSONArray) jsonArray.get(i);
					LOGGER.info("arrayObject " + array.toString());
					Node newRow = newDataNode.addNode("row_" + i,
							"nt:unstructured");
					stateNode.getSession().save();

					for (int z = 0; z < array.length(); z++) {
						String columnKey = "";
						switch (z) {
						case 0:
							columnKey = "categoryName";
							break;
						case 1:
							columnKey = "col1_val";
							break;
						case 2:
							columnKey = "col2_val";
							break;
						case 3:
							columnKey = "col3_val";
							break;
						case 4:
							columnKey = "categoryDescription";
							break;
						case 5:
							columnKey = "categoryToolTipTitle";
							break;
						case 6:
							columnKey = "categoryToolTipText";
							break;
						default:
							columnKey = "Invalid";
							break;
						}
						if (!columnKey.equalsIgnoreCase("Invalid")) {
							String keyValue = array.get(z).toString();
							LOGGER.info("key value" + keyValue);
							if (keyValue != null
									&& keyValue.equalsIgnoreCase("null")) {
								keyValue = "";
							}
							newRow.setProperty(columnKey, keyValue);
							session.save();
						}
					}

					if (!stateCode.equalsIgnoreCase("global")) {
						newRow.setProperty("stateRow", "true");
					} else {
						newRow.setProperty("stateRow", "false");
					}

				}

				session.save();
				resultObject.put("success", "true");
			} else {
				resultObject.put("message", "no stateCode");
			}
			LOGGER.info("resultObject " + resultObject.toString());
			// change the response type to JSON
			response.setHeader("Content-Type", "application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(resultObject);

		} catch (JSONException e) {
			LOGGER.info("Error" + e.getMessage());
		} catch (RepositoryException e) {
			LOGGER.info("Error" + e.getMessage());
		}
	}
}
