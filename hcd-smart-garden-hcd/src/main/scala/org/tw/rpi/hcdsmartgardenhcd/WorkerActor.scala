package org.tw.rpi.hcdsmartgardenhcd

import akka.actor.typed.scaladsl.{ActorContext, Behaviors, MutableBehavior}
import akka.actor.typed.{ActorRef, Behavior}
import csw.framework.scaladsl.CurrentStatePublisher
import csw.messages.commands.{CommandName, Observe}
import csw.messages.location.AkkaLocation
import csw.messages.params.generics.KeyType
import csw.messages.params.models.Prefix
import csw.messages.params.states.{CurrentState, StateName}
import csw.services.command.scaladsl.CommandService
import csw.services.config.api.models.ConfigData
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.tw.rpi.hcdsmartgardenhcd.WorkerActorMsgs.{DevicePreferences, Subscribe}

import scala.concurrent.duration.DurationLong

trait WorkerActorMsg
object WorkerActorMsgs {
  case class DevicePreferences(topic: String, payload: String) extends WorkerActorMsg
  case class Subscribe(topics: Set[String]) extends WorkerActorMsg
}

object WorkerActor {
  def make(currentStatePublisher: CurrentStatePublisher): Behavior[WorkerActorMsg] = {
    Behaviors.setup(ctx ⇒ new WorkerActor(ctx, currentStatePublisher))
  }
}

class WorkerActor(ctx: ActorContext[WorkerActorMsg],
                  currentStatePublisher: CurrentStatePublisher,
                  var topics: Set[String] = Set.empty[String]
                 ) extends MutableBehavior[WorkerActorMsg] {

  private val mqttClient = new RpiMqttClient()

  mqttClient.setup()
  mqttClient.setMqttCallback(mqttCallback())

  private def mqttCallback() = { (topic: String, message: MqttMessage) =>
    val param = KeyType.StringKey.make("data").set(message.toString)
    currentStatePublisher.publish(CurrentState(Prefix("dms.topic.data"), StateName(topic), Set(param)))
  }

  override def onMessage(msg: WorkerActorMsg): Behavior[WorkerActorMsg] = {
    msg match {
      case DevicePreferences(topic, payload) => mqttClient.publish(topic, payload)
      case Subscribe(myTopics) =>
        topics = topics ++ myTopics
        myTopics.foreach(mqttClient.subscribe)
    }
    this
  }
}
