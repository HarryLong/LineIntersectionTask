/**
 * Created by harry on 2014/10/22.
 */

public class Subject {
    Integer uniqueId; // db stuff
    String name;
    boolean hasGlasses, glassesOn;
    Helper.Hand goodHand;

    Subject(String name, Helper.Hand goodHand, boolean hasGlasses, boolean glassesOn, Integer uniqueId)
    {
        this.name = name;
        this.goodHand = goodHand;
        this.hasGlasses = hasGlasses;
        this.glassesOn = glassesOn;
        this.uniqueId = uniqueId;
    }
}
