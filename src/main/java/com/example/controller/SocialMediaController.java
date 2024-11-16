package com.example.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.entity.Account;
import com.example.entity.Message;
import com.example.repository.AccountRepository;
import com.example.repository.MessageRepository;
import com.example.service.AccountService;
import com.example.service.MessageService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;


/**
 * TODO: You will need to write your own endpoints and handlers for your controller using Spring. The endpoints you will need can be
 * found in readme.md as well as the test cases. You be required to use the @GET/POST/PUT/DELETE/etc Mapping annotations
 * where applicable as well as the @ResponseBody and @PathVariable annotations. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */

@RestController
@Slf4j
public class SocialMediaController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MessageService messageService;


    @PostMapping("/register")
    public ResponseEntity<Account> register(@RequestBody Account account) {
        // Check if username is not blank
        if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Check if password is at least 4 characters long
        if (account.getPassword() == null || account.getPassword().length() < 4) {
            return ResponseEntity.badRequest().body(null);
        }

        // Check if an account with the given username already exists
        Account existingAccount = accountRepository.findByUsername(account.getUsername());
        if (existingAccount != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        // Save the new account
        Account newAccount = accountRepository.save(account);
        return ResponseEntity.ok(newAccount);
    }

    @PostMapping("/login")
    public ResponseEntity<Account> login(@RequestBody Account account) {
        // Find the account by username
        Account existingAccount = accountRepository.findByUsername(account.getUsername());

        // Check if account exists and password matches
        if (existingAccount != null && existingAccount.getPassword().equals(account.getPassword())) {
            return ResponseEntity.ok(existingAccount);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> createMessage(@RequestBody Message message) {
        // Check if message text is not blank and under 255 characters
        if (message.getMessageText() == null || message.getMessageText().trim().isEmpty() || message.getMessageText().length() >= 255) {
            return ResponseEntity.badRequest().body(null);
        }

        // Check if posted_by refers to a real existing user
        Optional<Account> existingAccount = accountRepository.findById(message.getPostedBy());
        if (!existingAccount.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account does not exist");
        }
      
      
        // Optional<Account> existingAccount = accountRepository.findById(message.getPostedBy());
        // if (existingAccount == null) {
        //     return ResponseEntity.badRequest().body(null);
        // }

        // Save the new message
        Message newMessage = messageRepository.save(message);
        return ResponseEntity.ok(newMessage);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<Message>> getAllMessages() {
        List<Message> messages = messageRepository.findAll();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/messages/{message_id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Integer message_id) {
        Message message = messageRepository.findById(message_id).orElse(null);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/messages/{message_id}")
    public ResponseEntity<?> deleteMessageById(@PathVariable Integer message_id) {
        log.info("deleteMessageById called with messageId: {}", message_id);

        try {
            boolean exists = messageRepository.existsById(message_id);
            if (exists) {
                messageRepository.deleteById(message_id);
                log.info("Message with messageId: {} deleted.", message_id);
                return ResponseEntity.ok(1);  // Return 1 row deleted
            } else {
                log.info("Message with messageId: {} not found.", message_id);
                return ResponseEntity.ok().build();  // Return 200 OK with empty body
            }
        } catch (Exception e) {
            log.error("An error occurred while trying to delete message with messageId: {}", message_id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");}
        
        // //log.info("deleteMessageById called with messageId: " + messageId); // Print statement for debugging
        // //System.out.print("Saleh");//debugging

        // boolean exists = messageRepository.existsById(messageId);
        // if (exists) {
        //     messageRepository.deleteById(messageId);
        //     return ResponseEntity.ok(1);  // Return 1 row deleted
        // } else {
        //     return ResponseEntity.ok().build();  // Return 200 OK with empty body
        // }
    }

    @PatchMapping("/messages/{message_id}")
    public ResponseEntity<?> updateMessageById(@PathVariable Integer message_id, @RequestBody Message updatedMessage) {
        // Check if the message ID exists
        if (!messageRepository.existsById(message_id)) {
            return ResponseEntity.badRequest().body("Message ID does not exist");
        }

        // Validate the new message_text
        String newText = updatedMessage.getMessageText();
        if (newText == null || newText.trim().isEmpty() || newText.length() >= 255) {
            return ResponseEntity.badRequest().body("Invalid message text");
        }

        // Update the message text
        Message existingMessage = messageRepository.findById(message_id).orElse(null);
        if (existingMessage != null) {
            existingMessage.setMessageText(newText);
            messageRepository.save(existingMessage);
            return ResponseEntity.ok(1);
        }

        return ResponseEntity.badRequest().body("Update failed");
    }

    @GetMapping("/accounts/{account_id}/messages")
    public ResponseEntity<List<Message>> getAllMessagesFromUser(@PathVariable Integer account_id) {
        List<Message> messages = messageRepository.findByPostedBy(account_id);
        return ResponseEntity.ok(messages);
    }




}
