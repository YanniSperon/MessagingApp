import Data.Chat;
import Data.Group;
import Data.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javafx.util.Pair;

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

    public UUID createNewUser() {
        UUID u = UUID.randomUUID();
        while (users.containsKey(u)) {
            u = UUID.randomUUID();
        }
        users.put(u, new User(u));
        return u;
    }

    public UUID createNewGroup() {
        UUID u = UUID.randomUUID();
        while (groups.containsKey(u)) {
            u = UUID.randomUUID();
        }
        groups.put(u, new Group(u));
        return u;
    }

    public boolean leaveGroup(UUID user, UUID group) {
        boolean res = false;
        if (isValidGroup(group)) {
            Group g = groups.get(group);
            res = g.removeUserIfNecessary(user);
            if (g.isPrivate) {
                if (g.isGroupEmpty()) {
                    System.out.println("Should remove group " + g.name);
                    removeGroup(group);
                }
            }
        }
        return res;
    }

    public void removeUser(UUID uuid) {
        ArrayList<UUID> groupsToDelete = new ArrayList<UUID>();
        for (Map.Entry<UUID, Group> pair : groups.entrySet()) {
            Group g = pair.getValue();
            if (g.isPrivate) {
                g.removeUserIfNecessary(uuid);
                if (g.isGroupEmpty()) {
                    System.out.println("Should remove group " + g.name);
                    groupsToDelete.add(pair.getKey());
                }
            }
        }
        for (UUID entry : groupsToDelete) {
            removeGroup(entry);
        }
        users.remove(uuid);
    }

    public void addToGroup(UUID user, UUID group)
    {
        if (isValidUser(user) && isValidGroup(group)) {
            groups.get(group).users.add(user);
        }
    }

    public void removeGroup(UUID uuid) {
        groups.remove(uuid);
        groupChats.remove(uuid);
    }

    public boolean containsUsername(String username) {
        boolean res = false;
        for (Map.Entry<UUID, User> pair : users.entrySet()) {
            if (pair.getValue().username != null && pair.getValue().username.equals(username)) {
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
        if (u1 == null || u2 == null) {
            return null;
        }
        if (u1.compareTo(u2) < 0) {
            UUID temp = u1;
            u1 = u2;
            u2 = temp;
        }
        Pair<UUID, UUID> p = new Pair<UUID, UUID>(u1, u2);
        if (!directMessages.containsKey(p)) {
            directMessages.put(p, new Chat());
        }
        return directMessages.get(p);
    }

    public Chat getGroupChat(UUID group) {
        if (group == null) {
            return null;
        }
        if (!groupChats.containsKey(group)) {
            groupChats.put(group, new Chat());
        }
        return groupChats.get(group);
    }

    public boolean isValidUser(UUID user) {
        return users.containsKey(user);
    }

    public boolean isValidGroup(UUID group) {
        return groups.containsKey(group);
    }

    public UUID getByUsername(String username) {
        for (Map.Entry<UUID, User> pair : users.entrySet()) {
            if (pair.getValue().username != null && pair.getValue().username.equals(username)) {
                return pair.getKey();
            }
        }
        return null;
    }

    public UUID getByGroupName(String groupName) {
        for (Map.Entry<UUID, Group> pair : groups.entrySet()) {
            if (pair.getValue().name != null && pair.getValue().name.equals(groupName)) {
                return pair.getKey();
            }
        }
        return null;
    }
}
