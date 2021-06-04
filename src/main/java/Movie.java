public class Movie {
    String movieID;
    String title;
    String year;
    String director;

    public Movie(){
        this.movieID = "";
        this.title = "";
        this.year = "";
        this.director = "";
    }

    public Movie(String movieID, String title, String year, String director) {
        this.movieID = movieID;
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("ID:" + getMovieID());
        sb.append(", ");
        sb.append("Title:" + getTitle());
        sb.append(", ");
        sb.append("Year:" + getYear());
        sb.append(", ");
        sb.append("Director:" + getDirector());
        sb.append(".");
        return sb.toString();
    }
}
