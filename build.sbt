lazy val `avro-protocol` = project
  .in(file("protocol/avro"))
  .settings(moduleName := "avro-protocol")
  .settings(name := n(moduleName.value))
  .settings(avroProtocolSettings)
  .enablePlugins(IdlGenPlugin)

lazy val `pb-protocol` = project
  .in(file("protocol/pb"))
  .settings(moduleName := "pb-protocol")
  .settings(name := n(moduleName.value))
  .settings(pbProtocolSettings)
  .enablePlugins(IdlGenPlugin)

lazy val server = project
  .in(file("server"))
  .settings(moduleName := "server")
  .settings(name := n(moduleName.value))
  .settings(serverSettings)
  .dependsOn(`pb-protocol`)
  .aggregate(`pb-protocol`)

lazy val client = project
  .in(file("client"))
  .settings(moduleName := "client")
  .settings(name := n(moduleName.value))
  .settings(clientSettings)
  .dependsOn(`pb-protocol`)