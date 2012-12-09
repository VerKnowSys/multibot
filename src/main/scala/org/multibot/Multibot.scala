package org.multibot

import org.jibble.pircbot.PircBot

import java.io.{PrintStream, ByteArrayOutputStream}

object Multibottest extends PircBot {
    val stdOut = System.out
    val stdErr = System.err

    val PRODUCTION = Option(System getProperty "multibot.production") map (_ toBoolean) getOrElse false
    val BOTNAME = if (PRODUCTION) "lang-bot" else "lang-bot-0"
    val BOTMSG = BOTNAME + ":"
    val NUMLINES = 5
    val INNUMLINES = 8
    val LAMBDABOT = "lambdabot"
    val LAMBDABOTIGNORE = Set("#scala", "#scalaz")
    val ADMINS = List("****")


    def main(args: Array[String]) {
        setName(BOTNAME)
        setVerbose(false)
        setEncoding("UTF-8")
        connect()
    }

    def connect() {
        connect("irc.freenode.net")
        val channels = if (PRODUCTION) List("#verknowsys") else List("#multibottest")
        channels foreach joinChannel
    }

    override def onDisconnect: Unit = while (true)
        try {
            connect()
            return
        } catch { case e: Exception =>
            e.printStackTrace
            Thread sleep 10000
        }

    var lastChannel: Option[String] = None

    override def onPrivateMessage(sender: String, login: String, hostname: String, message: String) = sender match {
        case LAMBDABOT => lastChannel foreach (sendMessage(_, message))
        case _         => onMessage(sender, sender, login, hostname, message)
    }

    override def onNotice(sender: String, login: String, hostname: String, target: String, notice: String) = sender match {
        case LAMBDABOT => lastChannel foreach (sendNotice(_, notice))
        case _ =>
    }

    override def onAction(sender: String, login: String, hostname: String, target: String, action: String) = sender match {
        case LAMBDABOT => lastChannel foreach (sendAction(_, action))
        case _ =>
    }

    override def onMessage(channel: String, sender: String, login: String, hostname: String, message: String) =
        serve(Msg(channel, sender, login, hostname, message))

    object      Cmd {def unapply(s: String) = if (s.contains(' ')) Some(s.split(" ", 2).toList) else None}

    case class Msg(channel: String, sender: String, login: String, hostname: String, message: String)

    val conOut = new ByteArrayOutputStream
    val conOutStream = new PrintStream(conOut)
    val writer = new java.io.PrintWriter(conOutStream)

    def captureOutput(block: => Unit) = try {
        System setOut conOutStream
        System setErr conOutStream
        block
    } finally {
        System setOut stdOut
        System setErr stdErr
        conOut.flush
        conOut.reset
    }

    import scala.tools.nsc.interpreter.{IMain}

    val scalaInt = scala.collection.mutable.Map[String, IMain]()
    def scalaInterpreter(channel: String)(f: (IMain, ByteArrayOutputStream) => Unit) = this.synchronized {
        val si = scalaInt.getOrElseUpdate(channel, {
            val settings = new scala.tools.nsc.Settings(null)
            settings.usejavacp.value = true
            settings.deprecation.value = true
            // settings.YdepMethTpes.value = true
            val si = new IMain(settings, writer) { override def parentClassLoader = Thread.currentThread.getContextClassLoader }

            si.quietImport("scalaz._")
            si.quietImport("Scalaz._")
            si.quietImport("org.scalacheck.Prop._")
            si
        })
        captureOutput{f(si, conOut)}
    }

    def sendLines(channel: String, message: String) = message split ("\n") filter (! _.isEmpty) take NUMLINES foreach (m => sendMessage(channel, " " + (if (!m.isEmpty && m.charAt(0) == 13) m.substring(1) else m)))

    def serve(implicit msg: Msg): Unit = msg.message match {
        case Cmd(BOTMSG :: m :: Nil) if ADMINS contains msg.sender => m match {
            case Cmd("join" :: ch :: Nil) => joinChannel(ch)
            case Cmd("leave" :: ch :: Nil) => partChannel(ch)
            case Cmd("reply" :: ch :: Nil) => sendMessage(msg.channel, ch)
            case _ => sendMessage(msg.channel, "unknown command")
        }

        // case "@listchans" => sendMessage(msg.channel, getChannels mkString " ")

        case "@bot" | "@bots" => sendMessage(msg.channel, ":)")
        case "@help" => sendMessage(msg.channel, "(!!!) scala (reset|type|scalex)")

        case Cmd("!!!" :: m :: Nil) => scalaInterpreter(msg.channel){(si, cout) =>
            import scala.tools.nsc.interpreter.Results._
            val lines = (si interpret m match {
                case Success => cout.toString.replaceAll("(?m:^res[0-9]+: )", "") // + "\n" + iout.toString.replaceAll("(?m:^res[0-9]+: )", "")
                case Error => cout.toString.replaceAll("^<console>:[0-9]+: ", "")
                case Incomplete => "error: unexpected EOF found, incomplete expression"
            })
            sendLines(msg.channel, lines)
            //.split("\n") take NUMLINES foreach (m => sendMessage(msg.channel, " " + (if (m.charAt(0) == 13) m.substring(1) else m)))
        }

        case Cmd("!!!type" :: m :: Nil) => scalaInterpreter(msg.channel)((si, cout) => sendMessage(msg.channel, (si.typeOfExpression(m).directObjectString)))
        case "!!!reset" => scalaInt -= msg.channel
        case "!!!reset-all" => scalaInt.clear

<<<<<<< HEAD
        case Cmd("%%" :: m :: Nil) => respondJSON(:/("tryruby.org") / "/levels/1/challenges/0" <:<
                    Map("Accept" -> "application/json, text/javascript, */*; q=0.01",
                        "Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8",
                        "X-Requested-With" -> "XMLHttpRequest",
                        "Connection" -> "keep-alive") <<< "cmd=" + java.net.URLEncoder.encode(m, "UTF-8")) {
            case JObject(JField("success", JBool(true)) :: JField("output", JString(output)) :: _) => Some(output)
            case JObject(JField("success", JBool(false)) :: _ :: JField("result", JString(output)) :: _) => Some(output)
            case e => Some("unexpected: " + e)
        }

        case "%reset" => jrubyInt -= msg.channel
        case "%reset-all" => jrubyInt.clear

        case Cmd("%" :: m :: Nil) => jrubyInterpreter(msg.channel){(jr, sc, cout) =>
            try {
                val result = jr.evalScriptlet(m, sc).toString
                sendLines(msg.channel, cout.toString)
                sendLines(msg.channel, result.toString)
            } catch {
                case e: Exception => sendMessage(msg.channel, e.getMessage)
            }
        }

        case Cmd("&" :: m :: Nil) =>
            val src = """
                var http = require('http');

                http.createServer(function (req, res) {
                  res.writeHead(200, {'Content-Type': 'text/plain'});
                  var a = (""" + m + """) + "";
                  res.end(a);
                }).listen();
            """

            respondJSON((:/("jsapp.us") / "ajax" << compact(render( ("actions", List(("action", "test") ~ ("code", src) ~ ("randToken", "3901") ~ ("fileName", ""))) ~ ("user", "null") ~ ("token", "null"))))) {
                case JObject(JField("user", JNull) :: JField("data", JArray(JString(data) :: Nil)) :: Nil) => var s: String = ""; (new Http with NoLogging)(url(data) >- {source => s = source}); Some(s)
                case e => Some("unexpected: " + e)
            }


        //case Cmd("^" :: m :: Nil) => jythonInterpreter(msg.channel){(jy, cout) =>
        //    try {
        //        //val result = jr.evalScriptlet(m, sc).toString
        //        jy.exec(m)
        //        sendLines(msg.channel, cout.toString)
        //        //sendLines(msg.channel, result.toString)
        //    } catch {
        //        case e: Exception => sendMessage(msg.channel, e.getMessage)
        //    }
        //}

        case Cmd("^" :: m :: Nil) => respondJSON2(:/("try-python.appspot.com") / "json" << compact(render( ("method", "exec") ~ ("params", List(pythonSession, m)) ~ ("id" -> "null") )),
                                                 :/("try-python.appspot.com") / "json" << compact(render( ("method", "start_session") ~ ("params", List[String]()) ~ ("id" -> "null") ))) {
            case JObject(JField("error", JNull) :: JField("id" , JString("null")) :: JField("result", JObject(JField("text", JString(result)) :: _)) :: Nil) => Some(result)
            case e => Some("unexpected: " + e)
        } {
            case JObject(_ :: _ :: JField("result" , JString(session)) :: Nil) => pythonSession = session; None
            case e => None
        }

        case Cmd("##" :: m :: Nil) => respondJSON(:/("groovyconsole.appspot.com") / "executor.groovy"  <<? Map("script" -> m), true) {
            case JObject(JField("executionResult", JString(result)) :: JField("outputText", JString(output)) :: JField("stacktraceText", JString("")) :: Nil) => Some(result.trim + "\n" + output.trim)
            case JObject(JField("executionResult", JString("")) :: JField("outputText", JString("")) :: JField("stacktraceText", JString(err)) :: Nil) => Some(err)
            case e => Some("unexpected" + e)
        }

        case m if (m.startsWith("@") || m.startsWith(">") || m.startsWith("?")) && m.trim.length > 1 && !LAMBDABOTIGNORE.contains(msg.channel) =>
            lastChannel = Some(msg.channel)
            sendMessage(LAMBDABOT, m)

        case _ =>
    }

    val cookies = scala.collection.mutable.Map[String, String]()

    def respondJSON(req: Request, join: Boolean = false)(response: JValue => Option[String])(implicit msg: Msg) = respond(req, join){line => response(JsonParser.parse(line))}

    def respondJSON2(req: Request, init: Request)(response: JValue => Option[String])(initResponse: JValue => Option[String])(implicit msg: Msg) = try {
        respond(req){line => response(JsonParser.parse(line))}
    } catch {
        case _ =>
    }
}
