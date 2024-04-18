import Data.Chat;
import Data.Group;
import Data.User;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    public final HashMap<UUID, Group> groups;
    public final HashMap<UUID, User> users;
    public final HashMap<Pair<UUID, UUID>, Chat> directMessages;
    public final HashMap<UUID, Chat> groupChats;

    DataManager() {
        groups = new HashMap<UUID, Group>();
        users = new HashMap<UUID, User>();
        directMessages = new HashMap<Pair<UUID, UUID>, Chat>();
        groupChats = new HashMap<UUID, Chat>();
    }

    public boolean containsUsername(String username) {
        boolean res = false;
        for (Map.Entry<UUID, User> pair : users.entrySet()) {
            if (pair.getValue().username.equals(username)) {
                res = true;
            }
        }
        return res;
    }

    public boolean containsGroupName(String name) {
        boolean res = false;
        for (Map.Entry<UUID, Group> pair : groups.entrySet()) {
            if (pair.getValue().name.equals(name)) {
                res = true;
            }
        }
        return res;
    }

    public Chat getDM(UUID u1, UUID u2) {
        if (u1.compareTo(u2) < 0) {
            UUID temp = u1;
            u1 = u2;
            u2 = temp;
        }
        Pair<UUID, UUID> p = new Pair<UUID, UUID>(u1, u2);
        if (directMessages.containsKey(p)) {
            return directMessages.get(p);
        } else {
            return directMessages.put(p, new Chat());
        }
    }

    public Chat getGroupChat(UUID group) {
        if (groupChats.containsKey(group)) {
            return groupChats.get(group);
        } else {
            return groupChats.put(group, new Chat());
        }
    }

    public boolean isValidUser(UUID user) {
        return users.containsKey(user);
    }

    public boolean isValidGroup(UUID group) {
        return groups.containsKey(group);
    }

    public UUID getGlobalGroup()
    {
        for (Map.Entry<UUID, Group> pair : groups.entrySet()) {
            Group g = pair.getValue();
            if (g.name.equals("Global")) {
                return pair.getKey();
            }
        }
        return null;
    }
}
