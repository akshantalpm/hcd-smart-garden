package org.tw.rpi.hcdsmartgardendeploy

import csw.framework.deploy.containercmd.ContainerCmd

object HcdsmartgardenContainerCmdApp extends App {

  ContainerCmd.start("hcd-smart-garden-container-cmd-app", args)

}
