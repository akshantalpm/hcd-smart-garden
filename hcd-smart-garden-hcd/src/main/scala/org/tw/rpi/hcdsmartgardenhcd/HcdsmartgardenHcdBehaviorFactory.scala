package org.tw.rpi.hcdsmartgardenhcd

import akka.actor.typed.scaladsl.ActorContext
import csw.framework.scaladsl.{ComponentBehaviorFactory, ComponentHandlers, CurrentStatePublisher}
import csw.messages.framework.ComponentInfo
import csw.messages.scaladsl.TopLevelActorMessage
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.event.scaladsl.EventService
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory

class HcdsmartgardenHcdBehaviorFactory extends ComponentBehaviorFactory {

  override def handlers(
      ctx: ActorContext[TopLevelActorMessage],
      componentInfo: ComponentInfo,
      commandResponseManager: CommandResponseManager,
      currentStatePublisher: CurrentStatePublisher,
      locationService: LocationService,
      eventService: EventService,
      loggerFactory: LoggerFactory
  ): ComponentHandlers =
    new HcdsmartgardenHcdHandlers(ctx,
                                  componentInfo,
                                  commandResponseManager,
                                  currentStatePublisher,
                                  locationService,
                                  eventService,
                                  loggerFactory)

}
