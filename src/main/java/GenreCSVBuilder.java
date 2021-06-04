import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

public class GenreCSVBuilder {
    Hashtable<String, String> genreDic;
    private Set<String> genres;
    int countParsed;
    int countRemoved;


    public GenreCSVBuilder(Set<String> genres) {
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

        this.countParsed = genres.size();
        this.countRemoved = 0;
        this.genres = genres;
    }

    public void decodeGenreSet(){
        HashSet<String> tobeDecoded = new HashSet<>();
        int initialSize = genres.size();
        for(String genreCode:genres){
            if(genreDic.containsKey(genreCode)){
                tobeDecoded.add(genreCode);
            }
        }
        tobeDecoded.forEach(genres::remove);
        //System.out.println("Number of genres that could be decoded: "+ tobeDecoded.size());
        for(String genreCode: tobeDecoded){
            genres.add(genreDic.get(genreCode));
        }
        countRemoved += initialSize-genres.size();
    }

    public void removeDuplicate(){
        Hashtable<String, String> genreDBInverted = new Hashtable<>();
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

        HashSet<String> toBeRemoved = new HashSet<>();
        for(String genre: genres){
            if(genreDBInverted.containsKey(genre)){
                toBeRemoved.add(genre);
            }
        }

        //System.out.println("Number of records removed because of collision: "+ toBeRemoved.size());
        countRemoved += toBeRemoved.size();
        countRemoved ++;
        toBeRemoved.forEach(genres::remove);
        genres.remove("");


        try {
            FileWriter logWriter = new FileWriter("inconsistent-genre-records.txt");
            logWriter.append("Inconsistent Genre Records: \n");
            logWriter.append("The following ["+toBeRemoved.size()+"] records are dropped because same [genres.name] exist in the database\n");
            for(String s : toBeRemoved){
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
            logWriter.append("Genre Records:\n");
            logWriter.append("There are ["+countParsed+"] new genre records parsed from the .xml file\n");
            logWriter.append("  ["+countRemoved+"] genre records were removed, details could be found in <inconsistent-genre-records.txt>\n");
            logWriter.append("  ["+genres.size()+"] new genres added to the database\n");
            logWriter.append("\n");
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateCSV(){
        this.decodeGenreSet();
        this.removeDuplicate();
        this.updateReport();
        try {
            FileWriter csvWriter = new FileWriter("genres.csv", Charset.forName("ISO8859_1"));
            csvWriter.append("name\n");
            System.out.println("No of genres when generating csv: " + genres.size() + ".");
            for(String genre: genres){
                String csvRow = genre+"\n";
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
            String sqlString = "LOAD DATA LOCAL INFILE 'genres.csv'\n" +
                    "INTO TABLE genres\n" +
                    "CHARACTER SET utf8mb4\n" +
                    "FIELDS TERMINATED BY ',' ENCLOSED BY ''\n" +
                    "LINES TERMINATED BY '\\n'\n" +
                    "IGNORE 1 LINES\n" +
                    "(name)\n"+
                    ";";
            stmt.execute(sqlString);
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
