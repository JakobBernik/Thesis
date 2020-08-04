package thesis.testing.utils;

/**
 * Enables simple configuration of data point capture for service
 */
public class Configuration {

    private String pointIdMatch;
    private boolean checkValid;
    private int valueCap;

    public Configuration(boolean check, int cap, String id){
        checkValid = check;
        valueCap = cap;
        pointIdMatch = id;
    }

    public void setPointIdMatch(String pointIdMatch) {
        this.pointIdMatch = pointIdMatch;
    }

    public void setCheckValid(boolean checkValid) {
        this.checkValid = checkValid;
    }

    public void setValueCap(int valueCap) {
        this.valueCap = valueCap;
    }

    public String getPointIdMatch() {
        return pointIdMatch;
    }

    public int getValueCap() {
        return valueCap;
    }

    public boolean isCheckValid() {
        return checkValid;
    }
}
