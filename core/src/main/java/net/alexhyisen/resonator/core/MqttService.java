/*
 * This file is part of resonator.
 *
 * resonator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * resonator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with resonator.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.alexhyisen.resonator.core;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttService {
    private MqttClient mqttClient;

    private static void handleMqttException(MqttException me) {
        System.out.println("reason " + me.getReasonCode());
        System.out.println("msg " + me.getMessage());
        System.out.println("loc " + me.getLocalizedMessage());
        System.out.println("cause " + me.getCause());
        System.out.println("excep " + me);
        me.printStackTrace();
    }

    public boolean connect(String broker, String client, String username, String password) {
        try {
            mqttClient = new MqttClient(broker, client);
            var options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setCleanSession(true);//A simple reporter do not need states.
            System.out.println("Connecting to broker: " + broker);
            mqttClient.connect(options);
            System.out.println("Connected");
            return true;
        } catch (MqttException me) {
            handleMqttException(me);
            return false;
        }
    }

    public boolean publish(String topic, String content) {
        try {
            System.out.println("Publishing message: \n" + content);
            mqttClient.publish(topic, content.getBytes(), 0, false);
            System.out.println("Message published");
            return true;
        } catch (MqttException me) {
            handleMqttException(me);
            return false;
        }
    }

    public boolean disconnect() {
        try {
            mqttClient.disconnect();
            System.out.println("Disconnected");
            mqttClient.close();
            System.out.println("Closed");
            return true;
        } catch (MqttException me) {
            handleMqttException(me);
            return false;
        }
    }

    public static void oneShot(String topic, String content, String broker, String clientId, String username, String password) {
        var one = new MqttService();
        if (one.connect(broker, clientId, username, password)) {
            if (one.publish(topic, content)) {
                if (one.disconnect()) {
                    System.out.println("succeeded");
                }
            }
        }
    }
}
