package thrashbarg;

public class PojoSource {

    private int getterSetterOnSourceAndDest = 1;
    private int getterOnSourceAndDest = 2;
    private int setterOnSourceAndDest = 3;
    private int getterOnSourceSetterOnDest = 4;
    private int setterOnSourceGetterOnDest = 5;
    private int uniqueNameOnSource = 6;
    private int differentTypeOnDest = 7;

    public int getGetterSetterOnSourceAndDest() {
        return getterSetterOnSourceAndDest;
    }
    public void setGetterSetterOnSourceAndDest(int getterSetterOnSourceAndDest) {
        this.getterSetterOnSourceAndDest = getterSetterOnSourceAndDest;
    }

    public int getUniqueNameOnSource() {
        return uniqueNameOnSource;
    }

    public void setUniqueNameOnSource(int uniqueNameOnSource) {
        this.uniqueNameOnSource = uniqueNameOnSource;
    }

    public int getGetterOnSourceAndDest() {
        return getterOnSourceAndDest;
    }

    public void setSetterOnSourceAndDest(int setterOnSourceAndDest) {
        this.setterOnSourceAndDest = setterOnSourceAndDest;
    }

    public int getDifferentTypeOnDest() {
        return differentTypeOnDest;
    }

    public void setDifferentTypeOnDest(int differentTypeOnDest) {
        this.differentTypeOnDest = differentTypeOnDest;
    }

    public int getGetterOnSourceSetterOnDest() {
        return getterOnSourceSetterOnDest;
    }

    public void setSetterOnSourceGetterOnDest(int setterOnSourceGetterOnDest) {
        this.setterOnSourceGetterOnDest = setterOnSourceGetterOnDest;
    }
}
