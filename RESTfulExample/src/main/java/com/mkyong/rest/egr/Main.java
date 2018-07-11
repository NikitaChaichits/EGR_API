package com.mkyong.rest.egr;

import java.io.BufferedReader;

import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {

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
        Date date = new Date();
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

    public static void main(String[] args) {
        System.out.println("Enter 1 if you want check YO by UNP");
        System.out.println("Enter 2 if you want check YO by name");
        Scanner in = new Scanner(System.in);
        int input = in.nextInt();

        if (input == 1) {
            CheckByUNP connServer = new CheckByUNP();
            connServer.dbConnect("jdbc:sqlserver://localhost:1584;databasename=eduorg;", "nikita", "root");
        } else if (input == 2) {
            CheckByName connServer = new CheckByName();
            connServer.dbConnect("jdbc:sqlserver://localhost:1584;databasename=eduorg;", "nikita", "root");
        }
    }
}