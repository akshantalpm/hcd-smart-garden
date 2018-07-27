package org.tw.rpi.hcdsmartgardenhcd

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RpiMqttClient {

  val brokerUrl             = "tcp://localhost:1883"
  val REGISTER_DEVICE_TOPIC = "registerDevice"
  val SENSOR_READING_TOPIC  = "sensor/reading"

  val client = new MqttClient(brokerUrl, MqttClient.generateClientId, new MemoryPersistence)

  def setup() = {
    client.connect
  }

  def subscribe(topic: String) = client.subscribe(topic)

  def setMqttCallback( callback: (String, MqttMessage) => Unit) = {
    client.setCallback(new MqttCallback {

      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        println("Receiving Data, Topic : %s, Message : %s".format(topic, message))

        topic match {
          case REGISTER_DEVICE_TOPIC | SENSOR_READING_TOPIC => callback(topic, message)
          case _                                            =>
        }
      }

      override def connectionLost(cause: Throwable): Unit = println(cause)

      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {}
    })
  }

  def publish(topic: String, message: String) = {
    client.publish(topic, new MqttMessage(message.getBytes()))
  }
}
