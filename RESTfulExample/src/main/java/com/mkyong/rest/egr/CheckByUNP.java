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
import java.util.Date;


public class CheckByUNP
{
    public String nameEGR;
    public String shortnameEGR;
    public String status;
    public Integer versionnumber;

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

            Statement forGeneralInfo = connection.createStatement();
            Statement forNameHistory = connection.createStatement();

            String query = "select * from generalinfo";
            ResultSet rs = forGeneralInfo.executeQuery(query);

            while (rs.next()){
                String unp = rs.getString("unp"); //get UNP from DB
                Integer name_id = rs.getInt("name"); //get Eduname from DB
                Integer gi_id = rs.getInt("id");

                String queryName = "select * from namehistory where namehistory.id = " + name_id + "";
                ResultSet rsname = forNameHistory.executeQuery(queryName);

                try {
                    JSONArray jsonArray=new JSONArray(getText("http://egr.gov.by/egrn/API.jsp?NM=" + unp));
                    //iterate loop
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject json = jsonArray.getJSONObject(i);
                        nameEGR = json.getString("VNM"); //полное наименование УО из ЕГР
                        shortnameEGR = json.getString("VSN"); //сокращенное наименование УО из ЕГР
                    }
                }catch (Exception e){ // УНП нет в ЕГР
                    status = "2";
                    String insertStatus = "update namehistory set statusEGR = '" + status + "'where id = '"+ name_id +"'";
                    query(connection, insertStatus);
                    continue;
                }

                while (rsname.next()) {
                    String nameDB = rsname.getString("fullname");
                    String version = rsname.getString("version");
                    versionnumber = Integer.parseInt(version)+1;

                    if (nameEGR.equalsIgnoreCase(nameDB)) {
                        System.out.println(nameDB + " наименование совпадает");
                        status = "1";
                    }
                    else {
                        if ((rs.getString("head_id"))!=null){
                            System.out.println("Данное УО является филиалом.");
                            status = "3";
                        }else{
                            System.out.println(nameDB + " наименование не совпадает. Наименование в ЕГР: " + nameEGR);
                            System.out.println("Наименование УО изменено");
                            status = "4";
                        }
                    }
                }

                //обновление статуса записи в Namehistory в результате проверок
                if (status.equals("4")){
//                    String insertQuery = "update namehistory " +
//                            "set version = '" + versionnumber + "', fullname = '" + nameEGR + "', shortname = '" + shortnameEGR + "', renamedateEGR = '" + getCurrentDate() + "', statusEGR = '" + status +
//                            "' where id = '"+ name_id +"'";
                    //обновляет название университета
                    String insertQuery = "insert into namehistory (generalinfo_id, version, fullname, shortname, startdate, statusEGR) " +
                            "values (" + gi_id + ",'" + versionnumber + "','" + nameEGR + "','" + shortnameEGR + "'," +
                            "'" + getCurrentDate() + "','" + 5 + "')";
                    query(connection, insertQuery);

                    //обновляет id университета в Generalinfo
                    String getNamehistoryID = "select max(id) from namehistory";
                    ResultSet rsGI = connection.createStatement().executeQuery(getNamehistoryID);
                    while (rsGI.next()){
                        Integer namehistory_id = rsGI.getInt(1);

                        String updateGenerainfo = "update generalinfo set name = '" + namehistory_id + "' where id ='"+ gi_id +"'";
                        query(connection, updateGenerainfo);
                    }
                }else{
                    String insertQuery = "update namehistory set statusEGR = '" + status + "' where id = '"+ name_id +"'";
                    query(connection, insertQuery);
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        CheckByUNP connServer = new CheckByUNP();
//        connServer.dbConnect("jdbc:sqlserver://localhost:1584;databasename=eduorg;", "nikita","root");
        connServer.dbConnect("jdbc:sqlserver://195.50.1.212:1433;databasename=eduorg;", "sa","vli64m0sdg!Q");
    }

    public static String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader( new InputStreamReader(connection.getInputStream(),"UTF8"));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        return response.toString();
    }

    public static String getCurrentDate (){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = new Date();
        String renamedDate = dateFormat.format(date);
        return renamedDate;
    }

    static void query(Connection connection, String query){
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}