package com.example.webchatserver;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    private static Map<String, String> usernames = new HashMap<String, String>(); // Map<sessionID, username>

    // static means there is only 1 version of map
    private static Map<String, String> roomList = new HashMap<>(); //
    @OnOpen
    public void open(@PathParam("roomID") String roomID, Session session) throws IOException, EncodeException {
        roomList.put(session.getId(), roomID);
        Testing.addRoom(roomID);
        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome to the chat room. Please state your username to begin.\"}");
    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();
        String roomID = roomList.get(userId);
        if (usernames.containsKey(userId)) {
            String username = usernames.get(userId);
            usernames.remove(userId);
            for (Session peer : session.getOpenSessions()){ //broadcast this person left the server
                if(roomList.containsKey(peer.getId()))
                {
                    if(roomList.get(peer.getId()).equals(roomID)) {
                        peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + username + " left the chat room.\"}");
                    }
                }
            }
        }
    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        String userID = session.getId();
        String roomID = roomList.get(userID);
        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = (String) jsonmsg.get("msg");

        if (usernames.containsKey(userID)) { // not their first message
            String username = usernames.get(userID);
            System.out.println(username);
            if(message.equals("/users"))
            {
                usersCommands(session);
            }
            else
            {
                normalMessage(session, roomID, username, message);
            }

        } else { //first message is their username
            usernames.put(userID, message);
            session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome, " + message + "!\"}");
            firstMessage(session, userID, roomID, message);

        }
    }
    public void firstMessage(Session session, String userID, String roomID, String message) throws IOException {
        for(Session peer: session.getOpenSessions()){
            if(!peer.getId().equals(userID) && (roomList.get(peer.getId()).equals(roomID))){
                peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + message + " joined the chat room.\"}");
            }
        }
    }

    public void normalMessage(Session session, String roomID, String username, String message) throws IOException {
        for(Session peer: session.getOpenSessions()){
            if(roomList.containsKey(peer.getId()))
            {
                if(roomList.get(peer.getId()).equals(roomID))
                {
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + message+"\"}");
                }
            }
        }
    }
    public void usersCommands(Session session) throws IOException {
        StringBuilder stringB = new StringBuilder();
        for(String user : usernames.values())
        {
            stringB.append(user).append(",");
        }
        stringB.deleteCharAt(stringB.length() - 1);
        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\" Users In Room: " + stringB.toString() + "!\"}");
    }
}