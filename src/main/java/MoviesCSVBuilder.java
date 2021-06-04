import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class MoviesCSVBuilder {
    List<Movie> myMovies;
    Hashtable<String, String> movieDB;
    int countParsed;
    int countRemoved;

    public MoviesCSVBuilder(List<Movie> myMovies) {
        this.countParsed = myMovies.size();
        this.myMovies = myMovies;
        this.countRemoved = 0;
    }

    public void loadMoviesFromDB(){
        movieDB = new Hashtable<>();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from movies;");
            while(rs.next()){
                String id = rs.getString("id");
                String title = rs.getString("title");
                String year = rs.getString("year");
                String director = rs.getString("director");
                String movieInfo = title+year+director;
                movieDB.put(movieInfo,id);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println("Size of moviedb: "+movieDB.size());
    }

    public void removeInconsistent(){
        HashSet<Movie> tobeRemoved = new HashSet<>();
        for(Movie m : myMovies){
            boolean isDirty = false;
            if(m.getMovieID().length()>10 || m.getDirector().length()>100 || m.getTitle().length()>100){
                isDirty = true;
            }else if(m.getMovieID().equals("")||m.getDirector().equals("")||m.getTitle().equals("")||m.getYear().equals("")){
                isDirty = true;
            }else{
                try{
                    Integer.parseInt(m.getYear());
                }catch (NumberFormatException e){
                    isDirty = true;
                }
            }
            if(isDirty){
                tobeRemoved.add(m);
            }
        }

        //System.out.println("Number of inconsistent movie records: "+ tobeRemoved.size());
        countRemoved+=tobeRemoved.size();
        this.myMovies.removeAll(tobeRemoved);
        //System.out.println("Size of myMovies after removing inconsistent: "+ myMovies.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-movie-records.txt");
            logWriter.append("Inconsistent Movie Records: \n");
            logWriter.append("The following ["+tobeRemoved.size()+"] records are dropped because of violating constraints in the database\n");
            for(Movie s : tobeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeDuplicate(){
        HashSet<Movie> tobeRemoved = new HashSet<>();
        HashSet<String> uniqueMovieInfo = new HashSet<>();
        HashSet<String> uniqueMovieId = new HashSet<>();
        for(Movie m : myMovies){
            String movieInfo = m.getTitle()+m.getYear()+m.getDirector();
            if(this.movieDB.containsKey(movieInfo)){
                tobeRemoved.add(m);
            }
            if(uniqueMovieInfo.contains(movieInfo)){
                tobeRemoved.add(m);
            }
            if(uniqueMovieId.contains(m.getMovieID())){
                tobeRemoved.add(m);
            }
            uniqueMovieInfo.add(movieInfo);
            uniqueMovieId.add(m.getMovieID());
        }

        //System.out.println("Number of duplicated movie records: "+ tobeRemoved.size());
        countRemoved+=tobeRemoved.size();
        this.myMovies.removeAll(tobeRemoved);
        //System.out.println("Size of myMovies after removing duplicated records: "+ myMovies.size());

        try {
            FileWriter logWriter = new FileWriter("inconsistent-movie-records.txt",true);
            logWriter.append("\nThe following ["+tobeRemoved.size()+"] records are dropped because same record exists in database\n");
            for(Movie s : tobeRemoved){
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
            logWriter.append("Movies Records:\n");
            logWriter.append("There are ["+countParsed+"] new movie records parsed from the .xml file\n");
            logWriter.append("  ["+countRemoved+"] movie records were removed, details could be found in <inconsistent-movie-records.txt>\n");
            logWriter.append("  ["+myMovies.size()+"] new movie added to the database\n");
            logWriter.append("\n");
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateCSV(){
        this.loadMoviesFromDB();
        this.removeInconsistent();
        this.removeDuplicate();
        this.updateReport();
        try {
            FileWriter csvWriter = new FileWriter("movies.csv", Charset.forName("ISO8859_1"));
            csvWriter.append("id|title|year|director\n");
            //int count = 0;
            System.out.println("No of Movies when generating csv: " + myMovies.size() + ".");
            for(Movie m: myMovies){
                String id=m.getMovieID();
                String title = m.getTitle();
                String year = m.getYear();
                String director = m.getDirector();

                String csvRow = id+"|"+title+"|"+year+"|"+director+"\n";
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
            String sqlString = "LOAD DATA LOCAL INFILE 'movies.csv'\n" +
                    "INTO TABLE movies\n" +
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
