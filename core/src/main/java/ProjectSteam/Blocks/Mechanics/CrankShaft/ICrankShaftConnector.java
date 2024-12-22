package ProjectSteam.Blocks.Mechanics.CrankShaft;

import java.util.List;

public interface ICrankShaftConnector {
    public enum CrankShaftType{
        SMALL,
        LARGE
    }

    List<CrankShaftType> getConnectableCrankshafts();
}
