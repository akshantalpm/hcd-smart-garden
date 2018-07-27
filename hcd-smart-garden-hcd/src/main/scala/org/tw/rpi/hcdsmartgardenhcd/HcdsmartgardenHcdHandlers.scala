package org.tw.rpi.hcdsmartgardenhcd

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.util.Timeout
import csw.framework.scaladsl.{ComponentHandlers, CurrentStatePublisher}
import csw.messages.commands._
import csw.messages.framework.ComponentInfo
import csw.messages.location.{AkkaLocation, Connection, TrackingEvent}
import csw.messages.params.generics.{Key, KeyType}
import csw.messages.params.models.Prefix
import csw.messages.params.states.{CurrentState, StateName}
import csw.messages.scaladsl.TopLevelActorMessage
import csw.services.command.scaladsl.{CommandResponseManager, CommandService}
import csw.services.event.scaladsl.EventService
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.tw.rpi.hcdsmartgardenhcd.WorkerActorMsgs.{DevicePreferences, Subscribe}

import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to HcdsmartgardenHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
class HcdsmartgardenHcdHandlers(
    ctx: ActorContext[TopLevelActorMessage],
    componentInfo: ComponentInfo,
    commandResponseManager: CommandResponseManager,
    currentStatePublisher: CurrentStatePublisher,
    locationService: LocationService,
    eventService: EventService,
    loggerFactory: LoggerFactory
) extends ComponentHandlers(ctx,
                              componentInfo,
                              commandResponseManager,
                              currentStatePublisher,
                              locationService,
                              eventService,
                              loggerFactory) {

  implicit val ec: ExecutionContextExecutor = ctx.executionContext
  private val log                           = loggerFactory.getLogger
  private val ALARM_TIME_TOPIC              = "alarmTime"
  private val DEVICE_PREFERENCES_TOPIC      = "devicePreferences"
  private val REGISTER_DEVICE               = "registerDevice"
  private val SENSOR_READING                = "sensor/reading"
  private implicit val timeout = Timeout(5.seconds)
  private val worker: ActorRef[WorkerActorMsg] = ctx.spawnAnonymous(WorkerActor.make(currentStatePublisher))


  override def initialize(): Future[Unit] = {
    worker ! Subscribe(Set(REGISTER_DEVICE, SENSOR_READING))

    resolveMqttComponent().flatMap {
      case Some(mqtt) =>
        val param = KeyType.StringKey.make("topics").set(REGISTER_DEVICE, SENSOR_READING)
        new CommandService(mqtt)(ctx.system).oneway(Setup(Prefix("dms.topic.topics"), CommandName("topics"), None, Set(param)))
      case None => throw new RuntimeException("Mqtt Component not found")
    }

    Future.successful({})
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = ???

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = {
    log.info(s"Validating command: ${controlCommand.commandName.name}")
    val validCommands = Seq(ALARM_TIME_TOPIC, DEVICE_PREFERENCES_TOPIC)
    controlCommand.commandName.name match {
      case x if validCommands.contains(x)  => CommandResponse.Accepted(controlCommand.runId)
      case x                               => CommandResponse.Invalid(controlCommand.runId, CommandIssue.UnsupportedCommandIssue(s"Command $x. not supported."))
    }
  }

  override def onSubmit(controlCommand: ControlCommand): Unit = ???

  override def onOneway(controlCommand: ControlCommand): Unit = {
    controlCommand match {
      case cmd: Observe => onObserve(cmd)
      case cmd: Setup   =>
    }
  }

  override def onShutdown(): Future[Unit] = ???

  override def onGoOffline(): Unit = ???

  override def onGoOnline(): Unit = ???

  private def onObserve(observe: Observe) = {
    val key: Key[String] = KeyType.StringKey.make("data")
    val data             = observe.parameter(key).head

    worker ! DevicePreferences(observe.commandName.name, data)
  }


  private def resolveMqttComponent(): Future[Option[AkkaLocation]] = {
    val hcd = Connection.from("MqttsmartgardenHcd-hcd-akka")
    locationService.resolve(hcd.of[AkkaLocation], 5.seconds)
  }
}
