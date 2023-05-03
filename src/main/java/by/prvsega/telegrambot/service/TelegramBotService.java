package by.prvsega.telegrambot.service;

import by.prvsega.telegrambot.config.BotConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@AllArgsConstructor
public class TelegramBotService extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String name = update.getMessage().getChat().getFirstName();
            switch (messageText){
                case "/start": startCommand(chatId, name);
                break;
                default: sendMessage(chatId, "Sorry " + name + ", but this command won't work");

            }

        }

    }

    public void startCommand(long chatId, String name){
            String answer = "Hi, " + name + ", nice to meet you!";
            sendMessage(chatId, answer);
    }

    public void sendMessage(long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        try{
            execute(sendMessage);
        }
        catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }
    @Override
    public String getBotToken(){
        return botConfig.getToken();
    }



}
