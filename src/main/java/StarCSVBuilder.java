import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class StarCSVBuilder {
    Hashtable<String,String> starsDB;
    List<Star> myStars;
    int countParsed;
    int countRemoved;

    StarCSVBuilder(List<Star> myStars){
        this.myStars = myStars;
        this.starsDB = new Hashtable<>();
        this.countRemoved = 0;
        this.countParsed = myStars.size();
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from stars;");
            while(rs.next()){
                String name = "";
                String birthYear = "null";
                String id = rs.getString("id");
                name = rs.getString("name"); //name is NOT NULL with db constraint
                if(rs.getString("birthYear")!= null){
                    birthYear = rs.getString("birthYear");
                }
                starsDB.put(name+birthYear,id);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void removeExistingRecord(){
        HashSet<Star> toBeRemoved = new HashSet<>();
        HashSet<String> uniqueStarInfo = new HashSet<>();
        int count = 0;
        for (Star s: myStars) {
            String dob = s.getBirthYear();
            if(dob.equals("")){
                dob="null";
            }
            String starInfo = s.getName()+dob;
            if(starsDB.keySet().contains(starInfo)){   //already exist in database, remove
                toBeRemoved.add(s);
                count++;
            }
            if(uniqueStarInfo.contains(starInfo)){
                toBeRemoved.add(s);
            }
            uniqueStarInfo.add(starInfo);
        }


        //System.out.println("Number of records removed because of collision: "+ toBeRemoved.size());
        int beforeRemove = myStars.size();
        myStars.removeAll(toBeRemoved);
        countRemoved += beforeRemove - myStars.size();
        //record all removed file:
        try {
            FileWriter logWriter = new FileWriter("inconsistent-star-records.txt");
            logWriter.append("Inconsistent Star Records: \n");
            logWriter.append("The following ["+toBeRemoved.size()+"] records are dropped because same [name] [birthYear] exist in the database\n");
            for(Star s : toBeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeInvalidDob(){
        HashSet<Star> toBeRemoved = new HashSet<>();
        for (Star s: myStars) {
            try{
                if(s.getBirthYear().equals("")){
                    continue;
                }
                int dob = Integer.parseInt(s.getBirthYear());
            }
            catch(NumberFormatException nfe){
                toBeRemoved.add(s);
            }
        }
        //System.out.println("Number of records removed because of invalid dob: "+ toBeRemoved.size());
        countRemoved += toBeRemoved.size();
        myStars.removeAll(toBeRemoved);
        //System.out.println(toBeRemoved);

        try {
            FileWriter logWriter = new FileWriter("inconsistent-star-records.txt",true);
            logWriter.append("\nThe following ["+toBeRemoved.size()+"] records are dropped because the value of [birthYear] is inconsistent with DB\n");
            for(Star s : toBeRemoved){
                logWriter.append(s.toString()+"\n");
            }
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLargestID(){
        String maxId = "";
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false","mytestuser","My6$Password");
            Statement stmt= con.createStatement();
            ResultSet rs=stmt.executeQuery("select max(id) as maxId from stars;");
            while(rs.next()){
                maxId = rs.getString("maxId");
                //System.out.println(maxId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return maxId;
    }

    public void updateReport(){
        try {
            FileWriter logWriter = new FileWriter("Report.txt");
            logWriter.append("Stars Records:\n");
            logWriter.append("There are ["+countParsed+"] new star records parsed from the .xml file\n");
            logWriter.append("  ["+countRemoved+"] star records were removed, details could be found in <inconsistent-star-records.txt>\n");
            logWriter.append("  ["+myStars.size()+"] new stars added to the database\n");
            logWriter.append("\n");
            logWriter.flush();
            logWriter.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateCSV(){
        this.removeExistingRecord();
        this.removeInvalidDob();
        this.updateReport();
        int id = Integer.parseInt(getLargestID().substring(2))+1;
        try {
            File f = new File("stars.csv");
            FileWriter csvWriter = new FileWriter(f, Charset.forName("ISO8859_1"));
            csvWriter.append("id,name,birthYear\n");
            //int count = 0;
            System.out.println("No of Stars when generating csv: " + myStars.size() + ".");
            for(Star s: myStars){
                String birthYear="";
                birthYear = s.getBirthYear();
                if(birthYear.equals("")){
                    birthYear = "\\N";
                }
                String csvRow = "nm"+id+","+s.getName()+","+birthYear+"\n";
                csvWriter.append(csvRow);
                id++;
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
            String sqlString = "LOAD DATA LOCAL INFILE 'stars.csv'\n" +
                    "INTO TABLE stars\n" +
                    "CHARACTER SET utf8mb4\n" +
                    "FIELDS TERMINATED BY ',' ENCLOSED BY ''\n" +
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
