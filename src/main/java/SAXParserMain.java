import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SAXParserMain extends DefaultHandler {
    List<Movie> myMovies;
    List<GenresInMovies> myGenresInMovies;
    Set<String> genres;

    private String tempVal;
    private String tempDirector;
    private String tempMovieId;
    //to maintain context
    private Movie tempMovie;

    public SAXParserMain() {
        myMovies = new ArrayList<Movie>();
        genres = new HashSet<>();
        myGenresInMovies = new ArrayList<GenresInMovies>();
    }

    public void runExample() {
        parseDocument();
        printData();
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            InputStream inputStream= new FileInputStream("mains243.xml");
            InputStreamReader inputReader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);
            InputSource inputSource = new InputSource(inputReader);
            inputSource.setEncoding("ISO-8859-1");
            sp.parse(inputSource, this);


        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {
        System.out.println("No of Movies '" + myMovies.size() + "'.");
        System.out.println("No of GIM '" + myGenresInMovies.size() + "'.");
        System.out.println("No of Genres '" + genres.size() + "'.");
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            //create a new instance of employee
            tempMovie = new Movie();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("film")) {
            //add it to the list
            tempMovie.setDirector(tempDirector);
            this.myMovies.add(tempMovie);
        } else if (qName.equalsIgnoreCase("dirname")) {
            tempDirector = tempVal;
        } else if (qName.equalsIgnoreCase("fid")) {
            tempMovie.setMovieID(tempVal);
            tempMovieId = tempVal;
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setYear(tempVal);
        } else if (qName.equalsIgnoreCase("cat")) { //生成所有genres
            genres.add(tempVal.toLowerCase());
            GenresInMovies gim = new GenresInMovies(tempVal.toLowerCase(),this.tempMovieId);
            this.myGenresInMovies.add(gim);
        }
    }

//    public static void main(String[] args) {
//        SAXParserMain spm = new SAXParserMain();
//        spm.runExample();
//        //build genre csv
//        GenreCSVBuilder builder = new GenreCSVBuilder(spm.genres);
//        builder.generateCSV();
//
//        //build movie csv
//        MoviesCSVBuilder builderm = new MoviesCSVBuilder(spm.myMovies);
//        builderm.generateCSV();
//
//    }
}
