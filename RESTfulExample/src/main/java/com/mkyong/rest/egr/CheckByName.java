package com.mkyong.rest.egr;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class CheckByName
{
    public String nameEGR;
    public String shortnameEGR;
    public String status;
    public String versionnumber;
    public Integer count;

    /**
    Status:
    1 - Название УО правильное
    2 - УНП УО нет в ЕГР
    3 - УНП совпало, наименование нет. УО является филлиалом
    4 - УО переименовано
    5 - Новая запись из ЕГР
    */

    public void dbConnect(String db_connect_string,
                          String db_userid,
                          String db_password)
    {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(db_connect_string, db_userid, db_password);
//            ArrayList<String> arrayList = new ArrayList<String>();
//            arrayList.add("школа");
//            arrayList.add("гимназия");
//            arrayList.add("ясли-сад");
//
//            /**
//            Add to bufer YO from EGR
//             */
//
//            for (int j=0; j<arrayList.size(); j++){
//                String url = arrayList.get(j);
//                JSONArray jsonArray=new JSONArray(Main.getText("http://egr.gov.by/egrn/API.jsp?VNM=" + url));
//
//                for(int i=0;i<jsonArray.length();i++){
//                    JSONObject json = jsonArray.getJSONObject(i);
//
//                    if (json.get("VS").equals("Действующий") && !json.get("VSN").equals(null) && !json.get("VNM").equals(null) && !json.get("NM").equals(null)) {
//                        String insertIntoBufer = "insert into bufer (name, shortname, unp, status) " +
//                                "values ('" + json.getString("VNM") + "','" + json.getString("VSN") + "','" + json.get("NM") + "', 5)";
//                        Main.query(connection, insertIntoBufer);
//                        System.out.println("YO = " + json.getString("VNM") + " added");
//                    }else {
//                        System.out.println("YO = " + json.getString("VNM") + " not added");
//                    }
//                }
//            }

            /**
             Update GI.unp from bufer if Namehistory.fullname = bufer.fullname
             */

            String updateUNP = "select namehistory.id, bufer.unp, namehistory.fullname, bufer.shortname  from bufer " +
                    "inner join Namehistory on Namehistory.fullname=bufer.name " +
                    "where bufer.name = Namehistory.fullname " +
                    "group by namehistory.id, namehistory.fullname, bufer.id, bufer.unp, bufer.shortname";

            Statement forGeneralInfo = connection.createStatement();
            ResultSet rs = forGeneralInfo.executeQuery(updateUNP);

            while (rs.next()) {
                Integer namehistory_id = rs.getInt("id");
                String unpFromBufer = rs.getString("unp");
                String fullnameFromBufer = rs.getString("fullname");
                String shortnameFromBufer = rs.getString("shortname");

                String checkUNP = "select unp from generalinfo where generalinfo.name =" + namehistory_id;
                Statement forCheckUNP = connection.createStatement();
                ResultSet rs2 = forCheckUNP.executeQuery(checkUNP);
                while (rs2.next()){
                    String unpFromGI = rs2.getString("unp");
                    if (!unpFromBufer.equals(unpFromGI)){
                        String updateGeneralInfo = "update generalinfo set unp = '" + unpFromBufer + "', updatedate = '" + Main.getCurrentDate() + "' where generalinfo.name=" + namehistory_id + "";
                        Main.query(connection, updateGeneralInfo);

                        String updateStatus = "update Namehistory set statusEGR = 5, shortname= '" + shortnameFromBufer + "' where id=" + namehistory_id + "";
                        Main.query(connection, updateStatus);
                        System.out.println("у УО = " + fullnameFromBufer + " обновлено УНП ");
                    }
                }
            }

        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        CheckByName connServer = new CheckByName();
//        connServer.dbConnect("jdbc:sqlserver://localhost:1584;databasename=eduorg;", "nikita","root");
        connServer.dbConnect("jdbc:sqlserver://195.50.1.212:1433;databasename=eduorg;", "sa","vli64m0sdg!Q");
    }

}