package com.example.websocket.controller;

import com.example.websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 登录管理
 *
 * @author GuoJun
 * @since 1.0.0
 * Created by GuoJun on 2020/05/06
 */
@RestController
public class AccessController extends AbstractController {

    @PostMapping("/sendAllWebSocket")
    public ResponseEntity<String> test() {
        String text = "你们好！这是websocket群体发送！";
        try {
            WebSocketServer.sendGroupMessage(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpStatus status = HttpStatus.OK;
        return new ResponseEntity<>(text, status);
    }
}
