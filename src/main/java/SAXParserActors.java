import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class SAXParserActors extends DefaultHandler {

    List<Star> myStars;

    private String tempVal;

    //to maintain context
    private Star tempStar;

    public SAXParserActors() {
        myStars = new ArrayList<Star>();
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

            InputStream inputStream= new FileInputStream("actors63.xml");
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

        System.out.println("No of Stars '" + myStars.size() + "'.");

//        Iterator<Star> it = myStars.iterator();
//        System.out.println("First 20 in the list");
//        int i=0;
//        while (it.hasNext()) {
//            System.out.println(it.next().toString());
//            i++;
//            if(i==19){
//                break;
//            }
//        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            //create a new instance of employee
            tempStar = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("actor")) {
            //add it to the list
            this.myStars.add(tempStar);
        } else if (qName.equalsIgnoreCase("stagename")) {
            tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
            tempStar.setBirthYear(tempVal);
        }

    }

}
