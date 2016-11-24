package example

import org.http4s._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

import scalaz.concurrent.Task
import scalaz.syntax.all._
import scalaz.syntax.std.all._
import scalaz.{Kleisli, \/}

object Zauth {

  case object UnauthorizedErr
  type AuthResult = UnauthorizedErr.type \/ Unit

  val tokenInfoUri: Uri = Uri.uri("https://auth.zalando.com/oauth2/tokeninfo")

  val client = PooledHttp1Client()

  def checkToken(token: OAuth2BearerToken): Task[AuthResult] =
    client.fetch(Request(uri = tokenInfoUri.copy(query = Query("access_token" -> token.token.some)))) { resp =>
      if (resp.status == Status.Ok) Task.now(().right)
      else Task.now(UnauthorizedErr.left)
    }

  val checkTokenService: Service[Request, AuthResult] = Service lift { req =>
    req.headers.get(Authorization) match {
      case Some(Authorization(cred: OAuth2BearerToken)) => checkToken(cred)
      case _ => Task.now(UnauthorizedErr.left)
    }
  }

  val onAuthFailure: Kleisli[Task, AuthedRequest[UnauthorizedErr.type], Response] =
    Kleisli kleisli { _ => Task.now(Response(status = Status.Unauthorized)) }

  def authMiddleWare: AuthMiddleware[Unit] =
    AuthMiddleware(checkTokenService, onAuthFailure)

  def apply(srvc: HttpService): HttpService = authMiddleWare(srvc.contramap(_.req))
}
