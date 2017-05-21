package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def hello(name: String) = Action {
    Ok(views.html.hello(name))
  }

  def echo = Action { implicit request =>
    Ok("Got request [" + request + "]")
  }

  def jsonEcho = Action(parse.json) { implicit request =>
    Ok("Got request [" + request + "]")
  }

  def helloResult = Action {
    import play.api.http.HttpEntity
    import akka.util.ByteString
    Result(
      header = ResponseHeader(200, Map.empty),
      body = HttpEntity.Strict(ByteString("Hello World!"), Some("text/plain"))
    )
  }

  def notFound = Action {
    NotFound(<h1>Page not found</h1>)
  }

  def redirection = Action {
    Redirect("/hello") // Path that in 'routes' points to hello method in this class
  }

  def todo = TODO

}
