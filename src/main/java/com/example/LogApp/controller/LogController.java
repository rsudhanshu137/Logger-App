package com.example.LogApp.controller;

import com.example.LogApp.model.Message;
import com.example.LogApp.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class LogController {

    @Autowired
    LogService logService;

    @MessageMapping("/logs")
    @SendTo("/topic/log")
    public Message getFileUpdates(Message message){
        return message;
    }

    @SubscribeMapping("/topic/log")
    public void initialSubsription() throws IOException {
        logService.getLast10Lines();
    }
}
