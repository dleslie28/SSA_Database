package org.ssanames.project;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ParseBabyNames {
    private static final String COMMA_DELIM = ",";
    private String dbName;

    public ParseBabyNames(String dbName){
        this.dbName = dbName;
    }

    public String getDBName(){
        return this.dbName;
    }

    //method to establish connection to the database
    private Connection connect(){
        String dataFile = "C://sqlite/db/" + this.dbName;
        new File(dataFile).getParentFile().mkdirs();
        String url = "jdbc:sqlite:C://sqlite/db/" + this.dbName;
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(url);
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return conn;
    }

    //method that creates database in C:/sqlite folder
    public void createDatabase(){
        try(Connection conn = this.connect()){
            if (conn != null){
                System.out.println("Database " + dbName + " has been created.");
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    //method that creates table in specified database
    public void createTable(int year) throws SQLException {
        String tableName = "Ssa" + year;
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(\n"
                + " rank INTEGER, \n"
                + " name text NOT NULL, \n"
                + " sex CHAR(1) NOT NULL, \n"
                + " occurrence INTEGER, \n"
                + " year      INTEGER,\n"
                + "PRIMARY KEY(name,sex)\n"
                + ");";

        try(Connection conn = this.connect()){
            if (conn != null){
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
                System.out.println("Table " + tableName + " created...");
            }
        } catch(SQLException e){
            System.out.println(e.getMessage());
            throw e;
        }
    }

    //method to parse txt file and load the specified number of rows into sql database
    public void loadTable(int year, int recordLimit ) throws IOException, SQLException {
        String tableName = "Ssa" + year;
        String sql = "INSERT INTO " + tableName + "(rank,name,sex,occurrence,year) VALUES (?,?,?,?,?)";
        List<List<String>> records = new ArrayList<>();
        String fileName = "/SSAData/yob" + year + ".txt";

        BufferedReader br = new BufferedReader(new InputStreamReader(ParseBabyNames.class.getResourceAsStream(fileName)));
        String line;

        while(( line = br.readLine()) != null) {
            String[] values = line.split(COMMA_DELIM);
            records.add(Arrays.asList(values));
        }

        //variable to limit the amount of records loaded into table
        int cnt = 0;
        int rank_id = 1;

        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            while (cnt < recordLimit) {
                String name = records.get(cnt).get(0);
                String sex = records.get(cnt).get(1);
                int occurrence = Integer.parseInt(records.get(cnt).get(2));

                pstmt.setInt(1, rank_id);
                pstmt.setString(2, name);
                pstmt.setString(3, sex);
                pstmt.setInt(4, occurrence);
                pstmt.setInt(5, year);
                pstmt.executeUpdate();

                ++cnt;
                ++rank_id;
            }
        conn.commit();
        }catch (SQLException e){
            System.out.println(e.getMessage());
            throw e;
        }
            System.out.println(cnt + " rows inserted");
    }

    //method that loads all rows in file to database
    public void loadTable(int year) throws IOException, SQLException {
        String tableName = "Ssa" + year;
        String sql = "INSERT INTO " + tableName + "(rank,name,sex,occurrence,year) VALUES (?,?,?,?,?)";
        List<List<String>> records = new ArrayList<>();
        String fileName = "/SSAData/yob" + year + ".txt";

        BufferedReader br = new BufferedReader(new InputStreamReader(ParseBabyNames.class.getResourceAsStream(fileName)));
        String line;

        while(( line = br.readLine()) != null) {
            String[] values = line.split(COMMA_DELIM);
            records.add(Arrays.asList(values));
        }

        //variable to limit the amount of records loaded into table
        int cnt = 0;
        int rank_id = 1;

        try(Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            while (cnt < records.size()) {
                String name = records.get(cnt).get(0);
                String sex = records.get(cnt).get(1);
                int occurrence = Integer.parseInt(records.get(cnt).get(2));

                pstmt.setInt(1, rank_id);
                pstmt.setString(2, name);
                pstmt.setString(3, sex);
                pstmt.setInt(4, occurrence);
                pstmt.setInt(5, year);
                pstmt.executeUpdate();

                ++cnt;
                ++rank_id;
            }
            conn.commit();
        }catch (SQLException e){
            System.out.println(e.getMessage());
            throw e;
        }
        System.out.println(cnt + " rows inserted");
    }

    //method returns all records in specified table and database
    public void selectAll(String tableName){
        String sql = "SELECT name,sex,occurrence,year from " + tableName;

        try (Connection conn = this.connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                System.out.println(
                        rs.getString("name") + "\t" +
                                rs.getString("sex") + "\t" +
                                rs.getInt("occurrence") + "\t" +
                                rs.getInt("year")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}


