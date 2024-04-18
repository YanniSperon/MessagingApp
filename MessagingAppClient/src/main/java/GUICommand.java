import java.io.Serializable;

public class GUICommand implements Serializable {
    public enum Type {
        LOGIN_SUCCESS, LOGIN_ERROR,
        REFRESH,
    }

    public Type type;
    GUICommand(Type type)
    {
        this.type = type;
    }
}
