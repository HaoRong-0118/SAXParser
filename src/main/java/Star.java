

public class Star {

    private String name;

    private String birthYear;


    public Star(){
        this.birthYear="";
    }

    public Star(String id, String name, String birthYear) {
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("Name:" + getName());
        sb.append(", ");
        sb.append("BirthYear:" + getBirthYear());
        sb.append(".");
        return sb.toString();
    }
}
