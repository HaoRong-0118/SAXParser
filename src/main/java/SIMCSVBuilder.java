import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class SIMCSVBuilder {
    List<StarsInMovies> mySIMs;
    HashSet<String> movieIdFromDB;
    Hashtable<String, String> starDBInverted;
    HashSet<String> SIMDB;
    int countRemoved;
    int countParsed;

    public SIMCSVBuilder(List<StarsInMovies> mySIMs) {
        this.mySIMs = mySIMs;
        this.countRemoved = 0 ;
        this.countParsed= mySIMs.size();
    }

    public void loadStarDB(){
        starDBInverted = new Hashtable<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery("SELECT id, name FROM stars;");
            while(rs.next()){
                String id = rs.getString("id");
                String name = rs.getString("name");

                starDBInverted.put(name, id);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("Size of starDBInverted: "+starDBInverted.size());
    }

    public void loadSIMFromDB(){
        SIMDB = new HashSet<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from stars_in_movies;");
            while(rs.next()){
                String starId = rs.getString("starId");
                String movieId = rs.getString("movieId");
                String SIMInfo = starId+movieId;
                SIMDB.add(SIMInfo);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("Size of SIMdb: "+SIMDB.size());
    }

    public void loadMovieIDFromDB(){
        movieIdFromDB = new HashSet<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery(" select id from movies;");
            while(rs.next()){
                String movieId = rs.getString("id");
                movieIdFromDB.add(movieId);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("Size of movieIdFromDB: "+movieIdFromDB.size());
    }

    //will also remove SIM record if id could not be found in DB
    public void assignIdToSIMS(){
        HashSet<StarsInMovies> tobeRemoved = new HashSet<>();
        for(StarsInMovies SIM: mySIMs){
           if(starDBInverted.containsKey(SIM.getStarName())){
               String starId = starDBInverted.get(SIM.getStarName());
               SIM.setStarId(starId);
           }
           else{
               tobeRemoved.add(SIM);
           }
        }
        //System.out.println("Number of records removed because of cannot find a star Id for the star name: "+tobeRemoved.size());
        countRemoved += tobeRemoved.size();
        mySIMs.removeAll(tobeRemoved);
        //System.out.println("Number of records after removed: "+mySIMs.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-SIM-records.txt");
            logWriter.append("The following ["+tobeRemoved.size()+"] records are dropped because star of SIM is not found in database\n");
            for(StarsInMovies s : tobeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeInconsistent(){
        HashSet<StarsInMovies> tobeRemoved = new HashSet<>();
        for(StarsInMovies SIM: mySIMs){
            if(!movieIdFromDB.contains(SIM.getMovieId())){
                tobeRemoved.add(SIM);
                continue;
            }
            boolean isDirty = false;
            if(SIM.getStarName().equals("")||SIM.getStarId().equals("")){
                isDirty = true;
            }else if(SIM.getStarId().length()>10 || SIM.getMovieId().length()>10){
                isDirty = true;
            }
            if(isDirty){
                tobeRemoved.add(SIM);
            }
        }
        //System.out.println("Number of records removed because of inconsistent: "+tobeRemoved.size());
        countRemoved += tobeRemoved.size();
        mySIMs.removeAll(tobeRemoved);
        //System.out.println("Number of records after removed: "+mySIMs.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-SIM-records.txt",true);
            logWriter.append("\nThe following ["+tobeRemoved.size()+"] records are dropped because SIM is inconsistent with the database\n");
            for(StarsInMovies s : tobeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeDuplicate(){
        HashSet<StarsInMovies> tobeRemoved = new HashSet<>();
        HashSet<String> uniqueSIMInfos = new HashSet<>();
        for(StarsInMovies SIM: mySIMs){
            String starId = SIM.getStarId();
            String movieId = SIM.getMovieId();
            String SIMInfo = starId+movieId;
            if(this.SIMDB.contains(SIMInfo)){
                tobeRemoved.add(SIM);
            }
            if(uniqueSIMInfos.contains(SIMInfo)){
                tobeRemoved.add(SIM);
            }
            uniqueSIMInfos.add(SIMInfo);
        }

        //System.out.println("Number of records removed because of collision: "+tobeRemoved.size());
        countRemoved += tobeRemoved.size();
        mySIMs.removeAll(tobeRemoved);
        //System.out.println("Number of records after removed collision: "+mySIMs.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-SIM-records.txt",true);
            logWriter.append("\nThe following ["+tobeRemoved.size()+"] records are dropped SIM is because same record exists in database\n");
            for(StarsInMovies s : tobeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateReport(){
        try {
            FileWriter logWriter = new FileWriter("Report.txt",true);
            logWriter.append("SIM Records:\n");
            logWriter.append("There are ["+countParsed+"] new SIM records parsed from the .xml file\n");
            logWriter.append("  ["+countRemoved+"] movie records were removed, details could be found in <inconsistent-SIM-records.txt>\n");
            logWriter.append("  ["+mySIMs.size()+"] new SIMs added to the database\n");
            logWriter.append("\n");
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateCSV(){
        this.loadSIMFromDB();
        this.loadStarDB();
        this.loadMovieIDFromDB();
        this.assignIdToSIMS();
        this.removeInconsistent();
        this.removeDuplicate();
        updateReport();
        try {
            FileWriter csvWriter = new FileWriter("SIM.csv", Charset.forName("ISO8859_1"));
            csvWriter.append("starId|movieId\n");
            System.out.println("No of SIM records when generating csv: " + mySIMs.size() + ".");
            for(StarsInMovies SIM : mySIMs){
                String starId = SIM.getStarId();
                String movieId = SIM.getMovieId();
                String csvRow = starId+"|"+movieId+"\n";
                csvWriter.append(csvRow);
            }

            csvWriter.flush();
            csvWriter.close();
        }catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public void loadCSV(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false&allowLoadLocalInfile=true","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            String sqlString = "LOAD DATA LOCAL INFILE 'SIM.csv'\n" +
                    "INTO TABLE stars_in_movies\n" +
                    "CHARACTER SET utf8mb4\n" +
                    "FIELDS TERMINATED BY '|' ENCLOSED BY ''\n" +
                    "LINES TERMINATED BY '\\n'\n" +
                    "IGNORE 1 LINES\n" +
                    ";";
            stmt.execute(sqlString);
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}