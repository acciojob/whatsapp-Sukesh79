package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }


    public Boolean getMobile(String mobile) {
        return userMobile.contains(mobile);
    }
    public void addUser(String name, String moblie){
        userMobile.add(moblie);
    }

    public Group createGroup(List<User> users) {
        if(users.size() == 2){
            Group group = new Group(users.get(1).getName(), 2);
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            return group;
        }

        else{
            customGroupCount++;
            Group group = new Group("Group " + customGroupCount, users.size());
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            adminMap.put(group, users.get(0));
            return group;
        }
    }

    public int createMessage(String content) {
        messageId++;
        Message message = new Message(messageId,content,new Date());
        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!groupMessageMap.containsKey(group)) throw new Exception("Group does not exist");
        if(!groupUserMap.get(group).contains(sender)) throw new Exception("You are not allowed to send message");
        groupMessageMap.get(group).add(message);
        senderMap.put(message, sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(!groupUserMap.containsKey(group)) throw new Exception("Group does not exist");
        if(adminMap.get(group) != approver) throw new Exception("Approver does not have rights");
        if(!groupUserMap.get(group).contains(user)) throw new Exception("User is not a participant");

        adminMap.put(group, user);

        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        for(Group group : groupUserMap.keySet()){
            List<User> userList = groupUserMap.get(group);
            if(userList.contains(user)){
                for(User admin : adminMap.values()){
                    if(user == admin) throw new Exception("cannot remove admin");
                }
                groupUserMap.get(group).remove(user);

                for(Message m : senderMap.keySet()){
                    User u = senderMap.get(m);
                    if(u == user){
                        senderMap.remove(m);
                        groupMessageMap.get(group).remove(m);
                    }

                    return groupUserMap.get(group).size() + groupMessageMap.get(group).size() + senderMap.size();
                }
            }
        }

        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        TreeMap<Integer, String> map = new TreeMap<>();
        ArrayList<Integer> list = new ArrayList<>();

        for(Message message : senderMap.keySet()){
            if(message.getTimestamp().compareTo(start) > 0 && message.getTimestamp().compareTo(end) < 0){
                map.put(message.getId(), message.getContent());
                list.add(message.getId());
            }
        }
        if(map.size() > k) throw new Exception("K is greater than the number of messages");

        Collections.sort(list);
        int id  = list.get(k);

        return map.get(id);
    }
}
