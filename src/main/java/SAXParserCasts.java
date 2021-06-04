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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SAXParserCasts extends DefaultHandler {
    List<StarsInMovies> mySIMs;
    private String tempVal;
    private StarsInMovies tempSIM;

    public SAXParserCasts() {
        mySIMs = new ArrayList<StarsInMovies>();
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

            InputStream inputStream= new FileInputStream("casts124.xml");
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
        System.out.println("No of SIM records '" + mySIMs.size() + "'.");
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
            //create a new instance of employee
            tempSIM = new StarsInMovies();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("m")) {
            //add it to the list
            this.mySIMs.add(tempSIM);
        } else if (qName.equalsIgnoreCase("f")) {
            this.tempSIM.setMovieId(tempVal);
        }
        else if(qName.equalsIgnoreCase("a")){
            this.tempSIM.setStarName(tempVal);
        }
    }
}
