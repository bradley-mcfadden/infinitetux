package ch.idsia.tools;

public class ActionTrace {
    private int frame;
    private int[] keys;
    public static final String[] KEYS = {"LEFT  ", "RIGHT ", " DOWN ", " JUMP ", " SPEED"};

    public ActionTrace(int frame, int[] keys)
    {
        this.frame = frame;
        this.keys = keys;
    }

    public int getKey(int index)
    {
        return keys[index];
    }

    public int getFrame()
    {
        return frame;
    }


}
