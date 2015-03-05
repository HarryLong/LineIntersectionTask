/**
 * Created by harry on 2014/10/22.
 */
public class Subject {
    Integer uniqueId; // db stuff
    String name;
    boolean rightHanded, hasGlasses, glassesOn;

    Subject(String name, boolean rightHanded, boolean hasGlasses, boolean glassesOn, Integer uniqueId)
    {
        this.name = name;
        this.rightHanded = rightHanded;
        this.hasGlasses = hasGlasses;
        this.glassesOn = glassesOn;
        this.uniqueId = uniqueId;
    }
}
