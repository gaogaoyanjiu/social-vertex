package cn.net.polyglot.verticle

import cn.net.polyglot.config.DEFAULT_PORT
import cn.net.polyglot.config.EventBusConstants.HTTP_TO_MSG
import cn.net.polyglot.utils.text
import cn.net.polyglot.utils.tryJson
import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject

/**
 * @author zxj5470
 * @date 2018/7/9
 */
class IMHttpServerVerticle : AbstractVerticle() {
  override fun start() {
    val port = config().getInteger("port", DEFAULT_PORT)

    vertx.createHttpServer().requestHandler { req ->
      req.bodyHandler { buffer ->
        if (req.method() == HttpMethod.POST) {
          val json = buffer.text().tryJson()
          if (json == null) {
            req.response()
              .putHeader("content-type", "application/json")
              .end("""{"info":"json format error"}""")
          } else {
            vertx.eventBus().send<JsonObject>(HTTP_TO_MSG, json) { ar ->
              if (ar.succeeded()) {
                val ret = ar.result().body()
                println(ret)
                req.response()
                  .putHeader("content-type", "application/json")
                  .end(ret.toString())
              }
            }
          }
        }
        else{
          req.response().end("""{"info":"request method is not POST"}""")
        }
      }
    }.listen(port){
      if(it.succeeded()){
        println(this.javaClass.name + " is deployed on $port port")
      }else{
        println("deploy on $port failed")
      }
    }
  }
}
