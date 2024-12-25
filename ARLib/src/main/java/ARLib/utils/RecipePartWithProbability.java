package ARLib.utils;

public class RecipePartWithProbability extends RecipePart{

    public RecipePartWithProbability(String id, int num, float p) {
        super(id,num);
        this.p = p;
    }

    public RecipePartWithProbability(String id, int num) {
        super(id,num);
    }

    public RecipePartWithProbability(String id) {
        super(id);
    }

    public float p = 1;         // probability to produce / consume
    int actual_num;  // how much is actually consumed / produced after 'rolling the dice'
    public int getRandomAmount(){return actual_num;}

    public void computeRandomAmount() {
        actual_num = 0;
        // Roll the dice `num` times if `p < 1`
        for (int i = 0; i < amount; i++) {
            if (p >= 1 || Math.random() < p) {
                actual_num++;
            }
        }
    }
}
