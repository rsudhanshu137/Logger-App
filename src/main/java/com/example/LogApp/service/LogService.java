package com.example.LogApp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LogService {

    public static final String FILE_PATH="C:\\Users\\pummy\\OneDrive\\Documents\\SystemDesign\\logApp\\LogApp\\src\\main\\resources\\log.txt";
    public static final String DESTINATION="/topic/log";
    public static final String READ_MODE="r";

    public long offset;

    Queue<String>queue;

    public RandomAccessFile randomAccessFile;

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    public LogService() throws IOException {
        queue=new LinkedList<>();
        randomAccessFile=new RandomAccessFile(FILE_PATH,READ_MODE);
        offset=initialOffset();
    }

    public long initialOffset() throws IOException {
        int lineCount = 0;
         offset = 0;

            long fileLength = randomAccessFile.length();

            if (fileLength == 0) {
                return 0; // Empty file
            }

            long pointer = fileLength - 1;
            randomAccessFile.seek(pointer); // Start at the end of the file

            while (pointer >= 0) {
                char c = (char) randomAccessFile.read();

                if (c == '\n' || pointer == 0) { // Line break or start of the file
                    lineCount++;
                    if (lineCount > 10) {
                        offset = pointer + 1; // Set offset to the start of the next line
                        break;
                    }
                }

                pointer--;
                if (pointer >= 0) {
                    randomAccessFile.seek(pointer); // Move the pointer backward
                }
            }

            if (lineCount <= 10) {
                offset = 0; // Entire file has <= 10 lines
            }


        return offset;
    }


    @Scheduled(fixedDelay = 100,initialDelay = 1000)
    public void sendUpdates() throws IOException {
        int fileLength= (int) randomAccessFile.length();
        randomAccessFile.seek(offset);

        while(randomAccessFile.getFilePointer()<fileLength){
            String latestData=randomAccessFile.readLine();
            queue.add(latestData);
            while(queue.size()>10){
                queue.poll();
            }
            ObjectMapper objectMapper=new ObjectMapper();
            String payLoad=objectMapper.writeValueAsString(Map.of("content",latestData));
            simpMessagingTemplate.convertAndSend(DESTINATION,payLoad);
        }
        offset=fileLength;
    }

    @Scheduled(fixedDelay = 500,initialDelay = 10000)
    public void appendToLogFile(){
        try(BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(FILE_PATH,true))){
            String timeStamp= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-DD HH:mm:SS"));
            bufferedWriter.write("["+timeStamp+"]");
            bufferedWriter.newLine();
        }catch (Exception e){
            System.out.println("Error in writing to the file");
        }
    }

    public void getLast10Lines() throws IOException {
        if(queue.size()>0) {
            for (String line : queue) {
                ObjectMapper objectMapper = new ObjectMapper();
                String payLoad = objectMapper.writeValueAsString(Map.of("content", line));
                simpMessagingTemplate.convertAndSend(DESTINATION, payLoad);
            }
        }

    }
}
