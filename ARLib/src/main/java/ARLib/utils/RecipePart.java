package ARLib.utils;

public class RecipePart {
    public RecipePart(String id, int num) {
        this.id = id;
        this.amount = num;
    }

    public RecipePart(String id) {
        this.id = id;
    }

    public RecipePart() {}

    public String id = "";       // id/tag
    public int amount = 1;         // how often to 'roll the dice' / max num

}
