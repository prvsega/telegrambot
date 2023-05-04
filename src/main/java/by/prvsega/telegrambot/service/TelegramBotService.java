package by.prvsega.telegrambot.service;

import by.prvsega.telegrambot.config.BotConfig;
import by.prvsega.telegrambot.model.User;
import by.prvsega.telegrambot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    private final UserRepository userRepository;

    private static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities. \n\n"
            + "You can execute commands from main menu\n\n"
            +"Type /start to see a welcom message\n\n"
            +"Type /mydata to see information about you\n\n"
            +"Link or not? - https://seoclick.by";
    private final BotConfig botConfig;

    @Autowired
    public TelegramBotService(UserRepository userRepository, BotConfig botConfig) {
        this.userRepository = userRepository;
        this.botConfig = botConfig;
        createdMenu();
    }



    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();
            switch (messageText) {
                case "/start":
                    startCommand(chatId, name);
                    registerUser(update.getMessage());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Sorry " + name + ", but this command won't work");

            }

        }

    }

    private void registerUser(Message message) {
      if (userRepository.findById(message.getChatId()).isEmpty()){
           var chatId = message.getChatId();
           var chat = message.getChat();
           User user = new User();

           user.setChatId(chatId);
           user.setUsername(chat.getUserName());
           user.setFirstName(chat.getFirstName());
           user.setLastName(chat.getLastName());
           user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

           userRepository.save(user);
           log.info("user saved: " + user);

      }


    }

    public void startCommand(long chatId, String name) {
        String answer = "Hi, " + name + ", nice to meet you!";
        log.info("Bot replied to user " + name);
        sendMessage(chatId, answer);
    }

    public void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void createdMenu() {
        List<BotCommand> list = new ArrayList<>();
        list.add(new BotCommand("/start", "get a welcome message"));
        list.add(new BotCommand("/mydata", "get an information about user"));
        list.add(new BotCommand("/deletedata", "delete my data"));
        list.add(new BotCommand("/help", "info how to use this bot"));
        list.add(new BotCommand("/settings", "set your preferences"));

        try {
            this.execute(new SetMyCommands(list, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list" + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


}
