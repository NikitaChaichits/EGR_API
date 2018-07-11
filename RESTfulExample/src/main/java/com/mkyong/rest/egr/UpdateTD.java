package com.mkyong.rest.egr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class UpdateTD {
    public void dbConnect(String db_connect_string,
                          String db_userid,
                          String db_password)
    {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(db_connect_string, db_userid, db_password);

            Statement forDistrict = connection.createStatement();
            Statement forTerritorialdivision = connection.createStatement();


            String query = "select id, region, district from territorialdivision";
            ResultSet rs = forDistrict.executeQuery(query);
            while (rs.next()) {
                String id = rs.getString("id");
                String region = rs.getString("region");
                String district = rs.getString("district");

                String query2 = "select District.id from District " +
                        "inner join territorialdivision on territorialdivision.district = District.code " +
                        "where territorialdivision.region = '" + region + "' and territorialdivision.district = '" + district + "'";
                ResultSet rs2 = forTerritorialdivision.executeQuery(query2);

                loop:
                while (rs2.next()) {
                    String dist_id = rs2.getString("id");
                    String update = "update Territorialdivision set district_id = '" + dist_id + "' where Territorialdivision.id =" + id + "";
                    Main.query(connection, update);
                    break loop;
                }
            }
        }catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        UpdateTD connServer = new UpdateTD();
//        connServer.dbConnect("jdbc:sqlserver://localhost:1584;databasename=eduorg;", "nikita","root");
        connServer.dbConnect("jdbc:sqlserver://195.50.1.212:1433;databasename=eduorg;", "sa","vli64m0sdg!Q");
    }

}
