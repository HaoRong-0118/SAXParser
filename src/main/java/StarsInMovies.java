public class StarsInMovies {
    String movieId;
    String starId;
    String starName;

    public StarsInMovies(){
        this.movieId="";
        this.starId="";
        this.starName="";
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getStarId() {
        return starId;
    }

    public void setStarId(String starId) {
        this.starId = starId;
    }

    public String getStarName() {
        return starName;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SIM Details - ");
        sb.append("movieId:" + getMovieId());
        sb.append(", ");
        sb.append("starName:" + getStarName());
        sb.append(", ");
        sb.append("starId:" + getStarId());
        sb.append(".");
        return sb.toString();
    }
}
