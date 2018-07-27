package org.tw.rpi.hcdsmartgardendeploy

import csw.framework.deploy.hostconfig.HostConfig

object HcdsmartgardenHostConfigApp extends App {

  HostConfig.start("hcd-smart-garden-host-config-app", args)

}
