package org.ssanames.project;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ChartUtils;

import org.jfree.chart.ChartFrame;
import javax.swing.JFrame;
import org.jfree.data.xy.XYDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import  java.util.*;

public class NamesAnalysis {

    private String dbName;

    public NamesAnalysis(String dbName) {
        this.dbName = dbName;
    }

    private Connection connect() {
        String dataFile = "C://sqlite/db/" + this.dbName;
        new File(dataFile).getParentFile().mkdirs();
        String url = "jdbc:sqlite:C://sqlite/db/" + this.dbName;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public int getMaxYear() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("SSAData");
        String path = url.getPath();
        String filesList[] = new File(path).list();
        List<String> years = new ArrayList<>();
        //System.out.println(files.length);
        for (String file : filesList) {
            if (file.charAt(0) == 'N') {
                continue;
            }
            years.add(file.substring(3, 7));
        }
        int maxYear = Integer.parseInt(Collections.max(years));
        return maxYear;
    }

    /*
    This is a function that calculates the change in rank of a name between two years.
    String prevYearTable corresponds to the table holding data for the previous or smallest year. String currYearTable corresponds to the
    table holding data for the current or highest year.
    */
    public void getRankChange(String name, char sex, int prevYear, int currYear) {
        //creates tables names
        String prevYearTable = "Ssa" + prevYear;
        String currYearTable = "Ssa" + currYear;

        //sql to select rank
        String prevYearSql = "SELECT rank from " + prevYearTable + " where name=" + "'" + name + "'" + " and sex=" + "'" + sex + "'";
        String currYearSql = "SELECT rank from " + currYearTable + " where name=" + "'" + name + "'" + " and sex=" + "'" + sex + "'";

        //initialize rank variables
        int prevRank = 0;
        int currRank = 0;
        int rankChg;

        //get rank for prev year from table
        try (Connection conn = this.connect();
             Statement stmt1 = conn.createStatement();
             ResultSet rs1 = stmt1.executeQuery(prevYearSql)) {
            // loop through the result set
            while (rs1.next()) {
                prevRank = rs1.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        //get rank for curr year from table
        try (Connection conn = this.connect();
             Statement stmt2 = conn.createStatement();
             ResultSet rs2 = stmt2.executeQuery(currYearSql)) {
            // loop through the result set
            while (rs2.next()) {
                currRank = rs2.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //calculate change in rank
        rankChg = currRank - prevRank;

        //print rank change
        if (rankChg == 0) {
            System.out.println("Name " + name.toUpperCase() + " has not changed in rank");
        } else if (rankChg > 0) {
            System.out.println("Name " + name.toUpperCase() + " has decreased in rank by " + rankChg);
        } else {
            System.out.println("Name " + name.toUpperCase() + " has increased in rank by " + Math.abs(rankChg));
        }
    }

    //This method returns the first occurrence of a name in the SSA Database
    public int nameFirstListed(String name, char sex) throws IOException {
        //gets list of database files available in project
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("SSAData");
        String filesList[] = new File(url.getPath()).list();

        String firstYear = null;

        searchLoop:
        for (String fileName : filesList) {
            if (fileName.charAt(0) == 'N') {
                continue;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(ParseBabyNames.class.getResourceAsStream("/SSAData/" + fileName)));
            String line;
            //System.out.println(fileName);
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values[0].equals(name) && values[1].charAt(0) == sex) {
                    firstYear = fileName.substring(3, 7);
                    br.close();
                    break searchLoop;
                }
            }
        }
        //System.out.println(firstYear);
        return Integer.parseInt(firstYear);
    }

    public int getOccurenceVal (String name, char sex, int year) throws IOException {
        String fileName = "yob" + year + ".txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(ParseBabyNames.class.getResourceAsStream("/SSAData/" + fileName)));
        String line;
        int occurVal = 0;
        searchLoop:
        while ((line = br.readLine()) != null){
            String[] values = line.split(",");
            if (values[0].equals(name) && values[1].charAt(0) == sex){
                occurVal = Integer.parseInt(values[2]);
                br.close();
                break searchLoop;
            }
        }
        //System.out.println(occurVal);
        return occurVal;
    }

    public HashMap getPopularity(String name, char sex) throws IOException {
        HashMap<Integer, Integer> occurMap = new HashMap<>();
        int firstYearOccur = nameFirstListed(name,sex);
        int stopYear = getMaxYear();
        for(int i = firstYearOccur; i <= stopYear; i++){
            occurMap.put(i,getOccurenceVal(name,sex,i));
        }
        return occurMap;
    }

    public void LineChart(String name, char sex) throws IOException {
        String title = "Popularity of Name: " + name.toUpperCase() + " Over Time";
        String userDir = System.getProperty("user.dir");
        DefaultCategoryDataset lineChartDate = new DefaultCategoryDataset();
        HashMap<Integer, Integer> nameData = getPopularity(name, sex);

        for(Integer keyVal: nameData.keySet()){
            lineChartDate.addValue(nameData.get(keyVal),"names",keyVal);
        }

        JFreeChart lineChartObj  = ChartFactory.createLineChart(title, "Year",
                "Occurrence", lineChartDate,PlotOrientation.VERTICAL, true, true, false
        );

        int width = 800;
        int height = 800;

        File newDir = new File(userDir + "/images/");

        if(!newDir.exists()) {
            System.out.println("Creating images directory...");
            newDir.mkdir();
        }
            File lineChart = new File(userDir + "/images/" + name + "_Occur_Over_Time.jpeg");
            ChartUtils.saveChartAsJPEG(lineChart, lineChartObj, width, height);
    }

}

