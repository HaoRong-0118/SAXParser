public class Main {
    public static void main(String [] args){

        //create parsers
        SAXParserActors spa = new SAXParserActors();
        SAXParserMain spm = new SAXParserMain();
        SAXParserCasts spc = new SAXParserCasts();

        //execute
        spa.runExample();
        spm.runExample();
        spc.runExample();

        StarCSVBuilder builderS = new StarCSVBuilder(spa.myStars);
        GenreCSVBuilder builderG = new GenreCSVBuilder(spm.genres);
        MoviesCSVBuilder builderM = new MoviesCSVBuilder(spm.myMovies);
        GIMCSVBuilder builderGIM = new GIMCSVBuilder(spm.myGenresInMovies);
        SIMCSVBuilder builderSIM = new SIMCSVBuilder(spc.mySIMs);

        //load the [movies], [stars] and [genres] csv into database before generate sim.csv and gim.csv
        builderS.generateCSV();  //stars.csv
        builderG.generateCSV();  //genres.csv
        builderM.generateCSV();  //movies.csv

        //load csv to db
        builderS.loadCSV();
        builderG.loadCSV();
        builderM.loadCSV();

        //generates the dependent csv
        builderGIM.generateCSV();
        builderSIM.generateCSV();

        //load relations into DB
        builderGIM.loadCSV();
        builderSIM.loadCSV();

    }
}
