package spray

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.actor._
import spray.routing.{Route, HttpService, RequestContext}
import spray.util._
import spray.http._
import scala.util.Success

/**
  * Created by yuto on 2016/09/09.
  */
class DemoServiceActor extends Actor with DemoService{
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing,
  // timeout handling or alternative handler registration
  def receive = runRoute(demoRoute)
}

trait DemoService extends HttpService {

  implicit def executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2000))


  val index = "{'test':'123'}"
  val demoRoute = {
    get {
      pathSingleSlash {
        complete(index)
      } ~
        path("ping") {
          random()
        } ~ path("st") { complete(stop())
        } ~ path("st2") {
        onComplete(res()){
          case Success(t) => complete(t)
          case _ => complete(500, "error was happen")
        }
      }
    }
  }

  /**
    * 乱数をレスポンスに入れても、固定になる
    *
    * @return
    */
  def random(): Route = {
    val n = math.random.toString
    complete(n)
  }

  def res(): Future[String] = Future{
    stop()
  }
  def stop(): String = {
    val start = System.currentTimeMillis()
    Thread.sleep(2000)
    val status = s"timediff ${System.currentTimeMillis() - start}ms ${Thread.currentThread().getName}"
    status
  }
}

