package ProjectSteam;

import com.ibm.icu.impl.Pair;

import java.util.List;

public class utils {
    public static double calculateWeightedAverage(List<Pair<Double, Double>> values_weights) {
        double maxWeight = 0.0;

        // Find the maximum weight to normalize weights
        for (Pair<Double, Double> momentum : values_weights) {
            maxWeight = Math.max(maxWeight, momentum.second);
            System.out.println(momentum.first+":"+momentum.second);
        }

        if (maxWeight == 0) {
            throw new IllegalArgumentException("All weights are zero.");
        }

        double weightedSum = 0.0;
        double normalizedTotalWeight = 0.0;

        // Compute the weighted sum using normalized weights
        for (Pair<Double, Double> momentum : values_weights) {
            double value = momentum.first;
            double weight = momentum.second / maxWeight; // Normalize weight

            weightedSum += value * weight;
            normalizedTotalWeight += weight;
        }

        if (normalizedTotalWeight == 0) {
            throw new IllegalArgumentException("Normalized total weight must not be zero.");
        }

        return weightedSum / normalizedTotalWeight;  // Calculate the weighted average
    }
}

