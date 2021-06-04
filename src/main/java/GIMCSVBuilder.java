import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class GIMCSVBuilder {
    List<GenresInMovies> myGenresInMovies;
    HashSet<String> GIMDB;
    HashSet<String> movieIdFromDB;
    Hashtable<String, String> genreDBInverted;
    Hashtable<String, String> genreDic;
    int countRemoved;
    int countParsed;

    public GIMCSVBuilder(List<GenresInMovies> myGenresInMovies) {
        genreDic = new Hashtable<>();
        genreDic.put("susp","Thriller");
        genreDic.put("cnr","Cops and Robbers");
        genreDic.put("dram","Drama");
        genreDic.put("west","Western");
        genreDic.put("myst","Mystery");
        genreDic.put("s.f.","Science Fiction");
        genreDic.put("advt","Adventure");
        genreDic.put("horr","Horror");
        genreDic.put("romt","Romantic");
        genreDic.put("comd","Comedy");
        genreDic.put("musc","Musical");
        genreDic.put("docu","Documentary");
        genreDic.put("porn","Pornography");
        genreDic.put("noir","Black");
        genreDic.put("biop","Biographical Picture");
        genreDic.put("tv","TV show");
        genreDic.put("tvs","TV series");
        genreDic.put("ctxx","Uncategorized");
        genreDic.put("actn","Violence");
        genreDic.put("disa","Disaster");
        genreDic.put("epic","Epic");
        genreDic.put("scfi","Science Fiction");
        genreDic.put("cart","Cartoon");
        genreDic.put("faml","Family");
        genreDic.put("surl","Sureal");
        genreDic.put("avga","Avant Garde");
        genreDic.put("hist","History");

        this.countRemoved = 0;
        this.countParsed = myGenresInMovies.size();
        this.myGenresInMovies = myGenresInMovies;
    }

    public void loadGenreFromDB(){
        genreDBInverted = new Hashtable<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from genres;");
            while(rs.next()){
                String name = "";
                String id = rs.getString("id");
                name = rs.getString("name");
                genreDBInverted.put(name,id);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("Size of genreDBInverted: "+genreDBInverted.size());
    }

    public void loadGIMFromDB(){
        GIMDB = new HashSet<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from genres_in_movies;");
            while(rs.next()){
                String movieId = rs.getString("movieId");
                String genreId = rs.getString("genreId");
                String GIMInfo = movieId+genreId;
                GIMDB.add(GIMInfo);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("Size of GIMdb: "+GIMDB.size());
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

    public void decodeGIMGenres(){
        //System.out.println(genreDic);
        for(GenresInMovies GIM: myGenresInMovies){
            if(genreDic.containsKey(GIM.getGenreName())){
                String decodedGenre = genreDic.get(GIM.getGenreName());
                GIM.setGenreName(decodedGenre);
            }
        }
    }

    //will remove
    public void replaceGenreNameWithGenreID(){
        HashSet<GenresInMovies> tobeRemoved = new HashSet<>();
        for(GenresInMovies GIM: myGenresInMovies){
            if(genreDBInverted.containsKey(GIM.getGenreName())){
                String id = genreDBInverted.get(GIM.getGenreName());
                GIM.setGenreName(id);
            }
            else{
                tobeRemoved.add(GIM);
            }
        }
        //System.out.println("Number of records removed because of no genre in db "+tobeRemoved.size());
        countRemoved += tobeRemoved.size();
        myGenresInMovies.removeAll(tobeRemoved);
        //System.out.println("length of GIM after remove "+myGenresInMovies.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-GIM-records.txt");
            logWriter.append("\nThe following ["+tobeRemoved.size()+"] records are dropped because genre of GIM is not found in database\n");
            for(GenresInMovies s : tobeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeDuplicate(){
        HashSet<GenresInMovies> tobeRemoved = new HashSet<>();
        HashSet<String> uniqueGIMInfo = new HashSet<>();
        for(GenresInMovies GIM: myGenresInMovies){
            String movieId = GIM.getMovieID();
            String genreId = GIM.getGenreName();
            String GIMInfo = movieId + genreId ;
            if(this.GIMDB.contains(GIMInfo)){
                tobeRemoved.add(GIM);
            }
            if(uniqueGIMInfo.contains(GIMInfo)){
                tobeRemoved.add(GIM);
            }
            uniqueGIMInfo.add(GIMInfo);
        }
        //System.out.println("Number of internal collision " + count);
        //System.out.println("Number of records removed because of collision "+tobeRemoved.size());
        countRemoved += tobeRemoved.size();
        myGenresInMovies.removeAll(tobeRemoved);
        //System.out.println("length of GIM after remove "+myGenresInMovies.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-GIM-records.txt",true);
            logWriter.append("\nThe following ["+tobeRemoved.size()+"] records are dropped because same record exists in database\n");
            for(GenresInMovies s : tobeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeInconsistent(){
        HashSet<GenresInMovies> tobeRemoved = new HashSet<>();
        for(GenresInMovies GIM: myGenresInMovies){
            if(!movieIdFromDB.contains(GIM.getMovieID())){
                tobeRemoved.add(GIM);
            }
        }
       // System.out.println("Number of records removed because of no movieId "+tobeRemoved.size());
        countRemoved += tobeRemoved.size();
        myGenresInMovies.removeAll(tobeRemoved);
       // System.out.println("length of GIM after remove(no movieId) "+myGenresInMovies.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-GIM-records.txt",true);
            logWriter.append("\nThe following ["+tobeRemoved.size()+"] records are dropped because movie of GIM is not found in database\n");
            for(GenresInMovies s : tobeRemoved){
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
            logWriter.append("GIM Records:\n");
            logWriter.append("There are ["+countParsed+"] new GIM records parsed from the .xml file\n");
            logWriter.append("  ["+countRemoved+"] movie records were removed, details could be found in <inconsistent-GIM-records.txt>\n");
            logWriter.append("  ["+myGenresInMovies.size()+"] new GIM added to the database\n");
            logWriter.append("\n");
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateCSV(){
        this.loadGenreFromDB();
        this.loadGIMFromDB();
        this.loadMovieIDFromDB();
        this.decodeGIMGenres();
        this.replaceGenreNameWithGenreID();
        this.removeDuplicate();
        this.removeInconsistent();
        this.updateReport();
        try {
            FileWriter csvWriter = new FileWriter("GIM.csv", Charset.forName("ISO8859_1"));
            csvWriter.append("genreId|movieId\n");
            //int count = 0;
            System.out.println("No of GIM records when generating csv: " + myGenresInMovies.size() + ".");
            for(GenresInMovies GIM: myGenresInMovies){
                String movieId = GIM.getMovieID();
                String genreId = GIM.getGenreName();
                String csvRow = genreId+"|"+movieId+"\n";
                csvWriter.append(csvRow);
            }
            csvWriter.flush();
            csvWriter.close();
        }catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //load the csv data into the database;
    public void loadCSV(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false&allowLoadLocalInfile=true","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            String sqlString = "LOAD DATA LOCAL INFILE 'GIM.csv'\n" +
                    "INTO TABLE genres_in_movies\n" +
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
