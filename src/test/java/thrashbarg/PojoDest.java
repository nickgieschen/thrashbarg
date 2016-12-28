package thrashbarg;

public class PojoDest {

    private int getterSetterOnSourceAndDest;
    private int getterOnSourceAndDest;
    private int setterOnSourceAndDest;
    private int getterOnSourceSetterOnDest;
    private int setterOnSourceGetterOnDest;
    private int uniqueNameOnDest;
    private String differentTypeOnDest;

    public int getGetterSetterOnSourceAndDest() {
        return getterSetterOnSourceAndDest;
    }

    public void setGetterSetterOnSourceAndDest(Integer getterSetterOnSourceAndDest) {
        this.getterSetterOnSourceAndDest = getterSetterOnSourceAndDest;
    }

    public Integer getUniqueNameOnDest() {
        return uniqueNameOnDest;
    }

    public void setUniqueNameOnDest(int x) {
        this.uniqueNameOnDest = x;
    }

    public int getGetterOnSourceAndDest() {
        return getterOnSourceAndDest;
    }

    public void setSetterOnSourceAndDest(int setterOnSourceAndDest) {
        this.setterOnSourceAndDest = setterOnSourceAndDest;
    }

    public void setGetterOnSourceSetterOnDest(int getterOnSourceSetterOnDest) {
        this.getterOnSourceSetterOnDest = getterOnSourceSetterOnDest;
    }

    public int getSetterOnSourceGetterOnDest() {
        return setterOnSourceGetterOnDest;
    }

    public String getDifferentTypeOnDest() {
        return differentTypeOnDest;
    }

    public void setDifferentTypeOnDest(String differentTypeOnDest) {
        this.differentTypeOnDest = differentTypeOnDest;
    }
}
