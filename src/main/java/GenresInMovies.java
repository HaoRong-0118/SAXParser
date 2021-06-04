public class GenresInMovies {
    String genreName;
    String MovieID;

    public GenresInMovies(){
        this.genreName="";
        this.MovieID="";
    }

    public GenresInMovies(String genreName, String movieID) {
        this.genreName = genreName;
        this.MovieID = movieID;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getMovieID() {
        return MovieID;
    }

    public void setMovieID(String movieID) {
        MovieID = movieID;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GIM Details - ");
        sb.append("ID:" + getMovieID());
        sb.append(", ");
        sb.append("Genre:" + getGenreName());
        sb.append(".");
        return sb.toString();
    }


}
