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
     0 - Закрытая запись, старое наименование
     1 - Название УО правильное
     2 - УНП УО нет в ЕГР
     3 - УНП совпало, наименование нет. УО является филлиалом
     4 - Новая запись из ЕГР
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
                Integer gi_name = rs.getInt("name"); //get Eduname from DB
                Integer gi_id = rs.getInt("id");

                try {
                    JSONArray jsonArray=new JSONArray( Main.getText("http://egr.gov.by/egrn/API.jsp?NM=" + unp));
                    //iterate loop
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject json = jsonArray.getJSONObject(i);
                        nameEGR = json.getString("VNM"); //полное наименование УО из ЕГР
                        shortnameEGR = json.getString("VSN"); //сокращенное наименование УО из ЕГР
                    }
                }catch (Exception e){ // УНП нет в ЕГР
                    String insertStatus = "update namehistory set statusEGR = '2' where id = '"+ gi_name +"'";
                    Main.query(connection, insertStatus);
                    continue;
                }

                String queryName = "select * from namehistory where generalinfo_id = " + gi_id + " and statusEGR!='0'";
                ResultSet rsname = forNameHistory.executeQuery(queryName);

                while (rsname.next()) {
                    String nameDB = rsname.getString("fullname");
                    String version = rsname.getString("version");
                    versionnumber = Integer.parseInt(version)+1;

                    if (nameEGR.equalsIgnoreCase(nameDB)) {
//                        System.out.println(nameDB + " наименование совпадает");
                        status = "1";
                    }
                    else {
                        if ((rs.getString("head_id"))!=null){
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
                    //устанавливается статус 0 для записи с неактульным названием
                    String insertStatus = "update namehistory set statusEGR = '0' where id = '"+ gi_name +"'";
                    Main.query(connection, insertStatus);

                    //обновляет название университета
                    String insertQuery = "insert into namehistory (generalinfo_id, version, fullname, shortname, renamedateEGR, statusEGR) " +
                            "values (" + gi_id + "," +
                            "'" + versionnumber + "'," +
                            "'" + nameEGR + "'," +
                            "'" + shortnameEGR + "'," +
                            "'" + Main.getCurrentDate() + "'," +
                            "'" + 4 + "')";
                    Main.query(connection, insertQuery);

                    //обновляет id университета в Generalinfo
                    String getNamehistoryID = "select max(id) from namehistory";
                    ResultSet rsGI = connection.createStatement().executeQuery(getNamehistoryID);
                    while (rsGI.next()){
                        Integer namehistory_id = rsGI.getInt(1);
                        String updateGenerainfo = "update generalinfo set name = '" + namehistory_id + "' where id ='"+ gi_id +"'";
                        Main.query(connection, updateGenerainfo);
                    }
                }else{
                    String insertQuery = "update namehistory set statusEGR = '" + status + "' where id = '"+ gi_name +"'";
                    Main.query(connection, insertQuery);
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
}