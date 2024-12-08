package ProjectSteam.api;

public class MechanicalFlowData {


    public double combinedTransformedMass;

    //List<Pair<Double, Double>> momentums = new ArrayList<>();

    // I will use clockwise rotation of the axis as positive
    // and counter-clockwise rotation as negative.
    // (rotation you see facing the + direction of the axis)
    public double combinedTransformedForce;

    public double combinedTransformedResistanceForce;

    public double combinedTransformedMomentum;
}
