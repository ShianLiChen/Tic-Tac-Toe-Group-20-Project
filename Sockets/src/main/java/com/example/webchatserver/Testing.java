package com.example.webchatserver;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "testing", value = "/testing")
public class Testing extends HttpServlet {

    public static ArrayList<String> rooms = new ArrayList<>();

    public String existingRooms() throws IOException {
        StringBuilder roomToString = new StringBuilder();
        for(String room : rooms)
        {
            roomToString.append(room).append(",");
        }
        return roomToString.toString();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");

        // send the random code as the response's content
        PrintWriter out = response.getWriter();
        out.println(existingRooms());
    }

    public static void addRoom(String room)
    {
        if(!rooms.contains(room))
        {
            rooms.add(room);
        }
    }

    public void destroy() {
    }
}