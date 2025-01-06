package NPCs.programs;

public enum ExitCode {
    SUCCESS_STILL_RUNNING,
    EXIT_SUCCESS,
    EXIT_FAIL;

    public boolean isStillRunning() {
        return this == ExitCode.SUCCESS_STILL_RUNNING;
    }

    public boolean isCompleted() {
        return this == ExitCode.EXIT_SUCCESS;
    }

    public boolean isFailed() {
        return this == ExitCode.EXIT_FAIL;
    }

    public boolean isEnd() {
        return this == EXIT_SUCCESS || this == EXIT_FAIL;
    }
}
