package org.example.service.observer;

import org.example.domain.VideoRequest;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    // Список всех подписчиков (например, веб-клиентов)
    private List<JobSubscriber> subscribers = new ArrayList<>();

    // Метод для добавления нового подписчика
    public void subscribe(JobSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    // Метод, который мы вызываем, когда работа закончена
    public void notifyAllSubscribers(VideoRequest request) {
        System.out.println("Observer: Notifying " + subscribers.size() + " subscribers about job completion...");
        for (JobSubscriber subscriber : subscribers) {
            subscriber.onJobCompleted(request); // Рассылаем уведомление каждому
        }
    }
}