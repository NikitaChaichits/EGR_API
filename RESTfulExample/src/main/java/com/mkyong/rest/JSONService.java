package com.mkyong.rest;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


@Path("/json/education")
public class JSONService {

	@GET
	@Path("/get")
	@Produces("application/json")
	public String getEducationInJSON() {
		JSONArray jsonArray = new JSONArray();

		String fullname;
		String shortname;
		String unp;

		try {
			Connection connection = DriverManager.getConnection("jdbc:sqlserver://195.50.1.212:1433;databasename=eduorg;", "sa","vli64m0sdg!Q");
			Statement forGeneralInfo = connection.createStatement();
			Statement forNameHistory = connection.createStatement();

			String query = "select * from generalinfo";
			ResultSet rs = forGeneralInfo.executeQuery(query);

			while (rs.next()) {
				unp = rs.getString("unp"); //get UNP from DB
				Integer name_id = rs.getInt("name"); //get Eduname from DB

				String queryName = "select * from namehistory where namehistory.id = " + name_id + "";
				ResultSet rsname = forNameHistory.executeQuery(queryName);

				while (rsname.next()) {
					fullname = rsname.getString("fullname");
					shortname = rsname.getString("shortname");

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("fullname", fullname);
					jsonObject.put("shortname", shortname);
					jsonObject.put("unp", unp);
					jsonArray.put(jsonObject);
				}
			}
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}

		String s = jsonArray.toString();

		return s;
	}


	@POST
	@Path("/post")
	@Consumes("application/json")
	public Response createProductInJSON(Education education) {

		String result = "Product created : " + education;
		return Response.status(201).entity(result).build();

	}
}